package org.renjin.primitives.annotations.processor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.codemodel.*;
import org.renjin.primitives.annotations.processor.args.ArgConverterStrategies;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;

public class VarArgApplyBuilder extends ApplyMethodBuilder {

  private List<JExpression> arguments = Lists.newArrayList();
  private Map<JvmMethod.Argument, JVar> namedFlags = Maps.newHashMap();
  private JVar varArgBuilder;

  public VarArgApplyBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive) {
    super(codeModel, invoker, primitive);
  }

  @Override
  protected void apply(JBlock parent) {
    JvmMethod overload = primitive.getOverloads().get(0);

    boolean varArgsSeen = false;

    varArgBuilder = parent.decl(classRef(ListVector.NamedBuilder.class), "varArgs",
            _new(classRef(ListVector.NamedBuilder.class)));

    for (JvmMethod.Argument argument : overload.getAllArguments()) {
      if (argument.isContextual()) {
        // contextual arguments are filled in from the current
        // thread and calling environment
        arguments.add(contextualExpression(argument));

      } else if (argument.isVarArg()) {
        // the @ArgumentList parameter receives our varArg list
        arguments.add(varArgBuilder.invoke("build"));
        varArgsSeen = true;

      } else if (argument.isNamedFlag()) {
        // declare our flag with its default value and add it to the
        // namedFlags maps for subsequent matching of remainng arguments
        JVar flag = parent.decl(argumentType(argument), nextFlagName(), defaultValueExpression(argument));
        namedFlags.put(argument, flag);
        arguments.add(flag);

      } else {
        // normal positional parameter. we consume these in sequence
        if (varArgsSeen) {
          throw new GeneratorDefinitionException(
                  "Any argument following a @ArgumentList must be annotated with @NamedFlag");
        }

        JVar arg = parent.decl(argumentType(argument), nextPosName(argument), convert(argument, nextArgAsSexp(argument.isEvaluated())));
        arguments.add(arg);
      }
    }

    // now we consume remaining args
    JWhileLoop loop = parent._while(hasMoreArguments());
    matchVarArg(loop.body());

    // finally invoke the underlying function
    JInvocation invocation = classRef(overload.getDeclaringClass()).staticInvoke(overload.getName());
    for(JExpression argument : arguments) {
      invocation.arg(argument);
    }

    parent._return(invocation);

  }

  private void matchVarArg(JBlock block) {
    JVar node = block.decl(classRef(PairList.Node.class), "node", argumentIterator.invoke("nextNode"));
    JVar value = block.decl(classRef(SEXP.class), "value", node.invoke("getValue"));
    JVar evaluated = block.decl(classRef(SEXP.class), "evaluated");

    JConditional ifMissing = block._if(value.eq(classRef(Symbol.class).staticRef("MISSING_ARG")));
    ifMissing._then().assign(evaluated, value);
    ifMissing._else().assign(evaluated, context.invoke("evaluate").arg(value).arg(environment));

    JConditional unnamed = block._if(node.invoke("hasName").not());

    // if the argument is unnamed, just add to the var arg list
    unnamed._then().invoke(varArgBuilder, "add").arg(evaluated);

    // otherwise we may need to check it against named flags
    JBlock namedBlock = unnamed._else();
    JVar name = namedBlock.decl(classRef(String.class), "name", node.invoke("getName"));

    IfElseBuilder matchSequence = new IfElseBuilder(namedBlock);
    for(JvmMethod.Argument namedFlag : namedFlags.keySet()) {
      matchSequence._if(lit(namedFlag.getName()).invoke("equals").arg(name))
              .assign(namedFlags.get(namedFlag), convert(namedFlag, evaluated));

    }
    matchSequence._else().invoke(varArgBuilder, "add").arg(name).arg(value);
  }

  private JExpression hasMoreArguments() {
    return argumentIterator.invoke("hasNext");
  }

  private String nextPosName(JvmMethod.Argument argument) {
    return "pos" + argument.getIndex();
  }

  private String nextFlagName() {
    return "flag" + namedFlags.size();
  }

  private JType argumentType(JvmMethod.Argument argument) {
    return codeModel._ref(argument.getClazz());
  }

  private JExpression convert(JvmMethod.Argument formal, JExpression sexp) {
    return ArgConverterStrategies.findArgConverterStrategy(formal).convertArgument(this, sexp);
  }

  private JExpression defaultValueExpression(JvmMethod.Argument argument) {
    return argument.getDefaultValue() ? JExpr.TRUE : JExpr.FALSE;
  }

}
