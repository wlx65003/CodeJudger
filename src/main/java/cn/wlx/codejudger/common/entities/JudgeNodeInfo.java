package cn.wlx.codejudger.common.entities;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JudgeNodeInfo {

  public int nodeID;
  public String host;
  public int socketPort;
  public int taskNumLimit;
}
