package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.field.FieldGenerator;
import org.renjin.gcc.codegen.field.RecordArrayFieldGenerator;
import org.renjin.gcc.codegen.field.RecordFieldGenerator;
import org.renjin.gcc.codegen.field.RecordPtrFieldGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.RecordPtrParamGenerator;
import org.renjin.gcc.codegen.ret.RecordPtrReturnGenerator;
import org.renjin.gcc.codegen.ret.ReturnGenerator;
import org.renjin.gcc.codegen.var.RecordPtrVarGenerator;
import org.renjin.gcc.codegen.var.RecordVarGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.type.GimpleArrayType;

/**
 * Creates generators for variables and values of type {@code GimpleRecordType}
 */
public class RecordTypeFactory extends TypeFactory {
    private final RecordClassGenerator generator;

    public RecordTypeFactory(RecordClassGenerator generator) {
        this.generator = generator;
    }

    @Override
    public TypeFactory pointerTo() {
        return new Pointer();
    }

    @Override
    public VarGenerator addressableVarGenerator(LocalVarAllocator allocator) {
        return new RecordVarGenerator(generator, allocator.reserveObject());
    }

    @Override
    public VarGenerator varGenerator(LocalVarAllocator allocator) {
        return new RecordVarGenerator(generator, allocator.reserveObject());
    }

    @Override
    public TypeFactory arrayOf(GimpleArrayType arrayType) {
        return new Array(arrayType);
    }

    @Override
    public FieldGenerator fieldGenerator(String className, String fieldName) {
        return new RecordFieldGenerator(className, fieldName, generator);
    }
    
    public class Array extends TypeFactory {

        private GimpleArrayType arrayType;

        public Array(GimpleArrayType arrayType) {
            this.arrayType = arrayType;
        }

        @Override
        public FieldGenerator fieldGenerator(String className, String fieldName) {
            return new RecordArrayFieldGenerator(className, fieldName, generator, arrayType);
        }
    }

    public class Pointer extends TypeFactory {

        @Override
        public ParamGenerator paramGenerator() {
            return new RecordPtrParamGenerator(generator);
        }

        @Override
        public FieldGenerator fieldGenerator(String className, String fieldName) {
            return new RecordPtrFieldGenerator(className, fieldName, generator);
        }

        @Override
        public ReturnGenerator returnGenerator() {
            return new RecordPtrReturnGenerator(generator);
        }

        @Override
        public VarGenerator varGenerator(LocalVarAllocator allocator) {
            return new RecordPtrVarGenerator(generator, allocator.reserveObject());
        }
    }
}
