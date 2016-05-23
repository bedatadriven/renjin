package org.renjin.primitives.combine;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.sexp.*;

/**
 * Special function to do cbind and use objectnames as columnnames
 */
public class ColumnBindFunction extends SpecialFunction {

  public RowBindFunction() {
    super("cbind");
  }
//  > a = data.frame(G=c(0,0),H=c(1,1))
//  > b = data.frame(I=c(2,2),J=c(3,3))
//  > cbind(a,b)
//    G H I J
//  1 0 1 2 3
//  2 0 1 2 3
//  > rbind(a,b)
//  Error in match.names(clabs, names(xi)) :
//  names do not match previous names
//  > rbind(t(a),t(b))
//    [,1] [,2]
//  G    0    0
//  H    1    1
//  I    2    2
//  J    3    3

  @Override
  public SEXP apply(@Current Context context, @Current Environment rho, FunctionCall call, PairList args) {

    int deparseLevel = ((Double) context.evaluate(args.getElementAsSEXP(0), rho).asReal()).intValue();
    StringVector nameArgument = evaluateName(args.getElementAsSEXP(1));

    return null;
  }
}
