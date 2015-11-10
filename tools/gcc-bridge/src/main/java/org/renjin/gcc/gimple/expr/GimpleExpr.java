package org.renjin.gcc.gimple.expr;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
    @Type(value = GimpleMemRef.class, name = "mem_ref"),
    @Type(value = GimpleVariableRef.class, name = "var_decl"),
    @Type(value = GimpleFieldRef.class, name = "field_decl"),
    @Type(value = GimpleParamRef.class, name = "parm_decl"),
    @Type(value = GimpleArrayRef.class, name = "array_ref"),
    @Type(value = GimpleObjectTypeRef.class, name = "obj_type_ref"),
    @Type(value = GimpleAddressOf.class, name = "addr_expr"),
    @Type(value = GimpleIntegerConstant.class, name = "integer_cst"),
    @Type(value = GimpleRealConstant.class, name = "real_cst"),
    @Type(value = GimpleStringConstant.class, name = "string_cst"),
    @Type(value = GimpleFunctionRef.class, name = "function_decl"),
    @Type(value = GimpleConstantRef.class, name = "const_decl"),
    @Type(value = GimpleComponentRef.class, name = "component_ref"),
    @Type(value = GimpleConstructor.class, name = "constructor"),
    @Type(value = GimpleRealPartExpr.class, name = "realpart_expr"),
    @Type(value = GimpleImPartExpr.class, name = "imagpart_expr"),
    @Type(value = GimpleComplexConstant.class, name = "complex_cst"),
    @Type(value = GimpleResultDecl.class, name = "result_decl"),
    @Type(value = GimpleNopExpr.class, name = "nop_expr") 
})
public abstract class GimpleExpr {

    private Integer line;

    public void setLine(Integer line) {
        this.line = line;
    }

    public Integer getLine() {
        return line;
    }
}
