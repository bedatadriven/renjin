package r.jvmi.wrapper.generator;

import java.util.Map;

import r.jvmi.binding.JvmMethod;
import r.jvmi.binding.JvmMethod.Argument;
import r.jvmi.wrapper.WrapperSourceWriter;
import r.jvmi.wrapper.generator.args.ArgConverterStrategies;

public class VarArgsMatcher {

  
  public void write(WrapperSourceWriter s, Map<Argument, String> namedFlags) {
    s.writeBeginBlock("while(argIt.hasNext()) { ");
    writeHandleNode(s, namedFlags);
    s.writeCloseBlock();
  }
  

  private void writeHandleNode(WrapperSourceWriter s,
      Map<JvmMethod.Argument, String> namedFlags) {

    loadNextNode(s);
    s.writeBeginBlock("if(node.hasTag()) {");

    if (!namedFlags.isEmpty()) {
      s.writeStatement("String name = node.getTag().getPrintName()");

      boolean needElseIf = false;
      for (JvmMethod.Argument namedFlag : namedFlags.keySet()) {

        if (needElseIf) {
          s.outdent();
        }

        s.writeBeginBlock((needElseIf ? "} else " : "") + "if(name.equals(\""
            + namedFlag.getName() + "\")) {");
        s.writeBeginIf("node.getValue() != Symbol.MISSING_ARG");
        s.writeStatement(ArgConverterStrategies.findArgConverterStrategy(
            namedFlag).conversionStatement(namedFlags.get(namedFlag), "evaled"));
        s.writeCloseBlock();
        needElseIf = true;
      }
      s.outdent();
      s.writeBeginBlock("} else {");
    }

    s.writeStatement("argListBuilder.add(node.getTag(), evaled);");

    if (!namedFlags.isEmpty()) {
      s.writeCloseBlock();
    }

    s.outdent();
    s.writeBeginBlock("} else {");
    s.writeStatement("argListBuilder.add(evaled);");
    s.writeCloseBlock();
  }


  private void loadNextNode(WrapperSourceWriter s) {
    s.writeStatement("PairList.Node node = argIt.nextNode()");
    s.writeStatement("SEXP value = node.getValue()");
    s.writeStatement("SEXP evaled");
    s.writeBeginBlock("if(Symbol.MISSING_ARG.equals(value)) {");
    s.writeStatement("evaled = value");
    s.outdent();
    s.writeBeginBlock("} else {");
    s.writeStatement("evaled = context.evaluate( value, rho)");
    s.writeCloseBlock();
  }


  
}
