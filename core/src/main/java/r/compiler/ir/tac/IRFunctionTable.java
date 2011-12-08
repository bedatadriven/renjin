package r.compiler.ir.tac;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import r.lang.PairList;
import r.lang.SEXP;

public class IRFunctionTable implements Iterable<IRFunction> {

  private List<IRFunction> functions = Lists.newArrayList();
  
  public IRFunction newFunction(PairList formals, SEXP body) {
    
    IRBlockBuilder blockBuilder = new IRBlockBuilder(this);
    IRBlock block = blockBuilder.build(body);
    
    IRFunction irFunction = new IRFunction(formals, body, block);
    functions.add(irFunction);
    
    return irFunction;
    
  }

  @Override
  public Iterator<IRFunction> iterator() {
    return functions.iterator();
  }
  
}
