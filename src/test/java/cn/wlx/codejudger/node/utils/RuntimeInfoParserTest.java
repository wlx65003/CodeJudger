package cn.wlx.codejudger.node.utils;

import cn.wlx.codejudger.common.entities.RuntimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RuntimeInfoParserTest {

  private static final Logger LOG = LoggerFactory.getLogger(RuntimeInfoParserTest.class);

  @Test
  public void testParseFromTime() {
    String testStr = "ok1\n"
        + "ok1\n"
        + "\tCommand being timed: \"./test_cpu\"\n"
        + "\tUser time (seconds): 5.16\n"
        + "\tSystem time (seconds): 0.00\n"
        + "\tPercent of CPU this job got: 194%\n"
        + "\tElapsed (wall clock) time (h:mm:ss or m:ss): 1:02.64\n"
        + "\tAverage shared text size (kbytes): 0\n"
        + "\tAverage unshared data size (kbytes): 0\n"
        + "\tAverage stack size (kbytes): 0\n"
        + "\tAverage total size (kbytes): 0\n"
        + "\tMaximum resident set size (kbytes): 3124\n"
        + "\tAverage resident set size (kbytes): 0\n"
        + "\tMajor (requiring I/O) page faults: 5\n"
        + "\tMinor (reclaiming a frame) page faults: 122\n"
        + "\tVoluntary context switches: 10\n"
        + "\tInvoluntary context switches: 280\n"
        + "\tSwaps: 0\n"
        + "\tFile system inputs: 1088\n"
        + "\tFile system outputs: 0\n"
        + "\tSocket messages sent: 0\n"
        + "\tSocket messages received: 0\n"
        + "\tSignals delivered: 0\n"
        + "\tPage size (bytes): 4096\n"
        + "\tExit status: 0";
    RuntimeInfo res = RuntimeInfoParser.parseFromGnuTime(testStr);
    LOG.info("res: {}.", res);
    Assert.assertEquals(res.exeTimeMs, 62640);
    Assert.assertEquals(res.peakMemoryKB, 3124);

  }
}