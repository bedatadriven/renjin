package org.renjin.gcc.codegen.var;

import org.renjin.gcc.codegen.expr.AbstractExprGenerator;
import org.renjin.gcc.codegen.expr.ExprGenerator;
import org.renjin.gcc.codegen.param.ParamGenerator;
import org.renjin.gcc.codegen.param.RecordPtrParamGenerator;
import org.renjin.gcc.codegen.type.TypeFactory;
import org.renjin.gcc.gimple.type.GimpleType;

/**
 * Created by parham on 10/27/15.
 */
public class RecordPtrVarGenerator extends AbstractExprGenerator implements ExprGenerator {
    private GimpleType type;
    private int varIndex;

    public RecordPtrVarGenerator(GimpleType type, int varIndex) {
        this.type = type;
        this.varIndex = varIndex;
    }


    @Override
    public GimpleType getGimpleType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExprGenerator valueOf() {
        return new ValueOf();
    }

    @Override
    public ExprGenerator memberof() {
        return new Member();
    }


    private class ValueOf extends AbstractExprGenerator {

        @Override
        public GimpleType getGimpleType() {
            return type.getBaseType();
        }
    }

//    private class Member extends AbstractExprGenerator {
//        @Override
//        public GimpleType getGimpleType() {
//            return type.;
//        }
//    }
}