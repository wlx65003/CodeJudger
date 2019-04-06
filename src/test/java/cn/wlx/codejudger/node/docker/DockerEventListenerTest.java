package cn.wlx.codejudger.node.docker;

import static org.testng.Assert.*;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DockerEventListenerTest {

  @Test
  public void testListen() throws Exception {
    DockerEventListener eventListener = new DockerEventListener();
    eventListener.start();
    Thread.sleep(1000);

    // trigger an OOM
    Runtime.getRuntime().exec(new String[]{
        "docker", "exec", "my_ubuntu", "/root/mle"
    }).waitFor();

    Assert.assertTrue(eventListener.hasOOM("my_ubuntu"));
    eventListener.stop();
  }
}