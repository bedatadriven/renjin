package org.renjin.cli.build;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.renjin.repackaged.guava.base.Strings;

import java.util.List;


public class Builder {
  public static void execute(String action, String[] args) {

    OptionParser parser = new OptionParser();
    OptionSet options = parser.parse(args);

    BuildReporter reporter = new BuildReporter();

    List<String> packagePaths = options.nonOptionArguments();
    for (String packagePath : packagePaths) {
      PackageSource source = new PackageSource(packagePath, options);
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
