package org.renjin.primitives.annotations.processor.scalars;


import java.util.List;

import org.renjin.primitives.annotations.processor.JvmMethod;
import org.renjin.primitives.annotations.processor.WrapperSourceWriter;

public class SingleRecycledArgument extends RecycledArguments {

  private RecycledArgument arg;
  
  public SingleRecycledArgument(WrapperSourceWriter s,
      JvmMethod overload,
      List<RecycledArgument> recycledArguments) {
    super(s, overload, recycledArguments);

    arg = recycledArguments.get(0);
  }

  @Override
  public void writeSetup() {
    s.writeStatementF("int cycles = %s.length()", arg.getVectorLocal());
  }

  @Override
  protected String indexVariable(RecycledArgument arg) {
    // if we have only one variable to be recycled, then 
    // we don't need to maintain separate counters for each vector
    return "i";
  }

  @Override
  public void writeIncrementCounters() {
    // do nothing, we only need to maintain one index counter
  }

}
