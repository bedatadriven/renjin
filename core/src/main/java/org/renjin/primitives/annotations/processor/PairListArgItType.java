package org.renjin.primitives.annotations.processor;


public class PairListArgItType implements ArgumentItType {

  @Override
  public void init(WrapperSourceWriter s) {
    s.writeStatement("ArgumentIterator argIt = new ArgumentIterator(context, rho, args)");
  }


  public void writeFetchNextNode(WrapperSourceWriter s) {

    s.writeStatement("PairList.Node node = argIt.nextNode()");
    s.writeStatement("SEXP value = node.getValue()");
    s.writeBeginBlock("if(Symbol.MISSING_ARG.equals(value)) {");
    s.writeStatement("evaled = value");
    s.outdent();
    s.writeBeginBlock("} else {");
    s.writeStatement("evaled = context.evaluate( value, rho)");
    s.writeCloseBlock();
  }

  public String hasName() {
    return "node.hasTag()";
  }

  public String fetchArgName() {
    return "node.getTag().getPrintName()";
  }

  @Override
  public String hasNext() {
    return "argIt.hasNext()";
  }

  @Override
  public String nextArg(boolean evaled) {
    return evaled ? "argIt.evalNext()" : "argIt.next()";
  }

}
