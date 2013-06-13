package org.renjin.maven.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.renjin.parser.RdParser;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.SexpVisitor;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;

/**
 * Extracts the examples section from an .Rd file 
 *
 */
public class ExamplesParser extends SexpVisitor<String> {

  private StringBuilder code = new StringBuilder();
  
  @Override
  public void visit(ListVector list) {
    SEXP tag = list.getAttribute(Symbol.get("Rd_tag"));
    if(tag != Null.INSTANCE) {
      StringVector tagName = (StringVector)tag;
      if(tagName.getElementAsString(0).equals("\\examples")) {
        for(SEXP exp : list) {
          exp.accept(this);
        }
      }   
    }
    for(SEXP sexp : list) {
      if(sexp instanceof ListVector) {
        sexp.accept(this);
      }
    }
  }
  
  @Override
  public void visit(StringVector vector) {
    for(String line : vector) {
      code.append(line);
    }
  }

  @Override
  protected void unhandled(SEXP exp) {
    throw new UnsupportedOperationException(exp.toString());
  }
  
  /**
   * Parses the examples from an *.Rd file
   * 
   * @param file an *.Rd file
   * @return the text of the examples section
   * @throws IOException
   */
  public static String parseExamples(File file) throws IOException {
    FileReader reader = new FileReader(file);
    RdParser parser = new RdParser();
    SEXP rd = parser.R_ParseRd(reader, StringVector.valueOf(file.getName()), false);
    
    ExamplesParser examples = new ExamplesParser();
    rd.accept(examples);
     
    return examples.code.toString();
  }
}
