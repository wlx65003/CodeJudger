package cn.wlx.codejudger.master.asigner;

import cn.wlx.codejudger.common.entities.JudgeNodeInfo;
import cn.wlx.codejudger.common.entities.JudgeResult;
import cn.wlx.codejudger.common.entities.Task;
import com.google.gson.Gson;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketAssigner implements ITaskAssigner {

  private static final Logger LOG = LoggerFactory.getLogger(SocketAssigner.class);
  private Gson gson = new Gson();
  private BlockingQueue<SortNode> nodeQueue = new PriorityBlockingQueue<>();
  private HashMap<JudgeNodeInfo, Integer> runningTaskMap = new HashMap<>();

  /**
   * this method will be called by Master's listener thread, which will add a new judgeNode.
   *
   * @param judgeNodeInfo new judge node info
   */
  @Override
  public void registerNode(JudgeNodeInfo judgeNodeInfo) {
    nodeQueue.offer(new SortNode(judgeNodeInfo));
  }

  /**
   * called by Master's listener thread. when a task is finished, reduce the corresponding node's
   * running task count.
   */
  @Override
  public void reportResult(JudgeResult judgeResult) {
    int runningTaskNum = runningTaskMap.getOrDefault(judgeResult.judgeNodeInfo, 0);
    runningTaskNum = Math.max(0, runningTaskNum - 1);
    runningTaskMap.put(judgeResult.judgeNodeInfo, runningTaskNum);
  }

  /**
   * called by Master's assigner thread, assigner will take a node from blockingQueue and assign
   * this task to it.
   *
   * @param task task to be assigned
   */
  @Override
  public void assign(Task task) {
    try {
      SortNode node = nodeQueue.take();
      // send request
      Socket socket = new Socket(node.judgeNodeInfo.host, node.judgeNodeInfo.socketPort);
      OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
      gson.toJson(task, out);
      out.flush();
      // update node queue
      int curRunningTaskNum = runningTaskMap.getOrDefault(node.judgeNodeInfo, 0);
      runningTaskMap.put(node.judgeNodeInfo, curRunningTaskNum + 1);
      nodeQueue.offer(node);
    } catch (Exception e) {
      LOG.error("exception occurred when assigning task.", e);
    }
  }

  private class SortNode implements Comparable<SortNode> {

    JudgeNodeInfo judgeNodeInfo;

    public SortNode(JudgeNodeInfo judgeNodeInfo) {
      this.judgeNodeInfo = judgeNodeInfo;
    }

    public int getAvailableSlotNum() {
      return judgeNodeInfo.taskNumLimit - runningTaskMap.getOrDefault(judgeNodeInfo, 0);
    }

    @Override
    public int compareTo(SortNode that) {
      // sort by spare task slots
      return this.getAvailableSlotNum() - that.getAvailableSlotNum();
    }
  }
}
