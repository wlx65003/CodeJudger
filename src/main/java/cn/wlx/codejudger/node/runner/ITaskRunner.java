package cn.wlx.codejudger.node.runner;

import cn.wlx.codejudger.common.entities.Task;

public interface ITaskRunner {

  void notifyComplete();

  void runTask(Task task);
}
