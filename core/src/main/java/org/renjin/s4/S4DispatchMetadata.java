package org.renjin.s4;

import org.renjin.eval.Context;
import org.renjin.eval.DispatchTable;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.*;

import java.util.*;

public class S4DispatchMetadata implements DispatchTable {

  public static final String R_methods = "methods";

  public static final String R_package = "package";

  public static final Symbol DOT_DEFINED = Symbol.get(".defined");

  public static final Symbol DOT_TARGET = Symbol.get(".target");

  public static final Symbol DOT_GENERIC = Symbol.get(".Generic");

  public static final Symbol DOT_METHOD = Symbol.get(".Method");

  public static final Symbol DOT_METHODS = Symbol.get(".Methods");


  private final String opName;
  private final RankedMethod method;
  private final Signature signature;
  private final List<String> argumentClasses;
  private final List<String> argumentPackages;

  public S4DispatchMetadata(Context context, String opName, RankedMethod method, Signature signature) {
    this.opName = opName;
    this.method = method;
    this.signature = signature;

    argumentClasses = method.getMethod().getSignature().getClasses();
    argumentPackages = new ArrayList<>();
    for (String argumentClass : argumentClasses) {
      argumentPackages.add(getClassPackage(context, argumentClass));
    }
  }

  @Override
  public SEXP get(Symbol symbol) {
    if(symbol == DOT_DEFINED) {
      return getDotDefined();
    }
    if(symbol == DOT_TARGET) {
      return getDotTarget();
    }
    if(symbol == DOT_GENERIC) {
      return method.getMethod().getGeneric().asSEXP();
    }
    if(symbol == DOT_METHOD) {
      return method.getMethodDefinition();
    }

    if(symbol == DOT_METHODS) {
      return getMethods();
    }

    //
//    metadata.put(R_dot_defined, buildDotDefined(context, method));
//    metadata.put(R_dot_target, buildDotTarget(method, signature));
//    metadata.put(DOT_GENERIC, method.getMethod().getGeneric().asSEXP());
//    metadata.put(R_dot_Method, method.getMethodDefinition());
//    if(Primitives.isBuiltin(opName)) {
//      metadata.put(s_dot_Methods, Symbol.get(".Primitive(\"" + opName + "\")"));
//    } else {
//      metadata.put(s_dot_Methods, Null.INSTANCE);
//    }
//    return metadata;



    throw new UnsupportedOperationException("TODO");
  }

  private SEXP getMethods() {
    if(Primitives.isBuiltin(opName)) {
      return Symbol.get(".Primitive(\"" + opName + "\")");
    } else {
      return Null.INSTANCE;
    }
  }


  @Override
  public Collection<Symbol> getEnvironmentSymbols() {
    return Arrays.asList(DOT_DEFINED, DOT_TARGET, DOT_GENERIC, DOT_METHOD, DOT_METHODS);
  }

  private SEXP getDotDefined() {

    return new StringVector.Builder()
        .addAll(argumentClasses)
        .setAttribute("names", method.getMethod().getFormalNames())
        .setAttribute(R_package, new StringArrayVector(argumentPackages))
        .setAttribute("class", signatureClass())
        .build();
  }

  private SEXP getDotTarget() {

    List<String> argumentClasses = signature.getClasses();
    List<String> argumentPackages = new ArrayList<>(Collections.nCopies(argumentClasses.size(), R_methods));

    return new StringVector.Builder()
        .addAll(argumentClasses)
        .setAttribute("names", method.getMethod().getFormalNames())
        .setAttribute(R_package, new StringArrayVector(argumentPackages))
        .setAttribute("class", signatureClass())
        .build();
  }

  private static SEXP signatureClass() {
    return StringVector.valueOf("signature")
        .setAttribute(R_package, StringVector.valueOf(R_methods));
  }


  private static String getClassPackage(Context context, String objClass) {
    if("ANY".equals(objClass) || "signature".equals(objClass)) {
      return R_methods;
    }

    S4Cache s4Cache = context.getSession().getS4Cache();
    S4Class s4Class = s4Cache.getS4ClassCache().lookupClass(context, objClass);
    SEXP classDef = s4Class.getDefinition();

    StringArrayVector packageSlot = (StringArrayVector) classDef.getAttribute(S4.PACKAGE);
    return packageSlot.getElementAsString(0);
  }


}
