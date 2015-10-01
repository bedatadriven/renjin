package org.renjin.utils;

import com.google.common.collect.Lists;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.primitives.io.connections.Connection;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Implementation of write table
 */
public class WriteTable {


  public static void write(@Current Context context,
                           ListVector dataFrame,
                           SEXP connHandle,
                           int numRows, 
                           int numColumns,
                           Vector rowNames,
                           String sep,
                           String eol,
                           String na, 
                           String dec,
                           SEXP quote, 
                           SEXP quoteMethod) throws IOException {

    
    Connection connection = Connections.getConnection(context, connHandle);
    PrintWriter writer = connection.getPrintWriter();

    // Setup printers
    List<ColumnPrinter> printers = Lists.newArrayList();

    if(rowNames != Null.INSTANCE) {
      // rownames are quoted unless quote is FALSE
      printers.add(new StringPrinter(writer, (StringVector) rowNames, isColumnQuoted(quote, 0), na));
    }

    for (int i = 0; i < dataFrame.length(); i++) {
      
      SEXP column = dataFrame.getElementAsSEXP(i);
      
      if(column instanceof StringVector) {
        printers.add(new StringPrinter(writer, (StringVector)column, isColumnQuoted(quote, i), na));
        
      } else if(column instanceof IntVector) {
        if(column.inherits("factor")) {
          printers.add(new FactorPrinter(writer, (IntVector) column, isColumnQuoted(quote, i), na));
        } else {
          printers.add(new IntPrinter(writer, (IntVector) column, na));
        }
      } else if(column instanceof DoubleVector) {
        printers.add(new DoublePrinter(writer, (DoubleVector) column, dec, na));

      } else if(column instanceof LogicalVector) {
        printers.add(new LogicalPrinter(writer, (LogicalVector) column, na));
      } else {
        throw new EvalException("Unsupported column type " + column.getTypeName());
      }
    }
    
    for(int i=0;i!=numRows;++i) {
        for(int j=0;j!=printers.size();++j) {
          if(j > 0) {
            writer.print(sep);
          }
          printers.get(j).print(i);
        }
        writer.print(eol);
    }
  }
  
  private static boolean isColumnQuoted(SEXP quote, int index) {
    
    if(quote instanceof LogicalVector) {
      return ((LogicalVector) quote).getElementAsLogical(0) == Logical.TRUE;
    }
    
    if(quote instanceof IntVector) {
      int[] columns = ((IntVector) quote).toIntArray();
      for (int i = 0; i < columns.length; i++) {
        if(columns[i] == (index+1)) {
          return true;
        }
      }
    }
    return false;
  }
}
