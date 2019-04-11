package cn.wlx.codejudger.master.writer;

import cn.wlx.codejudger.common.entities.JudgeReport;
import lombok.Getter;

public class MockWriter implements IJudgeResultWriter {

  @Getter
  private int cnt = 0;

  @Override
  public void write(JudgeReport judgeReport) {
    cnt++;
  }
}
