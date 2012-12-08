package org.renjin.gcc;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

public class CygwinEnvironment extends GccEnvironment {

  private String cygwinRoot = "c:\\cygwin";
  private String cygdriveRoot = "c:\\cygdrive\\";
  private String tempFolderName;
  
  public CygwinEnvironment() {
    assertCygwinInstalled();
    tempFolderName =  "gcc-bridge" + Long.toString(System.nanoTime(), 16);
    
    getWorkingDirectory().mkdirs();
  }


  private void assertCygwinInstalled() {
    File root = new File(cygwinRoot);
    if(!root.exists() || !root.isDirectory()) {
      throw new IllegalStateException("Cygwin root folder does not exist at " +
            root.getAbsolutePath());
    }
  }
     
  public File getWorkingDirectory() {
    return new File(cygwinRoot + File.separator + "tmp" +
          File.separator + tempFolderName);
  }
  
  @Override
  public Process startGcc(List<String> arguments) throws IOException {
    
    StringBuilder script = new StringBuilder();
    script.append("cd /tmp/").append(tempFolderName);
    script.append(" && ");
    script.append("gcc ");
    Joiner.on(' ').appendTo(script, arguments);
    
    ProcessBuilder gcc = new ProcessBuilder()
      .command(cygwinRoot + File.separator + "bin" + File.separator + "bash.exe", "-c", script.toString())
      .directory(new File(cygwinRoot + File.separator + "bin"));
    
    gcc.environment().put("PATH", cygwinRoot + File.separator + "bin");
    
    return gcc.start();
    
  }


  @Override
  public String toString(File file) {
    StringBuilder cygpath = new StringBuilder();
    file = file.getAbsoluteFile();
    while(file.getParent() != null) {
      cygpath.insert(0, "/" + file.getName());
      file = file.getParentFile();
    }
    String drive = file.getAbsolutePath().substring(0,1).toLowerCase();
    cygpath.insert(0, "/cygdrive/" + drive);
    return cygpath.toString();
  }
  
}
