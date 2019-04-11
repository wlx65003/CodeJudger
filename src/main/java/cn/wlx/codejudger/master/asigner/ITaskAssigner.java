package cn.wlx.codejudger.master.asigner;

import cn.wlx.codejudger.common.entities.JudgeNodeInfo;
import cn.wlx.codejudger.common.entities.JudgeReport;
import cn.wlx.codejudger.common.entities.Task;

public interface ITaskAssigner {

  void registerNode(JudgeNodeInfo judgeNodeInfo);

  void assign(Task task);

  void reportResult(JudgeReport judgeReport);
}
