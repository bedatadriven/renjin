package r.compiler.ir.tac;

import java.util.List;
import java.util.Map;

import r.compiler.ir.tac.expressions.Constant;
import r.compiler.ir.tac.expressions.DynamicCall;
import r.compiler.ir.tac.expressions.EnvironmentVariable;
import r.compiler.ir.tac.expressions.Expression;
import r.compiler.ir.tac.expressions.LocalVariable;
import r.compiler.ir.tac.expressions.PrimitiveCall;
import r.compiler.ir.tac.expressions.SimpleExpression;
import r.compiler.ir.tac.expressions.Temp;
import r.compiler.ir.tac.functions.FunctionCallTranslator;
import r.compiler.ir.tac.functions.FunctionCallTranslators;
import r.compiler.ir.tac.functions.TranslationContext;
import r.compiler.ir.tac.statements.Assignment;
import r.compiler.ir.tac.statements.ExprStatement;
import r.compiler.ir.tac.statements.GotoStatement;
import r.compiler.ir.tac.statements.IfStatement;
import r.compiler.ir.tac.statements.ReturnStatement;
import r.compiler.ir.tac.statements.Statement;
import r.lang.ExpressionVector;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class IRScopeBuilder {
  
  private int nextTemp = 0;
  private int nextLocalVariableIndex = 0;
  private int nextLabel = 0;
  
  private FunctionCallTranslators builders = new FunctionCallTranslators();
 
  private List<Statement> statements;
  private IRLabel currentLabel;
  private Map<IRLabel, Integer> labels;
  
  private IRFunctionTable functionTable;
  
  public IRScopeBuilder(IRFunctionTable functionTable) {
    this.functionTable = functionTable;
  }
  
  public IRScope build(SEXP exp) {
    
    statements = Lists.newArrayList();
    labels = Maps.newHashMap();
    
    TranslationContext context = new TopLevelContext();
    Expression returnValue = translateExpression(context, exp);
    
    addStatement(new ReturnStatement(returnValue));
   
    removeRedundantJumps();
    
    return new IRScope(statements, labels, nextTemp);
  }
  
  public void dump(SEXP exp) {
    System.out.println( build(exp ).toString());
  }

  public Expression translateExpression(TranslationContext context, SEXP exp) {
    if(exp instanceof ExpressionVector) {
      return translateExpressionList(context, (ExpressionVector)exp);
    } else if(exp instanceof Vector) {
      return new Constant(exp);
    } else if(exp instanceof Symbol) {
      return new EnvironmentVariable((Symbol)exp);
    } else if(exp instanceof FunctionCall) {
      FunctionCallTranslator builder = builders.get(((FunctionCall) exp).getFunction());
      if(builder == null) {
        return translateCall(context, (FunctionCall) exp);
      } else {
        return builder.translateToExpression(this, null, (FunctionCall)exp);
      }
    } else {
      // environments, pairlists, etc
      return new Constant(exp);
    }
  }
  
  public void translateStatements(TranslationContext context, SEXP exp) {
    if(exp instanceof FunctionCall) {
      FunctionCallTranslator builder = builders.get(((FunctionCall) exp).getFunction());
      if(builder == null) {
        addStatement(new ExprStatement(translateCall(context, (FunctionCall)exp)));
      } else {
        builder.addStatement(this, context, (FunctionCall)exp);
      }
    } else {
      addStatement(new ExprStatement(translateExpression(context, exp)));
    }
  }
  
  public Expression translateCall(TranslationContext context, FunctionCall call) {
    Symbol name = (Symbol)call.getFunction();
    if(name.isReservedWord()) {
      return translatePrimitiveCall(context, call);
    } else {
      return new DynamicCall(new EnvironmentVariable(name), makeNameList(call), makeOperandList(context, call));
    }
  }
  
  public Expression translateSetterCall(TranslationContext context, FunctionCall call, Expression rhs) {
    FunctionCallTranslator translator = builders.get(call.getFunction());
    if(translator != null) {
      return translator.translateToSetterExpression(this, context, call, rhs);
    } 
    Symbol name = (Symbol)call.getFunction();
    List<SEXP> argumentNames = makeNameList(call);
    List<Expression> arguments = makeOperandList(context, call);
    
    // add rhs
    argumentNames.add(Symbol.get("value"));
    arguments.add(rhs);
    
    if(name.isReservedWord()) {
      return new PrimitiveCall(name, arguments);
    } else {
      return new DynamicCall(new EnvironmentVariable(name), argumentNames, arguments);
    }
  }

  public Expression translatePrimitiveCall(TranslationContext context,
      FunctionCall call) {
    SEXP function = call.getFunction();
    if(!(function instanceof Symbol)) {
      throw new IllegalArgumentException("Expected symbol, got '" + function + "'");
    }
    return new PrimitiveCall((Symbol)function, makeOperandList(context, call));
  }

  private List<Expression> makeOperandList(TranslationContext context, FunctionCall call) {
    List<Expression> arguments = Lists.newArrayList();
    for(SEXP arg : call.getArguments().values()) {
      arguments.add( simplify( translateExpression(context, arg) ));
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
