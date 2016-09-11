package org.renjin.cli.build;

import org.renjin.packaging.BuildReporter;
import org.renjin.packaging.PackageSource;
import org.renjin.repackaged.guava.base.Strings;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class Builder {

  public static void execute(String action, String[] args) throws IOException {
    BuildReporter reporter = new BuildReporter();

    List<String> packagePaths = Arrays.asList(args);
    for (String packagePath : packagePaths) {
      PackageSource source = new PackageSource.Builder(packagePath)
          .setGroupId("org.renjin.cran")
          .build();
      
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
