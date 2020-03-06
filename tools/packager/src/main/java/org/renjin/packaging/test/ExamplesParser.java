/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */
package org.renjin.packaging.test;

import org.renjin.parser.RdParser;
import org.renjin.sexp.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Extracts the examples section from an .Rd file 
 *
 */
public class ExamplesParser extends SexpVisitor<String> {

  private static final Symbol RD_TAG = Symbol.get("Rd_tag");

  private ExampleParser example = new ExampleParser();

  @Override
  public void visit(ListVector list) {
    String tag = getTag(list);
    if(tag.equals("\\examples")) {
      for(SEXP element : list) {
        element.accept(example);
      }
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
  }

  static String getTag(SEXP sexp) {
    SEXP tagObject = sexp.getAttribute(RD_TAG);
    if(tagObject instanceof StringVector && tagObject.length() == 1) {
      String tagName = ((StringVector) tagObject).getElementAsString(0);
      if(!StringVector.isNA(tagName)) {
        return tagName;
      }
    }
    return "";
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
    try(FileReader reader = new FileReader(file)) {
      RdParser parser = new RdParser();
      SEXP rd = parser.R_ParseRd(reader, StringVector.valueOf(file.getName()), false);

      ExamplesParser examples = new ExamplesParser();
      rd.accept(examples);

      return examples.example.toString();
    } catch(Exception e) {
      System.err.println("WARNING: Failed to parse examples from " + file.getName() + ": " + e.getMessage());
      return "";
    }
  }
}
