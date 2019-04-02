package cn.wlx.codejudger.master.asigner;

import cn.wlx.codejudger.common.entities.JudgeNodeInfo;
import cn.wlx.codejudger.common.entities.JudgeResult;
import cn.wlx.codejudger.common.entities.Task;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockAssigner implements ITaskAssigner {

  private static final Logger LOG = LoggerFactory.getLogger(MockAssigner.class);
  @Getter
  private int assignCnt = 0;
  @Getter
  private int nodeCnt = 0;

  @Override
  public void assign(Task task) {
    assignCnt++;
    LOG.info("assign task, task={}.", task);
  }

  @Override
  public void registerNode(JudgeNodeInfo judgeNodeInfo) {
    nodeCnt++;
  }

  @Override
  public void reportResult(JudgeResult judgeResult) {

  }
}
