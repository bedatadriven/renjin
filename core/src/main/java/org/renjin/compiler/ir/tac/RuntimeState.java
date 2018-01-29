/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.ir.tac;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.builtins.ArgumentBounds;
import org.renjin.compiler.ir.TypeSet;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.eval.ArgumentMatcher;
import org.renjin.eval.Context;
import org.renjin.eval.MatchedArgumentPositions;
import org.renjin.packaging.SerializedPromise;
import org.renjin.primitives.S3;
import org.renjin.primitives.S4;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.repackaged.guava.primitives.Ints;
import org.renjin.sexp.*;
import org.renjin.sexp.Vector;

import java.util.*;

/**
 * Provides access to the runtime environment at the moment of compilation,
 * recording all lookups as assumptions that can be tested at future moments
 * in the runtime.
 */
public class RuntimeState {
  private Context context;
  private Environment rho;
  
  private Environment methodTable;


  /**
   * List of symbols that we have resolved to builtins / or inlined
   * closures. We need to check at the end that there is no possiblity
   * they have been assigned to.
   */
  private Map<Symbol, Function> resolvedFunctions = Maps.newHashMap();

  /**
   * Creates a new {@code RuntimeState} for an arbitrary execution environment.
   * @param context
   * @param rho
   */
  public RuntimeState(Context context, Environment rho) {
    this.context = context;
    this.rho = rho;
  }

  /**
   * Creates a new {@code RuntimeState} for an inlined function.
   * @param parentState
   * @param enclosingEnvironment
   */
  public RuntimeState(RuntimeState parentState, Environment enclosingEnvironment) {
    this(parentState.context, enclosingEnvironment);
    
    SEXP methodTableSexp = enclosingEnvironment.getVariable(S3.METHODS_TABLE);
    if(methodTableSexp instanceof Promise) {
      throw new NotCompilableException(S3.METHODS_TABLE, S3.METHODS_TABLE + " is not evaluated.");
    }
    if(methodTableSexp instanceof Environment) {
      methodTable = (Environment) methodTableSexp;
    }
  }

  public PairList getEllipsesVariable() {
    SEXP ellipses = rho.getEllipsesVariable();
    if(ellipses == Symbol.UNBOUND_VALUE) {
      throw new InvalidSyntaxException("'...' used in an incorrect context.");
    }
    return (PairList) ellipses;
  }

  public SEXP findVariable(Symbol name) {

    SEXP value = null;
    Environment environment = rho;
    while(environment != Environment.EMPTY) {
      if (environment.isActiveBinding(name)) {
        throw new NotCompilableException(name, "Active Binding encountered");
      }
      value = rho.findVariable(context, name);
      if(value instanceof Promise) {
        Promise promisedValue = (Promise) value;
        if(promisedValue.isEvaluated()) {
          value = promisedValue.force(context);
        } else {
          // Promises can have side effects, and evaluation order is important
          // so we can't just force all the promises in the beginning of the loop
          throw new NotCompilableException(name, "Unevaluated promise encountered");
        }
      }
      environment = environment.getParent();
    }
    if(value == null) {
      throw new NotCompilableException(name, "Symbol not found. Should not reach here!");
    }
    return value;
  }


  public Function findFunction(Symbol functionName) {

    Function f = findFunctionIfExists(functionName);
    if (f != null) {
      return f;
    }
    throw new NotCompilableException(functionName, "Could not find function " + functionName);
  }

  public Function findFunctionIfExists(Symbol functionName) {
    if(resolvedFunctions.containsKey(functionName)) {
      return resolvedFunctions.get(functionName);
    }

    Environment environment = rho;
    while(environment != Environment.EMPTY) {
      Function f = isFunction(functionName, environment.getVariable(context, functionName));
      if(f != null) {
        resolvedFunctions.put(functionName, f);
        return f;
      }
      environment = environment.getParent();
    }
    return null;
  }

