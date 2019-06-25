package org.renjin.invoke.codegen;

import com.sun.codemodel.*;
import org.renjin.eval.*;
import org.renjin.invoke.annotations.Materialize;
import org.renjin.invoke.annotations.SessionScoped;
import org.renjin.invoke.codegen.args.ArgConverterStrategies;
import org.renjin.invoke.codegen.args.ArgConverterStrategy;
import org.renjin.invoke.codegen.scalars.ScalarType;
import org.renjin.invoke.codegen.scalars.ScalarTypes;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.invoke.model.PrimitiveModel;
import org.renjin.primitives.S3;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr.lit;

public class OperatorsWriter {

  private final JCodeModel codeModel;
  private final JDefinedClass builtinClass;
  private final JClass wrapperRuntime;

  public OperatorsWriter(JCodeModel codeModel) throws JClassAlreadyExistsException {
    this.codeModel = codeModel;

    builtinClass = codeModel._class("org.renjin.primitives.Builtins");
    wrapperRuntime = codeModel.ref(WrapperRuntime.class);
  }

  public void add(PrimitiveModel model) {
    try {
      if (model.hasVargs()) {
        writeVarArgDispatcher(model);
      } else {
        writeFixedArgDispatcher(model);
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception generating " + model.getName(), e);
    }
  }

  /**
   * Writes a method that dispatches to one or more Java methods that do not include an
   * {@link org.renjin.invoke.annotations.ArgumentList} argument.
   */
  private void writeFixedArgDispatcher(PrimitiveModel model) {
    String methodName = WrapperGenerator2.toJavaName("", model.getName());
    JMethod method = builtinClass.method(JMod.PUBLIC | JMod.STATIC, SEXP.class, methodName);
    method._throws(codeModel.ref(Exception.class));

    JVar contextVar = method.param(codeModel.ref(Context.class), "context");
    JVar rhoVar = method.param(codeModel.ref(Environment.class), "rho");
    JVar argListVar = method.param(codeModel.ref(ArgList.class), "argList");
    JVar callVar = method.param(codeModel.ref(FunctionCall.class), "call");


    Set<Integer> arities = model.getOverloads().stream()
        .map(o -> o.getPositionalFormals().size())
        .collect(Collectors.toSet());

    JSwitch argSwitch = method.body()._switch(argListVar.invoke("size"));

    for (Integer arity : arities) {

      // Write a separate static method that can be invoked by compiled code when the number
      // of arguments are known at compile time
      writeFixedArityDispatcher(model, arity);

      // Add a switch branch for this number of arguments
      JInvocation invocation = builtinClass.staticInvoke(methodName)
          .arg(contextVar)
          .arg(rhoVar)
          .arg(callVar);

      for (int i = 0; i < arity; i++) {
          invocation.arg(argListVar.ref("values").component(JExpr.lit(i)));
      }

      argSwitch._case(JExpr.lit(arity))
          .body()
          ._return(invocation);
    }

    argSwitch._default().body()._throw(codeModel.ref(Support.class).staticInvoke("arityException")
        .arg(callVar)
        .arg("FOO"));
  }


  private void writeFixedArityDispatcher(PrimitiveModel primitive, int nargs) {

    String methodName = WrapperGenerator2.toJavaName("", primitive.getName());
    JMethod method = builtinClass.method(JMod.PUBLIC | JMod.STATIC, codeModel.ref(SEXP.class), methodName)
        ._throws(codeModel.ref(Exception.class));

    JVar contextVar = method.param(codeModel.ref(Context.class), "context");
    JVar rhoVar = method.param(codeModel.ref(Environment.class), "rho");
    JVar callVar = method.param(codeModel.ref(FunctionCall.class), "call");

    List<JVar> arguments = new ArrayList<>();
    for (int i = 0; i < nargs; i++) {
      arguments.add(method.param(codeModel.ref(SEXP.class), "arg" + i));
    }

    String generic = primitive.getGenericName();
    String groupGeneric = primitive.getGenericGroupName();

    if(generic != null && nargs > 0) {

      JBlock ifObject = method.body()._if(arguments.get(0).invoke("isObject"))._then();
      JVar genericResult = ifObject.decl(codeModel.ref(SEXP.class), "genericResult",
          codeModel.ref(S3.class).staticInvoke("tryDispatchFromPrimitive")
              .arg(contextVar)
              .arg(rhoVar)
              .arg(callVar)
              .arg(JExpr.lit(generic))
              .arg(groupGeneric == null ? JExpr._null() : JExpr.lit(groupGeneric))
              .arg(JExpr._null())
              .arg(argumentArrayLiteral(SEXP.class, arguments)));
      ifObject._if(genericResult.ne(JExpr._null()))
          ._then()
          ._return(genericResult);
    }


    // If have not delegated to the generic, then test which overload matches

    IfElseBuilder matchSequence = new IfElseBuilder(method.body());
    List<JvmMethod> overloads = Lists.newArrayList( primitive.overloadsWithPosArgCountOf(nargs) );

    if(primitive.isRelationalOperator()) {
      JVar arg0 = arguments.get(0);
      JVar arg1 = arguments.get(1);
      overloads.sort(new OverloadComparator());
      Collections.reverse(overloads);

      method.body()
          ._if(
            wrapperRuntime.staticInvoke("isEmptyOrNull").arg(arg0).cor(
            wrapperRuntime.staticInvoke("isEmptyOrNull").arg(arg1)))
          ._then()
          ._return(codeModel.ref(LogicalVector.class).staticRef("EMPTY"));


//      This code will be generated to handle FunctionCall and Symbol coercion to character
//      arg0 = maybeConvertToStringVector(arg0);
//      arg1 = WrapperRuntime.maybeConvertToStringVector(arg1);
      method.body().assign(JExpr.ref("arg0"), wrapperRuntime.staticInvoke("maybeConvertToStringVector").arg(contextVar).arg(arg0));
      method.body().assign(JExpr.ref("arg1"), wrapperRuntime.staticInvoke("maybeConvertToStringVector").arg(contextVar).arg(arg1));

      for(JvmMethod overload : overloads) {
        ScalarType scalarType = ScalarTypes.get(overload.getFormals().get(0).getClazz());
        JClass vectorType = codeModel.ref(scalarType.getVectorType());
        JBlock stringBlock = matchSequence
            ._if(arg0._instanceof(vectorType)
                .cor(arg1._instanceof(vectorType)));
        invokeOverload(primitive, overload, stringBlock, contextVar, rhoVar, arguments);
      }

    } else {
      /*
       * Sort the overloads so that we test more narrow types first, e.g.,
       * try "int" before falling back to "double".
       */
      overloads.sort(new OverloadComparator());
      for(JvmMethod overload : overloads) {
        /*
         * If the types match, invoke the Java method
         */
        invokeOverload(primitive, overload, matchSequence._if(argumentsMatch(overload, arguments)), contextVar, rhoVar, arguments);
      }
    }

    /*
     * No matching methods, throw an exception
     */
    matchSequence._else()._throw(_new(codeModel.ref(EvalException.class))
        .arg(typeMismatchErrorMessage(primitive, arguments)));

  }

  /**
   * Compute the expression that will test whether the provided arguments
   * match the given overload.
   */
  private JExpression argumentsMatch(JvmMethod overload, List<JVar> arguments) {
    JExpression condition = JExpr.TRUE;
    List<JvmMethod.Argument> posFormals = overload.getPositionalFormals();
    for (int i = 0; i != posFormals.size(); ++i) {

      ArgConverterStrategy strategy = ArgConverterStrategies
          .find(posFormals.get(i));

      JExpression argCondition = strategy.getTestExpr(codeModel, arguments.get(i));
      if(condition == null) {
        condition = argCondition;
      } else {
        condition = condition.cand(argCondition);
      }
    }
    return condition;
  }

  private void invokeOverload(PrimitiveModel primitive, JvmMethod overload, JBlock block, JVar contextVar, JVar environmentVar, List<JVar> arguments) {

    if(overload.isDataParallel()) {
      new RecycleLoopBuilder(codeModel, block, contextVar, primitive, overload, mapArguments(overload, contextVar, environmentVar, arguments))
          .build();
    } else {
      invokeSimpleMethod(overload, block, contextVar, environmentVar, arguments);
    }
  }

  private JExpression typeMismatchErrorMessage(PrimitiveModel primitive, List<JVar> arguments) {
    JInvocation format = codeModel.ref(String.class).staticInvoke("format");
    format.arg(lit(typeMessageErrorFormat(primitive, arguments.size())));
    for(JVar arg : arguments) {
      format.arg(arg.invoke("getTypeName"));
    }
    return format;
  }

  /**
   * Invokes with the JVM method simply (without recycling) using the
   * provided arguments.
   */
  private void invokeSimpleMethod(JvmMethod overload, JBlock block, JVar contextVar, JVar environmentVar, List<JVar> arguments) {
    JInvocation invocation = codeModel.ref(overload.getDeclaringClass())
        .staticInvoke(overload.getName());

    Map<JvmMethod.Argument, JExpression> argumentMap = mapArguments(overload, contextVar, environmentVar, arguments);

    for(JvmMethod.Argument argument : overload.getAllArguments()) {
      invocation.arg(argumentMap.get(argument));
    }
    CodeModelUtils.returnSexp(contextVar, codeModel, block, overload, invocation);
  }


  private String typeMessageErrorFormat(PrimitiveModel primitive, int nargs) {

    String escapedFunctionName = primitive.getName().replaceAll("%", "%%");

    StringBuilder message = new StringBuilder();
    message.append("Invalid argument:\n");
    message.append("\t").append(escapedFunctionName).append("(");

    for(int i=0;i<nargs;++i) {
      if(i > 0) {
        message.append(", ");
      }
      message.append("%s");
    }
    message.append(")\n");
    message.append("\tExpected:");
    for(JvmMethod method : primitive.getOverloads()) {
      message.append("\n\t");
      method.appendFriendlySignatureTo(escapedFunctionName, message);
    }
    return message.toString();
  }

  private Map<JvmMethod.Argument, JExpression> mapArguments(JvmMethod overload, JVar contextVar, JVar environmentVar, List<JVar> arguments) {
    Map<JvmMethod.Argument, JExpression> argumentMap = Maps.newHashMap();

    int argumentPos = 0;
    for(JvmMethod.Argument argument : overload.getAllArguments()) {
      if(argument.isContextual()) {
        if(argument.getClazz().equals(Context.class)) {
          argumentMap.put(argument, contextVar);
        } else if(argument.getClazz().equals(Environment.class)){
          argumentMap.put(argument, environmentVar);
        } else if(argument.getClazz().equals(Session.class)) {
          argumentMap.put(argument, contextVar.invoke("getSession"));
        } else if(argument.getClazz().getAnnotation(SessionScoped.class) != null) {
          argumentMap.put(argument, contextVar.invoke("getSingleton").arg(JExpr.dotclass(codeModel.ref(argument.getClazz()))));
        } else {
          throw new UnsupportedOperationException(argument.getClazz().getName());
        }
      } else {
        JVar argumentValue = arguments.get(argumentPos++);
        JExpression materializedValue = materialize(overload, contextVar, argumentValue);
        ArgConverterStrategy strategy = ArgConverterStrategies.find(argument);
        JExpression convertedValue = strategy.convertArgument(codeModel, contextVar, environmentVar, materializedValue);

        argumentMap.put(argument, convertedValue);
      }
    }
    return argumentMap;
  }

  private JExpression materialize(JvmMethod overload, JVar contextVar, JVar argumentVar) {
    // this is a little tricky.
    // We need to decide when to materialize a deferred tasks. We only need to do this
    // when the method is actually going to access the content of the vector rather than just attributes
    // or length, etc.
    if(overload.isAnnotatedWith(Materialize.class)) {
      return contextVar.invoke("materialize").arg(argumentVar);
    } else {
      return argumentVar;
    }
  }

  private JArray argumentArrayLiteral(Class<?> type, List<JVar> elements) {
    JArray array = JExpr.newArray(codeModel.ref(type));
    for (JVar argumentVar : elements) {
      array.add(argumentVar);
    }
    return array;
  }

  private JExpression genericGroup(PrimitiveModel model) {
    return model.getOverloads().stream()
        .filter(o -> o.isGroupGeneric())
        .map(o -> o.getGenericGroup())
        .findFirst()
        .map(group -> JExpr.lit(group))
        .orElse(JExpr._null());
  }

  /**
   * Writes a method that dispatches a set of evaluated arguments to a Java method that includes
   * a {@link org.renjin.invoke.annotations.ArgumentList} argument.
   *
   */
  private void writeVarArgDispatcher(PrimitiveModel primitive) {
    if(primitive.getOverloads().size() != 1) {
      throw new IllegalStateException(primitive.getName());
    }
    JvmMethod overload = primitive.getOverloads().get(0);

    JMethod method = builtinClass.method(JMod.PUBLIC | JMod.STATIC, SEXP.class, WrapperGenerator2.toJavaName("", primitive.getName()));
    method._throws(codeModel.ref(Exception.class));

    JVar contextVar = method.param(codeModel.ref(Context.class), "context");
    JVar rhoVar = method.param(codeModel.ref(Environment.class), "rho");
    JVar argListVar = method.param(codeModel.ref(ArgList.class), "args");
    JVar callVar = method.param(codeModel.ref(FunctionCall.class), "call");

    JVar nargs = method.body().decl(codeModel.INT, "nargs", argListVar.invoke("size"));

    // Check for S3 dispatch
    if(overload.isGeneric()) {
      JExpression anyArguments = nargs.gt(lit(0));
      JExpression object = argListVar.ref("values").component(lit(0));
      JInvocation isObject = object.invoke("isObject");

      JBlock ifObject = method.body()._if(anyArguments.cand(isObject))._then();
      JVar genericResult = ifObject.decl(codeModel.ref(SEXP.class), "generic",
          codeModel.ref(S3.class).staticInvoke("tryDispatchFromPrimitive")
              .arg(contextVar)
              .arg(rhoVar)
              .arg(callVar)
              .arg(primitive.getName())
              .arg(genericGroup(primitive))
              .arg(argListVar.ref("names"))
              .arg(argListVar.ref("values")));
      ifObject._if(genericResult.ne(JExpr._null()))
          ._then()
          ._return(genericResult);
    }

    // First find all the positional arguments that proceed the argument list
    int positionalArgumentCount = 0;
    List<JVar> positionalArguments = new ArrayList<>();
    for (JvmMethod.Argument argument : overload.getAllArguments()) {
      if(argument.isVarArg()) {
        break;
      } else if(!argument.isContextual()) {
        int argIndex = positionalArgumentCount;
        JType argType = codeModel._ref(argument.getClazz());
        ArgConverterStrategy strategy = ArgConverterStrategies.find(argument);
        JExpression argumentValue = argListVar.ref("values").component(lit(argIndex));
        JExpression convertedValue = strategy.convertArgument(codeModel, contextVar, rhoVar, argumentValue);

        JVar var = method.body().decl(argType, "pos" + argIndex, convertedValue);

        positionalArguments.add(var);
        positionalArgumentCount++;
      }
    }

    // Identify any named flags
    Map<String, JVar> flagMap = new HashMap<>();
    for (JvmMethod.Argument argument : overload.getAllArguments()) {
      if(argument.isNamedFlag()) {
        JType varType = codeModel._ref(argument.getClazz());
        String varName = WrapperGenerator2.toJavaName("", argument.getName());
        JExpression defaultValue = defaultValue(argument);

        flagMap.put(argument.getName(), method.body().decl(varType, varName, defaultValue));
      }
    }

    // Define the ListBuilder for the var arg list
    JClass builderClass = codeModel.ref(ListVector.NamedBuilder.class);
    JVar varArgBuilder = method.body().decl(builderClass, "varArgs", _new(builderClass));

    // Loop through the arguments to find the var args and the named flags
    JForLoop loop = method.body()._for();
    JVar i = loop.init(codeModel.INT, "i", JExpr.lit(positionalArgumentCount));
    loop.test(i.lt(nargs));
    loop.update(i.incr());

    // For each argument, check whether it matches
    // one of the named flags.

    JConditional conditional = null;
    for (String flag : flagMap.keySet()) {

      JExpression matches = JExpr.lit(flag).invoke("equals").arg(argListVar.ref("names").component(i));

      if (conditional == null) {
        conditional = loop.body()._if(matches);
      } else {
        conditional = conditional._elseif(matches);
      }

      JVar flagVar = flagMap.get(flag);
      conditional._then().assign(flagVar, convertFlag(flagVar, argListVar.ref("values").component(i)));
    }

    // If doesn't match any of the named flags, add it to the
    // var args builder.

    JBlock defaultBranch;
    if(conditional == null) {
      defaultBranch = loop.body();
    } else {
      defaultBranch = conditional._else();
    }

    JVar argName = defaultBranch.decl(codeModel.ref(String.class), "name", argListVar.ref("names").component(i));
    JExpression argValue = argListVar.ref("values").component(i);
    JConditional named = defaultBranch._if(argName.eq(JExpr._null()));
    named._then().invoke(varArgBuilder, "add").arg(argValue);
    named._else().invoke(varArgBuilder, "add").arg(argName).arg(argValue);

    // Finally invoke the whole mess
    JInvocation invocation = codeModel.ref(overload.getDeclaringClass())
        .staticInvoke(overload.getName());

    int argumentIndex = 0;
    for (JvmMethod.Argument argument : overload.getAllArguments()) {
      if(argument.isContextual()) {
        if(argument.getClazz().equals(Context.class)) {
          invocation.arg(contextVar);
        } else if(argument.getClazz().equals(Environment.class)) {
          invocation.arg(rhoVar);
        } else {
          throw new UnsupportedOperationException("@Context " + argument.getClazz());
        }
      } else if(argument.isVarArg()) {
        invocation.arg(varArgBuilder.invoke("build"));

      } else if(argument.isNamedFlag()) {
        invocation.arg(flagMap.get(argument.getName()));
      } else {
        invocation.arg(positionalArguments.get(argumentIndex++));
      }
    }
    if(overload.getReturnType().equals(void.class)) {
      method.body().add(invocation);
      method.body()._return(codeModel.ref(Null.class).staticRef("INSTANCE"));
    } else {
      method.body()._return(convertResultToSEXP(invocation, overload.getReturnType()));
    }
  }

  private JExpression convertArgument(JvmMethod.Argument argument, JExpression value) {

    if(argument.isRecycle()) {

    }


    Class clazz = argument.getClazz();
    if(clazz.equals(boolean.class)) {
      return convertArgument("convertToBooleanPrimitive", value);
    } else if(clazz.equals(int.class)) {
      return convertArgument("convertToInteger", value);
    } else if(clazz.equals(double.class)) {
      return convertArgument("convertToDouble", value);
    } else if(clazz.equals(String.class)) {
      return convertArgument("convertToString", value);
    } else if(clazz.equals(Logical.class)) {
      return convertArgument("convertToLogical", value);
    } else if(SEXP.class.isAssignableFrom(clazz)) {
      return JExpr.cast(codeModel.ref(clazz), value);
    } else {
      throw new UnsupportedOperationException("argument type: " + clazz);
    }
  }

  private JExpression convertArgument(String method, JExpression value) {
    return wrapperRuntime.staticInvoke(method).arg(value);
  }

  private JExpression convertResultToSEXP(JInvocation invocation, Class returnType) {
    if(SEXP.class.isAssignableFrom(returnType)) {
      return invocation;
    }
    if(returnType.equals(Logical.class) || returnType.equals(boolean.class)) {
      return codeModel.ref(LogicalVector.class).staticInvoke("valueOf").arg(invocation);
    }
    if(returnType.equals(int.class)) {
      return codeModel.ref(IntVector.class).staticInvoke("valueOf").arg(invocation);
    }
    if(returnType.equals(double.class)) {
      return codeModel.ref(DoubleVector.class).staticInvoke("valueOf").arg(invocation);
    }
    if(returnType.equals(String.class)) {
      return codeModel.ref(StringVector.class).staticInvoke("valueOf").arg(invocation);
    }
    throw new UnsupportedOperationException("return type: " + returnType);
  }

  private JExpression convertFlag(JVar flagVar, JExpression value) {
    if (flagVar.type().equals(codeModel.BOOLEAN)) {
      return codeModel.ref(Support.class).staticInvoke("toFlag").arg(value);

    } else if (flagVar.type().equals(codeModel.ref(String.class))) {
      return codeModel.ref(Support.class).staticInvoke("toStringFlag").arg(value);

    } else {
      return JExpr.cast(flagVar.type(), value);
    }
  }

  private JExpression defaultValue(JvmMethod.Argument argument) {
    if(argument.getClazz().equals(boolean.class)) {
      return JExpr.lit(argument.getDefaultValue());
    } else {
      return JExpr._null();
    }
  }


}
