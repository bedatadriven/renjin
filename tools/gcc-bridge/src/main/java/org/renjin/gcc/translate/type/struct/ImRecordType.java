package org.renjin.gcc.translate.type.struct;

import org.renjin.gcc.jimple.JimpleType;
import org.renjin.gcc.translate.type.ImType;

/**
 *
 */
public abstract class ImRecordType implements ImType {

  public abstract JimpleType getJimpleType();

}