  /**
   * Tries to safely determine whether the expression is a function, without
   * forcing any promises that might have side effects.
   * @param exp
   * @return null if the expr is definitely not a function, or {@code expr} if the
   * value can be resolved to a Function without side effects
   * @throws NotCompilableException if it is not possible to determine
   * whether the value is a function without risking side effects.
   */
  private Function isFunction(Symbol functionName, SEXP exp) {
    if(exp instanceof Function) {
      return (Function)exp;

    } else if(exp instanceof SerializedPromise) {
      // Functions loaded from packages are initialized stored as SerializedPromises
      // so the AST does not have to be loaded into memory until the function is first 
      // needed. Though technically this does involve I/O and thus side-effects,
      // we don't consider it to have any affect on the running program, and so
      // can safely force it.
      return isFunction(functionName, exp.force(this.context));

    } else if(exp instanceof Promise) {
      Promise promise = (Promise)exp;
      if(promise.isEvaluated()) {
        return isFunction(functionName, promise.getValue());
      } else {
        throw new NotCompilableException(functionName, "Symbol " + functionName + " cannot be resolved to a function " +
            " an enclosing environment has a binding of the same name to an unevaluated promise");
      }
    } else {
      return null;
    }
  }

  public Map<Symbol, Function> getResolvedFunctions() {
    return resolvedFunctions;
  }

  /**
   * Tries to safely resolve an S3 method, without forcing any promises that might have side effects.
   * @param generic
   * @param group
   * @param objectClasses
   */
  public Function findMethod(String generic, String group, StringVector objectClasses) {

    Function method = null;
    
    for(String className : objectClasses) {
      method = findMethod(generic, group, className);
      if(method != null) {
        return method;
      }
    }
    
    return findMethod(generic, group, "default");
  }
  
  private Function findMethod(String generic, String group, String className) {
    
    Function method = findMethod(generic, className);
    if(method != null) {
      return method;
    }
    if(group != null) {
      method = findMethod(group, className);
      if(method != null) {
        return method;
      }
    }
    return null;
  }

  private Function findMethod(String generic, String className) {

    // TODO: this requires some thought because we are making
    // assumptions about not only the functions we find, but those we DON'T find,
    // and method table entires can easily be changed if new packages are loaded, even if the
    // environment is sealed.


    Symbol method = Symbol.get(generic + "." + className);
    Function function = findFunctionIfExists(method);
    if(function != null) {
      return function;
    }
  
    if(methodTable != null) {
      SEXP functionSexp = methodTable.getVariable(method);
      if(functionSexp instanceof Promise) {
        throw new NotCompilableException(method, "Unevaluated entry in " + S3.METHODS_TABLE);
      }
      if(functionSexp instanceof Function) {
        return (Function) functionSexp;
      }
    }
    
    return null;
  }
  
  
  // related to S4 method dispatch
  
  private Map<Symbol, List<Environment>> s4GenericMethodTables = new HashMap<>();
  private Map<Symbol, List<Environment>> s4GroupMethodTables = new HashMap<>();
  public static final Set<String> GROUPS = Sets.newHashSet("Ops", "Math", "Summary");
  private static final Set<String> ARITH_GROUP = Sets.newHashSet("+", "-", "*", "^", "%%", "%/%", "/");
  private static final Set<String> COMPARE_GROUP = Sets.newHashSet("==", ">", "<", "!=", "<=", ">=");
  private static final Set<String> LOGIC_GROUP = Sets.newHashSet("&", "&&", "|", "||", "xor");
  private static final Set<String> SPECIAL = Sets.newHashSet("$", "$<-");
  
  
  public void findS4methodTables(String generic, Symbol opName, List<ArgumentBounds> args) {
  
    List<Environment> groupMethodTables = null;
    List<Environment> genericMethodTables = null;
    genericMethodTables = findMethodTable(context, opName);
    if("Ops".equals(generic)) {
      groupMethodTables = findOpsMethodTable(context, generic);
    } //else if(!"".equals(group)) {
    //  groupMethodTables = findMethodTable(context, group);
    //}
  
    if(genericMethodTables != null) {
      s4GenericMethodTables.put(opName, genericMethodTables);
    }
    if(groupMethodTables != null) {
      s4GroupMethodTables.put(opName, genericMethodTables);
    }
  }
  
