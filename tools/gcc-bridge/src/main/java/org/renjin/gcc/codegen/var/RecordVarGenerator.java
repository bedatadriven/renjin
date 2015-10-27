package org.renjin.gcc.codegen.var;

import org.objectweb.asm.MethodVisitor;
import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.gimple.type.GimpleIntegerType;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleType;
import static org.objectweb.asm.Opcodes.*;

/**
 * Created by parham on 10/27/15.
 */
public class RecordVarGenerator extends AbstractExprGenerator implements VarGenerator {

    private final String recordClassName;
    private GimpleRecordType type;
    private int varIndex;

    public RecordVarGenerator(GimpleRecordType type, int varIndex) {
        this.type = type;

        this.varIndex = varIndex;
        recordClassName = "Structs$record1";
    }

    @Override
    public void emitDefaultInit(MethodVisitor mv) {
        mv.visitTypeInsn(NEW, recordClassName);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, recordClassName, "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, varIndex);

//        0: new           #4                  // class Main$account
//        3: dup
//        4: invokespecial #5                  // Method Main$account."<init>":()V
//        7: astore_0

    }

    @Override
    public ExprGenerator addressOf() {
        return new Pointer();
    }

    @Override
    public GimpleType getGimpleType() {
        return type;
    }

    public ExprGenerator memberOf(String name) {
        return new Member(name);
    }

    public class Member extends AbstractExprGenerator{

        private String memberName;

        public Member(String name) {
            this.memberName = name;
        }

        @Override
        public GimpleType getGimpleType() {
            return new GimpleIntegerType(32);
        }

        @Override
        public void emitStore(MethodVisitor mv, ExprGenerator valueGenerator) {
            mv.visitVarInsn(ALOAD, varIndex);
            valueGenerator.emitPrimitiveValue(mv);
            mv.visitFieldInsn(PUTFIELD, recordClassName, memberName, "I");
//            8: aload_0
//            9: iconst_2
//            10: putfield      #3                  // Field Main$account.years_open:I
        }
    }

    public class Pointer extends AbstractExprGenerator {

        @Override
        public GimpleType getGimpleType() {
            return new GimplePointerType(type);
        }

        @Override
        public void emitPushRecordRef(MethodVisitor mv) {
            mv.visitVarInsn(ALOAD, varIndex);
        }
    }
}
