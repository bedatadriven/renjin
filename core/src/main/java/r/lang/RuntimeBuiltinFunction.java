package r.lang;

import java.util.List;

import r.base.BaseFrame;
import r.jvmi.binding.JvmMethod;
import r.jvmi.binding.RuntimeInvoker;
import r.lang.exception.EvalException;

public class RuntimeBuiltinFunction extends BuiltinFunction {

  protected List<JvmMethod> methodOverloads;
  private String name;
  private Class methodClass;
  private String methodName;

  public RuntimeBuiltinFunction(BaseFrame.Entry functionEntry) {
    super(functionEntry.name);
    name = functionEntry.name;
    methodClass = functionEntry.functionClass;
    methodName = functionEntry.methodName;
  }

  public RuntimeBuiltinFunction(String name, Class methodClass, String methodName) {
    super(name);
    this.name = name;
    this.methodClass = methodClass;
    this.methodName = methodName;
  }

  public RuntimeBuiltinFunction(String name, Class methodClass) {
    super(name);
    this.name = name;
    this.methodClass = methodClass;
  }

  @Override
  public EvalResult apply(Context context, Environment rho, FunctionCall call, PairList arguments) {
    try {
      return RuntimeInvoker.INSTANCE.invoke(context, rho, name, call, getOverloads());
    } catch (EvalException e) {
      if(e.getContext() == null) {
        e.initContext(context);
      }
      throw e;
    }
  }

  protected List<JvmMethod> getOverloads() {
    List<JvmMethod> overloads = getMethodOverloads(methodClass, name, methodName);
    if(overloads.isEmpty()) {
      StringBuilder message = new StringBuilder();
      message.append("'")
             .append(name)
             .append("' is not yet implemented");
      if(methodClass != null) {
        message.append(" (")
             .append(methodClass.getName())
             .append(".")
             .append(methodName)
             .append(")");
      }

      throw new EvalException(message.toString());
    }
    return overloads;
  }

  protected List<JvmMethod> getMethodOverloads(Class clazz, String name, String alias) {
    if (methodOverloads == null) {
      methodOverloads = JvmMethod.findOverloads(clazz, name, alias);
    }
    return methodOverloads;
  }

}
