package org.renjin.gcc.translate.expr;

import org.renjin.gcc.gimple.expr.GimpleStringConstant;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.type.ImIndirectType;
import org.renjin.gcc.translate.type.ImPrimitiveType;
import org.renjin.gcc.translate.type.ImType;


public class ImStringConstant extends AbstractImExpr {

  private GimpleStringConstant constant;
  private ImPrimitiveType type;
  
  public ImStringConstant(GimpleStringConstant constant) {
    this.constant = constant;
  }

  @Override
  public ImType type() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return JimpleExpr.stringLiteral(constant.getValue()).toString();
  }

  @Override
  public ImExpr addressOf() {
    return new Pointer(null);
  }

  @Override
  public ImExpr elementAt(ImExpr index) {
    return new Substring(index);
  }
  
  public class Substring extends AbstractImExpr {
    private ImExpr startIndex;

    public Substring(ImExpr startIndex) {
      this.startIndex = startIndex;
    }

    @Override
    public ImExpr addressOf() {
      return new Pointer(startIndex);
    }

    @Override
    public ImPrimitiveType type() {
      return type;
    }
  }

  public class Pointer extends AbstractImExpr implements ImIndirectExpr {
    
    private ImExpr startIndex;

    public Pointer(ImExpr startIndex) {
      this.startIndex = startIndex;
    }

    @Override
    public ArrayRef translateToArrayRef(FunctionContext context) {
      String stringTmp = context.declareTemp(String.class);
      String arrayTmp = context.declareTemp(char.class);
      
      context.getBuilder().addStatement(stringTmp + " = " +
          JimpleExpr.stringLiteral(constant.getValue()));
      context.getBuilder().addStatement(arrayTmp + " = virtualinvoke " +
              stringTmp + ".<java.lang.String: char[] toCharArray()>()");
      
      JimpleExpr indexExpr;
      if(startIndex == null) {
        indexExpr = JimpleExpr.integerConstant(constant.getType().getLbound());        
      } else {
        indexExpr = subtractLowerBound(context, startIndex);
      }

      return new ArrayRef(new JimpleExpr(arrayTmp), indexExpr);
    }

    private JimpleExpr subtractLowerBound(FunctionContext context, ImExpr expr) {
      if(constant.getType().getLbound() == 0) {
        return startIndex.translateToPrimitive(context, null);
      } else if(expr instanceof ImPrimitiveConstant) {
        Number startIndex = (Number)((ImPrimitiveConstant) expr).getConstantValue();
        return JimpleExpr.integerConstant(startIndex.intValue() - constant.getType().getLbound());
      } else {
        throw new UnsupportedOperationException();
      }
    }

    @Override
    public ImIndirectType type() {
      return ImPrimitiveType.CHAR.pointerType();
    }

    @Override
    public String toString() {
      return "&" + ImStringConstant.this.toString();
    }
  }
}
