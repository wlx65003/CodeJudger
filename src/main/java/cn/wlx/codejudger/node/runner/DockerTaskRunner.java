package cn.wlx.codejudger.node.runner;

import cn.wlx.codejudger.common.entities.Task;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerTaskRunner implements ITaskRunner {

  private final static Logger LOG = LoggerFactory.getLogger(DockerTaskRunner.class);
  /**
   * use a blocking queue to ensure fixed number of task is running
   */
  private final BlockingQueue<Integer> blockingQueue;

  public DockerTaskRunner(int maxRunningNum) {
    this.blockingQueue = new ArrayBlockingQueue<>(maxRunningNum);
  }

  @Override
  public void notifyComplete() {
    Integer x = blockingQueue.poll();
  }

  @Override
  public void runTask(Task task) {
    try {
      blockingQueue.put(0);
      String[] cmd = {
          "/bin/sh",
          "-c",
          "echo hello"
      };

      Process p = Runtime.getRuntime().exec(cmd);

    } catch (Exception e) {
      LOG.error("error when starting task.", e);
    }
  }
}
