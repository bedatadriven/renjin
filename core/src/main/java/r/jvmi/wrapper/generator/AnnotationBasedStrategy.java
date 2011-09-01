package r.jvmi.wrapper.generator;

import java.util.List;
import java.util.Map;

import r.jvmi.annotations.NamedFlag;
import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.GeneratorDefinitionException;
import r.jvmi.wrapper.WrapperSourceWriter;
import r.jvmi.wrapper.generator.recycling.RecycledArgument;
import r.jvmi.wrapper.generator.recycling.RecycledArguments;
import r.jvmi.wrapper.generator.recycling.SingleRecycledArgument;
import r.jvmi.wrapper.generator.scalars.ScalarType;
import r.jvmi.wrapper.generator.scalars.ScalarTypes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AnnotationBasedStrategy extends GeneratorStrategy {

  @Override
  public boolean accept(List<JvmMethod> overloads) {
    return overloads.size() == 1 && !overloads.get(0).isGeneric();
  }

  @Override
  protected void generateCall(WrapperSourceWriter s, List<JvmMethod> overloads) {
    JvmMethod method = overloads.get(0);

    
    ArgumentList argumentList = new ArgumentList(); 
    Map<JvmMethod.Argument, String> namedFlags = Maps.newHashMap();
    List<RecycledArgument> recycledArgs = Lists.newArrayList();
    
    s.writeStatement("ArgumentIterator argIt = new ArgumentIterator(context, rho, args)");
    s.writeBlankLine();
    
    s.writeComment("extract (and maybe) evaluate positional arguments");
    
    int argIndex = 0;
    boolean varArgsSeen = false;    
    
    for(JvmMethod.Argument argument : method.getAllArguments()) {
      if(argument.isContextual()) {
        argumentList.add(contextualArgumentName(argument));
      
      } else if(argument.isAnnotatedWith(r.jvmi.annotations.ArgumentList.class)) {
        argumentList.add("argList.build()");
        varArgsSeen = true;
        
      } else {
        String tempLocal = "arg" + (argIndex++);
        
        if(argument.isAnnotatedWith(NamedFlag.class)) {
          s.writeStatement(argument.getClazz().getName() + " " + tempLocal + " = " + (argument.getDefaultValue() ? "true" : "false") );
          namedFlags.put(argument, tempLocal);
        } else {
          if(varArgsSeen) {
            throw new GeneratorDefinitionException("Any argument following a @ArgumentList must be annotated with @NamedFlag");
          }
          s.writeTempLocalDeclaration(findArgConverterStrategy(argument).getTempLocalType(argument), tempLocal);
          s.writeStatement(argConversionStatement(argument, tempLocal));

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
        s.writeStatement(findArgConverterStrategy(namedFlag).conversionStatement(namedFlag, namedFlags.get(namedFlag), "evaled"));
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
    
    s.writeStatement(toJava(resultType.getBuilderClass()) + " result = new " +
        toJava(resultType.getBuilderClass()) + "(cycles);");
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
    
    switch(method.getPreserveAttributesStyle()) {
    case ALL:
      s.writeStatement("result.copyAttributesFrom(" + recycled.getLongestLocal() + ")");
      break;
    case SPECIAL:
      s.writeStatement("result.copySomeAttributesFrom(" + recycled.getLongestLocal() + 
            ", Symbol.DIM, Symbol.DIMNAMES, Symbol.NAMES);");
      break;
    }

    s.writeStatement("return new EvalResult(result.build());" );

  }

  
}