  private static List<Environment> findMethodTable(Context context, Symbol opName) {
    SEXP methodTableMethodsPkg;
    List<Environment> methodTableList = new ArrayList<>();
    
    if (SPECIAL.contains(opName.getPrintName())) {
      Namespace methodsNamespace = context.getNamespaceRegistry().getNamespace(context, "methods");
      Frame methodFrame = methodsNamespace.getNamespaceEnvironment().getFrame();
      methodTableMethodsPkg = methodFrame.getVariable(opName).force(context);
      if(methodTableMethodsPkg == Symbol.UNBOUND_VALUE || !(methodTableMethodsPkg instanceof Environment)) {
        return null;
      }
      methodTableList.add((Environment) methodTableMethodsPkg);
    } else {
      
      SEXP methodTableGlobalEnv = context.getGlobalEnvironment().getFrame().getVariable(opName);
      if (methodTableGlobalEnv != Symbol.UNBOUND_VALUE && methodTableGlobalEnv instanceof Environment) {
        methodTableList.add((Environment) methodTableGlobalEnv);
      }
      
      for(Symbol loadedNamespace : context.getNamespaceRegistry().getLoadedNamespaces()) {
        String packageName = loadedNamespace.getPrintName();
        Namespace packageNamespace = context.getNamespaceRegistry().getNamespace(context, packageName);
        Environment packageEnvironment = packageNamespace.getNamespaceEnvironment();
        SEXP methodTablePackage = packageEnvironment.getFrame().getVariable(opName).force(context);
        if(methodTablePackage instanceof Environment) {
          methodTableList.add((Environment) methodTablePackage);
        }
      }
    }
    return methodTableList.size() == 0 ? null : methodTableList;
  }
  
  private static List<Environment> findOpsMethodTable(Context context, String generic) {
    List<Environment> methodTableList = new ArrayList<>();
    
    Frame globalFrame = context.getGlobalEnvironment().getFrame();
    SEXP methodTableGlobalEnv = getMethodTable(context, generic, globalFrame);
    if (methodTableGlobalEnv instanceof Environment) {
      methodTableList.add((Environment) methodTableGlobalEnv);
    }
    
    for(Symbol packageSymbol : context.getNamespaceRegistry().getLoadedNamespaces()) {
      String packageName = packageSymbol.getPrintName();
      Collection<Symbol> exports = context.getNamespaceRegistry().getNamespace(context, packageName).getExports();
      if(exports.contains(Symbol.get("Arith")) ||
          exports.contains(Symbol.get("Compare")) ||
          exports.contains(Symbol.get("Logic")) ||
          exports.contains(Symbol.get(generic))) {
        Namespace packageNamespace = context.getNamespaceRegistry().getNamespace(context, packageName);
        Frame packageFrame = packageNamespace.getNamespaceEnvironment().getFrame();
        SEXP methodTablePackage = getMethodTable(context, generic, packageFrame);
        if(methodTablePackage instanceof Environment &&
            ((Environment) methodTablePackage).getFrame().getSymbols().size() > 0) {
          methodTableList.add((Environment) methodTablePackage);
        }
      }
    }
    
    if (methodTableList.size() == 0) {
      return null;
    }
    
    return methodTableList;
  }
  
