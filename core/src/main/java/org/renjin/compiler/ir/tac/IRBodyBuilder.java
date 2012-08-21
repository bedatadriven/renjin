package org.renjin.compiler.ir.tac;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.compiler.ir.tac.expressions.*;
import org.renjin.compiler.ir.tac.functions.FunctionCallTranslator;
import org.renjin.compiler.ir.tac.functions.FunctionCallTranslators;
import org.renjin.compiler.ir.tac.functions.TranslationContext;
import org.renjin.compiler.ir.tac.statements.*;
import org.renjin.sexp.*;

import java.util.List;
import java.util.Map;

public class IRBodyBuilder {
  
  private int nextTemp = 0;
  private int nextLocalVariableIndex = 0;
  private int nextLabel = 0;
  
  private FunctionCallTranslators builders = new FunctionCallTranslators();
 
  private List<Statement> statements;
  private IRLabel currentLabel;
  private Map<IRLabel, Integer> labels;
  
  private IRFunctionTable functionTable;
  private List<IRThunk> thunks = Lists.newArrayList();
  
  public IRBodyBuilder(IRFunctionTable functionTable) {
    this.functionTable = functionTable;
  }
  
  public IRBody build(SEXP exp) {
    
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();
    
    TranslationContext context = new TopLevelContext();
    Expression returnValue = translateExpression(context, exp);
    
    addStatement(new ReturnStatement(returnValue));
   
    removeRedundantJumps();
    
    return new IRBody(statements, labels, nextTemp);
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
        return new EnvironmentVariable((Symbol)exp);
      }
    } else if(exp instanceof FunctionCall) {
        return translateCall(context, (FunctionCall) exp);
    } else {
      // environments, pairlists, etc
      return new Constant(exp);
    }
  }

  private boolean isReservedFunction(SEXP exp) {
    if(exp instanceof FunctionCall) {
      SEXP fn = ((FunctionCall) exp).getFunction();
      return fn instanceof Symbol && ((Symbol) fn).isReservedWord();
    } else {
      return false;
    }
  }
  
  public static boolean isConstant(SEXP exp) {
    return ! (exp instanceof ExpressionVector ||
              exp instanceof Symbol ||
              exp instanceof FunctionCall);
  }
  
  public void translateStatements(TranslationContext context, SEXP sexp) {
    if( isReservedFunction(sexp) ) {
      FunctionCallTranslator translator = builders.get( ((FunctionCall)sexp).getFunction() );
      if(translator != null) {
        translator.addStatement(this, context, (FunctionCall)sexp);
        return;
      }
    }
    Expression expr = translateExpression(context, sexp);
    if(!(expr instanceof Constant)) {
      addStatement(new ExprStatement(expr));  
    }
  }
  
  public Expression translateCall(TranslationContext context, FunctionCall call) {
    SEXP function = call.getFunction();
    if(function instanceof Symbol && ((Symbol) function).isReservedWord()) {
      return translatePrimitiveCall(context, call);
    } else {
      return new DynamicCall(call, 
          translateSimpleExpression(context, function), 
          makeNameList(call), 
          makeUnevaledArgList(context, call.getArguments()));
    }
  }
  
  public List<Expression> makeUnevaledArgList(TranslationContext context, PairList argumentSexps) {
    List<Expression> list = Lists.newArrayList();
    for(SEXP argument : argumentSexps.values()) {
      if(argument == Symbol.MISSING_ARG) {
        list.add(new Constant(argument));
      } else if(argument == Symbols.ELLIPSES) {
        list.add(Elipses.INSTANCE);
      } else {
        list.add(unevaledArg(argument));
      }
    }
    return list;
  }
  
  public Expression unevaledArg(SEXP exp) {
    if(isConstant(exp)) {
      return new Constant(exp);
    } else {
      return translateThunk(exp);
    }
  }
  
  
  private IRThunk translateThunk(SEXP exp) {
    IRBodyBuilder thunkBodyBuilder = new IRBodyBuilder(functionTable);
    IRBody body = thunkBodyBuilder.build(exp);
    IRThunk thunk = new IRThunk(exp, body);
    thunks.add(thunk);
    return thunk;
  }

  public Expression translateSetterCall(TranslationContext context, FunctionCall getterCall, Expression rhs) {
    Symbol getter = (Symbol) getterCall.getFunction();
    Symbol setter = Symbol.get(getter.getPrintName() + "<-");
    
    // normally this call is created at runtime, with the  value 
    // of the rhs in the argument list. Since we don't have
    // the value of the rhs yet
    FunctionCall setterCall = new FunctionCall(
        setter,
        PairList.Node.newBuilder()
          .addAll(getterCall.getArguments())
          .add("value", new StringArrayVector("TODO: evaluated RHS here"))
          .build());
    
    FunctionCallTranslator translator = builders.get(setter);
    if(translator != null) {
      return translator.translateToSetterExpression(this, context, setterCall, rhs);
    } 

    
    if(setter.isReservedWord()) {
      List<Expression> arguments = makeEvaledArgList(context, getterCall.getArguments());
      arguments.add(simplify( rhs ));
      
      return new PrimitiveCall(setterCall, setter, arguments);
      
    } else {
      
      // note that rhs has been evaled at this point
      List<Expression> arguments = makeUnevaledArgList(context, getterCall.getArguments());
      arguments.add(rhs);

      List<SEXP> argumentNames = makeNameList(getterCall);
      argumentNames.add(Symbol.get("value"));
      
      return new DynamicCall(setterCall,
          new EnvironmentVariable(setter), 
          argumentNames, 
          arguments);
    }
  }

  public Expression translatePrimitiveCall(TranslationContext context,
      FunctionCall call) {
    SEXP function = call.getFunction();
    FunctionCallTranslator translator = builders.get(function);
    if(translator != null) {
      return translator.translateToExpression(this, context, call);
    } else {
      if(!(function instanceof Symbol)) {
        throw new IllegalArgumentException("Expected symbol, got '" + function + "'");
      }
      return new PrimitiveCall(call, (Symbol)function, makeEvaledArgList(context, call.getArguments()));
    }
  }

  private List<Expression> makeEvaledArgList(TranslationContext context, PairList argumentSexps) {
    List<Expression> arguments = Lists.newArrayList();
    for(SEXP arg : argumentSexps.values()) {
      if(arg == Symbols.ELLIPSES) {
        arguments.add( Elipses.INSTANCE );
      } else {
        arguments.add( simplify( translateExpression(context, arg) ));
      }
    }
    return arguments;
  }
  
  private List<SEXP> makeNameList(FunctionCall call) {
    List<SEXP> names = Lists.newArrayList();
    for(PairList.Node node : call.getArguments().nodes()) {
      names.add(node.getRawTag());
    }
    return names;
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
  
  public LocalVariable newLocalVariable() {
    return new LocalVariable("Λ" + (nextLocalVariableIndex++), nextTemp++);
  }
  
  public IRLabel newLabel() {
    return new IRLabel(nextLabel++);
  }
  
  public IRFunction newFunction(PairList formals, SEXP body) {
    return functionTable.newFunction(formals, body);
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
    
  }
}
