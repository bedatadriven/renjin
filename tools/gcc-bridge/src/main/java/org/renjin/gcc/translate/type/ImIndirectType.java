package org.renjin.gcc.translate.type;


import org.renjin.gcc.jimple.JimpleType;

public interface ImIndirectType extends ImType {

  JimpleType getWrapperType();

  JimpleType getArrayType();
}
