package r.benchmarks;

import r.base.Ops;
import r.base.Sequences;
import r.base.Subscript;
import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.Environment;
import r.lang.ListVector;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.Vector;

/**
 * hand-translated class to establish lower baseline
 */
public class MeanOnline {
	
	private static final Symbol X = Symbol.get("x");
	private static final Symbol XBAR = Symbol.get("xbar");
	private static final Symbol N = Symbol.get("n");
	
	private static final DoubleVector CONSTANT_1 = new DoubleVector(1);

//  # original R code
//	mean.online <- function(x) {
//	    xbar <- x[1]
//
//	    for(n in seq(from = 2, to = length(x))) {
//	        xbar <- ((n - 1) * xbar + x[n]) / n
//	    }
//
//	    xbar
//	}

	
	
	public static SEXP meanOnline(Context context, Environment rho, SEXP x) {

		rho.setVariable(X, x);
	
		//####   xbar <- x[1]
		
		// t0 := x[1]
		SEXP t0 = Subscript.getSubset(x, new ListVector(CONSTANT_1), false);
		
		rho.setVariable(XBAR, t0);
				
		// for(n in 2:length(x))) {
		// 
		SEXP t01 = new DoubleVector(2);
		SEXP t02 = r.base.primitives.R$primitive$length.doApply(context, rho, x).getExpression();
		SEXP forTarget0 = Sequences.colon(t01, t02);
		
		for(int n_i=0;n_i!=forTarget0.length();++n_i) {
			rho.setVariable(N, forTarget0.getElementAsSEXP(n_i));
		
	        ////// xbar <- ((n - 1) * xbar + x[n]) / n
			
			// n - 1
			SEXP t1 = r.base.primitives.R$primitive$_$45$_.doApply(context, rho,
					rho.getVariable(N), CONSTANT_1).getExpression(); 
			
			// (n - 1) * x[n - 1] 
			SEXP t3 = r.base.primitives.R$primitive$_$42$_.doApply(context, rho,
					t1, rho.getVariable(XBAR)).getExpression(); 

			// x[n] 
			SEXP t4 = Subscript.getSubset(x, new ListVector(rho.getVariable(N)), false);
			
			
			// ((n - 1) * x[n - 1] + x[n])
			SEXP t5 = r.base.primitives.R$primitive$_$43$_.doApply(context, rho,
					t3, t4).getExpression(); 
			
			// ((n - 1) * x[n - 1] + x[n]) / n
			SEXP t6 = r.base.primitives.R$primitive$_$47$_.doApply(context, rho,
					t5, rho.getVariable(N)).getExpression(); 
			
			
			rho.setVariable(XBAR, t6);
			
	    }

	    return rho.getVariable(XBAR);
	}
	
	/// same as above, but detect which variables do not escape the closure and 
	/// and treat them as local variables (gains about ~20% reduction in runtime)
	public static SEXP meanOnline_varsInlind(Context context, Environment rho, SEXP x) {

	
		//####   xbar <- x[1]
		
		// t0 := x[1]
		SEXP t0 = Subscript.getSubset(x, new ListVector(CONSTANT_1), false);
		
		SEXP xbar = t0;
		
		// for(n in 2:length(x))) {
		// 
		SEXP t01 = new DoubleVector(2);
		SEXP t02 = r.base.primitives.R$primitive$length.doApply(context, rho, x).getExpression();
		SEXP forTarget0 = Sequences.colon(t01, t02);
		
		for(int n_i=0;n_i!=forTarget0.length();++n_i) {
			SEXP n = forTarget0.getElementAsSEXP(n_i);
					
	        ////// xbar <- ((n - 1) * xbar + x[n]) / n
			
			// n - 1
			SEXP t1 = r.base.primitives.R$primitive$_$45$_.doApply(context, rho,
					n, CONSTANT_1).getExpression(); 
			
			// (n - 1) * x[n - 1] 
			SEXP t3 = r.base.primitives.R$primitive$_$42$_.doApply(context, rho,
					t1, xbar).getExpression(); 

			// x[n] 
			SEXP t4 = Subscript.getSubset(x, new ListVector(n), false);
			
			
			// ((n - 1) * x[n - 1] + x[n])
			SEXP t5 = r.base.primitives.R$primitive$_$43$_.doApply(context, rho,
					t3, t4).getExpression(); 
			
			// ((n - 1) * x[n - 1] + x[n]) / n
			SEXP t6 = r.base.primitives.R$primitive$_$47$_.doApply(context, rho,
					t5, n).getExpression(); 
			
			
			xbar = t6;
	    }

	    return xbar;
	}
	
	/// finally, narrow the types based on information we get from the primitive method signatures
	/// (we are cheating a bit because we are assuming that x is a double vector, while we have no 
	/// gaurantees, and the R code would work on a list just well, or even an object that provides
	/// an S3 overload of the `[` function. 
	/// One approach might be to generate several versions of the same closure: one very fast one 
	/// for atomic vectors with no class, and then a slow one that makes no assumptions, and choose
	/// between them at runtime. 
	public static SEXP meanOnline_typesNarrowed(Context context, Environment rho, DoubleVector x) {

	
		//####   xbar <- x[1]
		
		// t0 := x[1]
		// the compiler should probably have special optimizations for key functions like subscripts
		double t0 = x.getElementAsDouble(0);
		double xbar = t0;
				
		// for(n in 2:length(x))) {
		// these types could also be narrowed if we write a more specific overload
		// of Sequences.colon
		SEXP t01 = new DoubleVector(2);
		SEXP t02 = r.base.primitives.R$primitive$length.doApply(context, rho, x).getExpression();
		Vector forTarget0 = Sequences.colon(t01, t02);
		
		for(int n_i=0;n_i!=forTarget0.length();++n_i) {
			double n = forTarget0.getElementAsDouble(n_i);
		
	        ////// xbar <- ((n - 1) * xbar + x[n]) / n
			
			// n - 1
			double t1 = n- 1;
			
		
			// (n - 1) * xbar 
			double t3 = t1 * xbar; 

			// x[n] 
			double t4 = x.getElementAsDouble(((int)n)-1); 
					
			// ((n - 1) * xbar + x[n])
			double t5 = t3 + t4;
			
			// ((n - 1) * x[n - 1] + x[n]) / n
			double t6 = t5 / n;
			
			xbar = t6;
	    }

	    return new DoubleVector(xbar);
	}

}
