package r.benchmarks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.EvalResult;
import r.lang.FunctionCall;
import r.lang.SEXP;
import r.lang.Symbol;
import r.parser.RParser;

public class CompiledBenchmarks {
	
	public static void main(String[] args) throws IOException {
		
		Context context = Context.newTopLevelContext();
		context.init();

		InputStream in = Benchmarks.class.getResourceAsStream("mean-online.R");
	    RParser.parseSource(new InputStreamReader(in)).evalToExp(context, context.getEnvironment());

	    
		long start1 = System.nanoTime();
		   
		EvalResult result1 = FunctionCall.newCall(Symbol.get("mean.online"), Symbol.get("x"))
			.evaluate(context, context.getEnvironment());
		
		long runtime1 = System.nanoTime() - start1;
		
		System.out.println("mean.online(x) = " + result1.getExpression());
		System.out.println("interpretered R finished in " + formatNanos(runtime1));
		
		long start2 = System.nanoTime();
		
		SEXP result2 = MeanOnline.meanOnline_typesNarrowed(context, context.getEnvironment(), 
				(DoubleVector)context.getEnvironment().getVariable(Symbol.get("x")));
		
		long runtime2 = System.nanoTime() - start2;
		
		System.out.println("mean.online(x) = " + result2);
		System.out.println("compiled R finished in " + formatNanos(runtime2));

		System.out.println("speed up factor = " + (runtime1 / runtime2));
		
		
	}


	  private static String formatNanos(long nanos) {
	    return (nanos * 1e-9) + " s";
	  }
}
