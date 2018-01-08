/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.io;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.connections.PushbackBufferedReader;
import org.renjin.primitives.matrix.StringMatrixBuilder;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Primitives for reading Debian Control Files (DCF), which are used to store
 * the package descriptions and the license files.
 *
 */
public class DebianControlFiles {
  
  private static class Cell {

    private int row;
    private String field;
    
    public Cell(int row, String column) {
      super();
      this.row = row;
      this.field = column;
    }
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + field.hashCode();
      result = prime * result + row;
      return result;
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Cell other = (Cell) obj;
      return row == other.row && field.equals(other.field);
    } 
  }
  
  /**
   * Internal primitive used by the read.dcf() function in the base library.
   * 
   * @param conn
   * @param fields a list of fields to return or NULL for all fields 
   * @param keepWhiteSpace
   * @return
   * @throws IOException
   */
  @Internal
  public static SEXP readDCF(@Current Context context, SEXP conn, Vector requestedFields, boolean keepWhiteSpace) throws IOException {
    
    Map<Cell, String> cells = Maps.newHashMap();
    List<String> fields = Lists.newArrayList();
    
    // if specific fields are requested, the fields parameter
    // dictates the order
    boolean allFields = true;
    if(requestedFields instanceof StringVector) {
      Iterables.addAll(fields, (StringVector)requestedFields);
      allFields = false;
    }
    
    int rowIndex = 0;
    boolean lastLineWasBlank = false;
    String line;
    PushbackBufferedReader reader = Connections.getConnection(context, conn).getReader();
    Cell lastCell = null;
    while((line=reader.readLine())!=null) {
      boolean lineIsBlank = line.trim().length() == 0;
      if(lineIsBlank) {
        if(!lastLineWasBlank) {
          rowIndex++;
        }
        lastCell = null;
        
      } else if(isContinuation(line)) {
        if(lastCell == null) {
          throw new EvalException("Malformed DCF exception '%s'", line);
        } else {
          StringBuilder value = new StringBuilder();
          value.append(cells.get(lastCell));
          value.append(" ");
          value.append(line.trim());
          cells.put(lastCell, value.toString());
        }
      } else {
        String[] fieldValue = parseLine(line);
        String fieldName = fieldValue[0];
        
        boolean includeValue = false;
        if(fields.contains(fieldName)) {
          includeValue = true;
        } else if(allFields) {
          fields.add(fieldName);
          includeValue = true;
        }
        Cell cell = new Cell(rowIndex, fieldName);
        if(includeValue) {
          cells.put(cell, fieldValue[1]);
        }   
        lastCell = cell;
      }
      lastLineWasBlank = lineIsBlank;
    }
    int numRows = rowIndex;
    if(!lastLineWasBlank) {
      numRows++;
    } 
    return constructMatrix(numRows, cells, fields);
  }

  protected static SEXP constructMatrix(int numRows, Map<Cell, String> cells,
      List<String> fields) {
    StringMatrixBuilder matrix = new StringMatrixBuilder(numRows, fields.size());
    for(int row=0;row!=numRows;++row) {
      for(int col=0; col!=fields.size();++col) {
        String value = cells.get(new Cell(row, fields.get(col)));
        matrix.setValue(row, col, value);
      }
    }
    matrix.setColNames(fields);
    return matrix.build();
  }
  
  private static boolean isContinuation(String line) {
    return Character.isWhitespace(line.charAt(0));
  }

  private static String[] parseLine(String line) {
    int colon = line.indexOf(':');
    if(colon == -1) {
      throw new EvalException("Malformed DCF line: '" + line + "'");
    }
    String fieldName = line.substring(0, colon).trim();
    String value = line.substring(colon+1).trim();
    return new String[] { fieldName, value };
  }

}
