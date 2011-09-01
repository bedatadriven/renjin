package r.jvmi.wrapper.generator;

import java.util.List;

import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.WrapperSourceWriter;

public class VarArgsStrategy extends GeneratorStrategy {

  @Override
  public boolean accept(List<JvmMethod> overloads) {
    return overloads.size() == 1 && overloads.get(0).acceptsArgumentList() &&
        overloads.get(0).getFormals().size() == 1;
  }

  @Override
  protected void generateCall(WrapperSourceWriter s, List<JvmMethod> overloads) {
    JvmMethod method = overloads.get(0);

    s.writeComment("build argument list");
    
    ArgumentList argumentList = new ArgumentList(); 
    
    s.writeStatement("ListVector.Builder argList = new ListVector.Builder();");
    
    for(JvmMethod.Argument argument : method.getAllArguments()) {
      if(argument.isContextual()) {
        argumentList.add(contextualArgumentName(argument));
      
      } else if(argument.isAnnotatedWith(r.jvmi.annotations.ArgumentList.class)) {
        argumentList.add("argList.build()");
      }
    }
    
    s.writeBeginBlock("for(PairList.Node node : args.nodes()) { ");
    
    s.writeBeginBlock("if(Symbol.ELLIPSES.equals(node.getValue())) {");
    s.writeComment("// the values of the '...' are just merged into the argument list");
    s.writeStatement("DotExp ellipses = (DotExp) node.getValue().evalToExp(context, rho);");
    s.writeBeginBlock("for(PairList.Node dotArg : ellipses.getPromises().nodes()) {");
    writeHandleNode(s, "dotArg");
    s.writeCloseBlock();
    s.outdent();
    
    s.writeBeginBlock("} else {");
    writeHandleNode(s, "node");
    s.writeCloseBlock();
    
    s.writeCloseBlock(); // for loop
    
    s.writeComment("make call");
    s.writeStatement(callStatement(method, argumentList));
    
    if(method.returnsVoid()) {
      s.writeStatement("return EvalResult.NON_PRINTING_NULL;");
    }
    
  }

  private void writeHandleNode(WrapperSourceWriter s, String nodeName) {
    s.writeStatement("SEXP evaled = " + nodeName + ".getValue().evalToExp(context, rho);");
    s.writeBeginBlock("if(" + nodeName + ".hasTag()) {");
    s.writeStatement("argList.add(" + nodeName + ".getTag(), evaled);");
    s.outdent();
    s.writeBeginBlock("} else {");
    s.writeStatement("argList.add(evaled);");
    s.writeCloseBlock();
  }

}
