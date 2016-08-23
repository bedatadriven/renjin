package org.renjin.cli.build;

import org.renjin.repackaged.guava.base.Strings;

import java.util.Arrays;
import java.util.List;


public class Builder {
  public static void execute(String action, String[] args) {


    BuildReporter reporter = new BuildReporter();

    List<String> packagePaths = Arrays.asList(args);
    for (String packagePath : packagePaths) {
      PackageSource source = new PackageSource(packagePath);
      PackageBuild build = new PackageBuild(reporter, source, buildSuffix());
      
      if(action.equals("build") || action.equals("install")) {
        build.build();
      }
      if(action.equals("install")) {
        build.install();
      }
    }
  }

  private static String buildSuffix() {
    String envBuildNum = Strings.nullToEmpty(System.getProperty("BUILD_NUMBER"));
    if(envBuildNum.isEmpty()) {
      envBuildNum = Strings.nullToEmpty(System.getenv("BUILD_NUMBER"));
    }
    
    if(envBuildNum.isEmpty()) {
      return "";
    } else {
      return "-b" + envBuildNum;
    }
  }
}
