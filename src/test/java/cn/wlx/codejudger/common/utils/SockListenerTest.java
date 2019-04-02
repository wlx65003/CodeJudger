package cn.wlx.codejudger.common.utils;

import cn.wlx.codejudger.common.entities.Task;
import cn.wlx.codejudger.common.enumerate.CodeLanguage;
import com.google.gson.Gson;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class SockListenerTest {

  private static final Logger LOG = LoggerFactory.getLogger(SockListenerTest.class);
  private static final int PORT = 59090;

  private void listen() {
    SockListener sockListener = new SockListener();
    sockListener.startListen(PORT, socket -> {
      try {
        String input = IOUtils.toString(socket.getInputStream(), "utf-8");
        LOG.info("got request, content={}.", input);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(String.format(
            "got request, content=%s, date=%s.", input, new Date().toString()
        ));

      } catch (Exception e) {
        LOG.error("err", e);
      }
    });
  }

  @Test
  public void testListen() {
    listen();
  }

  @Test
  public void testConnect() throws Exception {
    // start a listen thread
    Thread listenThread = new Thread(this::listen);
    listenThread.start();
    // wait for server building
    Thread.sleep(2000);

    // send some data to server
    try(Socket socket = new Socket("127.0.0.1", PORT)) {
      OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
      String code = new String(new char[10000000]);
      Task task = new Task(0, 0, code, CodeLanguage.CPP);
      Gson gson = new Gson();
      gson.toJson(task, writer);
      writer.flush();
    } catch (Exception e) {
      LOG.error("err", e);
    }
    Thread.sleep(2000);
  }
}