package cn.wlx.codejudger.common.entities;

import cn.wlx.codejudger.common.enumerate.JudgeStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JudgeResult {

  public JudgeNodeInfo judgeNodeInfo;
  public int taskId;
  public int problemId;
  public JudgeStatus status;
  public String additionalInfo;
}
