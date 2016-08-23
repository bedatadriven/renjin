package org.renjin.gcc.analysis;

import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleExprVisitor;
import org.renjin.gcc.gimple.expr.GimpleComponentRef;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleFieldRef;
import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.HashMap;
import java.util.Map;

/**
 * Searches the Gimple AST for implicit Field declarations and adds them to the 
 * GimpleRecordTypeDef structures.
 */
public class ImplicitFieldDeclFinder {
  
  public static void find(Iterable<GimpleCompilationUnit> units) {
    for (GimpleCompilationUnit unit : units) {
      ComponentRefVisitor visitor = new ComponentRefVisitor(unit);
      unit.accept(visitor);
    }
    
  }
  
  private static class ComponentRefVisitor extends GimpleExprVisitor {

    private Map<String, GimpleRecordTypeDef> defMap = new HashMap<>();

    public ComponentRefVisitor(GimpleCompilationUnit unit) {
      for (GimpleRecordTypeDef typeDef : unit.getRecordTypes()) {
        defMap.put(typeDef.getId(), typeDef);
      }
    }

    @Override
    public void visitComponentRef(GimpleComponentRef componentRef) {
      GimpleExpr record = componentRef.getValue();
      if(!(record.getType() instanceof GimpleRecordType)) {
        throw new IllegalStateException("record.type = " + record.getType());
      }
      
      GimpleRecordType recordType = (GimpleRecordType) record.getType();
      GimpleRecordTypeDef recordTypeDef = defMap.get(recordType.getId());
      
      if(recordTypeDef == null) {
        throw new IllegalStateException("No GimpleRecordTypeDef found for id " + recordType.getId() + " referenced in " +
          componentRef);
      }

      GimpleFieldRef fieldRef = componentRef.getMember();
      GimpleType fieldType = componentRef.getType();
      
      if(!isDeclared(recordTypeDef, fieldRef.getOffset(), fieldType)) {
        GimpleField field = new GimpleField();
        field.setName(fieldRef.getName());
        field.setOffset(fieldRef.getOffset());
        field.setType(fieldType);
        field.setSize(fieldType.getSize());

        recordTypeDef.getFields().add(field);
      }
    }

    private boolean isDeclared(GimpleRecordTypeDef recordTypeDef, int offset, GimpleType fieldType) {
      for (GimpleField gimpleField : recordTypeDef.getFields()) {
        if (gimpleField.getOffset() == offset &&
            gimpleField.getType().equals(fieldType)) {
          return true;
        }
      }
      return false;
    }
  }
  
}