  private static SEXP getMethodTable(Context context, String generic, Frame packageFrame) {
    SEXP methodTable = null;
    if(ARITH_GROUP.contains(generic)) {
      String[] groups = {".__T__Arith:base", ".__T__Ops:base"};
      methodTable = getMethod(context, packageFrame, groups);
    } else if (COMPARE_GROUP.contains(generic)) {
      String[] groups = {".__T__Compare:methods", ".__T__Ops:base"};
      methodTable = getMethod(context, packageFrame, groups);
    } else if (LOGIC_GROUP.contains(generic)) {
      String[] groups = {".__T__Logic:base", ".__T__Ops:base"};
      methodTable = getMethod(context, packageFrame, groups);
    }
    return methodTable;
  }
  
  private static SEXP getMethod(Context context, Frame frame, String[] groups) {
    SEXP methodTable = null;
    for (int i = 0; i < groups.length && methodTable == null; i++) {
      SEXP foundMethodTable = frame.getVariable(Symbol.get(groups[i])).force(context);
      methodTable = foundMethodTable instanceof Environment ? (Environment) foundMethodTable : null;
    }
    return methodTable;
  }
  
  public boolean hasS4MethodTable(Symbol opName) {
    return s4GenericMethodTables.containsKey(opName) ||
      s4GroupMethodTables.containsKey(opName);
  }
  
  public int[] computeSignatureLength(Symbol opName) {
    
    List<Environment> methodTable;
    if(s4GenericMethodTables.containsKey(opName)) {
      methodTable = s4GenericMethodTables.get(opName);
    } else {
      methodTable = s4GroupMethodTables.get(opName);
    }
    
    int[] length = new int[methodTable.size()];
    
    for(int i = 0; i < methodTable.size(); i++) {
      if(methodTable.get(i).getFrame().getSymbols().iterator().hasNext()) {
        String methodName = methodTable.get(i).getFrame().getSymbols().iterator().next().getPrintName();
        length[i] = methodName.split("#").length;
      } else {
        length[i] = 0;
      }
    }
    return length;
  }
  
  public Map<String,List<List<S4.MethodRanking>>> generateSignatures(Symbol opName, List<ArgumentBounds> arguments, int[] depth) {
  
    Map<String, List<List<S4.MethodRanking>>> mapListMethods = new HashMap<>();
    Map<String, List<Environment>> mapMethodTableLists = new HashMap<>();
    if(s4GenericMethodTables.size() > 0) {
      mapMethodTableLists.put("generic", s4GenericMethodTables.get(opName));
    }
    if(s4GroupMethodTables.size() > 0) {
      mapMethodTableLists.put("group", s4GroupMethodTables.get(opName));
    }
  
    for(int e = 0; e < mapMethodTableLists.size(); e++) {
      String type = mapMethodTableLists.keySet().toArray(new String[0])[e];
      List<Environment> methodTableList = mapMethodTableLists.get(type);
      List<List<S4.MethodRanking>> listSignatures = new ArrayList<>();
    
      for(int listIdx = 0; listIdx < methodTableList.size(); listIdx++) {
        Environment methodTable = methodTableList.get(listIdx);
        int currentDepth = depth[listIdx];
        S4.ArgumentSignature[] argSignatures;
        Symbol methodSymbol = methodTable.getFrame().getSymbols().iterator().next();
        Closure genericClosure = (Closure) methodTable.getFrame().getVariable(methodSymbol);
        PairList formals = genericClosure.getFormals();
  
        ArgumentMatcher argumentMatcher = new ArgumentMatcher(formals);
        String[] argNames = new String[arguments.size()];
        for(int i = 0; i < arguments.size(); i++) {
          argNames[i] = arguments.get(i).getName();
        }
        MatchedArgumentPositions matchedArguments = argumentMatcher.match(argNames);
        
        //if (!matchedArguments.hasExtraArguments()) {
        argSignatures = computeArgumentSignatures(context, matchedArguments, arguments, currentDepth);
        //} else {
        //  argSignatures = computeArgumentSignatures(context, matchedArguments, arguments, currentDepth);
        //}
      
        int numberOfPossibleSignatures = 1;
        for(int i = 0; i < argSignatures.length; i++) {
          numberOfPossibleSignatures = numberOfPossibleSignatures * argSignatures[i].getArgument().length;
        }
      
        List<S4.MethodRanking> possibleSignatures = new ArrayList<>(numberOfPossibleSignatures);
      
        int numberOfClassesCurrentArgument;
        int argumentClassIdx = 0;
        int repeat = 1;
        int repeatIdx = 1;
      
        for(int col = 0; col < depth[listIdx]; col++) {
          numberOfClassesCurrentArgument = argSignatures[col].getArgument().length;
          for(int row = 0; row < numberOfPossibleSignatures; row++, repeatIdx++) {
            if(argumentClassIdx == numberOfClassesCurrentArgument) {
              argumentClassIdx = 0;
            }
            S4.ArgumentSignature argSignature = argSignatures[col];
            String signature = argSignature.getArgument(argumentClassIdx);
            if(possibleSignatures.isEmpty() ||
                possibleSignatures.size() < row + 1 ||
                possibleSignatures.get(row) == null) {
              int[] distance = argSignature.getDistanceAsArray(argumentClassIdx);
              possibleSignatures.add(row, new S4.MethodRanking(signature, distance));
            } else {
              int distance = argSignature.getDistance(argumentClassIdx);
              possibleSignatures.set(row, possibleSignatures.get(row).append(signature, distance));
            }
            if(repeat == 1) {
              argumentClassIdx++;
            }
            if(repeat != 1 && repeatIdx == repeat) {
              repeatIdx = 0;
              argumentClassIdx++;
            }
          }
          repeatIdx = 1;
          argumentClassIdx = 0;
          repeat = repeat * numberOfClassesCurrentArgument;
        }
        listSignatures.add(possibleSignatures);
      }
      mapListMethods.put(type, listSignatures);
    }
    return mapListMethods;
  }
  
