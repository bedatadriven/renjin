/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.embed;

import org.renjin.eval.Session;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.*;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.REXPReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Lazily wraps pointers to SEXP in Renjin's SEXP classes
 */
public class Wrapper {

  private final Rengine engine = Rengine.getMainEngine();

  private Map<Long, SEXP> globals = new HashMap<>();

  private Map<Long, SEXP> cache = new HashMap<>();

  private final long globalEnv;
  private final long emptyEnv;
  private final long baseEnv;


  public Wrapper(Session session) {

    globalEnv = engine.rniSpecialObject(Rengine.SO_GlobalEnv);
    baseEnv = engine.rniSpecialObject(Rengine.SO_BaseEnv);
    emptyEnv = engine.rniSpecialObject(Rengine.SO_EmptyEnv);

    globals.put(engine.rniSpecialObject(Rengine.SO_UnboundValue), Symbol.UNBOUND_VALUE);
    globals.put(engine.rniSpecialObject(Rengine.SO_NilValue), Null.INSTANCE);

    globals.put(globalEnv, session.getGlobalEnvironment());
    globals.put(baseEnv, session.getBaseEnvironment());
    globals.put(emptyEnv, Environment.EMPTY);

    initPackageMappings(session);
    initNamespaceMappings(session);
  }

  public boolean isEmptyEnv(long envPtr) {
    return envPtr == emptyEnv;
  }

  private void initPackageMappings(Session session) {
    long emptyEnv = engine.rniSpecialObject(Rengine.SO_EmptyEnv);
    long hostEnv = engine.rniParentEnv(globalEnv);
    while(hostEnv != emptyEnv) {
      String hostEnvName = getHostEnvironmentName(hostEnv);
      if(hostEnvName != null && hostEnvName.startsWith("package:")) {
        Environment guestEnv = findGuestEnvironmentByName(session, hostEnvName);
        if(guestEnv != null) {
          globals.put(hostEnv, guestEnv);
        }
      }
      hostEnv = engine.rniParentEnv(hostEnv);
    }
  }

  private Environment findGuestEnvironmentByName(Session session, String name) {
    Environment env = session.getGlobalEnvironment().getParent();
    while(env != Environment.EMPTY) {
      if(name.equals(env.getName())) {
        return env;
      }
      env = env.getParent();
    }
    return null;
  }

  private String getHostEnvironmentName(long hostEnv) {
    long nameSexp = engine.rniGetAttr(hostEnv, "name");
    if(engine.rniExpType(nameSexp) == REXP.STRSXP) {
      return engine.rniGetString(nameSexp);
    } else {
      return null;
    }
  }

  private void initNamespaceMappings(Session session) {
    initNamespaceMapping(session, "base");
    initNamespaceMapping(session, "stats");
    initNamespaceMapping(session, "methods");
  }

  private void initNamespaceMapping(Session session, String packageName) {
    long hostNamespacePtr = rniEval(String.format("getNamespace(\"%s\")", packageName));
    Namespace guestNamespace = session.getNamespaceRegistry().getNamespace(session.getTopLevelContext(), packageName);

    globals.put(hostNamespacePtr, guestNamespace.getNamespaceEnvironment());

    initSymbolMappings(hostNamespacePtr, guestNamespace.getNamespaceEnvironment());
  }

  private void initSymbolMappings(long hostNamespacePtr, Environment namespaceEnvironment) {
    long list = engine.rniListEnv(hostNamespacePtr, true);
    if(list != 0) {
      String[] strings = engine.rniGetStringArray(list);
      if(strings != null) {
        for (String symbol : strings) {
          SEXP guestValue = namespaceEnvironment.getVariableUnsafe(symbol);
          if(guestValue != Symbol.UNBOUND_VALUE) {
            long hostValue = engine.rniFindVar(symbol, hostNamespacePtr);
            globals.put(hostValue, guestValue);
          }
        }
      }
    }
  }


