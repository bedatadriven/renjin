package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.RecordClassGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.RecordPtrParamGenerator;
import org.renjin.gcc.codegen.var.RecordVarGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;

/**
 * Creates generators for variables and values of type {@code GimpleRecordType}
 */
public class RecordTypeFactory extends TypeFactory {
    private final RecordClassGenerator generator;

    public RecordTypeFactory(RecordClassGenerator generator) {
        super();
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

    public class Pointer extends TypeFactory {

        @Override
        public ParamGenerator paramGenerator() {
            return new RecordPtrParamGenerator(generator);
        }
    }
}
