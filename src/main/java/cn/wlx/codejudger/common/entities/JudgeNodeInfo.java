package cn.wlx.codejudger.common.entities;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JudgeNodeInfo {

  public String host;
  public int socketPort;
  public int taskNumLimit;
}
