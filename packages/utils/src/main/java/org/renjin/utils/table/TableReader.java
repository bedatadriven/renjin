package org.renjin.utils.table;

import org.apache.commons.vfs2.FileObject;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.sexp.*;

import java.io.*;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TableReader {
    
    private final TableOptions options;
    private final LineSplitter lineSplitter;
    
    private CharBuffer buffer;
    private LineScanner lineScanner;
    
    private int columnCount;
    private StringVector columnNames;
    private StringVector rowNames;
    
    private boolean firstLineContainsHeaders;
    private boolean firstColumnContainsHeaders;
    
    private List<String> firstLines;
    private List<ColumnBuffer> columns;
    private int numRows;

    public TableReader() {
        this(new TableOptions());
    }

    public TableReader(TableOptions options) {
        this.options = options;
        this.lineSplitter = options.createSplitter();
    }

    public ListVector read(@Current Context context, String filename) throws IOException {
        FileObject fileObject = context.resolveFile(filename);
        InputStreamReader reader = new InputStreamReader(fileObject.getContent().getInputStream());
        try {
            return read(reader);
        } finally {
            reader.close();
        }
    }
    
    public ListVector readString(String text) throws IOException {
        return read(new StringReader(text));
    }
    
    private ListVector read(Reader reader) throws IOException {
        lineScanner = new LineScanner(options, reader);
        firstLines = lineScanner.peek(5);

        resolveColumnCount();
        resolveHeaderRow();
        resolveHeaderColumn();
        resolveColumnNames();
        readRows();
        return buildDataFrame();
    }

    /**
     * Determine how many columns are present in the file based on the first 
     * few lines
     */
    private void resolveColumnCount() {
        this.columnCount = countColumns(firstLines);
    }


    /**
     * Determines whether the first line of the file is a header row
     */
    private void resolveHeaderRow() {
        if(options.autoDetectHeader) {
            /*
             * The heuristic used in GNU R's implementation considers a first line to contain headers if
             * it has exactly one less column than the rest of the rows. That is, it looks like:
             * 
             *      a   b   c
             * 1   10  20  30
             * 2   40  50  60
             * 
             */
            firstLineContainsHeaders = countColumns(firstLines.get(0)) == (columnCount - 1);

        } else {
            firstLineContainsHeaders = options.header;
        }
    }

    /**
     * Determines whether the first column contains row headers
     */
    private void resolveHeaderColumn() {
        int headerColumnCount;
        if(options.colNames != Null.INSTANCE) {
            headerColumnCount = options.colNames.length();
        } else {
            headerColumnCount = countColumns(firstLines.get(0));
        }
        
        firstColumnContainsHeaders = (headerColumnCount == (columnCount - 1));
    }


    /**
     * Resolves the names of the column, using, in order or precedence:
     * <ol>
     *     <li>{@code col.names}, if provided</li>
     *     <li>the first line, if it contains headers</li>
     *     <li>generated column names (V1, V2, V3, ...)</li>
     * </ol>
     */
    private void resolveColumnNames() {
        if(options.colNames instanceof StringVector) {
            columnNames = (StringVector) options.colNames;

        } else if(firstLineContainsHeaders) {
            columnNames = parseHeaders(firstLines.get(0));
       
        } else {
            StringVector.Builder names = new StringVector.Builder();
            for(int i=0;i<columnCount;++i) {
                names.add("V" + i);
            }
            columnNames = names.build();
        }
    }
    
    private StringVector parseHeaders(String line) {
        return new StringArrayVector(lineSplitter.split(line));
    }

    private int countColumns(String line) {
        return lineSplitter.split(line).size();
    }
    
    private int countColumns(Iterable<String> lines) {
        int count = 0;
        for(String line : lines) {
            count = Math.max(count, countColumns(line));
        }
        return count;
    }
    

    private void readRows() throws IOException {

        TypeDetector detector = new TypeDetector(options);
        
        ColumnBuffer[] buffers = new ColumnBuffer[columnCount];
        for(int i=0;i<columnCount;++i) {
            buffers[i] = new TypeDetectingColumnBuffer(options, detector);
        }
        
        if(firstLineContainsHeaders) {
            lineScanner.skipLine();
        }

        numRows = 0;
        String line;
        while( (line=lineScanner.readLine()) != null) {
            List<String> columnValues = lineSplitter.split(line);
            int columnsRead = columnValues.size();
            for(int i=0;i<columnCount;++i) {
                if(i < columnsRead) {
                    buffers[i].add(columnValues.get(i));
                } else {
                    buffers[i].addNA();
                }
            }
            numRows++;
        }
        columns = Arrays.asList(buffers);
    }


//    private void resolveRowNames() {
//        if (!options.explicitRowNames) {
//            // no explicit row names
//            if (firstColumnContainsHeaders) {
//                rowNames = columns.remove(0);
//            } else {
//                rowNames = new RowNamesVector(numRows);
//            }
//        } else {
//            // row names explicitly provided by caller
//            if (options.rowNames == Null.INSTANCE) {
//                // This forces the row names to 1:nrow even
//                // if there are row names present in the file
//                rowNames = new RowNamesVector(numRows);
//
//            } else if (options.rowNames.length() == 1) {
//                // name or index of the column containing row indexes
//                throw new UnsupportedOperationException();
//
//            } else if (options.rowNames instanceof StringVector) {
//                this.rowNames = checkRowNames((StringVector) options.rowNames);
//
//            } else if (options.rowNames.isNumeric()) {
//                this.rowNames = checkRowNames(new ConvertingStringVector((Vector) options.rowNames));
//
//            } else {
//                throw new EvalException("invalid row.names specification");
//            }
//        }
//    }
    

    private ListVector buildDataFrame() {

        ListVector.Builder list = new ListVector.Builder();
        if(!firstColumnContainsHeaders) {
            list.add(columns.get(0).build());
        } 
        for(int i=1;i<columns.size();++i) {
            list.add(columns.get(i).build());
        }

        list.setAttribute(Symbols.ROW_NAMES, rowNames());
        list.setAttribute(Symbols.NAMES, columnNames);
        list.setAttribute(Symbols.CLASS, StringArrayVector.valueOf("data.frame"));

        return list.build();
    }

    private StringVector rowNames() {
        if(firstColumnContainsHeaders) {
            return columns.get(0).buildStringVector();
        } else {
            return new RowNamesVector(numRows, AttributeMap.EMPTY);
        }
    }
}
