package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.RecordPtrVarGenerator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.GimpleVoidType;

import java.util.Collections;
import java.util.List;

public class RecordPtrParamGenerator extends ParamGenerator {

    private RecordClassGenerator generator;

    public RecordPtrParamGenerator(RecordClassGenerator generator) {
        this.generator = generator;
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.singletonList(Type.getType(generator.getDescriptor()));
    }

    @Override
    public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, int startIndex, LocalVarAllocator localVars) {
        return new RecordPtrVarGenerator(generator, startIndex);
    }

    @Override
    public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
        parameterValueGenerator.emitPushRecordRef(mv);
        if(parameterValueGenerator.getGimpleType().isPointerTo(GimpleVoidType.class)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, generator.getType().getInternalName());
        }
    }

    @Override
    public GimpleType getGimpleType() {
        return new GimplePointerType(generator.getGimpleType());
    }
}
