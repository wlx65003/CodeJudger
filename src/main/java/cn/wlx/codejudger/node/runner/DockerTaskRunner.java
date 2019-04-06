package cn.wlx.codejudger.node.runner;

import cn.wlx.codejudger.common.Constants;
import cn.wlx.codejudger.common.entities.Task;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerTaskRunner implements ITaskRunner {

  private final static Logger LOG = LoggerFactory.getLogger(DockerTaskRunner.class);
  private ThreadPoolExecutor threadPool;

  public DockerTaskRunner(int maxRunningNum) {
    threadPool = new ThreadPoolExecutor(
        maxRunningNum,
        maxRunningNum,
        0, TimeUnit.MILLISECONDS,
        new LinkedBlockingDeque<>()
    );
  }

  private String genTaskKey(int solutionId) {
    return solutionId + "_" + System.currentTimeMillis();
  }

  private void writeStringToFile(String s, Path path) {
    try {
      //noinspection ResultOfMethodCallIgnored
      path.toFile().mkdirs();
      //Files.writeString(path, s);
    } catch (Exception e) {
      LOG.error("write file failed.", e);
    }
  }

  private Process runCmd(String[] cmd) throws IOException {
    return Runtime.getRuntime().exec(cmd);
  }

  @Override
  public void runTask(Task task) {
    threadPool.execute(() -> {
      try {
        String taskKey = genTaskKey(task.solutionId);
        String containerKey = "container_" + taskKey;

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
            containerKey,
            "ubuntu:16.04"
        }).waitFor();

        // 2. copy source code file into container
        runCmd(new String[]{
            "docker",
            "cp",
            sourceFilePath.toString(),
            containerKey + ":" + "/"
        }).waitFor();

        // 3. compile

        // 4. run

        // 5. remove container
        runCmd(new String[] {
            "docker",
            "rm",
            containerKey
        }).waitFor();
      } catch (Exception e) {
        LOG.error("error when executing docker.", e);
      }
    });
  }
}
