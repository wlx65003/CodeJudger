package cn.wlx.codejudger.master.writer;

import cn.wlx.codejudger.common.entities.JudgeReport;

public interface IJudgeResultWriter {

  void write(JudgeReport judgeReport);
}
