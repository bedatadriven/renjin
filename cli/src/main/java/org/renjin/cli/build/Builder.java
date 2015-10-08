package org.renjin.cli.build;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.util.List;


public class Builder {
  public static void execute(String action, String[] args) {

    OptionParser parser = new OptionParser();
    OptionSet options = parser.parse(args);

    BuildReporter reporter = new BuildReporter();

    List<String> packagePaths = options.nonOptionArguments();
    for (String packagePath : packagePaths) {
      PackageSource source = new PackageSource(packagePath, options);
      PackageBuild build = new PackageBuild(reporter, source);
      
      if(action.equals("build") || action.equals("install")) {
        build.build();
      }
      if(action.equals("install")) {
        build.install();
      }
    }
  }

}
