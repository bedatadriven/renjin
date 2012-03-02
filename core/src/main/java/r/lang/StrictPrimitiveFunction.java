package r.lang;

/**
 * Interface to a {@link PrimitiveFunction} which is strict; that is 
 * all of its arguments will be evaluated.
 */
public interface StrictPrimitiveFunction extends Function {

  SEXP applyStrict(Context context, Environment rho, FunctionCall call, SEXP arguments[]);
  
}
