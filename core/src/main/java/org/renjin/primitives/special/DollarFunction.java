package org.renjin.primitives.special;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.S3;
import org.renjin.sexp.*;

/**
 * {@code $} operator
 * 
 * <p>Requires special handling of the symbol argument and generics</p>
 */
public class DollarFunction extends SpecialFunction {
  
  public DollarFunction() {
    super("$");
  }

  @Override
  public SEXP apply(Context context, Environment rho, FunctionCall call, PairList args) {

   
    // Even though this function is generic, it MUST be called with exactly two arguments
    if(args.length() != 2) {
      throw new EvalException(String.format("%d argument(s) passed to '$' which requires 2", args.length()));
    }

    SEXP object = context.evaluate(args.getElementAsSEXP(0), rho);
    StringVector nameArgument = evaluateName(args.getElementAsSEXP(1));

    // For possible generic dispatch, repackage the name argument as character vector rather than
    // symbol
    PairList.Node repackagedArgs = new PairList.Node(object, new PairList.Node(nameArgument, Null.INSTANCE));
    
    SEXP genericResult = S3.tryDispatchFromPrimitive(context, rho, call, "$", object, repackagedArgs);
    if (genericResult!= null) {
      return genericResult;
    }
    
    // If no generic function, extract the element
    String name = nameArgument.getElementAsString(0);
    if(object instanceof PairList) {
      return fromPairList((PairList) object, name);
    
    } else if(object instanceof Environment) {
      return fromEnvironment((Environment)object, name);
      
    } else if(object instanceof ListVector) {
      return fromList((ListVector) object, name);

    } else if(object instanceof ExternalPtr) {
      return fromExternalPtr((ExternalPtr<?>) object, name);
    
    } else if(object instanceof AtomicVector) {
      throw new EvalException("$ operator is invalid for atomic vectors");
    
    } else {
      throw new EvalException("object of type '%s' is not subsettable", object.getTypeName());
    }
  }

  static StringVector evaluateName(SEXP name) {
    if(name instanceof Symbol) {
      return new StringArrayVector(((Symbol) name).getPrintName());
    } else if(name instanceof StringVector) {
      return (StringVector) name;
    } else {
      throw new EvalException("invalid subscript type '%s'", name.getTypeName());
    }
  }
  
  public static SEXP fromPairList(PairList list, String name) {
    SEXP match = null;
    int matchCount = 0;

    for (PairList.Node node : list.nodes()) {
      if (node.hasTag()) {
        if (node.getTag().getPrintName().startsWith(name)) {
          match = node.getValue();
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }
  
  public static SEXP fromEnvironment(Environment env, String name) {
    SEXP value = env.getVariable(name);
    if (value == Symbol.UNBOUND_VALUE) {
      return Null.INSTANCE;
    }
    return value;
  }

  public static SEXP fromExternalPtr(ExternalPtr<?> externalPtr, String name) {
    return externalPtr.getMember(Symbol.get(name));
  }

  public static SEXP fromList(ListVector list, String name) {
    SEXP match = null;
    int matchCount = 0;

    for (int i = 0; i != list.length(); ++i) {
      String elementName = list.getName(i);
      if (!StringVector.isNA(elementName)) {
        if (elementName.equals(name)) {
          return list.getElementAsSEXP(i);
        } else if (elementName.startsWith(name)) {
          match = list.get(i);
          matchCount++;
        }
      }
    }
    return matchCount == 1 ? match : Null.INSTANCE;
  }
  
}
