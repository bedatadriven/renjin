package r.jvmi.wrapper.generator;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import r.base.Primitives.Entry;
import r.jvmi.annotations.NamedFlag;
import r.jvmi.annotations.PreserveAttributeStyle;
import r.jvmi.binding.JvmMethod;
import r.jvmi.binding.JvmMethod.Argument;
import r.jvmi.wrapper.GeneratorDefinitionException;
import r.jvmi.wrapper.IfElseSeries;
import r.jvmi.wrapper.WrapperSourceWriter;
import r.jvmi.wrapper.generator.args.ArgConverterStrategies;
import r.jvmi.wrapper.generator.args.ArgConverterStrategy;
import r.jvmi.wrapper.generator.generic.GenericDispatchStrategy;
import r.jvmi.wrapper.generator.generic.OpsGroupGenericDispatchStrategy;
import r.jvmi.wrapper.generator.generic.SimpleDispatchStrategy;
import r.jvmi.wrapper.generator.recycling.RecycledArgument;
import r.jvmi.wrapper.generator.recycling.RecycledArguments;
import r.jvmi.wrapper.generator.recycling.SingleRecycledArgument;
import r.jvmi.wrapper.generator.scalars.ScalarType;
import r.jvmi.wrapper.generator.scalars.ScalarTypes;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AnnotationBasedStrategy extends GeneratorStrategy {

  @Override
  public boolean accept(List<JvmMethod> overloads) {
   // return !overloads.get(0).isGroupGeneric();
    return true;
  }

  @Override
  protected void generateCall(Entry entry, WrapperSourceWriter s, List<JvmMethod> overloads) {
    s.writeStatement("ArgumentIterator argIt = new ArgumentIterator(context, rho, args)");
    s.writeBlankLine();
   
    GenericDispatchStrategy genericDispatchStrategy = getGenericDispatchStrategy(entry, overloads);
   
    int maxArgumentCount = getMaxPositionalArgs(overloads);
    for(int i = 0; i!=maxArgumentCount;++i) {
      Collection<JvmMethod> matchingByCount = Collections2.filter(overloads, havingPositionalArgCountOf(i));
       
      if(!matchingByCount.isEmpty()) {
        s.writeBeginIf("!argIt.hasNext()");
        genericDispatchStrategy.beforePrimitiveIsCalled(s, i);
        dispatchArityGroup(entry, s, matchingByCount);
        s.writeCloseBlock();
      }
      
      if(isEvaluated(overloads, i)) {
        s.writeStatement("SEXP s" + i + " = argIt.next().evalToExp(context,rho)");
      } else {
        s.writeStatement("SEXP s" + i + " = argIt.next()");
      }
      genericDispatchStrategy.afterArgIsEvaluated(s, i);
    }
    
    
    Collection<JvmMethod> matchingByCount = Collections2.filter(overloads, havingPositionalArgCountOf(maxArgumentCount));
    genericDispatchStrategy.beforePrimitiveIsCalled(s, maxArgumentCount);
    dispatchArityGroup(entry, s, matchingByCount);
  }
    
  @Override
  protected void generateOtherCalls(Entry entry, WrapperSourceWriter s,
      List<JvmMethod> overloads) {
       
    int maxArgumentCount = getMaxPositionalArgs(overloads);
    for(int i = 0; i<=maxArgumentCount;++i) {
      Collection<JvmMethod> matchingByCount = Collections2.filter(overloads, havingPositionalArgCountOf(i));
      if(!matchingByCount.isEmpty() && ! hasVarArgs(matchingByCount)) {
        
        StringBuilder signature = new StringBuilder("public static EvalResult doApply(Context context, Environment rho");
        for(int j=0;j!=i;++j) {
          signature.append(", SEXP s"+ j);
        }
        signature.append(") {");
        
        s.writeBeginBlock(signature.toString());
        s.writeBeginTry();
        dispatchArityGroup(entry, s, matchingByCount);
        s.writeCatch(Exception.class, "e");
        s.writeStatement("throw new EvalException(e)");
        s.writeCloseBlock();
        s.writeCloseBlock();
      }
    }
  }

  private boolean hasVarArgs(Collection<JvmMethod> overloads) {
    for(JvmMethod overload : overloads) {
      if(overload.acceptsArgumentList()) {
        return true;
      }
    }
    return false;
  }

  private boolean isEvaluated(List<JvmMethod> overloads, int argumentIndex) {
    boolean evaluated = false;
    boolean unevaluated = false;
    for(JvmMethod overload : overloads) {
      if(argumentIndex < overload.getFormals().size()) {
        if(overload.getFormals().get(argumentIndex).isEvaluated()) {
          evaluated = true;
        } else {
          unevaluated = false;
        }
      }
    }
    if(evaluated && unevaluated) {
      throw new GeneratorDefinitionException("Mixing evaluated and unevaluated arguments at the same position is not yet supported");
    }
    return evaluated;
  }

  private int getMaxPositionalArgs(List<JvmMethod> overloads) {
    int max = 0;
    for(JvmMethod overload : overloads) {
      int count = overload.countPositionalFormals();
      if(count > max) {
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
  
  private GenericDispatchStrategy getGenericDispatchStrategy(Entry entry, List<JvmMethod> overloads) {
    if(overloads.get(0).isGroupGeneric()) {
      if (overloads.get(0).getGenericGroup().equals("Ops")) {
        return new OpsGroupGenericDispatchStrategy(entry.name);
      } else {
        throw new GeneratorDefinitionException("Only 'Ops' @GenericGroup functions are supported at the moment");
      }
    } else if(overloads.get(0).isGeneric()) {
      return new SimpleDispatchStrategy(entry.name);
    } else {
      return new GenericDispatchStrategy();
    }
  }

  
  private void dispatchArityGroup(Entry entry, WrapperSourceWriter s, Collection<JvmMethod> overloads) {
    if(overloads.size() == 1) {
      JvmMethod overload = overloads.iterator().next();
      if(overload.getPositionalFormals().isEmpty()) {
        generateCall(s, overload);
        return;
      }
    }
    
    IfElseSeries choice = new IfElseSeries(s, overloads.size());
    for(JvmMethod overload : overloads) {
      choice.elseIf(testCondition(overload));
      generateCall(s, overload);
    }
    choice.finish();
    s.writeStatement(noMatchingOverloadErrorMessage(entry, overloads));
  }

  private String testCondition(JvmMethod overload) {
    StringBuilder condition = new StringBuilder(); 
    List<Argument> posFormals = overload.getPositionalFormals();
    for(int i=0;i!=posFormals.size();++i) {
      if(condition.length() > 0) {
        condition.append(" && ");
      }
      ArgConverterStrategy strategy = ArgConverterStrategies.findArgConverterStrategy(posFormals.get(i));
      condition.append("(" + strategy.getTestExpr("s" + i) + ")");
    }
    return condition.toString();
  }

  
  protected void generateCall(WrapperSourceWriter s, JvmMethod method) {
  
    s.writeComment("**** " + method.toString());

    ArgumentList argumentList = new ArgumentList();
    Map<JvmMethod.Argument, String> namedFlags = Maps.newHashMap();
    List<RecycledArgument> recycledArgs = Lists.newArrayList();

    int argIndex = 0;
    boolean varArgsSeen = false;    
    
    for(JvmMethod.Argument argument : method.getAllArguments()) {
      if(argument.isContextual()) {
        argumentList.add(contextualArgumentName(argument));
      
      } else if(argument.isAnnotatedWith(r.jvmi.annotations.ArgumentList.class)) {
        argumentList.add("argList.build()");
        varArgsSeen = true;
        
      } else {

        String evaledLocal = "s" + argIndex;
        String convertedLocal = "arg" + argIndex;

        ArgConverterStrategy strategy = ArgConverterStrategies.findArgConverterStrategy(argument);
        s.writeTempLocalDeclaration(strategy.getTempLocalType(), convertedLocal);

        if(argument.isAnnotatedWith(NamedFlag.class)) {
          s.writeStatement(convertedLocal + " = " + (argument.getDefaultValue() ? "true" : "false") );
          namedFlags.put(argument, convertedLocal);
        } else {
          if(varArgsSeen) {
            throw new GeneratorDefinitionException("Any argument following a @ArgumentList must be annotated with @NamedFlag");
          }
          s.writeStatement(strategy.argConversionStatement(convertedLocal, evaledLocal));
        }

        if(argument.isRecycle()) {
          recycledArgs.add(new RecycledArgument(argument, convertedLocal));
          argumentList.add(convertedLocal + "_element");
        } else {
          argumentList.add(convertedLocal);
        }

        argIndex++;
      }
    }
    if(varArgsSeen) {
      s.writeBlankLine();
      s.writeComment("match var args");
      s.writeStatement("ListVector.Builder argList = new ListVector.Builder();");
      s.writeBeginBlock("while(argIt.hasNext()) { ");
      writeHandleNode(s, namedFlags);
      s.writeCloseBlock();     
    }

    s.writeBlankLine();
    
    if(method.isRecycle()) {
      writeRecyclingCalls(s, method, argumentList, recycledArgs);
    } else { 
      s.writeComment("make call");
      s.writeStatement(callStatement(method, argumentList));
    }
    if(method.returnsVoid()) {
      s.writeStatement("return EvalResult.NON_PRINTING_NULL;");
    }
    s.writeBlankLine();
  }

  private void writeHandleNode(WrapperSourceWriter s, Map<JvmMethod.Argument, String> namedFlags) {
    s.writeStatement("PairList.Node node = argIt.nextNode()");
    s.writeStatement("SEXP value = node.getValue()");
    s.writeStatement("SEXP evaled");
    s.writeBeginBlock("if(Symbol.MISSING_ARG.equals(value)) {");
    s.writeStatement("evaled = value");
    s.outdent();
    s.writeBeginBlock("} else {");
    s.writeStatement("evaled = value.evalToExp(context, rho)");
    s.writeCloseBlock();
    s.writeBeginBlock("if(node.hasTag()) {");
  
    if(!namedFlags.isEmpty()) {
      s.writeStatement("String name = node.getTag().getPrintName()");
      
      boolean needElseIf=false;
      for(JvmMethod.Argument namedFlag : namedFlags.keySet()) {

        if(needElseIf) {
          s.outdent();
        }
         
        s.writeBeginBlock( (needElseIf ? "} else " : "") + "if(name.equals(\"" + namedFlag.getName() + "\")) {");
        s.writeStatement(ArgConverterStrategies.findArgConverterStrategy(namedFlag).conversionStatement(namedFlags.get(namedFlag), "evaled"));
        needElseIf = true;
      }
      s.outdent();
      s.writeBeginBlock("} else {");
    }
      
    s.writeStatement("argList.add(node.getTag(), evaled);");
    
    if(!namedFlags.isEmpty()) {
      s.writeCloseBlock();
    }
    
    s.outdent();
    s.writeBeginBlock("} else {");
    s.writeStatement("argList.add(evaled);");
    s.writeCloseBlock();
  }
  
  private void writeRecyclingCalls(WrapperSourceWriter s, JvmMethod method, ArgumentList argumentList, List<RecycledArgument> recycledArguments) {
    ScalarType resultType = ScalarTypes.get(method.getReturnType());
    
    RecycledArguments recycled;
    if(recycledArguments.size() == 1) {
      recycled = new SingleRecycledArgument(s, method, recycledArguments);
    } else {
      recycled = new RecycledArguments(s, method, recycledArguments);
    }
    
    recycled.writeSetup();
    
    s.writeStatement(WrapperSourceWriter.toJava(resultType.getBuilderClass()) + " result = new " +
        WrapperSourceWriter.toJava(resultType.getBuilderClass()) + "(cycles);");
    s.writeStatement("int resultIndex = 0;");
    s.writeBlankLine();
    
    s.writeBeginBlock("for(int i=0;i!=cycles;++i) {");
        
    if(!method.acceptsNA()) {
      
      s.writeBeginBlock("if(" + recycled.composeAnyNACondition() + ") {");
      s.writeStatement("result.setNA(i)");
      s.outdent();
      s.writeBeginBlock("} else {");
    }
    recycled.writeElementExtraction();
    s.writeBlankLine();
    s.writeStatement("result.set(i, " + method.getDeclaringClass().getName() + "." + method.getName() + "(" + argumentList + "))");
    
    if(!method.acceptsNA()) {
      s.writeCloseBlock();
    }
    
    recycled.writeIncrementCounters();
    
    s.writeCloseBlock();
    if(method.getPreserveAttributesStyle() != PreserveAttributeStyle.NONE) {
      s.writeBeginBlock("if(cycles > 0) {");
      for(int i=recycled.size()-1;i>=0;--i) {
        if(recycled.size() > 1) {
          s.writeBeginIf(recycled.getLengthLocal(i) + "  == cycles");
        }
        String vectorLocal = recycled.getVectorLocal(i);
        switch(method.getPreserveAttributesStyle()) {
        case ALL:
          s.writeStatement("result.copyAttributesFrom(" + vectorLocal + ")");
          break;
        case SPECIAL:
          s.writeStatement("result.copySomeAttributesFrom(" + vectorLocal + 
                ", Symbols.DIM, Symbols.DIMNAMES, Symbols.NAMES);");
          break;
        }
        if(recycled.size() > 1) {
          s.writeCloseBlock();
        }
      }
      s.writeCloseBlock();
    }
    
    s.writeStatement("return new EvalResult(result.build());" );
  } 
}
