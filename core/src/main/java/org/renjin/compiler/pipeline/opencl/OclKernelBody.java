package org.renjin.compiler.pipeline.opencl;


import com.google.common.collect.Sets;

import java.io.StringWriter;
import java.util.Set;

public class OclKernelBody {

  private int indent = 0;
  private StringBuilder source = new StringBuilder();

  private Set<String> tempVars = Sets.newHashSet();

  public void println(String line) {
    for(int i=0;i!=indent;++i) {
      source.append("  ");
    }
    source.append(line);
    source.append("\n");
  }

  public void printlnf(String fmt, Object... args) {
    println(String.format(fmt, args));
  }

  public void println() {
    source.append("\n");

  }

  public String tempVar(String nameHint) {
    if(!tempVars.contains(nameHint)) {
      tempVars.add(nameHint);
      return "_" + nameHint;
    }
    int i=1;
    while(tempVars.contains(nameHint+i)) {
      i++;
    }
    tempVars.add(nameHint + i);
    return "_" + nameHint + i;
  }

  @Override
  public String toString() {
    return source.toString();
  }
}
