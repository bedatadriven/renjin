package org.renjin.packaging.test;

import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.SexpVisitor;
import org.renjin.sexp.StringVector;

public class ExampleParser extends SexpVisitor<String> {

  private final StringBuilder code = new StringBuilder();


  @Override
  public void visit(ListVector list) {
    String tag = ExamplesParser.getTag(list);
    if(tag.equals("\\dots")) {
      code.append("...");
    } else {
      // Descend into nested lists
      for (SEXP element : list) {
        if (element instanceof ListVector) {
          element.accept(this);
        }
      }
    }
  }


  @Override
  public void visit(StringVector vector) {
    if(ExamplesParser.getTag(vector).equals("RCODE")) {
      for (String line : vector) {
        code.append(line);
      }
    }
  }

  @Override
  public String toString() {
    return code.toString();
  }
}
