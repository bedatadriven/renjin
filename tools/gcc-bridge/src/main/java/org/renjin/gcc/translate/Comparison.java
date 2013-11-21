package org.renjin.gcc.translate;

import org.renjin.gcc.gimple.GimpleOp;
import org.renjin.gcc.jimple.JimpleExpr;
import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.expr.ArrayRef;
import org.renjin.gcc.translate.expr.ImExpr;
import org.renjin.gcc.translate.expr.ImIndirectExpr;
import org.renjin.gcc.translate.expr.ImLiteralPrimitiveExpr;
import org.renjin.gcc.translate.type.ImPrimitiveType;

public class Comparison {
  private GimpleOp op;
  private ImExpr a, ap;
  private ImExpr b, bp;
  private ImPrimitiveType type;


  public Comparison(GimpleOp op, ImExpr a, ImExpr b) {
    super();
    this.op = op;
    this.a = a;
    this.b = b;
  }

  /**
   * If pointers are being used in the comparison, than we
   * need to interpret them as integers.
   */
  private ImExpr pointerToInteger(FunctionContext context, ImExpr a) {
    if(a instanceof ImIndirectExpr) {
      ArrayRef arrayRef = ((ImIndirectExpr) a).translateToArrayRef(context);
      return new ImLiteralPrimitiveExpr(arrayRef.getIndexExpr(), ImPrimitiveType.INT);
    } else {
      return a;
    }
  }


  public JimpleExpr toCondition(FunctionContext context) {

    ap = pointerToInteger(context, this.a);
    bp = pointerToInteger(context, this.b);
    TypeChecker.assertSamePrimitiveType(ap, bp);
    type = (ImPrimitiveType) ap.type();


    switch(type) {
    case FLOAT:
    case DOUBLE:
      switch (op) {
      case NE_EXPR:
        return floatComparison(context, "cmpl", "!=", 0);
      case EQ_EXPR:
        return floatComparison(context, "cmpl", "==", 0);
      case LE_EXPR:
        return floatComparison(context, "cmpg", "<=", 0);
      case LT_EXPR:
        return floatComparison(context, "cmpg", "<", 0);
      case GT_EXPR:
        return floatComparison(context, "cmpl", ">", 0);
      case GE_EXPR:
        return floatComparison(context, "cmpl", ">=", 0);
      }
      break;
    case INT:
    case CHAR:
    case LONG:
    case BOOLEAN:

      switch (op) {
      case NE_EXPR:
        return intComparison(context, "!=");
      case EQ_EXPR:
        return intComparison(context, "==");
      case LE_EXPR:
        return intComparison(context, "<=");
      case LT_EXPR:
        return intComparison(context, "<");
      case GT_EXPR:
        return intComparison(context, ">");
      case GE_EXPR:
        return intComparison(context, ">=");
      }
    }
    throw new UnsupportedOperationException(" don't know how to compare expressions of type " + a.type());
  }

  private JimpleExpr floatComparison(FunctionContext context, String operator, String condition, int operand) {
    String cmp = context.declareTemp(JimpleType.INT);
    context.getBuilder().addStatement(String.format("%s = %s %s %s",
        cmp, 
        ap.translateToPrimitive(context, type),
        operator, 
        bp.translateToPrimitive(context, type)));

    return new JimpleExpr(cmp + " " + condition + " " + operand);
  }
  
  private JimpleExpr intComparison(FunctionContext context, String operator) {
    return JimpleExpr.binaryInfix(operator,
        ap.translateToPrimitive(context, type),
        bp.translateToPrimitive(context, type));
    
  }
}
