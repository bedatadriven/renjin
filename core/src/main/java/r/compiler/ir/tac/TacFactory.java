package r.compiler.ir.tac;

import java.util.List;

import r.compiler.ir.tac.functions.FunctionCallTranslator;
import r.compiler.ir.tac.functions.FunctionCallTranslators;
import r.compiler.ir.tac.functions.TranslationContext;
import r.compiler.ir.tac.instructions.Assignment;
import r.compiler.ir.tac.instructions.ExprStatement;
import r.compiler.ir.tac.instructions.Return;
import r.compiler.ir.tac.instructions.Statement;
import r.compiler.ir.tac.operand.Constant;
import r.compiler.ir.tac.operand.DynamicCall;
import r.compiler.ir.tac.operand.Operand;
import r.compiler.ir.tac.operand.LValue;
import r.compiler.ir.tac.operand.PrimitiveCall;
import r.compiler.ir.tac.operand.SimpleExpr;
import r.compiler.ir.tac.operand.Temp;
import r.compiler.ir.tac.operand.Variable;
import r.lang.ExpressionVector;
import r.lang.FunctionCall;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.Vector;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class TacFactory implements TranslationContext {
  
  private int nextTemp = 0;
  private int nextLabel = 0;
  
  private FunctionCallTranslators builders = new FunctionCallTranslators();
  private List<Node> nodes;
  
  public IRBlock build(SEXP exp) {
    
    nodes = Lists.newArrayList();
    Operand returnValue = translateExpression(exp);
    
    nodes.add(new Return(returnValue));
   
    return new IRBlock(nodes, nextTemp);
  }
  
  public void dump(SEXP exp) {
    System.out.println( toString( build(exp )));
  }

  @Override
  public Operand translateExpression(SEXP exp) {
    if(exp instanceof ExpressionVector) {
      return translateExpressionList((ExpressionVector)exp);
    } else if(exp instanceof Vector) {
      return new Constant(exp);
    } else if(exp instanceof Symbol) {
      return new Variable((Symbol)exp);
    } else if(exp instanceof FunctionCall) {
      FunctionCallTranslator builder = builders.get(((FunctionCall) exp).getFunction());
      if(builder == null) {
        return translateCall((FunctionCall) exp);
      } else {
        return builder.translateToExpression(this, (FunctionCall)exp);
      }
    } else {
      throw new UnsupportedOperationException(exp.toString());
    }
  }
  
  @Override
  public void translateStatements(SEXP exp) {
    if(exp instanceof FunctionCall) {
      FunctionCallTranslator builder = builders.get(((FunctionCall) exp).getFunction());
      if(builder == null) {
        addNode(new ExprStatement(translateCall((FunctionCall)exp)));
      } else {
        builder.addStatement(this, (FunctionCall)exp);
      }
    } else {
      addNode(new ExprStatement(translateExpression(exp)));
    }
  }
  
  public Operand translateCall(FunctionCall call) {
    Symbol name = (Symbol)call.getFunction();
    if(name.isReservedWord()) {
      return new PrimitiveCall(name, makeOperandList(call));
    } else {
      return new DynamicCall(name, makeOperandList(call));
    }
  }

  private List<Operand> makeOperandList(FunctionCall call) {
    List<Operand> arguments = Lists.newArrayList();
    for(SEXP arg : call.getArguments().values()) {
      arguments.add( simplify( translateExpression(arg) ));
    }
    return arguments;
  }

  public LValue addAssignment(Operand rvalue) {
    Temp target = newTemp();
    nodes.add(new Assignment(target, rvalue));
    return target;
  }
  
  public LValue addAssignment(LValue lvalue, Operand rvalue) {
    nodes.add(new Assignment(lvalue, rvalue));
    return lvalue;
  }
  
  public SimpleExpr simplify(Operand rvalue) {
    if(rvalue instanceof SimpleExpr) {
      return (SimpleExpr) rvalue;
    } else {
      Temp temp = newTemp();
      nodes.add(new Assignment(temp, rvalue));
      return temp;      
    }
  }



  @Override
  public SimpleExpr translateSimpleExpression(SEXP exp) {
    return simplify(translateExpression(exp));
  }

  
  public void addNode(Node node) {
    nodes.add(node);
  }
  
  private Operand translateExpressionList(ExpressionVector vector) {
    if(vector.length() == 0) {
      return new Constant(Null.INSTANCE);
    } else {
      for(int i=0;i+1<vector.length();++i) {
        translateStatements(vector.getElementAsSEXP(i));
      }
      return translateExpression(vector.getElementAsSEXP(vector.length()-1));
    }
  }
  
  public Temp newTemp() {
    return new Temp(nextTemp++);
  }
  
  public Label newLabel() {
    return new Label(nextLabel++);
  }
  
  public static String toString(IRBlock nodes) {
    StringBuilder sb = new StringBuilder();
    for(Node node : nodes.getStatements()) {
      if(node instanceof Label) {
        sb.append(node).append(":").append("\n");
      } else {
        sb.append("  ").append(node).append("\n");
      }
    }
    return sb.toString();
  }

  @Override
  public void addStatement(Statement statement) {
    nodes.add(statement);
  }

  @Override
  public void addLabel(Label label) {
    nodes.add(label);
  }
}
