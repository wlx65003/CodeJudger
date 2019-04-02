package cn.wlx.codejudger.common.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SockListener {

  private static final Logger LOG = LoggerFactory.getLogger(SockListener.class);

  private volatile ServerSocket listener;

  public void startListen(int port, SocketCallback callback) {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      listener = serverSocket;
      while (true) {
        Socket socket = listener.accept();
        callback.call(socket);
      }
    } catch (Exception e) {
      LOG.error("listener caught exception and exit.", e);
    }
  }

  public void stopListen() {
    try {
      listener.close();
    } catch (IOException e) {
      LOG.error("close listener failed.", e);
    }
  }

  public interface SocketCallback {

    void call(Socket socket);
  }
}
