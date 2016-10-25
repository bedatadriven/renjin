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
package org.renjin.compiler.ir.tac;

import org.renjin.compiler.NotCompilableException;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.compiler.ir.exception.InvalidSyntaxException;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.functions.*;
import org.renjin.compiler.ir.tac.statements.*;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.*;

import java.util.*;

/**
 * Attempts to create an intermediate representation of the R code, partially
 * evaluating as it goes.
 *
 * The idea is that we are ONLY interested in the result if the R code can be
 * reduced to a reasonably static form that we can reason about. If there are
 * calls to eval(), assign() or other black holes, we abort and defer to the
 * AST interpreter.
 */
public class IRBodyBuilder {
  
  private int nextTemp = 0;
  private int nextLabel = 0;
  
  private FunctionCallTranslators builders = new FunctionCallTranslators();
 
  private List<Statement> statements;
  private IRLabel currentLabel;
  private Map<IRLabel, Integer> labels;
  private Map<Symbol, EnvironmentVariable> variables = Maps.newHashMap();

  private RuntimeState runtimeContext;
  
  private Map<String, Integer> localVariableNames = Maps.newHashMap();
  private Set<Symbol> paramSet = new HashSet<>();

  public IRBodyBuilder(RuntimeState runtimeState) {
    this.runtimeContext = runtimeState;
  }
  
  public RuntimeState getRuntimeState() {
    return runtimeContext;
  }
  
  public IRBody build(SEXP exp) {
    
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();
    
    TranslationContext context = new TopLevelContext();
    Expression returnValue = translateExpression(context, exp);
    
    addStatement(new ReturnStatement(returnValue));
   
    removeRedundantJumps();
    insertVariableInitializations();
    updateVariableReturn();
    
    return new IRBody(statements, labels);
  }
  
  public IRBody buildLoopBody(FunctionCall call, SEXP sequence) {
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();

    LocalVariable vector = newLocalVariable("elements");
    LocalVariable counter = newLocalVariable("i");

    statements.add(new Assignment(vector, new ReadLoopVector(sequence)));
    statements.add(new Assignment(counter, new ReadLoopIt()));

    LoopBodyContext bodyContext = new LoopBodyContext(runtimeContext);
    
    ForTranslator.buildLoop(bodyContext, this, call, vector, counter);

    addStatement(new ReturnStatement(new Constant(Null.INSTANCE)));
    
    removeRedundantJumps();
    insertVariableInitializations();
    updateVariableReturn();
    
    return new IRBody(statements, labels);
  }
  
  public IRBody buildFunctionBody(Closure closure, Set<Symbol> suppliedArguments) {
    
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();


    for (PairList.Node node : closure.getFormals().nodes()) {
      paramSet.add(node.getTag());
    }
    
    // First define the parameters which will be supplied
    List<ReadParam> params = Lists.newArrayList();
    for (PairList.Node formal : closure.getFormals().nodes()) {
      if (suppliedArguments.contains(formal.getTag())) {
        ReadParam paramExpr = new ReadParam(formal.getTag());
        statements.add(new Assignment(new EnvironmentVariable(formal.getTag()), paramExpr));
        params.add(paramExpr);
      }
    }
    
    // Now define  default values for formals that are not supplied
    // These are not necessarily constants and are evaluated lazily, so some care is 
    // required. 
    for (PairList.Node formal : closure.getFormals().nodes()) {
      if(formal.getRawTag() != Symbols.ELLIPSES) {
        if (!suppliedArguments.contains(formal.getTag())) {
          SEXP defaultValue = formal.getValue();
          if (defaultValue == Symbol.MISSING_ARG) {
            throw new InvalidSyntaxException("argument '" + formal.getTag() + "' is missing, with no default");
          } else {
            if (!isConstant(defaultValue)) {
              throw new NotCompilableException(defaultValue, "argument '" + formal.getName() + "' has not been provided" +
                  " and has a default value with (potential) side effects.");
            }
            statements.add(new Assignment(new EnvironmentVariable(formal.getTag()), new Constant(formal.getValue())));
          }
        }
      }
    }
    
    TranslationContext context = new InlinedContext(closure.getFormals());
    Expression returnValue = translateExpression(context, closure.getBody());
    addStatement(new ReturnStatement(returnValue));

    removeRedundantJumps();
    insertVariableInitializations();

    IRBody body = new IRBody(statements, labels);
    body.setParams(params);
    return body;
  }

