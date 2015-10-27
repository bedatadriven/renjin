package org.renjin.gcc.codegen.type;

import org.renjin.gcc.codegen.LocalVarAllocator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.RecordPtrParamGenerator;
import org.renjin.gcc.codegen.var.RecordVarGenerator;
import org.renjin.gcc.codegen.var.VarGenerator;
import org.renjin.gcc.gimple.type.GimpleRecordType;

/**
 * Created by parham on 10/27/15.
 */
public class RecordTypeFactory extends TypeFactory {
    private GimpleRecordType type;

    public RecordTypeFactory(GimpleRecordType type) {
        super();
        this.type = type;
    }

    @Override
    public TypeFactory pointerTo() {
        return new Pointer();
    }

    @Override
    public VarGenerator addressableVarGenerator(LocalVarAllocator allocator) {
        return new RecordVarGenerator(type, allocator.reserveObject());
    }

    public class Pointer extends TypeFactory {

        @Override
        public ParamGenerator paramGenerator() {
            return new RecordPtrParamGenerator(type);
        }
    }
}
