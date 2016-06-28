package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.statement.*;
import org.renjin.gcc.gimple.type.*;

import java.util.List;

/**
 * Visits all GimpleRecordType instances in a program
 */
public abstract class RecordTypeUseVisitor {

  public void visit(List<GimpleCompilationUnit> units) {


    // Replace ids in each compilation unit with references to the canonical type
    for (GimpleCompilationUnit unit : units) {
      for (GimpleVarDecl decl : unit.getGlobalVariables()) {
        visitType(decl.getType());
        if(decl.getValue() != null) {
          updateTypes(decl.getValue());
        }
      }

      for (GimpleFunction function : unit.getFunctions()) {
        visitType(function.getReturnType());
        for (GimpleParameter gimpleParameter : function.getParameters()) {
          visitType(gimpleParameter.getType());
        }
        for (GimpleVarDecl decl : function.getVariableDeclarations()) {
          visitType(decl.getType());
        }
        for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
          for (GimpleStatement statement : basicBlock.getStatements()) {
            updateTypes(statement);
          }
        }
      }
    }
  }

  public void visit(GimpleRecordTypeDef recordTypeDef) {
    for (GimpleField gimpleField : recordTypeDef.getFields()) {
      visitType(gimpleField.getType());
    }
  }


  private void updateTypes(Iterable<GimpleType> types) {
    for (GimpleType type : types) {
      visitType(type);
    }
  }

  private void visitType(GimpleType type) {
    if(type instanceof GimpleRecordType) {
      visitRecordType((GimpleRecordType) type);

    } else if(type instanceof GimpleIndirectType) {
      visitType(type.getBaseType());
      
    } else if(type instanceof GimpleArrayType) {
      visitType(((GimpleArrayType) type).getComponentType());
    
    } else if(type instanceof GimpleFunctionType) {
      GimpleFunctionType functionType = (GimpleFunctionType) type;
      visitType(functionType.getReturnType());
      updateTypes(functionType.getArgumentTypes());
    }
  }

  protected abstract void visitRecordType(GimpleRecordType type);


  private void updateTypes(GimpleStatement gimpleIns) {
    if(gimpleIns instanceof GimpleReturn) {
      updateTypes(((GimpleReturn) gimpleIns).getValue());
    } else if(gimpleIns instanceof GimpleAssignment) {
      GimpleAssignment assignment = (GimpleAssignment) gimpleIns;
      updateTypes(assignment.getLHS());
      updateTypes(assignment.getOperands());
    } else if(gimpleIns instanceof GimpleCall) {
      GimpleCall call = (GimpleCall) gimpleIns;
      updateTypes(call.getLhs());
      updateTypes(call.getFunction());
      updateTypes(call.getOperands());
    } else if(gimpleIns instanceof GimpleConditional) {
      GimpleConditional conditional = (GimpleConditional) gimpleIns;
      updateTypes(conditional.getOperands());
    } else if(gimpleIns instanceof GimpleSwitch) {
      GimpleSwitch gimpleSwitch = (GimpleSwitch) gimpleIns;
      updateTypes(gimpleSwitch.getValue());
    }
  }

  private void updateTypes(List<GimpleExpr> operands) {
    for (GimpleExpr operand : operands) {
      updateTypes(operand);
    }
  }

  private void updateTypes(GimpleExpr expr) {
    if(expr != null) {
      visitType(expr.getType());
      if (expr instanceof GimpleAddressOf) {
        updateTypes(((GimpleAddressOf) expr).getValue());
      } else if (expr instanceof GimpleMemRef) {
        updateTypes(((GimpleMemRef) expr).getPointer());
      } else if (expr instanceof GimpleComponentRef) {
        GimpleComponentRef ref = (GimpleComponentRef) expr;
        updateTypes(ref.getValue());
        updateTypes(ref.getMember());
      } else if (expr instanceof GimpleArrayRef) {
        updateTypes(((GimpleArrayRef) expr).getArray());
      } else if (expr instanceof GimpleNopExpr) {
        updateTypes(((GimpleNopExpr) expr).getValue());
      } else if (expr instanceof GimpleConstructor) {
        GimpleConstructor constructor = (GimpleConstructor) expr;
        for (GimpleConstructor.Element element : constructor.getElements()) {
          updateTypes(element.getValue());
        }
      }
    }
  }

  
}
