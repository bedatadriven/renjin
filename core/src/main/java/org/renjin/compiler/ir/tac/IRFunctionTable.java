package org.renjin.compiler.ir.tac;

import java.util.Iterator;
import java.util.List;

import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;

import com.google.common.collect.Lists;


public class IRFunctionTable implements Iterable<IRFunction> {

  private List<IRFunction> functions = Lists.newArrayList();
  
  public IRFunction newFunction(PairList formals, SEXP body) {
    
    IRBodyBuilder blockBuilder = new IRBodyBuilder(this);
    IRBody block = blockBuilder.build(body);
    
    IRFunction irFunction = new IRFunction(formals, body, block);
    functions.add(irFunction);
    
    return irFunction;
    
  }

  @Override
  public Iterator<IRFunction> iterator() {
    return functions.iterator();
  }

  public Iterable<IRFunction> getFunctions() {
    return functions;
  }
  
}
