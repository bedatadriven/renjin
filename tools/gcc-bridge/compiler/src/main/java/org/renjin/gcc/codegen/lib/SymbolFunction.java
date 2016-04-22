package org.renjin.gcc.codegen.lib;

import org.renjin.gcc.codegen.call.CallGenerator;

public class SymbolFunction {

  private String alias;
  private CallGenerator call;

  public SymbolFunction(String alias, CallGenerator call) {
    super();
    this.alias = alias;
    this.call = call;
  }

  public String getAlias() {
    return alias;
  }

  public CallGenerator getCall() {
    return call;
  }
}