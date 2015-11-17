package org.renjin.gcc.analysis;

import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleVarDecl;
import org.renjin.gcc.gimple.expr.GimpleComplexConstant;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleIntegerConstant;
import org.renjin.gcc.gimple.expr.GimpleRealConstant;
import org.renjin.gcc.gimple.type.*;

import java.util.Set;

/**
 * Ensures that local variables are initialized before use.
 * 
 * <p>GCC happily permits the use of uninitialized variables, but the JVM's byte code verifier
 * will abort if we try to load a variable that has not been initialized.</p>
 */
public class LocalVariableInitializer implements FunctionBodyTransformer {
  
  public static final LocalVariableInitializer INSTANCE = new LocalVariableInitializer();
  
  @Override
  public boolean transform(GimpleCompilationUnit unit, GimpleFunction fn) {
    
    ControlFlowGraph cfg = new ControlFlowGraph(fn);
    InitDataFlowAnalysis flowAnalysis = new InitDataFlowAnalysis(fn, cfg);
    
    flowAnalysis.solve();
    flowAnalysis.dump();

    Set<GimpleVarDecl> toInitialize = flowAnalysis.getVariablesUsedWithoutInitialization();

    for (GimpleVarDecl decl : toInitialize) {
      GimpleExpr defaultValue = defaultValue(decl.getType());
      decl.setValue(defaultValue);
      System.out.println("INITIALIZING " + decl + " = " + defaultValue);
    }
    
    // one pass is always enough
    return false;
  }

  private GimpleExpr defaultValue(GimpleType type) {
    if(type instanceof GimpleIntegerType) {
      return new GimpleIntegerConstant((GimpleIntegerType) type, 0);
   
    } else if(type instanceof GimpleRealType) {
      return new GimpleRealConstant((GimpleRealType) type, 0);
      
    } else if(type instanceof GimpleIndirectType) {
      return GimpleIntegerConstant.nullValue((GimpleIndirectType) type);

    } else if(type instanceof GimpleBooleanType) {
      GimpleIntegerConstant defaultValue = new GimpleIntegerConstant();
      defaultValue.setValue(0);
      defaultValue.setType(type);
      return defaultValue;
       
    } else if(type instanceof GimpleComplexType) {
      GimpleComplexConstant zero = new GimpleComplexConstant();
      GimpleRealType partType = ((GimpleComplexType) type).getPartType();
      zero.setType(type);
      zero.setIm(new GimpleRealConstant(partType, 0));
      zero.setReal(new GimpleRealConstant(partType, 0));
      return zero;
      
    } else {
      throw new UnsupportedOperationException("Don't know how to create default value for " + type);
    }
  }
}
