package org.renjin.gcc;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class GccEnvironment {

  public abstract Process startGcc(List<String> arguments) throws IOException;

  public abstract File getWorkingDirectory();
  
  public String toString(File file) {
    return file.getAbsolutePath();
  }
}
