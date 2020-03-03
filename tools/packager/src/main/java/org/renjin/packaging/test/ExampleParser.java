package org.renjin.packaging.test;

import org.renjin.eval.EvalException;
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
    } else if(tag.equals("\\dontrun") || tag.equals("\\donttest")) {
      // ignore...
    } else if(tag.equals("\\dontshow")) {
      for (SEXP element : list) {
        element.accept(this);
      }
    } else {
      throw new EvalException("Unknown tag " + tag);
    }
  }


  @Override
  public void visit(StringVector vector) {
    String tag = ExamplesParser.getTag(vector);
    if(tag.equals("RCODE")) {
      for (String line : vector) {
        code.append(line);
      }
    } else if(tag.equals("COMMENT")) {
      // ignore
    } else {
      throw new EvalException("Unknown tag " + tag);
    }
  }

  @Override
  public String toString() {
    return code.toString();
  }
}
