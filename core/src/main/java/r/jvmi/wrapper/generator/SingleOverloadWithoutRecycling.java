package r.jvmi.wrapper.generator;

import java.util.List;

import r.base.BaseFrame.Entry;
import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.WrapperSourceWriter;
import r.jvmi.wrapper.generator.args.ArgConverterStrategy;
import r.lang.Context;
import r.lang.Environment;

/**
 * Strategy for generating a wrapper for the simplest case: a single, non-recycling java method.
 * 
 * <p>For example:
 * 
 * <br>
 * <code>
 * @Primitive("as.environment")
 * public static Environment asEnvironment(@Current Context context, double index);
 *  
 * </code>
 * 
 * <br>
 * The generated code handles argument conversion (via {@link ArgConverterStrategy}), wraps 
 * the return result in an {@code EvalResult}.
 * 
 * 
 * @author alex
 *
 */
public class SingleOverloadWithoutRecycling extends GeneratorStrategy {

  @Override
  public boolean accept(List<JvmMethod> overloads) {
    if(overloads.size() != 1) {
      return false;
    }
    JvmMethod method = overloads.get(0);
    return !method.isRecycle() && !method.acceptsArgumentList();
  }


  @Override
  protected void generateCall(WrapperSourceWriter s, List<JvmMethod> overloads) {
    JvmMethod method = overloads.get(0);

    s.writeComment("process arguments");
    
    ArgumentList argumentList = new ArgumentList();
    
    int argIndex = 0;
    for(JvmMethod.Argument argument : method.getAllArguments()) {
      if(argument.isContextual()) {
        argumentList.add(contextualArgumentName(argument));
      } else {
        s.writeBlankLine();
        if(argIndex!=0) {
          s.writeStatement("args = ((PairList.Node)args).getNextNode();");
        }
        s.writeStatementF("%s arg%d = %s;", argument.getClazz().getName(), argIndex, argExpression(argument));
        argumentList.add("arg" + argIndex);
        argIndex++;
      }
    }
    s.writeBlankLine();

    s.writeComment("make call");
    s.writeStatement(callStatement(method, argumentList));
    
    if(method.returnsVoid()) {
      s.writeStatement("return EvalResult.NON_PRINTING_NULL;");
    }
  }

}
