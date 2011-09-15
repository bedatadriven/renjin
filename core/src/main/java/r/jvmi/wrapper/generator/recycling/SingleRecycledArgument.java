package r.jvmi.wrapper.generator.recycling;

import java.util.List;
import java.util.Map;

import r.jvmi.binding.JvmMethod.Argument;
import r.jvmi.wrapper.WrapperSourceWriter;

public class SingleRecycledArgument extends RecycledArguments {

  private RecycledArgument arg;
  
  public SingleRecycledArgument(WrapperSourceWriter s,
      List<RecycledArgument> recycledArguments) {
    super(s, recycledArguments);

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

  @Override
  public String getLongestLocal() {
    return arg.getVectorLocal();
  }

  
  
}
