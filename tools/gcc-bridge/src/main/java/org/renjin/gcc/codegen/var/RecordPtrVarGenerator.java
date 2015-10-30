package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.RecordPtrParamGenerator;
import org.renjin.gcc.codegen.type.TypeFactory;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimpleType;
import static org.objectweb.asm.Opcodes.*;

public class RecordPtrVarGenerator extends AbstractExprGenerator implements ExprGenerator {
    private GimpleType type;
    private int varIndex;
    private final String recordClassName;

    public RecordPtrVarGenerator(GimpleType type, int varIndex) {
        this.type = type;
        this.varIndex = varIndex;
        recordClassName = "Structs$record1";
    }


    @Override
    public GimpleType getGimpleType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExprGenerator valueOf() {
        return new ValueOf();
    }

    private class ValueOf extends AbstractExprGenerator {

        @Override
        public GimpleType getGimpleType() {
            return type.getBaseType();
        }

        @Override
        public ExprGenerator memberOf(String memberName) {
            return new Member(memberName);
        }
    }

    private class Member extends AbstractExprGenerator {

        private String memberName;

        public Member(String memberName) {
            this.memberName = memberName;
        }

        public GimpleType getGimpleType() {
            return new GimpleIntegerType();
        }

        @Override
        public void emitPrimitiveValue(MethodVisitor mv) {
            if (this.getGimpleType() instanceof GimpleIntegerType) {
                mv.visitVarInsn(ALOAD, varIndex);
                mv.visitFieldInsn(GETFIELD, recordClassName, memberName, "I");
            } else {
                throw new UnsupportedOperationException(String.format("%s [%s] is not a value type",
                        toString(), getClass().getSimpleName()));
            }
        }
    }
}