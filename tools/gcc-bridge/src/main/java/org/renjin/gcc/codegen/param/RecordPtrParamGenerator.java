package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.RecordPtrVarGenerator;
import org.renjin.gcc.gimple.type.GimpleType;

import java.util.Collections;
import java.util.List;

public class RecordPtrParamGenerator extends ParamGenerator {

    private GimpleType type;

    public RecordPtrParamGenerator(GimpleType type){
        this.type = type;
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.singletonList(Type.getType("Lorg/renjin/gcc/Structs$record1;"));
    }

    @Override
    public ExprGenerator emitInitialization(MethodVisitor methodVisitor, int startIndex, LocalVarAllocator localVars) {
        return new RecordPtrVarGenerator(type, startIndex);
    }

    @Override
    public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
        parameterValueGenerator.emitPushRecordRef(mv);
    }
}
