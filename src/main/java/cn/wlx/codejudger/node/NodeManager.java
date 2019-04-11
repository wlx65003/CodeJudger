package cn.wlx.codejudger.node;

import cn.wlx.codejudger.common.Constants;
import cn.wlx.codejudger.common.entities.JudgeReport;
import cn.wlx.codejudger.common.entities.Request;
import cn.wlx.codejudger.common.entities.RuntimeInfo;
import cn.wlx.codejudger.common.entities.Task;
import cn.wlx.codejudger.common.enumerate.JudgeStatus;
import cn.wlx.codejudger.common.enumerate.RequestType;
import cn.wlx.codejudger.common.utils.SockListener;
import cn.wlx.codejudger.node.runner.DockerTaskRunner;
import cn.wlx.codejudger.node.runner.ITaskRunner;
import com.google.gson.Gson;
import java.io.OutputStreamWriter;
import java.net.Socket;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeManager {

  private final static Logger LOG = LoggerFactory.getLogger(NodeManager.class);
  private static final int cpuNum = Runtime.getRuntime().availableProcessors();
  @Setter
  private ITaskRunner taskRunner = new DockerTaskRunner(cpuNum, this);
  private volatile SockListener sockListener = new SockListener();
  private Thread socketListenThread;

  private void listen() {
    Gson gson = new Gson();
    sockListener.startListen(Constants.NODE_MANAGER_PORT, socket -> {
      try {
        String input = IOUtils.toString(socket.getInputStream(), Constants.DEFAULT_CHARSET);
        Request request = gson.fromJson(input, Request.class);
        switch (request.requestType) {
          case MASTER_SUBMIT:
            Task task = gson.fromJson(request.contents, Task.class);
            taskRunner.runTask(task);
            break;
          case LOCAL_REPORT:
            break;
          default:
            LOG.info("invalid request. type={}.", request.requestType);
            break;
        }
      } catch (Exception e) {
        LOG.warn("node manager socket listener got exception.", e);
      }
    });
  }

  private void sendRequestToMaster(RequestType type, Object content) {
    Gson gson = new Gson();
    Request request = new Request(type, gson.toJson(content));
    try (Socket socket = new Socket("127.0.0.1", Constants.MASTER_PORT)) {
      OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
      gson.toJson(request, writer);
      writer.flush();
    } catch (Exception e) {
      LOG.error("send has exception.", e);
    }
  }

  public void report(Task task, JudgeStatus status, RuntimeInfo runtimeInfo) {
    // TODO: node id.
    JudgeReport result = new JudgeReport(
        -1, task.solutionId, task.problemId, status, runtimeInfo
    );
    sendRequestToMaster(RequestType.NODE_REPORT, result);
  }

  public void start() {
    socketListenThread = new Thread(this::listen);
    socketListenThread.start();
  }

  public void stop() {
    try {
      sockListener.stopListen();
      socketListenThread.join();
    } catch (Exception e) {
      LOG.error("node manager stop failed.", e);
      return;
    }
    LOG.info("node manager stopped.");
  }
}
