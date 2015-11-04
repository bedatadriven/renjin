package org.renjin.invoke;

/**
 * Composes valid Java class names for R functions
 */
public class Naming {
  public static String toJavaName(String rName) {
    return toJavaName("R$primitive$", rName);
  }

  public static String toJavaName(String prefix, String rName) {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix);
    // for some readability, translate "." to $
    rName = rName.replace('.', '$');
    rName = rName.replace("<-", "$assign");
    
    // mnemonics are borrowed partly from scala
    for(int i=0;i!=rName.length();++i) {
      int cp = rName.codePointAt(i);
      if(Character.isJavaIdentifierPart(cp)) {
        sb.appendCodePoint(cp);
      } else if(cp == '=') {
        sb.append("$eq");
      } else if(cp == '>') {
        sb.append("$greater");
      } else if(cp == '<') {
        sb.append("$less");
      } else if(cp == '+') {
        sb.append("$plus");
      } else if(cp == '-') {
        sb.append("$minus");
      } else if(cp == '*') {
        sb.append("$times");
      } else if(cp == '/') {
        sb.append("$div");
      } else if(cp == '!') {
        sb.append("$bang");
      } else if(cp == '@') {
        sb.append("$at");
      } else if(cp == '#') {
        sb.append("$at");
      } else if(cp == '%') {
        sb.append("$percent");
      } else if(cp == '^') {
        sb.append("$up");
      } else if(cp == '&') {
        sb.append("$amp");
      } else if(cp == '~') {
        sb.append("$tilde");
      } else if(cp == '?') {
        sb.append("$qmark");
      } else if(cp == '|') {
        sb.append("$bar");
      } else if(cp == '\\') {
        sb.append("$bslash");
      } else if(cp == ':') {
        sb.append("$colon");
      } else if(cp == '(') {
        sb.append("$paren");
      } else if(cp == '[') {
        sb.append("$bracket");
      } else {
        sb.append("_$" + cp + "$_");
      }
    }
    return sb.toString();
  }

  public static String toFullJavaName(String rName) {
    return "org.renjin.primitives." + toJavaName(rName);
  }
}
