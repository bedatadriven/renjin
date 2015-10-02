package org.renjin.gcc.translate.type;

import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.type.*;
import org.renjin.gcc.translate.FunPtrTable;
import org.renjin.gcc.translate.RecordTypeTable;
import org.renjin.gcc.translate.TranslationContext;
import org.renjin.gcc.translate.type.struct.ImRecordType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Resolves the types of all gimple nodes to JVM types.
 */
public class TypeResolver extends GimpleVisitor {
  private FunPtrTable funPtrTable;
  private RecordTypeTable recordTypeTable;

  public TypeResolver(TranslationContext context, Map<String, ImRecordType> providedRecordTypes) {
    this.recordTypeTable = new RecordTypeTable(context, providedRecordTypes);
    this.funPtrTable = new FunPtrTable(context);
  }
  
  public TypeResolver() {
    this(null, Collections.<String, ImRecordType>emptyMap());
  }

  public void resolve(List<GimpleCompilationUnit> units) {
    for (GimpleCompilationUnit unit : units) {
      for (GimpleFunction gimpleFunction : unit.getFunctions()) {
        updateNode(gimpleFunction.getReturnType());
        for (GimpleParameter parameter : gimpleFunction.getParameters()) {
          updateNode(parameter.getType());
        }
        for (GimpleVarDecl varDecl : gimpleFunction.getVariableDeclarations()) {
          updateNode(varDecl.getType());
        }
      }
    }
  }

  private void updateNode(GimpleType type) {
    type.setResolvedType(resolveType(type));
  }

  private ImType resolveType(GimpleType type) {

    if (type instanceof GimplePrimitiveType) {
      return ImPrimitiveType.valueOf(type);

    } else if (type instanceof GimpleRecordType) {
      return recordTypeTable.resolveStruct((GimpleRecordType) type);

    } else if(type instanceof GimpleFunctionType) {
      return funPtrTable.resolveFunctionType((GimpleFunctionType) type);

    } else if (type instanceof GimpleIndirectType) {
      return resolveType(type.getBaseType()).pointerType();

    } else if (type instanceof GimpleArrayType) {
      GimpleArrayType arrayType = (GimpleArrayType) type;
      return resolveType(arrayType.getComponentType()).arrayType(
          arrayType.getLbound(), arrayType.getUbound());

    } else if (type instanceof GimpleVoidType) {
      return ImVoidType.INSTANCE;
    }
    throw new UnsupportedOperationException(type.toString());
  }
}
