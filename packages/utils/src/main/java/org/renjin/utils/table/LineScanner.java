package org.renjin.utils.table;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.LineReader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Reads in lines according to the settings
 */
public class LineScanner {
    
    private final char commentChar;
    private final boolean skipBlankLines;
    private final int nrow;
    private final LineReader lineReader;
    private int lineCount = 0;
    
    private boolean eof;

    private Queue<String> peeked = new ArrayDeque<String>();

    public LineScanner(TableOptions options, Reader reader) throws IOException {
        Preconditions.checkNotNull(reader);
        this.nrow = options.rowLimit;
        this.commentChar = options.commentChar;
        this.skipBlankLines = options.blankLinesSkip;

        Preconditions.checkArgument(nrow > 0, "rowLimit > 0");
        
        this.lineReader = new LineReader(reader);
        
        // Skip the requested number of lines
        for(int i=0;i<options.skip;++i) {
            skipLine();
        }
    }
    
    /**
     * Reads the next line:
     * <ul>
     *     <li>ignoring blank lines if {@code blank.lines.skip} is {@code true},</li>
     *     <li>ignoring comment lines if a {@code comment.char} is specified</li>
     *     <li>respecting the limit {@code rowLimit} if provided</li>
     * </ul>
     * @return the next line, or {@code null} if the end of file has been reached
     * @throws IOException
     */
    public String readLine() throws IOException {
        if(!peeked.isEmpty()) {
            return peeked.poll();
        }
        return readNextLine();
    }


    public List<String> peek(int limit) throws IOException {
        List<String> lines = Lists.newArrayListWithCapacity(limit);
        while(lines.size() < limit) {
            String line = readNextLine();
            if(line == null) {
                break;
            }
            lines.add(line);
            peeked.add(line);
        }
        return lines;
    }

    private String readNextLine() throws IOException {
        /**
         * Stop if we have reached the limit of rows to read (nrows)
         */
        if(lineCount >= nrow) {
            return null;
        }
        while(true) {
            String line = lineReader.readLine();
            if(line == null) {
                return null;
            }
            /**
             * Skip commented lines
             */
            if(commentChar != 0 && line.length() > 0 && line.charAt(0) == commentChar) {
                continue;
            }
            /**
             * Skip blank lines if desired
             */
            if(skipBlankLines && line.length() == 0) {
                continue;
            }
            lineCount++;
            return line;
        }
    }


    public void skipLine() throws IOException {
        String line = readLine();
        if(line == null) {
            throw new IOException("End of file");
        }
    }
}
