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
  public String convert(Argument formal, String argumentExpression) {
    return "checkedSubClass(" + argumentExpression + ")";
  }

}
