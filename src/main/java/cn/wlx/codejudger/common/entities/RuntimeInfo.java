package cn.wlx.codejudger.common.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RuntimeInfo {

  public int exitCode;
  public int exeTimeMs;
  public int peakMemoryKB;
  public String additionalInfo;
}
