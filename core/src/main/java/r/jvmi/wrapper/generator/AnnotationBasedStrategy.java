package r.jvmi.wrapper.generator;

import java.util.List;
import java.util.Map;

import r.base.BaseFrame.Entry;
import r.jvmi.annotations.NamedFlag;
import r.jvmi.annotations.PreserveAttributeStyle;
import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.GeneratorDefinitionException;
import r.jvmi.wrapper.IfElseSeries;
import r.jvmi.wrapper.WrapperSourceWriter;
import r.jvmi.wrapper.generator.args.ArgConverterStrategies;
import r.jvmi.wrapper.generator.args.ArgConverterStrategy;
import r.jvmi.wrapper.generator.recycling.RecycledArgument;
import r.jvmi.wrapper.generator.recycling.RecycledArguments;
import r.jvmi.wrapper.generator.recycling.SingleRecycledArgument;
import r.jvmi.wrapper.generator.scalars.ScalarType;
import r.jvmi.wrapper.generator.scalars.ScalarTypes;
import r.lang.SEXP;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AnnotationBasedStrategy extends GeneratorStrategy {

  @Override
  public boolean accept(List<JvmMethod> overloads) {
    return !overloads.get(0).isGroupGeneric();
  }
  

  @Override
  protected void generateCall(Entry entry, WrapperSourceWriter s, List<JvmMethod> overloads) {
    
    s.writeStatement("ArgumentIterator argIt = new ArgumentIterator(context, rho, args)");
    s.writeBlankLine();
   
    GenericDispatchStrategy genericDispatchStrategy = getGenericDispatchStrategy(entry, overloads);
    OverloadNode tree = OverloadNode.buildTree(overloads);
    testNextArg(s, tree, 0, genericDispatchStrategy);
  }
  
  private GenericDispatchStrategy getGenericDispatchStrategy(Entry entry, List<JvmMethod> overloads) {
    if(overloads.get(0).isGroupGeneric()) {
      return new GroupGenericDispatchStrategy(overloads.get(0).getGenericGroup(), entry.name);
    } else if(overloads.get(0).isGeneric()) {
      return new GenericDispatchStrategy(entry.name);
    } else {
      return null;
    }
  }

  private void testNextArg(WrapperSourceWriter s, OverloadNode parent, int argIndex,
      GenericDispatchStrategy genericDispatchStrategy) {
    
    if(parent.hasLeaf() && parent.hasNextArg() ) {
      s.writeBeginBlock("if(argIt.hasNext()) {");
    }
    if(parent.hasNextArg()) {
      branchOnArgType(s, parent, argIndex, genericDispatchStrategy);
    }
    if(parent.hasLeaf() && parent.hasNextArg()) {
      s.writeElse();
    }
    if(parent.hasLeaf()) {
      generateCall(s, parent.getLeaf());
    }
    if(parent.hasLeaf() && parent.hasNextArg()) {
      s.writeCloseBlock();  
    }
  }

  private void branchOnArgType(WrapperSourceWriter s, OverloadNode parent,
      int argIndex, GenericDispatchStrategy genericDispatchStrategy) {
    String argLocal = "s" + argIndex;
    if(parent.isEvaluated()) {
      s.writeStatement("SEXP " + argLocal + " = argIt.next().evalToExp(context,rho)");
    } else {
      s.writeStatement("SEXP " + argLocal + " = argIt.next()");
    }
    
    if(genericDispatchStrategy != null) {
      genericDispatchStrategy.writeMaybeDispatch(s, argIndex);
    }
    
    IfElseSeries choices = new IfElseSeries(s, parent.getChildren().size());
    for(OverloadNode child : parent.getChildren()) {
      choices.elseIf(child.getArgStrategy().getTestExpr(argLocal));
      
      String convertedLocal = "arg" + argIndex;
      s.writeTempLocalDeclaration(child.getArgStrategy().getTempLocalType(), convertedLocal);
      s.writeStatement(child.getArgStrategy().argConversionStatement(convertedLocal, argLocal));
      
      testNextArg(s, child, argIndex+1, genericDispatchStrategy);
    }
    choices.finish();
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
        ArgConverterStrategy strategy = ArgConverterStrategies.findArgConverterStrategy(argument);
        String tempLocal = "arg" + (argIndex++);
        
        if(argument.isAnnotatedWith(NamedFlag.class)) {
          s.writeStatement(argument.getClazz().getName() + " " + tempLocal + " = " + (argument.getDefaultValue() ? "true" : "false") );
          namedFlags.put(argument, tempLocal);
        } else {
          if(varArgsSeen) {
            throw new GeneratorDefinitionException("Any argument following a @ArgumentList must be annotated with @NamedFlag");
          }
        }
        
        if(argument.isRecycle()) {
          recycledArgs.add(new RecycledArgument(argument, tempLocal));
          argumentList.add(tempLocal + "_element");
        } else {
          argumentList.add(tempLocal);
        }
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
      recycled = new SingleRecycledArgument(s, recycledArguments);
    } else {
      recycled = new RecycledArguments(s, recycledArguments);
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
    s.writeStatement("result.set(i, " + method.getDeclaringClass().getName() + "." + method.getName() + "(" + argumentList +"))");
    
    if(!method.acceptsNA()) {
      s.writeCloseBlock();
    }
    
    recycled.writeIncrementCounters();
    
    s.writeCloseBlock();
    if(method.getPreserveAttributesStyle() != PreserveAttributeStyle.NONE) {
      s.writeBeginBlock("if(cycles > 0) {");
      switch(method.getPreserveAttributesStyle()) {
      case ALL:
        s.writeStatement("result.copyAttributesFrom(" + recycled.getLongestLocal() + ")");
        break;
      case SPECIAL:
        s.writeStatement("result.copySomeAttributesFrom(" + recycled.getLongestLocal() + 
              ", Symbol.DIM, Symbol.DIMNAMES, Symbol.NAMES);");
        break;
      }
      s.writeCloseBlock();
    }
    
    s.writeStatement("return new EvalResult(result.build());" );
  }  
}
