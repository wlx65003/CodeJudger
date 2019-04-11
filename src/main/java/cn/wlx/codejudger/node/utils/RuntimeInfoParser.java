package cn.wlx.codejudger.node.utils;

import cn.wlx.codejudger.common.entities.RuntimeInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeInfoParser {

  private static final Logger LOG = LoggerFactory.getLogger(RuntimeInfoParser.class);

  private static int parseTimeToMs(String str) {
    // example: 00:03.555, mm:ss
    String[] strs = str.split(":");
    double res = 0;
    res += Integer.parseInt(strs[0]);
    res *= 60;
    res += Double.parseDouble(strs[1]);
    res *= 1000;
    return (int) res;
  }

  /**
   * parse some info from GNU's "/usr/bin/time -v" output
   *
   * @param timeOutput output of "/usr/bin/time -v"
   * @return a RuntimeInfo
   */
  public static RuntimeInfo parseFromGnuTime(String timeOutput) {
    RuntimeInfo info = new RuntimeInfo();
    // use Elapsed (wall clock) time as execution time
    Pattern r = Pattern.compile("Elapsed.*time.*:.*([0-9]+:[0-9]+\\.[0-9]+)");
    Matcher m = r.matcher(timeOutput);
    if (m.find()) {
      info.exeTimeMs = parseTimeToMs(m.group(1));
    } else {
      LOG.error("time not find while parsing. rawStr={}.", timeOutput);
    }

    // use max resident set size as peak memory
    r = Pattern.compile("Maximum resident set size.*:.*?([0-9]+)");
    m = r.matcher(timeOutput);
    if (m.find()) {
      info.peakMemoryKB = Integer.valueOf(m.group(1));
    } else {
      LOG.error("memory not find while parsing. rawStr={}.", timeOutput);
    }
    return info;
  }

}
