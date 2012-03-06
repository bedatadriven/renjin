package org.renjin.primitives.annotations.processor;


public class ArrayArgItType implements ArgumentItType {


  @Override
  public void init(WrapperSourceWriter s) {
    s.writeStatement("int argIndex = -1");
  }
  
  @Override
  public void writeFetchNextNode(WrapperSourceWriter s) {
    s.writeStatement("argIndex++");
    s.writeStatement("evaled = arguments[argIndex]");
  }

  @Override
  public String hasName() {
    return "argumentNames[argIndex] != null";
  }

  @Override
  public String fetchArgName() {
    return "argumentNames[argIndex]";
  }

  @Override
  public String hasNext() {
    return "((argIndex+1) < arguments.length)";
  }

  @Override
  public String nextArg(boolean evaled) {
//    if(!evaled) {
//      throw new IllegalArgumentException("evaled cannot be false for ArrayArgItType");
//    }
    return "arguments[++argIndex]";
  }


}