  private long rniEval(String rCode) {
    long sexp = engine.rniParse(rCode, 1);
    if (sexp == 0) {
      throw new IllegalStateException("Failed to parse '" + rCode + "'");
    }
    long result = engine.rniEval(sexp, globalEnv);
    if(result == 0) {
      throw new IllegalStateException("Failed to eval '" + rCode + "'");
    }
    return result;
  }

  public void resetCache() {
    cache.clear();
    cache.putAll(globals);
  }

  public void clear() {
    cache.clear();
  }

  public SEXP wrap(REXPReference ref) {
    return wrap((Long)ref.getHandle());
  }

  /**
   * Wraps a pointer to a GNU R SEXP into a Renjin SEXP class.
   *
   * @param sexp the pointer provided by {@link REXP#xp}
   * @return a Renjin SEXP instance
   */
  public SEXP wrap(long sexp) {
    SEXP wrapper = cache.get(sexp);
    if(wrapper == null) {
      wrapper = createWrapper(sexp);
      cache.put(sexp, wrapper);
    }
    return wrapper;
  }

  private SEXP createWrapper(long sexp) {
    int sexpType = engine.rniExpType(sexp);
    switch (sexpType) {
      case REXP.LISTSXP:
        return createPairList(sexp);
      case REXP.NILSXP:
        return Null.INSTANCE;
      case REXP.SYMSXP:
        return findSymbol(sexp);
      case REXP.LANGSXP:
        return createFunctionCall(sexp);
      case REXP.INTSXP:
        return new IntVectorWrapper(engine, sexp, wrapAttributes(sexp));
      case REXP.REALSXP:
        return new DoubleVectorWrapper(engine, sexp, wrapAttributes(sexp));
      case REXP.LGLSXP:
        return new LogicalVectorWrapper(engine, sexp, wrapAttributes(sexp));
      case REXP.STRSXP:
        return new StringVectorWrapper(engine, sexp, wrapAttributes(sexp));
      case REXP.ENVSXP:
        return wrapEnvironment(sexp);
      case REXP.CLOSXP:
        return createClosureWrapper(sexp);
      case REXP.VECSXP:
        return createList(sexp);
      case REXP.BCODESXP:
        return wrapBytecode(sexp);
      case REXP.PROMSXP:
        return createPromise(sexp);
      case REXP.BUILTINSXP:
      case REXP.SPECIALSXP:
        throw new UnsupportedOperationException("unmapped BUILTINSXP/SPECIALSXP");
      default:
        throw new UnsupportedOperationException("type: "  + sexpType);
    }
  }

  private SEXP createPromise(long sexp) {

    // JRI doesn't expose accessor methods for closures,
    // but promises have the same layout as pairlist nodes

    SEXP value = wrap(engine.rniCAR(sexp));
    SEXP expr =  wrap(engine.rniCDR(sexp));
    SEXP rho =   wrap(engine.rniTAG(sexp));

    if(value == Symbol.UNBOUND_VALUE) {
      // Unevaluated promise
      // Do we need to populate back into GNU R?
      return Promise.repromise((Environment) rho, expr);

    } else {
      // Already evaluated
      return new Promise(expr, value);
    }
  }

  private SEXP wrapEnvironment(long sexp) {
    Environment parent = (Environment) wrap(engine.rniParentEnv(sexp));
    FrameWrapper frame = new FrameWrapper(engine, sexp);
    frame.setWrapper(this);
    Environment env = Environment.createChildEnvironment(parent, frame).build();
    env.setAttributes(wrapAttributes(sexp));
    return env;
  }

  private SEXP createList(long sexp) {
    long[] pointers = engine.rniGetVector(sexp);
    SEXP[] elements = new SEXP[pointers.length];
    for (int i = 0; i < pointers.length; i++) {
      elements[i] = wrap(pointers[i]);
    }
    return new ListVector(elements, wrapAttributes(sexp));
  }

