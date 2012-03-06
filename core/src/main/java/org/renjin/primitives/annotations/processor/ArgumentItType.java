package org.renjin.primitives.annotations.processor;


public interface ArgumentItType {

  void writeFetchNextNode(WrapperSourceWriter s);
  String nextArg(boolean evaled);
  String hasName();
  String fetchArgName();
  String hasNext();
  void init(WrapperSourceWriter s);
}
