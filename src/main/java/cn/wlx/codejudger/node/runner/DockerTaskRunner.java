package cn.wlx.codejudger.node.runner;

import cn.wlx.codejudger.common.Constants;
import cn.wlx.codejudger.common.entities.RuntimeInfo;
import cn.wlx.codejudger.common.entities.Task;
import cn.wlx.codejudger.common.enumerate.JudgeStatus;
import cn.wlx.codejudger.node.NodeManager;
import cn.wlx.codejudger.node.utils.CommandBuilder;
import cn.wlx.codejudger.node.utils.DockerEventListener;
import cn.wlx.codejudger.node.utils.RuntimeInfoParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerTaskRunner implements ITaskRunner {

  private final static Logger LOG = LoggerFactory.getLogger(DockerTaskRunner.class);
  private ThreadPoolExecutor threadPool;
  private DockerEventListener dockerEventListener = new DockerEventListener();
  private NodeManager nodeManager;

  public DockerTaskRunner(int maxRunningNum, NodeManager nodeManager) {
    threadPool = new ThreadPoolExecutor(
        maxRunningNum,
        maxRunningNum,
        0, TimeUnit.MILLISECONDS,
        new LinkedBlockingDeque<>()
    );
    dockerEventListener.start();
    this.nodeManager = nodeManager;
  }

  @Override
  public void runTask(Task task) {
    threadPool.execute(() -> {
      String taskKey = genTaskKey(task.solutionId);
      try {
        // 0. save source code to temp file
        Path sourceFilePath = Paths.get(
            Constants.NODE_MANAGER_TMP_SOURCE_DIR, taskKey
        );
        writeStringToFile(task.code, sourceFilePath);

        // 1. start a docker container
        runCmd(new String[]{
            "docker",
            "run",
            "--name",
            taskKey,
            "-d",
            "-v",
            String.format("%s:%s:ro", Constants.NODE_MANAGER_DATA_DIR,
                Constants.CONTAINER_DATA_DIR),
            Constants.DOCKER_IMAGE_NAME,
            "bash", "-c",
            "tail -f /dev/null"
        }).waitFor();

        // 2. copy source code file into container
        runCmd(new String[]{
            "docker",
            "cp",
            sourceFilePath.toString(),
            taskKey + ":" + CommandBuilder.getDefaultSourcePath(task.language)
        }).waitFor();

        // 3. compile
        // TODO add timeout and limit

        runCmd(new String[]{
            "docker", "exec", taskKey,
            "bash", "-c",
            CommandBuilder.getCompileCmd(task.language)
        }).waitFor();

        // [optional] copy problem data

        // 4. run each case and check
        File dataFolder = Paths.get(
            Constants.NODE_MANAGER_DATA_DIR, String.valueOf(task.problemId)
        ).toFile();
        //noinspection ResultOfMethodCallIgnored
        dataFolder.mkdirs();

        File[] listOfFiles = dataFolder.listFiles();
        assert listOfFiles != null;

        for (File f : listOfFiles) {
          if (!f.getName().matches(".*\\.in")) {
            continue;
          }
          String caseName = getCaseName(f);
          String inFileInDocker = Paths.get(
              Constants.CONTAINER_DATA_DIR, String.valueOf(task.problemId), caseName + ".in"
          ).toString();
          String ansFileInDocker = Paths.get(
              Constants.CONTAINER_DATA_DIR, String.valueOf(task.problemId), caseName + ".out"
          ).toString();

          // TODO TLE judge
          Process execProcess = runCmd(new String[]{
              "docker", "exec", taskKey,
              "bash", "-c",
              CommandBuilder.getRunCmd(task.language, inFileInDocker)
          });

          String execOutput = IOUtils.toString(execProcess.getErrorStream());
          RuntimeInfo runtimeInfo = RuntimeInfoParser.parseFromGnuTime(execOutput);
          int exitCode = execProcess.waitFor();

          if (exitCode != 0) {
            if (dockerEventListener.hasOOM(taskKey)) {
              nodeManager.report(task, JudgeStatus.MEMORY_LIMIT_EXCEED, runtimeInfo);
              return;
            } else {
              nodeManager.report(task, JudgeStatus.RUNTIME_ERROR, runtimeInfo);
              return;
            }
          } else {
            // recheck TLE/MLE by GNU's time
            if (runtimeInfo.exeTimeMs > task.timeLimitMs) {
              nodeManager.report(task, JudgeStatus.TIME_LIMIT_EXCEED, runtimeInfo);
              return;
            } else if (runtimeInfo.peakMemoryKB > task.memoryLimitKB) {
              nodeManager.report(task, JudgeStatus.MEMORY_LIMIT_EXCEED, runtimeInfo);
              return;
            }

            // check answer
            int cmpRes = runCmd(new String[]{
                "docker", "exec", taskKey,
                "bash", "-c",
                String.format("diff %s %s", ansFileInDocker, CommandBuilder.DEFAULT_OUT_PATH)
            }).waitFor();
            if (cmpRes == 0) {
              nodeManager.report(task, JudgeStatus.ACCEPTED, runtimeInfo);
              return;
            } else {
              nodeManager.report(task, JudgeStatus.WRONG_ANSWER, runtimeInfo);
              return;
            }
          }
        }

        // 5. remove container
        runCmd(new String[]{
            "docker",
            "rm",
            taskKey
        }).waitFor();
      } catch (Exception e) {
        LOG.error("error when executing docker.", e);
      } finally {
        try {
          LOG.info("clearing docker container.");
          runCmd(new String[]{
              "docker", "rm", taskKey
          }).waitFor();
        } catch (Exception e) {
          LOG.error("clear docker container failed.");
        }
      }
    });
  }

  private String genTaskKey(int solutionId) {
    return String.format("task_%d_%d", solutionId, System.currentTimeMillis());
  }

  private void writeStringToFile(String s, Path path) {
    try {
      //noinspection ResultOfMethodCallIgnored
      path.getParent().toFile().mkdirs();
      Files.writeString(path, s);
    } catch (Exception e) {
      LOG.error("write file failed.", e);
    }
  }

  private Process runCmd(String[] cmd) throws IOException {
    LOG.info("running cmd: {}", StringUtils.join(cmd, " "));
    return Runtime.getRuntime().exec(cmd);
  }

  private void printProcessOutput(Process p) {
    try {
      LOG.info("process output: {}.", IOUtils.toString(p.getInputStream()));
    } catch (Exception e) {
      LOG.error("err", e);
    }
  }

  private String getCaseName(File f) {
    return f.getName().substring(0, f.getName().lastIndexOf(".in"));
  }
}
