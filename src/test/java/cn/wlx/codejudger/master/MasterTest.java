package cn.wlx.codejudger.master;

import cn.wlx.codejudger.common.Constants;
import cn.wlx.codejudger.common.entities.JudgeNodeInfo;
import cn.wlx.codejudger.common.entities.Request;
import cn.wlx.codejudger.common.entities.Task;
import cn.wlx.codejudger.common.enumerate.CodeLanguage;
import cn.wlx.codejudger.common.enumerate.RequestType;
import cn.wlx.codejudger.master.asigner.MockAssigner;
import cn.wlx.codejudger.master.fetcher.MockFetcher;
import cn.wlx.codejudger.master.writer.MockWriter;
import com.google.gson.Gson;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MasterTest {

  private static final Logger LOG = LoggerFactory.getLogger(MasterTest.class);

  @Test
  public void testMaster() throws Exception {
    MockAssigner assigner = new MockAssigner();

    // fetch 10 tasks
    int taskNum = 3;
    Master master = new Master();
    master.setTaskFetcher(new MockFetcher(taskNum));
    master.setTaskAssigner(assigner);
    master.setJudgeResultWriter(new MockWriter());
    master.start();

    // wait for finishing
    Thread.sleep(3000);

    // check if they are all assigned
    Assert.assertEquals(assigner.getAssignCnt(), taskNum);

    master.stop();
  }

  private void sendRequestToMaster(Request request) {
    Gson gson = new Gson();
    try (Socket socket = new Socket("127.0.0.1", Constants.MASTER_PORT)) {
      OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
      gson.toJson(request, writer);
      writer.flush();
    } catch (Exception e) {
      LOG.error("send has exception.", e);
    }
  }

  @Test
  public void testReportAndWrite() throws Exception {
    MockWriter mockWriter = new MockWriter();

    Master master = new Master();
    master.setTaskFetcher(new MockFetcher(0));
    master.setTaskAssigner(new MockAssigner());
    master.setJudgeResultWriter(mockWriter);
    master.start();
    Thread.sleep(1000);

    // send some report to master
    int taskNum = 10;
    Task task = new Task(0, 0, "xxx", CodeLanguage.CPP);
    Gson gson = new Gson();
    Request request = new Request(RequestType.NODE_REPORT, gson.toJson(task));
    for (int i = 0; i < taskNum; ++i) {
      sendRequestToMaster(request);
    }

    Thread.sleep(1000);
    Assert.assertEquals(mockWriter.getCnt(), taskNum);

    master.stop();
  }

  @Test
  public void testNodeRegister() throws Exception {
    MockAssigner mockAssigner = new MockAssigner();

    Master master = new Master();
    master.setTaskFetcher(new MockFetcher(0));
    master.setTaskAssigner(mockAssigner);
    master.setJudgeResultWriter(new MockWriter());
    master.start();
    Thread.sleep(1000);

    // send some node_register request to master
    int nodeNum = 10;
    JudgeNodeInfo judgeNodeInfo = new JudgeNodeInfo("xxx", 0, -1);
    Gson gson = new Gson();
    Request request = new Request(RequestType.NODE_REGISTER, gson.toJson(judgeNodeInfo));
    for (int i = 0; i < nodeNum; ++i) {
      sendRequestToMaster(request);
    }

    Thread.sleep(1000);
    Assert.assertEquals(mockAssigner.getNodeCnt(), nodeNum);

    master.stop();
  }
}