package org.renjin.gcc.gimple.struct;


import com.google.common.collect.Maps;
import org.renjin.gcc.translate.TranslationContext;

import java.util.Map;

public class StructTable {

  private final TranslationContext context;

  private final Map<String, Struct> map = Maps.newHashMap();

  public StructTable(TranslationContext context) {
    this.context = context;
  }

  public Struct resolveStruct(String name) {
    if(map.containsKey(name)) {
      return map.get(name);
    } else {
      GccStruct struct = new GccStruct(context, name);
      map.put(name, struct);
      return struct;
    }
  }
}
