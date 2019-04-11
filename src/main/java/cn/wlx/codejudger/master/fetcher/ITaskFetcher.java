package cn.wlx.codejudger.master.fetcher;

import cn.wlx.codejudger.common.entities.Task;
import java.util.List;

public interface ITaskFetcher {
  List<Task> fetch();
}
