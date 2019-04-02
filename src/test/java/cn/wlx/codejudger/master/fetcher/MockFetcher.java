package cn.wlx.codejudger.master.fetcher;

import cn.wlx.codejudger.common.entities.Task;
import cn.wlx.codejudger.common.enumerate.CodeLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockFetcher implements ITaskFetcher {

  private int taskNum;
  private static int cnt = 0;

  public MockFetcher(int taskNum) {
    this.taskNum = taskNum;
  }

  @Override
  public List<Task> fetch() {
    if (cnt >= taskNum) {
      return null;
    }
    String code = "xxx";
    Task task = new Task(0, 0, code, CodeLanguage.CPP);
    Random random = new Random();
    int len = random.nextInt(Math.min(10, taskNum - cnt));
    if (len == 0) {
      len = 1;
    }
    List<Task> taskList = new ArrayList<>(len);
    for (int i = 0; i < len; ++i) {
      taskList.add(task);
    }
    cnt += len;
    return taskList;
  }
}
