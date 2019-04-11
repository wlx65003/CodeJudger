package cn.wlx.codejudger.node.utils;

import cn.wlx.codejudger.common.enumerate.CodeLanguage;
import java.nio.file.Paths;
import java.util.HashMap;

public class CommandBuilder {

  public static final String DEFAULT_WORK_DIR = "/root";
  public static final String DEFAULT_SOURCE_NAME = "source";
  public static final String DEFAULT_BIN_NAME = "Main";
  public static final String DEFAULT_OUT_NAME = "Main.out";

  public static final String DEFAULT_BIN_PATH = Paths.get(DEFAULT_WORK_DIR, DEFAULT_BIN_NAME)
      .toString();
  public static final String DEFAULT_OUT_PATH = Paths.get(DEFAULT_WORK_DIR, DEFAULT_OUT_NAME)
      .toString();

  private static final HashMap<CodeLanguage, String> FILE_EXT = new HashMap<>();
  private static final HashMap<CodeLanguage, String> COMPILE_CMD_PATTERN = new HashMap<>();
  private static final HashMap<CodeLanguage, String> RUN_CMD_PATTERN = new HashMap<>();

  private static final String COMMON_BIN_RUN_CMD = "/usr/bin/time -v '%s' <'%s'> '%s'";

  static {
    // file extensions
    FILE_EXT.put(CodeLanguage.CPP, ".cc");

    // compile cmd
    COMPILE_CMD_PATTERN.put(CodeLanguage.CPP, "g++ '%s' -o '%s' -O2 -std=c++11");

    // run cmd
    RUN_CMD_PATTERN.put(CodeLanguage.CPP, COMMON_BIN_RUN_CMD);
  }

  public static String getDefaultSourcePath(CodeLanguage codeType) {
    return Paths.get(
        DEFAULT_WORK_DIR, DEFAULT_SOURCE_NAME + FILE_EXT.get(codeType)
    ).toString();
  }

  public static String getCompileCmd(CodeLanguage codeType) {
    return getCompileCmd(codeType, getDefaultSourcePath(codeType), DEFAULT_BIN_PATH);
  }

  public static String getRunCmd(CodeLanguage codeType, String inFile) {
    return getRunCmd(codeType, DEFAULT_BIN_PATH, inFile, DEFAULT_OUT_PATH);
  }

  // private methods
  private static String getCompileCmd(CodeLanguage codeType, String sourcePath, String binPath) {
    return String.format(COMPILE_CMD_PATTERN.get(codeType), sourcePath, binPath);
  }

  private static String getRunCmd(CodeLanguage codeType, String binFilePath, String inFile,
      String outFile) {
    return String.format(RUN_CMD_PATTERN.get(codeType), binFilePath, inFile, outFile);
  }
}
