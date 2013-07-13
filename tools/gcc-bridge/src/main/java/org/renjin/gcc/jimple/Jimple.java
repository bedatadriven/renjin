package org.renjin.gcc.jimple;

import com.google.common.collect.Sets;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;

import java.util.Set;

public class Jimple {

  public static Set<String> RESERVED_WORDS = Sets.newHashSet(
      "new",
      "abstract",
      "assert",
      "boolean",
      "break",
      "byte",
      "case",
      "catch",
      "char",
      "class",
      "const",
      "continue",
      "default",
      "do",
      "double",
      "else",
      "enum",
      "extends",
      "false",
      "final",
      "finally",
      "float",
      "for",
      "goto",
      "if",
      "implements",
      "import");


  public static String id(String name) {
    if(RESERVED_WORDS.contains(name)) {
      return name + "__";
    }
    return name.replace('.', '$');
  }


  public static String type(Class<?> type) {
    return type.toString();
  }
}
