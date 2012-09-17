package org.renjin.primitives.annotations.processor;

import com.google.common.collect.Lists;
import com.sun.codemodel.*;
import org.renjin.primitives.annotations.AllowNA;
import org.renjin.primitives.annotations.PreserveAttributeStyle;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;

import java.util.List;

import static com.sun.codemodel.JExpr.lit;

public class DeferredVectorBuilder {

  public static final int LENGTH_THRESHOLD = 100;

  private JCodeModel codeModel;
  private PrimitiveModel primitive;
  private JvmMethod overload;
  private int arity;
  private JDefinedClass vectorClass;
  private VectorType type;

  private List<DeferredArgument> arguments = Lists.newArrayList();
  private JFieldVar lengthField;


  public DeferredVectorBuilder(JCodeModel codeModel, PrimitiveModel primitive, JvmMethod overload) {
    this.codeModel = codeModel;
    this.primitive = primitive;
    this.overload = overload;
    this.arity = overload.getPositionalFormals().size();

    if(overload.getReturnType().equals(double.class)) {
      type = VectorType.DOUBLE;
    } else if(overload.getReturnType().equals(boolean.class)) {
      type = VectorType.LOGICAL;
    } else if(overload.getReturnType().equals(Logical.class)) {
      type = VectorType.LOGICAL;
    } else if(overload.getReturnType().equals(int.class)) {
      type = VectorType.INTEGER;
    } else {
      throw new UnsupportedOperationException(overload.getReturnType().toString());
    }
  }

  public void buildClass()  {
    try {
      vectorClass = codeModel._class(  WrapperGenerator2.toFullJavaName(primitive.getName()) + "$deferred_" + typeSuffix() );
    } catch (JClassAlreadyExistsException e) {
      throw new RuntimeException(e);
    }
    vectorClass._extends(type.baseClass);
    vectorClass._implements(DeferredComputation.class);

    for(int i=0;i!=arity;++i) {
      arguments.add(new DeferredArgument(overload.getPositionalFormals().get(i), i));

    }
    this.lengthField = vectorClass.field(JMod.PRIVATE, codeModel._ref(int.class), "length");
    writeConstructor();
    implementAccessor();
    implementLength();
    implementAttributeSetter();
    implementGetOperands();
    implementGetComputationName();
    implementStaticApply();

    if(overload.acceptsNA() && overload.getReturnType().equals(boolean.class)) {
      overrideIsNaWithConstantValue();
    }
  }


  private void implementGetOperands() {
    JMethod method = vectorClass.method(JMod.PUBLIC, Vector[].class, "getOperands");
    JArray array = JExpr.newArray(codeModel.ref(Vector.class));
    for(DeferredArgument arg : arguments) {
      array.add(arg.valueField);
    }
    method.body()._return(array);
  }


  private void implementGetComputationName() {
    JMethod method = vectorClass.method(JMod.PUBLIC, String.class, "getComputationName");
    method.body()._return(lit(primitive.getName()));
  }

  private String typeSuffix() {
    StringBuilder suffix = new StringBuilder();
    for(JvmMethod.Argument formal : overload.getPositionalFormals()) {
      suffix.append(abbrev(formal.getClazz()));
    }
    return suffix.toString();

  }

  private String abbrev(Class clazz) {
    if(clazz.equals(double.class)) {
      return "d";
    } else if(clazz.equals(boolean.class)) {
      return "b";
    } else if(clazz.equals(String.class)) {
      return "s";
    } else if(clazz.equals(int.class)) {
      return "i";
    } else {
      throw new UnsupportedOperationException(clazz.toString());
    }
  }

  public void maybeReturn(JBlock parent, JExpression cycleCount, List<JExpression> arguments) {

    JExpression condition = cycleCount.gt(lit(LENGTH_THRESHOLD));
    for(JExpression arg : arguments) {
      condition = condition.cor(arg._instanceof(codeModel.ref(DeferredComputation.class)));
    }
    JBlock ifBig = parent._if(condition)._then();
    JExpression attributes = copyAttributes(arguments);

    JInvocation newInvocation = JExpr._new(vectorClass);
    for(JExpression arg : arguments) {
      newInvocation.arg(arg);
    }
    newInvocation.arg(attributes);

    ifBig._return(newInvocation);
  }

  private JExpression copyAttributes(List<JExpression> arguments) {
    if(overload.getPreserveAttributesStyle() == PreserveAttributeStyle.NONE) {
      return codeModel.ref(AttributeMap.class).staticRef("EMPTY");
    } else {
      if(arity == 1) {
        return copyAttributes(arguments.get(0));
      } else if(arity == 2) {
        return copyAttributes(arguments.get(0), arguments.get(1));
      } else {
        throw new UnsupportedOperationException("arity = " + arity);
      }
    }
  }

