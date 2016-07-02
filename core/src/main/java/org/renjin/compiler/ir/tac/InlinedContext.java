package org.renjin.compiler.ir.tac;

import org.renjin.compiler.ir.tac.functions.TranslationContext;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;


public class InlinedContext implements TranslationContext {
  @Override
  public PairList getEllipsesArguments() {
    // We are only supporting the inlining of functions when no arguments are passed via ...
    return Null.INSTANCE;
  }
}
