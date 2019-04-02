package cn.wlx.codejudger.master;

import cn.wlx.codejudger.common.Constants;
import cn.wlx.codejudger.common.entities.JudgeNodeInfo;
import cn.wlx.codejudger.common.entities.JudgeResult;
import cn.wlx.codejudger.common.entities.Request;
import cn.wlx.codejudger.common.entities.Task;
import cn.wlx.codejudger.common.utils.SockListener;
import cn.wlx.codejudger.master.asigner.ITaskAssigner;
import cn.wlx.codejudger.master.fetcher.ITaskFetcher;
import cn.wlx.codejudger.master.writer.HznuojResultWriter;
import cn.wlx.codejudger.master.writer.IJudgeResultWriter;
import com.google.gson.Gson;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Master {

  private final static Logger LOG = LoggerFactory.getLogger(Master.class);
  @Setter
  private ITaskFetcher taskFetcher;
  @Setter
  private ITaskAssigner taskAssigner;
  @Setter
  private IJudgeResultWriter judgeResultWriter = new HznuojResultWriter();
  private volatile SockListener sockListener = new SockListener();
  private Thread fetcherThread;
  private Thread assignerThread;
  private Thread listenerThread;
  private BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(Constants.MASTER_QUEUE_SIZE);
  private volatile boolean stop = false;


  private void fetcherLoop() {
    while (!stop) {
      try {
        List<Task> tasks = taskFetcher.fetch();
        if (tasks != null) {
          LOG.info("fetcher got tasks, len={}.", tasks.size());
          for (Task task : tasks) {
            taskQueue.put(task);
          }
        }
        Thread.sleep(Constants.FETCHER_SLEEP_MS);
      } catch (Exception e) {
        LOG.warn("Exception in fetcherLoop.", e);
      }
    }
    LOG.info("fetcher loop stopped.");
  }

  private void assignerLoop() {
    while (!stop) {
      try {
        Task task = taskQueue.take();
        LOG.info("got task, {}.", task);
        taskAssigner.assign(task);
      } catch (Exception e) {
        LOG.error("assigner loop got exception.", e);
      }
    }
    LOG.info("assigner loop stopped.");
  }

  private void listenSocket() {
    Gson gson = new Gson();
    sockListener.startListen(Constants.MASTER_PORT, socket -> {
      try {
        String input = IOUtils.toString(socket.getInputStream(), Constants.DEFAULT_CHARSET);
        Request request = gson.fromJson(input, Request.class);
        LOG.info("got request: {}.", request);
        switch (request.requestType) {
          // judge node finish task and report
          case NODE_REPORT:
            JudgeResult judgeResult = gson.fromJson(request.contents, JudgeResult.class);
            taskAssigner.reportResult(judgeResult);
            judgeResultWriter.write(judgeResult);
            break;

          // new node added and send register request
          case NODE_REGISTER:
            JudgeNodeInfo judgeNodeInfo = gson.fromJson(request.contents, JudgeNodeInfo.class);
            taskAssigner.registerNode(judgeNodeInfo);
            break;

          default:
            LOG.info("invalid request. type={}.", request.requestType);
            break;
        }
      } catch (Exception e) {
        LOG.warn("master socket listener got exception.", e);
      }
    });
    LOG.info("master listener stopped.");
  }

  public void start() {
    fetcherThread = new Thread(this::fetcherLoop);
    fetcherThread.setName("fetcherThread");
    assignerThread = new Thread(this::assignerLoop);
    assignerThread.setName("assignerThread");
    listenerThread = new Thread(this::listenSocket);
    listenerThread.setName("listenerThread");

    fetcherThread.start();
    assignerThread.start();
    listenerThread.start();
  }

  public void stop() {
    try {
      stop = true;
      // interrupt ServerSocket.listen()
      sockListener.stopListen();
      // interrupt blocking queue
      assignerThread.interrupt();

      fetcherThread.join();
      assignerThread.join();
      listenerThread.join();
    } catch (Exception e) {
      LOG.error("master stop failed.", e);
      return;
    }
    LOG.info("master stopped.");
  }
}
