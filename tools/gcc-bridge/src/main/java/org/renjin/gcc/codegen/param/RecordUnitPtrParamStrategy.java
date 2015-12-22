package org.renjin.gcc.codegen.param;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.Var;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.var.RecordPtrVarGenerator;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.type.GimpleVoidType;

import java.util.Collections;
import java.util.List;

/**
 * Strategy for a parameter that is a pointer to a single record value, implemented with a simple JVM reference type.
 */
public class RecordUnitPtrParamStrategy extends ParamStrategy {

    private RecordClassGenerator generator;

    public RecordUnitPtrParamStrategy(RecordClassGenerator generator) {
        this.generator = generator;
    }

    @Override
    public List<Type> getParameterTypes() {
        return Collections.singletonList(Type.getType(generator.getDescriptor()));
    }

    @Override
    public ExprGenerator emitInitialization(MethodVisitor methodVisitor, GimpleParameter parameter, List<Var> paramVars, LocalVarAllocator localVars) {
        return new RecordPtrVarGenerator(generator, paramVars.get(0));
    }

    @Override
    public void emitPushParameter(MethodVisitor mv, ExprGenerator parameterValueGenerator) {
        parameterValueGenerator.emitPushRecordRef(mv);
        if(parameterValueGenerator.getGimpleType().isPointerTo(GimpleVoidType.class)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, generator.getType().getInternalName());
        }
    }

}