  private static S4.ArgumentSignature[] computeArgumentSignatures(Context context, MatchedArgumentPositions match, List<ArgumentBounds> arguments, int currentDepth) {
    
    String argClass;
    
    S4.ArgumentSignature[] argSignatures = new S4.ArgumentSignature[currentDepth];
    
    int idx = 0;
    for (int i = 0; i < currentDepth; i++) {
      Symbol formal = match.getFormalName(i);
      ValueBounds value = arguments.get(match.getMatchedFormals().get(formal)).getBounds();
      if (value.getConstantClassAttribute() != Null.INSTANCE) {
        argClass = value.getConstantClassAttribute().getElementAsString(0);
      } else {
        argClass = TypeSet.implicitClass(value.getTypeSet());
      }
      
      argSignatures[idx] = getClassAndDistance(context, argClass);
      idx++;
    }
    
    return argSignatures;
  }
  
//  private static S4.ArgumentSignature getArgumentSignature(Context context, SEXP argValue) {
//    if (argValue == Symbol.MISSING_ARG) {
//      return new S4.ArgumentSignature();
//    }
//    String[] nodeClass = S4.computeDataClasses(context, argValue).toArray();
//
//    return getClassAndDistance(context, nodeClass);
//  }
  
  private static S4.ArgumentSignature getClassAndDistance(Context context, String argClass) {
    
    List<Integer> distances = new ArrayList<>();
    List<String> classes = new ArrayList<>();
  
    classes.add(argClass);
    distances.add(0);
    
    Symbol argClassObjectName = Symbol.get(".__C__" + argClass);
    Frame globalFrame = context.getGlobalEnvironment().getFrame();
    AttributeMap map = globalFrame.getVariable(argClassObjectName).getAttributes();
    SEXP containsSlot = map.get("contains");
    SEXP argSuperClasses = containsSlot.getNames();
    
    for(int i = 0; i < argSuperClasses.length(); i++) {
      SEXP distanceSlot = ((ListVector) containsSlot).get(i).getAttributes().get("distance");
      distances.add(((Vector) distanceSlot).getElementAsInt(0));
      classes.add(((Vector) argSuperClasses).getElementAsString(i));
    }
    
    int max = Collections.max(distances);
    if(!classes.contains("ANY") && !classes.contains("NULL")) {
      distances.add(max + 1);
      classes.add("ANY");
    }
    
    return new S4.ArgumentSignature(classes.toArray(new String[0]), Ints.toArray(distances));
  }
  
//  private static S4.ArgumentSignature getClassAndDistance(Context context, String[] argClass) {
//
//    List<Integer> distances = new ArrayList<>();
//    List<String> classes = new ArrayList<>();
//    for(int i = 0; i < argClass.length; i++) {
//      classes.add(argClass[i]);
//      distances.add(0);
//    }
//    Symbol argClassObjectName = Symbol.get(".__C__" + argClass[0]);
//    Frame globalFrame = context.getGlobalEnvironment().getFrame();
//    AttributeMap map = globalFrame.getVariable(argClassObjectName).getAttributes();
//    SEXP containsSlot = map.get("contains");
//    SEXP argSuperClasses = containsSlot.getNames();
//
//    for(int i = 0; i < argSuperClasses.length(); i++) {
//      SEXP distanceSlot = ((ListVector) containsSlot).get(i).getAttributes().get("distance");
//      distances.add(((Vector) distanceSlot).getElementAsInt(0));
//      classes.add(((Vector) argSuperClasses).getElementAsString(i));
//    }
//
//    int max = Collections.max(distances);
//    if(!classes.contains("ANY") && !classes.contains("NULL")) {
//      distances.add(max + 1);
//      classes.add("ANY");
//    }
//
//    return new S4.ArgumentSignature(classes.toArray(new String[0]), Ints.toArray(distances));
//  }
  
