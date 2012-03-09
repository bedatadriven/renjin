package org.renjin.primitives.annotations.processor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.renjin.primitives.Primitives.Entry;
import org.renjin.primitives.annotations.NamedFlag;
import org.renjin.primitives.annotations.PreserveAttributeStyle;
import org.renjin.primitives.annotations.processor.args.ArgConverterStrategies;
import org.renjin.primitives.annotations.processor.args.ArgConverterStrategy;
import org.renjin.primitives.annotations.processor.generic.GenericDispatchStrategy;
import org.renjin.primitives.annotations.processor.generic.OpsGroupGenericDispatchStrategy;
import org.renjin.primitives.annotations.processor.generic.SimpleDispatchStrategy;
import org.renjin.primitives.annotations.processor.generic.SummaryGroupGenericStrategy;
import org.renjin.primitives.annotations.processor.scalars.RecycledArgument;
import org.renjin.primitives.annotations.processor.scalars.RecycledArguments;
import org.renjin.primitives.annotations.processor.scalars.ScalarType;
import org.renjin.primitives.annotations.processor.scalars.ScalarTypes;
import org.renjin.primitives.annotations.processor.scalars.SingleRecycledArgument;

import r.jvmi.binding.JvmMethod;
import r.jvmi.binding.JvmMethod.Argument;
import r.lang.Context;
import r.lang.Environment;
import r.lang.ListVector;
import r.lang.SEXP;
import r.lang.exception.EvalException;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AnnotationBasedStrategy extends GeneratorStrategy  {

  protected void generateMethods(Entry entry, WrapperSourceWriter s, List<JvmMethod> overloads) {

    generateApplyMethod(entry, s, overloads);
    
    if(!entry.isSpecial()) {
      generateBuiltinApply(s, entry, overloads);
    }
    
    generateFixedArityMethods(s, entry, overloads);

    generateStaticMatchAndApply(s, entry, overloads);

  }

  private String argumentErrorMessage(Entry entry, List<JvmMethod> overloads) {
    StringBuilder message = new StringBuilder();
    message.append("\"Invalid argument. Expected:");
    for(JvmMethod method : overloads) {
      message.append("\\n\\t");
      method.appendFriendlySignatureTo(entry.name, message);
    }
    message.append("\"");
    return message.toString();
  }
  
  protected String noMatchingOverloadErrorMessage(Entry entry, Collection<JvmMethod> overloads) {
    
    int nargs = overloads.iterator().next().countPositionalFormals();
    
    StringBuilder message = new StringBuilder();
    message.append("throw new EvalException(context, ");
    message.append("\"Invalid argument:\\n");
    message.append("\\t").append(entry.name).append("(");
    
    for(int i=0;i<nargs;++i) {
      if(i > 0) {
        message.append(", ");
      }
      message.append("%s");
    }
    message.append(")\\n");
    message.append("\\tExpected:");
    for(JvmMethod method : overloads) {
      message.append("\\n\\t");
      method.appendFriendlySignatureTo(entry.name, message);
    }
    message.append("\"");
    for(int i=0;i<nargs;++i) {
      message.append(", s" + i + ".getTypeName()");
    }
    message.append(")");
    return message.toString();
  }

  protected final String contextualArgumentName(Argument formal) {
    if(formal.getClazz().equals(Context.class)) {
      return "context";
    } else if(formal.getClazz().equals(Environment.class)) {
      return "rho";
    } else if(formal.getClazz().equals(Context.Globals.class)) {
      return "context.getGlobals()";
    } else {
      throw new RuntimeException("Invalid contextual argument type: " + formal.getClazz());
    }
  }

  protected static class ArgumentList {
    private StringBuilder sb = new StringBuilder();
    
    public ArgumentList(String... args) {
      for(String arg : args) {
        add(arg);
      }
    }
    
    public void add(String name) {
      if(sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(name);
    }
    @Override
    public String toString() {
      return sb.toString();
    }
  }
    
    
  protected final String callStatement(JvmMethod method, ArgumentList argumentList) {
    StringBuilder call = new StringBuilder();
    call.append(method.getDeclaringClass().getName()).append(".")
      .append(method.getName())
      .append("(")
      .append(argumentList.toString())
      .append(")");
    
    return handleReturn(method, call.toString());
  }
  
  protected final String handleReturn(JvmMethod method, String execution) {
    if(method.returnsVoid()) {
      return execution + ";";
      
    } else if(SEXP.class.isAssignableFrom(method.getReturnType())) {
      return "return " + execution + ";";
    
    } else {
      verifyWeHaveWrapResult(method);
      return "return wrapResult(" + execution + ");";    
    }
  }
  
  private void verifyWeHaveWrapResult(JvmMethod method) {
    for(Method runtimeMethod : WrapperRuntime.class.getMethods()) {
      if(runtimeMethod.getName().equals("wrapResult") &&
         runtimeMethod.getParameterTypes().length == 1 &&
         (runtimeMethod.getModifiers() & Modifier.STATIC) != 0 &&
         runtimeMethod.getParameterTypes()[0].isAssignableFrom(method.getReturnType())) {
        return;
      }
    }
    throw new GeneratorDefinitionException("Do not have a wrapper for return type " + method.getReturnType().getName());
  }


  private void generateApplyMethod(Entry entry, WrapperSourceWriter s,
      List<JvmMethod> overloads) {

    s.println("@Override");
    s.writeBeginBlock("public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {");
   
    generateApplyMethodBody(s, entry, overloads, new PairListArgItType());
    
    s.writeCloseBlock();
  }

  private void generateApplyMethodBody(WrapperSourceWriter s, Entry entry,
      List<JvmMethod> overloads, ArgumentItType argItType) {
    
    s.writeBeginTry();
    
    argItType.init(s);
    s.writeBlankLine();

    GenericDispatchStrategy genericDispatchStrategy = getGenericDispatchStrategy(
        entry, overloads);

    int maxArgumentCount = getMaxPositionalArgs(overloads);
    for (int i = 0; i != maxArgumentCount; ++i) {
      Collection<JvmMethod> matchingByCount = Collections2.filter(overloads,
          havingPositionalArgCountOf(i));

      if (!matchingByCount.isEmpty()) {
        s.writeBeginIf("!" + argItType.hasNext());
        dispatchArityGroup(entry, s, matchingByCount, genericDispatchStrategy, 
           argItType);
        s.writeCloseBlock();
      }

      s.writeStatement("SEXP s" + i + " = " + argItType.nextArg(isEvaluated(overloads,i)));
      genericDispatchStrategy.afterArgIsEvaluated(s, i, argItType);
    }

    Collection<JvmMethod> matchingByCount = Collections2.filter(overloads,
        havingPositionalArgCountOf(maxArgumentCount));
    genericDispatchStrategy.beforeTypeMatching(s, maxArgumentCount);
    dispatchArityGroup(entry, s, matchingByCount, genericDispatchStrategy,
       argItType  );
    

    s.writeCatch(ArgumentException.class, "e");
    s.writeStatement("throw new EvalException(context, " + argumentErrorMessage(entry, overloads) + ");");
    s.writeCatch(EvalException.class, "e");
    s.writeStatement("e.initContext(context)");
    s.writeStatement("throw e;");    
    s.writeCatch(RuntimeException.class, "e");
    s.writeStatement("throw e;");
    s.writeCatch(Exception.class, "e");
    s.writeStatement("throw new r.lang.exception.EvalException(e);");
    s.writeCloseBlock();
  }

  /**
   * Generate the static methods that 
   * @param s
   * @param entry
   * @param overloads
   */
  private void generateFixedArityMethods(WrapperSourceWriter s, Entry entry, List<JvmMethod> overloads) {

    int maxArgumentCount = getMaxPositionalArgs(overloads);
    for (int i = 0; i <= maxArgumentCount; ++i) {
      Collection<JvmMethod> matchingByCount = Collections2.filter(overloads,
          havingPositionalArgCountOf(i));
      if (!matchingByCount.isEmpty() && !hasVarArgs(matchingByCount)) {

        StringBuilder signature = new StringBuilder(
            "public static SEXP doApply(Context context, Environment rho");
        for (int j = 0; j != i; ++j) {
          signature.append(", SEXP s" + j);
        }
        signature.append(") {");

        s.writeBeginBlock(signature.toString());
        s.writeBeginTry();
        dispatchArityGroup(entry, s, matchingByCount,
            new GenericDispatchStrategy(), null);
        s.writeCatch(Exception.class, "e");
        s.writeStatement("throw new EvalException(e)");
        s.writeCloseBlock();
        s.writeCloseBlock();
      }
    }

  }
  
  private void generateBuiltinApply(WrapperSourceWriter s, Entry entry,
      List<JvmMethod> overloads) {
    
    s.writeBeginBlock("public SEXP apply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP arguments[]) {");
    generateApplyMethodBody(s, entry, overloads, new ArrayArgItType());
    s.writeCloseBlock();
  }
  
  private void generateStaticMatchAndApply(WrapperSourceWriter s, Entry entry,
      List<JvmMethod> overloads) {
    
    s.writeBeginBlock("public static SEXP matchAndApply(Context context, Environment rho, FunctionCall call, String[] argumentNames, SEXP arguments[]) {");
    generateApplyMethodBody(s, entry, overloads, new ArrayArgItType());
    s.writeCloseBlock();
  }

  private boolean hasVarArgs(Collection<JvmMethod> overloads) {
    for (JvmMethod overload : overloads) {
      if (overload.acceptsArgumentList()) {
        return true;
      }
    }
    return false;
  }

  private boolean isEvaluated(List<JvmMethod> overloads, int argumentIndex) {
    boolean evaluated = false;
    boolean unevaluated = false;
    for (JvmMethod overload : overloads) {
      if (argumentIndex < overload.getFormals().size()) {
        if (overload.getFormals().get(argumentIndex).isEvaluated()) {
          evaluated = true;
        } else {
          unevaluated = true;
        }
      }
    }
    if (evaluated && unevaluated) {
      throw new GeneratorDefinitionException(
          "Mixing evaluated and unevaluated arguments at the same position is not yet supported");
    }
    return evaluated;
  }

  private int getMaxPositionalArgs(List<JvmMethod> overloads) {
    int max = 0;
    for (JvmMethod overload : overloads) {
      int count = overload.countPositionalFormals();
      if (count > max) {
        max = count;
      }
    }
    return max;
  }

  private Predicate<JvmMethod> havingPositionalArgCountOf(final int n) {
    return new Predicate<JvmMethod>() {

      @Override
      public boolean apply(JvmMethod input) {
        return input.countPositionalFormals() == n;
      }
    };
  }

  private GenericDispatchStrategy getGenericDispatchStrategy(Entry entry,
      List<JvmMethod> overloads) {
    JvmMethod overload = overloads.get(0);
    if (overload.isGroupGeneric()) {
      if (overload.getGenericGroup().equals("Ops")) {
        return new OpsGroupGenericDispatchStrategy(entry.name);
      } else if (overload.getGenericGroup().equals("Summary")) {
        return new SummaryGroupGenericStrategy(entry.name);
      } else {
        throw new GeneratorDefinitionException(
            "Group generic dispatch for group '" + overload.getGenericName()
                + "' is not implemented");
      }
    } else if (overload.isGeneric()) {
      return new SimpleDispatchStrategy(entry.name);
    } else {
      return new GenericDispatchStrategy();
    }
  }

  private void dispatchArityGroup(Entry entry, WrapperSourceWriter s,
      Collection<JvmMethod> overloads,
      GenericDispatchStrategy genericDispatchStrategy,
      ArgumentItType argItType) {
    if (overloads.size() == 1) {
      JvmMethod overload = overloads.iterator().next();
      if (overload.getPositionalFormals().isEmpty()) {
        generateCall(s, overload, genericDispatchStrategy, argItType);
        return;
      }
    }

    IfElseSeries choice = new IfElseSeries(s, overloads.size());
    for (JvmMethod overload : overloads) {
      choice.elseIf(testCondition(overload));
      generateCall(s, overload, genericDispatchStrategy, argItType);
    }
    choice.finish();
    s.writeStatement(noMatchingOverloadErrorMessage(entry, overloads));
  }

  private String testCondition(JvmMethod overload) {
    StringBuilder condition = new StringBuilder();
    List<Argument> posFormals = overload.getPositionalFormals();
    for (int i = 0; i != posFormals.size(); ++i) {
      if (condition.length() > 0) {
        condition.append(" && ");
      }
      ArgConverterStrategy strategy = ArgConverterStrategies
          .findArgConverterStrategy(posFormals.get(i));
      condition.append("(" + strategy.getTestExpr("s" + i) + ")");
    }
    return condition.toString();
  }

  private void generateCall(WrapperSourceWriter s, JvmMethod method,
      GenericDispatchStrategy genericDispatchStrategy, ArgumentItType argItType) {

    s.writeComment("**** " + method.toString());

    ArgumentList argumentList = new ArgumentList();
    Map<JvmMethod.Argument, String> namedFlags = Maps.newHashMap();
    List<RecycledArgument> recycledArgs = Lists.newArrayList();

    int argIndex = 0;
    boolean varArgsSeen = false;

    for (JvmMethod.Argument argument : method.getAllArguments()) {
      if (argument.isContextual()) {
        argumentList.add(contextualArgumentName(argument));

      } else if (argument
          .isAnnotatedWith(org.renjin.primitives.annotations.ArgumentList.class)) {
        argumentList.add("argList");
        varArgsSeen = true;

      } else {

        String evaledLocal = "s" + argIndex;
        String convertedLocal = "arg" + argIndex;

        ArgConverterStrategy strategy = ArgConverterStrategies
            .findArgConverterStrategy(argument);
        s.writeTempLocalDeclaration(strategy.getTempLocalType(), convertedLocal);

        if (argument.isAnnotatedWith(NamedFlag.class)) {
          s.writeStatement(convertedLocal + " = "
              + (argument.getDefaultValue() ? "true" : "false"));
          namedFlags.put(argument, convertedLocal);
        } else {
          if (varArgsSeen) {
            throw new GeneratorDefinitionException(
                "Any argument following a @ArgumentList must be annotated with @NamedFlag");
          }
          s.writeStatement(strategy.argConversionStatement(convertedLocal,
              evaledLocal));
        }

        if (argument.isRecycle()) {
          recycledArgs.add(new RecycledArgument(argument, convertedLocal));
          argumentList.add(convertedLocal + "_element");
        } else {
          argumentList.add(convertedLocal);
        }

        argIndex++;
      }
    }
    if (varArgsSeen) {
      s.writeBlankLine();
      s.writeComment("match var args");
      s.writeStatement("ListVector.NamedBuilder argListBuilder = new ListVector.NamedBuilder();");
      s.writeBeginBlock("while(" + argItType.hasNext() + ") { ");
      writeHandleNode(s, namedFlags, argItType);
      s.writeCloseBlock();
      s.writeStatement("ListVector argList = argListBuilder.build()");
    }

    s.writeBlankLine();

    genericDispatchStrategy.beforePrimitiveCalled(s);

    if (method.isRecycle()) {
      writeRecyclingCalls(s, method, argumentList, recycledArgs);
    } else {
      s.writeComment("make call");

      s.writeStatement(callStatement(method, argumentList));
    }
    if (method.returnsVoid()) {
      s.writeStatement("context.setInvisibleFlag()");
      s.writeStatement("return r.lang.Null.INSTANCE;");
    }
    s.writeBlankLine();
  }

  private void writeHandleNode(WrapperSourceWriter s,
      Map<JvmMethod.Argument, String> namedFlags, ArgumentItType argIt) {
   
    s.writeStatement("SEXP evaled");
    argIt.writeFetchNextNode(s);
    
    s.writeBeginIf(argIt.hasName());
    s.writeStatement("String name = " + argIt.fetchArgName());

    if (!namedFlags.isEmpty()) {
      boolean needElseIf = false;
      for (JvmMethod.Argument namedFlag : namedFlags.keySet()) {

        if (needElseIf) {
          s.outdent();
        }

        s.writeBeginBlock((needElseIf ? "} else " : "") + "if(name.equals(\""
            + namedFlag.getName() + "\")) {");
        s.writeBeginIf("evaled != Symbol.MISSING_ARG");
        s.writeStatement(ArgConverterStrategies.findArgConverterStrategy(
            namedFlag).conversionStatement(namedFlags.get(namedFlag), "evaled"));
        s.writeCloseBlock();
        needElseIf = true;
      }
      s.outdent();
      s.writeBeginBlock("} else {");
    }

    s.writeStatement("argListBuilder.add(name, evaled);");

    if (!namedFlags.isEmpty()) {
      s.writeCloseBlock();
    }

    s.outdent();
    s.writeBeginBlock("} else {");
    s.writeStatement("argListBuilder.add(evaled);");
    s.writeCloseBlock();
  }

  private void writeRecyclingCalls(WrapperSourceWriter s, JvmMethod method,
      ArgumentList argumentList, List<RecycledArgument> recycledArguments) {
    ScalarType resultType = ScalarTypes.get(method.getReturnType());

    RecycledArguments recycled;
    if (recycledArguments.size() == 1) {
      recycled = new SingleRecycledArgument(s, method, recycledArguments);
    } else {
      recycled = new RecycledArguments(s, method, recycledArguments);
    }

    recycled.writeSetup();

    s.writeStatement(WrapperSourceWriter.toJava(resultType.getBuilderClass())
        + " result = new "
        + WrapperSourceWriter.toJava(resultType.getBuilderClass())
        + "(cycles);");
    s.writeStatement("int resultIndex = 0;");
    s.writeBlankLine();

    s.writeBeginBlock("for(int i=0;i!=cycles;++i) {");

    if (!method.acceptsNA()) {

      s.writeBeginBlock("if(" + recycled.composeAnyNACondition() + ") {");
      s.writeStatement("result.setNA(i)");
      s.outdent();
      s.writeBeginBlock("} else {");
    }
    recycled.writeElementExtraction();
    String invocationExpression = method.getDeclaringClass().getName() + "."
        + method.getName() + "(" + argumentList + ")";

    s.writeBlankLine();
    boolean hasListsAsElements = method.getReturnType().isAssignableFrom(
        ListVector.class);
    if (hasListsAsElements) {
      // if we have a recycling function whose result is a list, if there is
      // only
      // one result, don't wrap it in a list
      s.writeBeginIf("cycles==1");
      s.writeStatement(handleReturn(method, invocationExpression));
      s.writeElse();
    }

    s.writeStatement("result.set(i, " + invocationExpression + ")");

    if (hasListsAsElements) {
      s.writeCloseBlock();
    }

    if (!method.acceptsNA()) {
      s.writeCloseBlock();
    }

    recycled.writeIncrementCounters();

    s.writeCloseBlock();
    if (method.getPreserveAttributesStyle() != PreserveAttributeStyle.NONE) {
      s.writeBeginBlock("if(cycles > 0) {");
      for (int i = recycled.size() - 1; i >= 0; --i) {
        if (recycled.size() > 1) {
          s.writeBeginIf(recycled.getLengthLocal(i) + "  == cycles");
        }
        String vectorLocal = recycled.getVectorLocal(i);
        switch (method.getPreserveAttributesStyle()) {
        case ALL:
          s.writeStatement("result.copyAttributesFrom(" + vectorLocal + ")");
          break;
        case SPECIAL:
          s.writeStatement("result.copySomeAttributesFrom(" + vectorLocal
              + ", Symbols.DIM, Symbols.DIMNAMES, Symbols.NAMES);");
          break;
        }
        if (recycled.size() > 1) {
          s.writeCloseBlock();
        }
      }
      s.writeCloseBlock();
    }
    s.writeStatement("return result.build();");
  }

  @Override
  public boolean accept(List<JvmMethod> overloads) {
    return true;
  }
}
