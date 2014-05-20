package org.renjin.primitives.sequence;

import org.renjin.eval.Calls;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.codegen.ArgumentIterator;
import org.renjin.primitives.S3;
import org.renjin.primitives.vector.DeferredComputation;
import org.renjin.sexp.*;
import org.renjin.util.NamesBuilder;

public class RepFunction extends SpecialFunction {

  public RepFunction() {
    super("rep");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

    // rep is one of the very few primitives that uses argument matching
    // *ALMOST* like that employed for closures.
    //
    // the one gotcha is that generic dispatch is done on the FIRST argument,
    // even if 'x' is provided as named argument elsewhere

    // check for zero args -- the result should be null
    PairList arguments = call.getArguments();
    if(arguments == Null.INSTANCE) {
      context.setInvisibleFlag();
      return Null.INSTANCE;
    }

    // evaluate the first arg
    ArgumentIterator argIt = new ArgumentIterator(context, rho, arguments);
    PairList.Node firstArgNode = argIt.nextNode();
    SEXP firstArg = context.evaluate( firstArgNode.getValue(), rho);
    if(firstArg.isObject()) {
      SEXP result = S3.tryDispatchFromPrimitive(context, rho, call, "rep", firstArg, arguments);
      if(result != null) {
        return result;
      }
    }

    // create a new pair list of evaluated arguments
    PairList.Builder evaled = new PairList.Builder();
    evaled.add(firstArgNode.getRawTag(), firstArg);
    while(argIt.hasNext()) {
      PairList.Node node = argIt.nextNode();
      evaled.add(node.getRawTag(), context.evaluate( node.getValue(), rho));
    }

    // declare formals
    PairList.Builder formals = new PairList.Builder();
    formals.add("x", Symbol.MISSING_ARG);
    formals.add("times", Symbol.MISSING_ARG);
    formals.add("length.out", Symbol.MISSING_ARG);
    formals.add("each", Symbol.MISSING_ARG);

    PairList matched = Calls.matchArguments(formals.build(), evaled.build(), true);

    SEXP x = matched.findByTag(Symbol.get("x"));
    SEXP times = matched.findByTag(Symbol.get("times"));
    SEXP lengthOut = matched.findByTag(Symbol.get("length.out"));
    SEXP each = matched.findByTag(Symbol.get("each"));

    return rep(
        (Vector) x,
        times == Symbol.MISSING_ARG ? new IntArrayVector(1) : (Vector) times,
        lengthOut == Symbol.MISSING_ARG ? IntVector.NA : ((Vector) lengthOut).getElementAsInt(0),
        each == Symbol.MISSING_ARG ? IntVector.NA : ((Vector) each).getElementAsInt(0));
  }


  private Vector rep(Vector x, Vector times, int lengthOut, int each) {
    int resultLength;

    if(times.length() == 1) {
      resultLength = x.length() * times.getElementAsInt(0);
    } else {
      resultLength = 0;
      for(int i=0;i!=x.length();++i) {
        resultLength += times.getElementAsInt(i);
      }
    }
    if(!IntVector.isNA(each)) {
      resultLength = x.length() * each;
    } else {
      each = 1;
    }
    if(!IntVector.isNA(lengthOut)) {
      if(lengthOut < 0) {
        throw new EvalException("invalid 'length.out' argument");
      }
      resultLength = lengthOut;
    }

    if(times.length() > 1 && each > 1) {
      throw new EvalException("invalid 'times' argument");
    }

    /*
     * If there is no per-element times parameter,
     * and we have a large vector, then just return
     * a wrapper around this vector and avoid
     * allocating the extra memory.
     */
    if(x instanceof DoubleVector &&
        times.length() == 1 &&
        (x instanceof DeferredComputation || resultLength > RepDoubleVector.LENGTH_THRESHOLD)) {

      return new RepDoubleVector(x, resultLength, each);

    } else if(x instanceof IntVector &&
        times.length() == 1 &&
        (x instanceof DeferredComputation || resultLength > RepIntVector.LENGTH_THRESHOLD)) {

      return new RepIntVector(x, resultLength, each);
    }

    /**
     * Go ahead and allocate and fill the memory
     */
    Vector.Builder result = x.newBuilderWithInitialCapacity(resultLength);
    NamesBuilder names = NamesBuilder.withInitialCapacity(resultLength);
    int result_i = 0;

    if(times.length() == 1) {
      for(int i=0;i!=resultLength;++i) {
        int x_i = (i / each) % x.length();
        result.setFrom(result_i++, x, x_i);
        names.add(x.getName(x_i));
      }
    } else {
      for(int x_i=0;x_i!=x.length();++x_i) {
        for(int j=0;j<times.getElementAsInt(x_i);++j) {
          result.setFrom(result_i++, x, x_i);
          names.add(x.getName(x_i));
        }
      }
    }
    if(names.haveNames()) {
      result.setAttribute(Symbols.NAMES, names.build(resultLength));
    }

    return result.build();
  }

}
