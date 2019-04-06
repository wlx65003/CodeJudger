package cn.wlx.codejudger.node.docker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerEventListener {

  private final static Logger LOG = LoggerFactory.getLogger(DockerEventListener.class);
  private Thread listenThread;
  private boolean stop = false;
  private ConcurrentMap<String, Boolean> oomRecord = new ConcurrentHashMap();

  public boolean hasOOM(String containerName) {
    // sleep for some time to ensure record has appeared
    // it's tricky, but I have no other solution.
    try {
      Thread.sleep(100);
    } catch (Exception e) {
      LOG.error("sleep interrupted", e);
    }
    return oomRecord.getOrDefault(containerName, false);
  }

  private void listen() {
    Process p;
    try {
      p = Runtime.getRuntime().exec(new String[]{
          "docker", "events", "--format", "{{json .}}",
          //"--since", "2006-01-02"
      });
    } catch (Exception e) {
      LOG.error("start docker event cmd failed.", e);
      return;
    }

    Scanner eventScanner = new Scanner(p.getInputStream());
    JsonParser parser = new JsonParser();

    while (!stop && eventScanner.hasNext()) {
      String eventJson = eventScanner.nextLine();
      try {
        JsonObject eventObj = parser.parse(eventJson).getAsJsonObject();
        if (eventObj.has("status")) {
          String status = eventObj.getAsJsonPrimitive("status").getAsString();
          // get oom log
          if (status.equals("oom")) {
            String containerName =
                eventObj.getAsJsonObject("Actor")
                    .getAsJsonObject("Attributes")
                    .getAsJsonPrimitive("name").getAsString();
            oomRecord.put(containerName, true);
            LOG.info("got oom log, containerName={}.", containerName);
          }
        }
      } catch (Exception e) {
        LOG.warn("parse docker event failed.", e);
      }
    }

    LOG.info("docker event listener stopped.");
  }

  public void start() {
    listenThread = new Thread(this::listen);
    listenThread.start();
  }

  public void stop() {
    stop = true;
    listenThread.interrupt();
  }
}
