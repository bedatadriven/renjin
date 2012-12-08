package org.renjin.gcc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

public class UnixEnvironment extends GccEnvironment {

  private File workingDirectory;
  
  public UnixEnvironment() {
    workingDirectory = Files.createTempDir();
  }
  

  @Override
  public Process startGcc(List<String> arguments) throws IOException {
    List<String> command = Lists.newArrayList();
    command.add("gcc");
    command.addAll(arguments);
    
    return new ProcessBuilder()
      .command(command)
      .directory(workingDirectory)
      .redirectErrorStream(true)
      .start();
  }

  @Override
  public File getWorkingDirectory() {
    return workingDirectory;
  }

}