  private boolean isConstant(SEXP defaultValue) {
    return ! ( defaultValue instanceof Symbol || 
               defaultValue instanceof ExpressionVector ||
               defaultValue instanceof FunctionCall);
    
  }

  private void updateVariableReturn() {

    for (Statement statement : statements) {
      if(statement instanceof UpdateStatement) {
        ((UpdateStatement) statement).addEnvironmentVariables(variables.values());
      }
    }
  }


  private void insertVariableInitializations() {
    // For every variable that comes from the environment, 
    // declare it as a constant in the beginning of the block
    
    List<Assignment> initializations = new ArrayList<>();
    
    for (EnvironmentVariable environmentVariable : variables.values()) {
      if(!paramSet.contains(environmentVariable.getName())) {
        SEXP value = runtimeContext.findVariable(environmentVariable.getName());

        if (value != Symbol.UNBOUND_VALUE) {
          initializations.add(new Assignment(environmentVariable,
              new ReadEnvironment(environmentVariable.getName(), ValueBounds.of(value))));
        }
      }
    }
    
    // Update the labels to reflect the additional statements at the beginning
    statements.addAll(0, initializations);
    for (IRLabel label : labels.keySet()) {
      labels.put(label, labels.get(label) + initializations.size());
    }
    
  }

  public void dump(SEXP exp) {
    System.out.println( build(exp ).toString());
  }

  public Expression translateExpression(TranslationContext context, SEXP exp) {
    if(exp instanceof ExpressionVector) {
      return translateExpressionList(context, (ExpressionVector)exp);
    } else if(exp instanceof Symbol) {
      if(exp == Symbol.MISSING_ARG) {
        return new Constant(exp);
      } else {
        return getEnvironmentVariable((Symbol) exp);
      }
    } else if(exp instanceof FunctionCall) {
      return translateCallExpression(context, (FunctionCall) exp);
    } else {
      // environments, pairlists, etc
      return new Constant(exp);
    }
  }

  public EnvironmentVariable getEnvironmentVariable(Symbol name) {
    EnvironmentVariable var = variables.get(name);
    if(var == null) {
      var = new EnvironmentVariable(name);
      variables.put(name, var);
    }
    return var;
  }

  public void translateStatements(TranslationContext context, SEXP sexp) {
    if(sexp instanceof FunctionCall) {
      FunctionCall call = (FunctionCall)sexp;
      Function function = resolveFunction(call.getFunction());
      builders.get( function ).addStatement(this, context, function, call);
    } else {
      Expression expr = translateExpression(context, sexp);
      if(!(expr instanceof Constant)) {
        addStatement(new ExprStatement(expr));
      }
    }
  }

  public Expression translateSetterCall(TranslationContext context, FunctionCall getterCall, Expression rhs) {
    Symbol getter = (Symbol) getterCall.getFunction();
    Function setter = resolveFunction(Symbol.get(getter.getPrintName() + "<-"));

    FunctionCallTranslator translator = builders.get(setter);
    return translator.translateToSetterExpression(this, context, setter, getterCall, rhs);
  }

  public Expression translateCallExpression(TranslationContext context, FunctionCall call) {
    SEXP functionName = call.getFunction();
    Function function = resolveFunction(functionName);

    FunctionCallTranslator translator = builders.get(function);
    return translator.translateToExpression(this, context, function, call);
  }

  private Function resolveFunction(SEXP functionName) {
    if( functionName instanceof PrimitiveFunction) {
      return (PrimitiveFunction) functionName;
    } else if (functionName instanceof Symbol) {
      return runtimeContext.findFunction((Symbol) functionName);
    }
    throw new NotCompilableException(functionName);
  }

