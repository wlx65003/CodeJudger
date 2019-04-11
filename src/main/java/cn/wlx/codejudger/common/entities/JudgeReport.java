package cn.wlx.codejudger.common.entities;

import cn.wlx.codejudger.common.enumerate.JudgeStatus;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class JudgeReport {

  public int nodeId;
  public int taskId;
  public int problemId;
  public JudgeStatus status;
  public RuntimeInfo runtimeInfo;
}
