package cn.wlx.codejudger.common.entities;

import cn.wlx.codejudger.common.enumerate.CodeLanguage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Task {

  public int solutionId;
  public int problemId;
  public String code;
  public CodeLanguage language;

  @Override
  public String toString() {
    return String.format("Task(id=%d, pid=%d, lang=%s)", solutionId, problemId, language);
  }
}