  public Map<String, List<S4.SelectedMethod>> findMatchingMethods(Symbol opName, Map<String, List<List<S4.MethodRanking>>> signatures) {
    
    Map<String, List<S4.SelectedMethod>> methods = new HashMap<>();
    List<S4.SelectedMethod> selectedMethods = new ArrayList<>();
    Map<String, List<Environment>> mapMethodTableLists = new HashMap<>();
    if(s4GenericMethodTables.containsKey(opName)) {
      mapMethodTableLists.put("generic", s4GenericMethodTables.get(opName));
    }
    if(s4GroupMethodTables.containsKey(opName)) {
      mapMethodTableLists.put("group", s4GroupMethodTables.get(opName));
    }
  
    for(int e = 0; e < signatures.size(); e++) {
      String type = signatures.keySet().toArray(new String[0])[e];
      List<List<S4.MethodRanking>> rankings = signatures.get(type);
      List<Environment> methodTableList = mapMethodTableLists.get(type);
    
      for(int i = 0; i < rankings.size(); i++) {
        List<S4.MethodRanking> rankedMethodsList = rankings.get(i);
        String inputSignature = rankedMethodsList.get(0).getSignature();
      
        for (S4.MethodRanking rankedMethod : rankedMethodsList) {
          String signature = rankedMethod.getSignature();
          double rank = rankedMethod.getRank();
          int[] distance = rankedMethod.getDistances();
          boolean has0 = rankedMethod.hasZeroDistanceArgument();
          Symbol signatureSymbol = Symbol.get(signature);
          SEXP function = methodTableList.get(i).getFrame().getVariable(signatureSymbol).force(context);
        
          if (function instanceof Closure) {
            selectedMethods.add(new S4.SelectedMethod((Closure) function, type, rank, distance, signature, signatureSymbol, inputSignature, has0));
          }
        }
      }
      if(selectedMethods.size() > 0) {
        Collections.sort(selectedMethods);
        methods.put(type, selectedMethods);
      }
    }
    return methods;
  }
}
