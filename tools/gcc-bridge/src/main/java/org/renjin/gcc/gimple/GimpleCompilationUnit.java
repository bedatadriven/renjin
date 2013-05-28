package org.renjin.gcc.gimple;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class GimpleCompilationUnit {

  private List<GimpleFunction> functions = Lists.newArrayList();

  public List<GimpleFunction> getFunctions() {
    return functions;
  }

  @Override
  public String toString() {
    return Joiner.on("\n").join(functions);
  }
}
