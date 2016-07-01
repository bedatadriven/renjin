package org.renjin.compiler.ir.tac.expressions;

import com.google.common.base.Joiner;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;
import org.renjin.compiler.cfg.InlinedFunction;
import org.renjin.compiler.codegen.EmitContext;
import org.renjin.compiler.codegen.InlineParamExpr;
import org.renjin.compiler.ir.ValueBounds;
import org.renjin.eval.Context;
import org.renjin.sexp.Closure;
import org.renjin.sexp.PairList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ClosureCall implements Expression {

  private String name;
  private Closure closure;
  private String[] argumentNames;
  private List<Expression> arguments;
  
  private InlinedFunction inlinedFunction;
  
  private ValueBounds returnBounds;
  private Type type;
  
  public ClosureCall(Context context, String name, Closure closure, String[] argumentNames, List<Expression> arguments) {
    this.name = name;
    this.closure = closure;
    this.argumentNames = argumentNames;
    this.arguments = arguments;
    this.inlinedFunction = new InlinedFunction(context, closure);
    
    this.returnBounds = ValueBounds.UNBOUNDED;
    this.type = returnBounds.storageType();
  }

  @Override
  public boolean isDefinitelyPure() {
    return false;
  }


  @Override
  public Type getType() {
    return type;
  }

  @Override
  public ValueBounds updateTypeBounds(Map<Expression, ValueBounds> typeMap) {

    for (int i = 0; i < arguments.size(); i++) {
      Expression argumentExpr = arguments.get(i);
      ValueBounds argumentBounds = typeMap.get(argumentExpr);
      inlinedFunction.updateParam(i, argumentBounds);
    }
    
    returnBounds = inlinedFunction.computeBounds();
    type = returnBounds.storageType();
    
    return returnBounds;
  }

  @Override
  public ValueBounds getValueBounds() {
    return returnBounds;
  }

  @Override
  public int load(EmitContext emitContext, InstructionAdapter mv) {

    EmitContext inlineContext = emitContext.inlineContext(inlinedFunction.getCfg(), inlinedFunction.getTypes());
    Iterator<PairList.Node> formalIt = closure.getFormals().nodes().iterator();
    for (int i = 0; i < arguments.size(); i++) {
      PairList.Node formal = formalIt.next();
      inlineContext.setInlineParameter(formal.getTag(), new InlineParamExpr(emitContext, arguments.get(i)));
    }

    inlinedFunction.writeInline(inlineContext, mv);
    
    return 0;
  }


  @Override
  public void setChild(int childIndex, Expression child) {
    arguments.set(childIndex, child);
  }

  @Override
  public int getChildCount() {
    return arguments.size();
  }

  @Override
  public Expression childAt(int index) {
    return arguments.get(index);
  }

  @Override
  public String toString() {
    return name + "(" + Joiner.on(", ").join(arguments) + ")";
  }


}
