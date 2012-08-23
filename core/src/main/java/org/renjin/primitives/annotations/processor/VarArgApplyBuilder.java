package org.renjin.primitives.annotations.processor;

import com.sun.codemodel.*;
import org.renjin.primitives.annotations.processor.args.ArgConverterStrategies;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import static com.sun.codemodel.JExpr.lit;

public class VarArgApplyBuilder extends ApplyMethodBuilder {


  private VarArgParser parser;

  public VarArgApplyBuilder(JCodeModel codeModel, JDefinedClass invoker, PrimitiveModel primitive) {
    super(codeModel, invoker, primitive);
  }

  @Override
  protected void apply(JBlock parent) {
    JvmMethod overload = primitive.getOverloads().get(0);

    parser = new VarArgParser(this, parent, overload);

    convertArgs(parser.getArgumentProcessingBlock());

    // try S3 dispatch
    genericDispatchStrategy.beforePrimitiveCalled(parent, parser, this, call);

    // finally invoke the underlying function
    JInvocation invocation = classRef(overload.getDeclaringClass()).staticInvoke(overload.getName());
    for(JExpression argument : parser.getArguments()) {
      invocation.arg(argument);
    }

    CodeModelUtils.returnSexp(codeModel, parent,  overload, invocation);
  }

  private void convertArgs(JBlock parent) {
    // convert the positional arguments
    int posIndex = 0;
    for(VarArgParser.PositionalArg posArgument : parser.getPositionalArguments()) {
      parent.assign(posArgument.getVariable(), convert(posArgument.getFormal(), nextArgAsSexp(posArgument.getFormal().isEvaluated())));
      genericDispatchStrategy.afterArgIsEvaluated(this, call, args, parent, posArgument.getVariable(), posIndex++);
    }

    // now we consume remaining args
    JWhileLoop loop = parent._while(hasMoreArguments());
    matchVarArg(loop.body());
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
    unnamed._then().invoke(parser.getVarArgBuilder(), "add").arg(evaluated);

    // otherwise we may need to check it against named flags
    JBlock namedBlock = unnamed._else();
    JVar name = namedBlock.decl(classRef(String.class), "name", node.invoke("getName"));

    IfElseBuilder matchSequence = new IfElseBuilder(namedBlock);
    for(JvmMethod.Argument namedFlag : parser.getNamedFlags().keySet()) {
      matchSequence._if(lit(namedFlag.getName()).invoke("equals").arg(name))
              .assign(parser.getNamedFlags().get(namedFlag), convert(namedFlag, evaluated));

    }
    matchSequence._else().invoke(parser.getVarArgBuilder(), "add").arg(name).arg(evaluated);
  }

  private JExpression hasMoreArguments() {
    return argumentIterator.invoke("hasNext");
  }

  private JExpression convert(JvmMethod.Argument formal, JExpression sexp) {
    return ArgConverterStrategies.findArgConverterStrategy(formal).convertArgument(this, sexp);
  }

}
