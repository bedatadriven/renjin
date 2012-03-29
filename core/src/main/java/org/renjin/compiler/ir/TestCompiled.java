package org.renjin.compiler.ir;

import org.renjin.compiler.CompiledBody;
import org.renjin.eval.Context;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;


public class TestCompiled implements CompiledBody {

  @Override
  public SEXP eval(Context context, Environment rho) {
    
    Vector x = new DoubleVector(1);
    int i = x.getElementAsInt(4);
    
    return Null.INSTANCE;
   
  }

}
