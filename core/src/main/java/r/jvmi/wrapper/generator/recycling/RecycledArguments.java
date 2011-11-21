package r.jvmi.wrapper.generator.recycling;

import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.WrapperSourceWriter;
import r.jvmi.wrapper.generator.scalars.ScalarTypes;

import java.util.List;

public class RecycledArguments {

  protected final WrapperSourceWriter s;
  private final JvmMethod overload;
  private final List<RecycledArgument> args;
  
  public RecycledArguments(WrapperSourceWriter s,
      JvmMethod overload,
      List<RecycledArgument> recycledArguments) {
    super();
    this.s = s;
    this.overload = overload;
    this.args = recycledArguments;
  }

  public void writeSetup() {
    storeArgLengths();
    computeLongestVector();
    declareAndInitializeCounterLocals();
  }


  private void storeArgLengths() {
    s.writeComment("component lengths");
    for(RecycledArgument arg : args) {
      s.writeStatementF("int %s_length = %s.length();", arg.getVectorLocal(), 
          arg.getVectorLocal());
    }
    s.writeBlankLine();
  }

  private void computeLongestVector() {
    s.writeComment("compute longest vector");
    s.writeStatement("Vector longest = Null.INSTANCE");
    s.writeStatement("int cycles = 0");
    for(RecycledArgument arg : args) {

      // if any of the recyclable vectors are of length zero then we bail
      s.writeBeginBlock("if(" + arg.getVectorLocal() +"_length == 0) {");
      s.writeStatement("return " + getEmptyResult());
      s.writeCloseBlock();

      s.writeBeginBlock("if(" + arg.getVectorLocal() +"_length > cycles) {");
      s.writeStatementF("cycles = %s_length", arg.getVectorLocal());
      s.writeCloseBlock();
    }
    s.writeBlankLine();
  }

  private String getEmptyResult() {
    return ScalarTypes.get(overload.getReturnType()).getVectorType().getName() + ".EMPTY";
  }


  private void declareAndInitializeCounterLocals() {
    s.writeComment("initialize counters");
    for(RecycledArgument arg : args) {
      s.writeStatementF("int %s_i = 0;", arg.getVectorLocal());
    }
    s.writeBlankLine();
  }
  
  public String composeAnyNACondition() {
    StringBuilder condition = new StringBuilder();
    for(RecycledArgument arg : args) {
      if(condition.length() > 0) {
        condition.append(" || ");
      }
      condition.append(String.format("%s.isElementNA(%s)", arg.getVectorLocal(), indexVariable(arg)));
    }    
    return condition.toString();
  }

  protected String indexVariable(RecycledArgument arg) {
    return arg.getVectorLocal() + "_i";
  }
  
  public void writeElementExtraction() {
    for(RecycledArgument arg : args) {
      s.writeStatement(new StringBuilder()
        .append(arg.getElementClassName())
        .append(" ")
        .append(arg.getElementLocal())
        .append(" = ")
        .append(arg.getAccessExpression(indexVariable(arg)))
        .toString());        
    }
  }
  
  public void writeIncrementCounters() {
    for(RecycledArgument arg : args) {
      String index = indexVariable(arg);
      s.writeStatement( index  + "++" );
      s.writeStatement( "if(" + index + " >= " + arg.getLengthLocal() + ") " +
              index + " = 0; "); 
    }
    s.writeBlankLine();
  }
  
  public int size() {
    return args.size();
  }
  
  public String getVectorLocal(int index) {
    return args.get(index).getVectorLocal();
  }
  
  public String getLengthLocal(int index) {
    return args.get(index).getLengthLocal();
  }
}
