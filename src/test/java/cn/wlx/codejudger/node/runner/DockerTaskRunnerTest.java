package cn.wlx.codejudger.node.runner;

import cn.wlx.codejudger.common.entities.Task;
import cn.wlx.codejudger.common.enumerate.CodeLanguage;
import cn.wlx.codejudger.node.NodeManager;
import org.testng.annotations.Test;

public class DockerTaskRunnerTest {

  @Test
  public void testRunTask() throws Exception {
    NodeManager nodeManager = new NodeManager();
    DockerTaskRunner taskRunner = new DockerTaskRunner(3, nodeManager);
    String code = "#include <iostream>\n"
        + "using namespace std;\n"
        + "int main()\n"
        + "{\n"
        + "    int a,b;\n"
        + "    while(cin >> a >> b)\n"
        + "        cout << a+b << endl;\n"
        + "}";
    Task task = new Task(0, 1000, 1000, 256*1024, code, CodeLanguage.CPP);
    taskRunner.runTask(task);
    while (true) {
      Thread.sleep(1000);
    }
  }
}