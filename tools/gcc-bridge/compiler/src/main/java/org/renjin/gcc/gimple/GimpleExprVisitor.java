package org.renjin.gcc.gimple;

import org.renjin.gcc.gimple.expr.*;

/**
 * Visitor 
 */
public class GimpleExprVisitor {
  
  public void visit(GimpleExpr expr) {
    
  }
  
  public void visitAddressOf(GimpleAddressOf addressOf) {
    visit(addressOf);
    addressOf.getValue().accept(this);  
  }

  public void visitArrayRef(GimpleArrayRef arrayRef) {
    visit(arrayRef);
    arrayRef.getArray().accept(this);
    arrayRef.getIndex().accept(this);
  }

  public void visitBitFieldRef(GimpleBitFieldRefExpr bitFieldRef) {
    visit(bitFieldRef);
  }

  public void visitComplexConstant(GimpleComplexConstant constant) {
    visit(constant);
    constant.getReal().accept(this);
    constant.getIm().accept(this);
  }

  
  public void visitComplexPart(GimpleComplexPartExpr complexPart) {
    visit(complexPart);
    complexPart.getComplexValue().accept(this);
  }

  public void visitComponentRef(GimpleComponentRef componentRef) {
    visit(componentRef);
    componentRef.getValue().accept(this);
    componentRef.getMember().accept(this);
  }

  public void visitCompoundLiteral(GimpleCompoundLiteral compoundLiteral) {
    visit(compoundLiteral);
    compoundLiteral.getDecl().accept(this);
  }

  public void visitConstantRef(GimpleConstantRef constantRef) {
    visit(constantRef);
    constantRef.getValue().accept(this);
  }

  public void visitConstructor(GimpleConstructor constructor) {
    visit(constructor);
    for (GimpleConstructor.Element element : constructor.getElements()) {
      element.getValue().accept(this);
    }
  }

  public void visitFieldRef(GimpleFieldRef fieldRef) {
    visit(fieldRef);
  }

  public void visitFunctionRef(GimpleFunctionRef functionRef) {
    visit(functionRef);
  }

  public void visitPrimitiveConstant(GimplePrimitiveConstant constant) {
    visit(constant);
  }

  public void visitMemRef(GimpleMemRef memRef) {
    memRef.getPointer().accept(this);
    if(memRef.getOffset() != null) {
      memRef.getOffset().accept(this);
    }
  }

  public void visitNop(GimpleNopExpr expr) {
    expr.getValue().accept(this);
  }

  public void visitObjectTypeRef(GimpleObjectTypeRef objectTypeRef) {
    objectTypeRef.getExpr().accept(this);
    objectTypeRef.getObject().accept(this);
    objectTypeRef.getToken().accept(this);
  }

  public void visitParamRef(GimpleParamRef paramRef) {
    visit(paramRef);
  }

  public void visitPointerPlus(GimplePointerPlus pointerPlus) {
    visit(pointerPlus);
    pointerPlus.getPointer().accept(this);
    pointerPlus.getOffset().accept(this);
  }

  public void visitResultDecl(GimpleResultDecl resultDecl) {
    visit(resultDecl);
  }


  public void visitSsaName(GimpleSsaName ssaName) {
    visit(ssaName);    
  }

  public void visitStringConstant(GimpleStringConstant stringConstant) {
    visit(stringConstant);
  }

  public void visitVariableRef(GimpleVariableRef variableRef) {
    visit(variableRef);
  }
}
