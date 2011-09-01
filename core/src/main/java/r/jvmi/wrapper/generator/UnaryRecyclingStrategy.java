package r.jvmi.wrapper.generator;

import java.util.List;

import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.WrapperSourceWriter;
import r.jvmi.wrapper.generator.scalars.ScalarType;
import r.jvmi.wrapper.generator.scalars.ScalarTypes;
import r.lang.Symbol;
import r.lang.Vector;

public class UnaryRecyclingStrategy extends GeneratorStrategy {

  @Override
  public boolean accept(List<JvmMethod> overloads) {
    return overloads.size() == 1 && overloads.get(0).isRecycle() && 
        overloads.get(0).getAllArguments().size() == 1;
  }

  @Override
  protected void generateCall(WrapperSourceWriter s, List<JvmMethod> overloads) {
    
    JvmMethod method = overloads.get(0);
    
    ScalarType argType = ScalarTypes.get(method.getFormals().get(0).getClazz());
    ScalarType resultType = ScalarTypes.get(method.getReturnType());
    
    s.writeTempLocalDeclaration(Vector.class, "arg0");
    s.writeTempLocalDeclaration(argType.getScalarType(), "argr0");
    s.writeStatement("try { arg0 = (Vector)((PairList.Node)args).getValue().evalToExp(context, rho); } " +
          "catch(ClassCastException e) { throw new ArgumentException(); }");
    
    s.writeStatement(toJava(resultType.getBuilderClass()) + " result = new " +
        toJava(resultType.getBuilderClass()) + "();");
    s.writeStatement("int resultIndex = 0;");
    
    s.writeStatement("for(int i=0;i!=arg0.length();++i) {");

    if(!method.acceptsNA()) {
      s.writeStatement("if(arg0.isElementNA(i)) {");
      s.writeStatement("result.setNA(resultIndex++);");
      s.writeStatement("} else {");
      writeResultAssignment(s, method, argType, resultType);
      s.writeStatement("}");      
    } else {
      writeResultAssignment(s, method, argType, resultType);
    }
    s.writeStatement("}");
    
    switch(method.getPreserveAttributesStyle()) {
    case ALL:
      s.writeStatement("result.copyAttributesFrom(arg0)");
      break;
    case SPECIAL:
      s.writeStatement("result.copySomeAttributesFrom(arg0, Symbol.DIM, Symbol.DIMNAMES, Symbol.NAMES);");
      break;
    }

    s.writeStatement("return new EvalResult(result.build());" );
  }

  private void writeResultAssignment(WrapperSourceWriter s, JvmMethod method,
      ScalarType argType, ScalarType resultType) {
    s.writeStatement("argr0 = arg0." + argType.getAccessorMethod() + "(i);");
    s.writeStatement("result.set(resultIndex++, " +
        method.getDeclaringClass().getName() + "." + method.getName() + "(argr0))" + ";");
  }
  
}