  private JExpression copyAttributes(JExpression arg0, JExpression arg1)  {
    String combineMethod;
    switch(overload.getPreserveAttributesStyle()) {
    case ALL:
      combineMethod = "combineAttributes";
      break;
    case SPECIAL:
      combineMethod = "combineStructuralAttributes";
      break;
    default:
      throw new UnsupportedOperationException();
    }

    return codeModel.ref(AttributeMap.class).staticInvoke(combineMethod)
            .arg(arg0)
            .arg(arg1);
  }

  private JExpression copyAttributes(JExpression arg) {
    if(overload.getPreserveAttributesStyle() == PreserveAttributeStyle.ALL) {
      return arg.invoke("getAttributes");
    } else if(overload.getPreserveAttributesStyle() == PreserveAttributeStyle.SPECIAL) {
      return arg.invoke("getAttributes").invoke("copyStructural");
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private void writeConstructor() {
//    public DoubleBinaryFnVector(Vector arg0, Vector arg1, AttributeMap attributes) {
//      super(attributes);
//      this.x = x;
//      this.y = y;
//      this.fn = fn;
//      this.xLength = x.length();
//      this.yLength = y.length();
//      this.length = Math.max(xLength, yLength);
//    }
    JMethod ctor = vectorClass.constructor(JMod.PUBLIC);
    List<JVar> argParams = Lists.newArrayList();
    for(int i=0;i!=arity;++i) {
      argParams.add(ctor.param(Vector.class, "arg" + i));
    }
    ctor.param(AttributeMap.class, "attributes");
    ctor.body().directStatement("super(attributes);");
    ctor.body().assign(lengthField, lit(0));
    for(int i=0;i!=arity;++i) {
      ctor.body().assign(JExpr._this().ref(arg(i).valueField), argParams.get(i));
      ctor.body().assign(arg(i).lengthField, arg(i).valueField.invoke("length"));
    }
    if(arity == 1) {
      ctor.body().assign(lengthField, arg(0).lengthField);
    } else if(arity == 2) {
      ctor.body().assign(lengthField, codeModel.ref(Math.class).staticInvoke("max")
              .arg(arg(0).lengthField)
              .arg(arg(1).lengthField));

    }
  }

  private DeferredArgument arg(int i) {
    return arguments.get(i);
  }

  private void implementLength() {
    JMethod method = vectorClass.method(JMod.PUBLIC, int.class, "length");
    method.body()._return(lengthField);
  }


  private void implementStaticApply() {
    JMethod method = vectorClass.method(JMod.PUBLIC | JMod.STATIC, type.accessorType, "compute");
    List<JExpression> params = Lists.newArrayList();
    for(DeferredArgument argument : arguments) {
      JVar param = method.param(argument.accessorType(), "p" + argument.index);
      params.add(argument.convert(param));
    }
    returnValue(method.body(), buildInvocation(params));
  }

  private void implementAccessor() {
    JMethod method = vectorClass.method(JMod.PUBLIC, type.accessorType, type.accessorName);
    JVar index = method.param(int.class, "index");

    // extract the arguments to the function from the given vectors
    List<JExpression> argValues = Lists.newArrayList();
    for(DeferredArgument arg : arguments) {

      JExpression elementIndex;
      if(arity == 1) {
        elementIndex = index;
      } else {
        // avoid using modulus if we can
        JVar indexVar = method.body().decl(codeModel._ref(int.class), "i" + arg.index);
        JConditional ifLessThan = method.body()._if(index.lt(arg.lengthField));
        ifLessThan._then().assign(indexVar, index);
        ifLessThan._else().assign(indexVar, index.mod(arg.lengthField));
        elementIndex = indexVar;
      }

      JVar argValue = method.body().decl(arg.accessorType(), "arg" + arg.index + "_i", arg.invokeAccessor(elementIndex));
      argValues.add(arg.convert(argValue));

      if(!overload.isAnnotatedWith(AllowNA.class)) {
        method.body()._if(arg.isNA(argValue))._then()._return(na());
      }
    }

    // invoke the underlying function
    returnValue(method.body(), buildInvocation(argValues));
  }

  private JInvocation buildInvocation(List<JExpression> argValues) {
    JInvocation invocation = codeModel
            .ref(overload.getDeclaringClass())
            .staticInvoke(overload.getName());
    for(JExpression argValue : argValues) {
      invocation.arg(argValue);
    }
    return invocation;
  }

  private JExpression na() {
    switch (type) {
      case DOUBLE:
        return codeModel.ref(DoubleVector.class).staticRef("NA");

      case LOGICAL:
      case INTEGER:
        return codeModel.ref(IntVector.class).staticRef("NA");
    }
    throw new UnsupportedOperationException(type.toString());
  }

  private void returnValue(JBlock parent, JExpression retVal) {
    if(overload.getReturnType().equals(boolean.class)) {
      JConditional ifTrue = parent._if(retVal);
      ifTrue._then()._return(lit(1));
      ifTrue._else()._return(lit(0));
    } else if(overload.getReturnType().equals(Logical.class)) {
      parent._return(retVal.invoke("getInternalValue"));
    } else {
      parent._return(retVal);
    }
  }

  public void overrideIsNaWithConstantValue() {
    JMethod method = vectorClass.method(JMod.PUBLIC, boolean.class, "isElementNA");
    method.param(int.class, "index");
    method.body()._return(JExpr.FALSE);
  }

  private void implementAttributeSetter() {
//    @Override
//    protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
//      return new DoubleBinaryFnVector(fn, x, y, attributes);
//    }

    JMethod method = vectorClass.method(JMod.PUBLIC, SEXP.class, "cloneWithNewAttributes");
    JVar attributes = method.param(AttributeMap.class, "attributes");

    JInvocation newInvocation = JExpr._new(vectorClass);
    for(DeferredArgument arg : arguments) {
      newInvocation.arg(arg.valueField);
    }
    newInvocation.arg(attributes);
    method.body()._return(newInvocation);
  }

  private class DeferredArgument {
    private JvmMethod.Argument model;
    private int index;
    private JFieldVar valueField;
    private JFieldVar lengthField;
    private ArgumentType type;

    private DeferredArgument(JvmMethod.Argument model, int index) {
      this.model = model;
      this.index = index;
      this.valueField = vectorClass.field(JMod.PRIVATE | JMod.FINAL, Vector.class, "arg" + index);
      this.lengthField = vectorClass.field(JMod.PRIVATE | JMod.FINAL, int.class, "argLength" + index);

      if(model.getClazz().equals(double.class)) {
        this.type = ArgumentType.DOUBLE;
      } else if(model.getClazz().equals(boolean.class)) {
        this.type = ArgumentType.BOOLEAN;
      } else if(model.getClazz().equals(int.class)) {
        this.type = ArgumentType.INTEGER;
      } else if(model.getClazz().equals(String.class)) {
        this.type = ArgumentType.STRING;
      } else {
        throw new UnsupportedOperationException(model.getClazz().toString());
      }
    }

    public JType type() {
      return codeModel._ref(model.getClazz());
    }

    public JExpression invokeAccessor(JExpression elementIndex) {
      return valueField.invoke(type.accessorName).arg(elementIndex);
    }

    public JType accessorType() {
      return codeModel._ref(type.accessorType());
    }

    public JExpression isNA(JExpression expr) {
      return type.isNa(codeModel, expr);
    }

    public JExpression convert(JExpression argValue) {
      return type.convertToArg(argValue);
    }
  }

  private enum VectorType {

    DOUBLE(DoubleVector.class, "getElementAsDouble", double.class),
    LOGICAL(LogicalVector.class, "getElementAsRawLogical", int.class),
    INTEGER(IntVector.class, "getElementAsInt", int.class);

    private Class baseClass;
    private String accessorName;
    private Class accessorType;

    private VectorType(Class baseClass, String accessorName, Class accessorType) {
      this.baseClass = baseClass;
      this.accessorName = accessorName;
      this.accessorType = accessorType;
    }
  }

  private enum ArgumentType {

    DOUBLE(double.class, "getElementAsDouble") {
      @Override
      public JExpression isNa(JCodeModel codeModel, JExpression expr) {
        return codeModel.ref(DoubleVector.class).staticInvoke("isNA").arg(expr);
      }
    },
    INTEGER(int.class, "getElementAsInt") {
      @Override
      public JExpression isNa(JCodeModel codeModel, JExpression expr) {
        return codeModel.ref(IntVector.class).staticInvoke("isNA").arg(expr);
      }
    },
    BOOLEAN(boolean.class, "getElementAsRawLogical") {
      @Override
      public JExpression convertToArg(JExpression expr) {
        return expr.ne(lit(0));
      }

      @Override
      public Class accessorType() {
        return int.class;
      }

      @Override
      public JExpression isNa(JCodeModel codeModel, JExpression expr) {
        return codeModel.ref(IntVector.class).staticInvoke("isNA").arg(expr);
      }
    },
    STRING(String.class, "getElementAsString") {
      @Override
      public JExpression isNa(JCodeModel codeModel, JExpression expr) {
        return codeModel.ref(StringVector.class).staticInvoke("isNA").arg(expr);
      }
    };

    private Class clazz;
    private String accessorName;


    private ArgumentType(Class clazz, String accessorName) {
      this.clazz = clazz;
      this.accessorName = accessorName;
    }

    public JExpression convertToArg(JExpression expr) {
      return expr;
    }

    public Class accessorType() {
      return clazz;
    }

    public abstract JExpression isNa(JCodeModel codeModel, JExpression expr);
  }


}
