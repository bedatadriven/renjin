package org.renjin.gcc.gimple;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.gimple.type.GimpleType;

public class GimpleCompilationUnit {

  private List<GimpleFunction> functions = Lists.newArrayList();
  private List<GimpleRecordTypeDef> recordTypes = Lists.newArrayList();

  public List<GimpleFunction> getFunctions() {
    return functions;
  }

  public List<GimpleRecordTypeDef> getRecordTypes() {
    return recordTypes;
  }

  @Override
  public String toString() {
    return Joiner.on("\n").join(functions);
  }
}
