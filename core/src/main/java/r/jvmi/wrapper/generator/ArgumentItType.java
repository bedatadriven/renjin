package r.jvmi.wrapper.generator;

import r.jvmi.wrapper.WrapperSourceWriter;

public interface ArgumentItType {

  void writeFetchNextNode(WrapperSourceWriter s);
  String nextArg(boolean evaled);
  String hasName();
  String fetchArgName();
  String hasNext();
  void init(WrapperSourceWriter s);
}
