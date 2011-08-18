package r.jvmi.wrapper.generator.args;

import r.jvmi.binding.JvmMethod.Argument;
import r.lang.SEXP;

/**
 * Handles checked narrowing of types to 
 * @author alex
 *
 */
public class SexpSubclass extends ArgConverterStrategy {

  @Override
  public boolean accept(Argument formal) {
    return SEXP.class.isAssignableFrom(formal.getClazz());
  }

  @Override
  public String conversionExpression(Argument formal, String argumentExpression) {
    return "checkedSubClass(" + argumentExpression + ")";
  }

  @Override
  public String conversionStatement(Argument formal, String tempLocal,
      String argumentExpression) {
    return "try { " + tempLocal + " = (" + formal.getClazz().getName() + ")(" + argumentExpression + ");" +
    		" } catch(ClassCastException cce) { throw new ArgumentException(); }";
  }

}