  public long unwrap(SEXP sexp) {
    if(sexp == Null.INSTANCE) {
      return engine.rniSpecialObject(Rengine.SO_NilValue);
    }
    if(sexp instanceof WrappedREXP) {
      return ((WrappedREXP) sexp).getHandle();
    }
    if(sexp instanceof DoubleVector) {
      return withAttributes(engine.rniPutDoubleArray(((DoubleVector) sexp).toDoubleArray()), sexp.getAttributes());
    }
    if(sexp instanceof IntVector) {
      return withAttributes(engine.rniPutIntArray(((IntVector) sexp).toIntArray()),
            sexp.getAttributes());
    }
    if(sexp instanceof StringVector) {
      return withAttributes(engine.rniPutStringArray(((StringVector) sexp).toArray()), sexp.getAttributes());
    }
    if(sexp instanceof Symbol) {
      return withAttributes(engine.rniInstallSymbol(((Symbol) sexp).getPrintName()), sexp.getAttributes());
    }
    if(sexp instanceof ListVector) {
      return withAttributes(unwrapList((ListVector) sexp), sexp.getAttributes());
    }

    throw new UnsupportedOperationException("TODO: Unwrap " + sexp.getClass().getName());
  }

  private long unwrapList(ListVector sexp) {
    long[] pointers = new long[sexp.length()];
    for (int i = 0; i < sexp.length(); i++) {
      pointers[i] = unwrap(sexp.getElementAsSEXP(i));
    }
    return engine.rniPutVector(pointers);
  }

  private long withAttributes(long sexp, AttributeMap attributes) {
    if(attributes != AttributeMap.EMPTY) {
      for (Symbol symbol : attributes.names()) {
        engine.rniSetAttr(sexp, symbol.getPrintName(), unwrap(attributes.get(symbol)));
      }
    }
    return sexp;
  }

  private Symbol findSymbol(long sexp) {
    String name = engine.rniGetSymbolName(sexp);
    if(name.isEmpty()) {
      return Symbol.MISSING_ARG;
    } else {
      return Symbol.get(name);
    }
  }

  private SEXP createFunctionCall(long sexp) {
    long function = engine.rniCAR(sexp);
    long arguments = engine.rniCDR(sexp);

    return new FunctionCall(wrap(function), createPairList(arguments));
  }

  private PairList createPairList(long node) {
    if(node == 0) {
      return Null.INSTANCE;
    }

    PairList.Builder builder = new PairList.Builder();
    builder.setAttributes(wrapAttributes(node));

    while(engine.rniExpType(node) != REXP.NILSXP) {
      long value = engine.rniCAR(node);
      long tag = engine.rniTAG(node);
      builder.add(wrap(tag), wrap(value));

      node = engine.rniCDR(node);
    }
    return builder.build();
  }


  private SEXP createClosureWrapper(long sexp) {

    // JRI doesn't expose accessor methods for closures,
    // but closures have the same layout as pairlist nodes

    PairList formals = (PairList) wrap(engine.rniCAR(sexp));
    SEXP body =  wrap(engine.rniCDR(sexp));
    Environment rho = (Environment) wrap(engine.rniTAG(sexp));

    return new Closure(rho, formals, body);
  }

  private SEXP wrapBytecode(long sexp) {

    // Bytecode constant pool is stored in the CDR slot
    long constantPoolList = engine.rniCDR(sexp);

    // Constant pool is of type 2
    int constantPoolType = engine.rniExpType(constantPoolList);
    if(constantPoolType != SexpType.VECSXP) {
      throw new RuntimeException("Constant pool type: " + constantPoolType);
    }

    // First entry in the constant pool is the original SEXP
    long[] constantPoolEntries = engine.rniGetVector(constantPoolList);
    long functionBody = constantPoolEntries[0];

    return wrap(functionBody);
  }


  private AttributeMap wrapAttributes(long sexp) {
    if(sexp == 0) {
      return AttributeMap.EMPTY;
    }
    String[] attributeNames = engine.rniGetAttrNames(sexp);
    if(attributeNames == null || attributeNames.length == 0) {
      return AttributeMap.EMPTY;
    } else {
      AttributeMap.Builder attributes = new AttributeMap.Builder();
      for (String attributeName : attributeNames) {
        attributes.set(attributeName, wrap(engine.rniGetAttr(sexp, attributeName)));
      }
      return attributes.build();
    }
  }

}