  public List<IRArgument> translateArgumentList(TranslationContext context, PairList argumentSexps) {
    List<IRArgument> arguments = Lists.newArrayList();
    for(PairList.Node argNode : argumentSexps.nodes()) {
      if(argNode.getValue() == Symbols.ELLIPSES) {
        for (PairList.Node extraArgument :  context.getEllipsesArguments().nodes()) {
          SimpleExpression expression = simplify(translateExpression(context, extraArgument));
          arguments.add( new IRArgument(extraArgument.getRawTag(), expression) );
        }
      } else {
        SimpleExpression argExpression = simplify(translateExpression(context, argNode.getValue()));
        arguments.add( new IRArgument(argNode.getRawTag(), argExpression) );
      }
    }
    return arguments;
  }

  public SimpleExpression simplify(Expression rvalue) {
    if(rvalue instanceof SimpleExpression) {
      return (SimpleExpression) rvalue;
    } else {
      Temp temp = newTemp();
      addStatement(new Assignment(temp, rvalue));
      return temp;      
    }
  }

  public SimpleExpression translateSimpleExpression(TranslationContext context, SEXP exp) {
    return simplify(translateExpression(context, exp));
  }
  
  private Expression translateExpressionList(TranslationContext context, ExpressionVector vector) {
    if(vector.length() == 0) {
      return new Constant(Null.INSTANCE);
    } else {
      for(int i=0;i+1<vector.length();++i) {
        translateStatements(context, vector.getElementAsSEXP(i));
      }
      return translateExpression(context, vector.getElementAsSEXP(vector.length()-1));
    }
  }
  
  public Temp newTemp() {
    return new Temp(nextTemp++);
  }
  
  public LocalVariable newLocalVariable(String debugName) {
    int index;
    String name;
    if(localVariableNames.containsKey(debugName)) {
      index = localVariableNames.get(debugName) + 1;
      name = debugName + index;
    } else {
      index = 1;
      name = debugName;
    }
    localVariableNames.put(debugName, index);
    return new LocalVariable("_" + name);
  }
  
  public IRLabel newLabel() {
    return new IRLabel(nextLabel++);
  }

  public void addStatement(Statement statement) {
    statements.add(statement);
    currentLabel = null;
  }
  
  public IRLabel addLabel() {
    if(currentLabel != null) {
      return currentLabel; 
    } else {
      IRLabel newLabel = newLabel();
      addLabel(newLabel);
      return newLabel;
    }
  }

  public void addLabel(IRLabel label) {
    labels.put(label, statements.size());
    currentLabel = label;
  }
  
  /**
   * Streamlines IR in the case that you have one goto 
   * pointing to another goto.
   */
  private void removeRedundantJumps() {
    boolean changed;
    do {
      changed = false;
      for(int i=0;i!=statements.size();++i) {
        Statement stmt = statements.get(i);
        if(stmt instanceof IfStatement) {
          IfStatement ifStmt = (IfStatement) stmt;
        
          IRLabel newTrueTarget = ultimateTarget(ifStmt.getTrueTarget());
          if(newTrueTarget != null) {
            statements.set(i, ifStmt.setTrueTarget(newTrueTarget));
            changed = true;
          }
          
          IRLabel newFalseTarget = ultimateTarget(ifStmt.getFalseTarget());
          if(newFalseTarget != null) {
            statements.set(i, ifStmt.setFalseTarget(newFalseTarget));
            changed = true;
          }
        }
      }
    } while(changed);
  }
  
  private IRLabel ultimateTarget(IRLabel label) {
    Statement targetStmt = statements.get( labels.get(label) );
    if(targetStmt instanceof GotoStatement) {
      return ((GotoStatement) targetStmt).getTarget();
    }
    return null;
  }
  
  private static class TopLevelContext implements TranslationContext {

    @Override
    public PairList getEllipsesArguments() {
      throw new InvalidSyntaxException("'...' used outside of a function");
    }
  }
}
