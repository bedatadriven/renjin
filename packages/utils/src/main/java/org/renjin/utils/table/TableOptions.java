package org.renjin.utils.table;

import com.google.common.base.Preconditions;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;


public class TableOptions {


    boolean header = false;
    boolean autoDetectHeader = true;
    String fieldSep = "";
    String quote = "\"'";

    /**
     * The character used in the file for decimal points.
     */
    char dec = '.';
    
    boolean explicitRowNames = false;
    
    SEXP rowNames = Null.INSTANCE;
    
    SEXP colNames = Null.INSTANCE;
    boolean asIs;
    StringVector naStrings = StringVector.valueOf("NA");
    SEXP colClasses = StringVector.valueOf(StringVector.NA);
    
    
    boolean stripWhitespace;
    
    /**
     * the maximum number of rows to read in. 
     * Negative and other invalid values are ignored.
     */
    int rowLimit = Integer.MAX_VALUE;

    /**
     * the number of lines of the data file to skip before beginning to read data.
     */
    int skip;

    /**
     * If TRUE then the names of the variables in the data frame are checked to 
     * ensure that they are syntactically valid variable names. If necessary they 
     * are adjusted (by make.names) so that they are, and also to ensure that there 
     * are no duplicates.
     */
    boolean checkNames;

    /**
     * if TRUE blank lines in the input are ignored.
     */
    boolean blankLinesSkip = true;

    /**
     * . If TRUE then in case the rows have unequal length, 
     * blank fields are implicitly added.
     */
    boolean fill;


    /**
     *  character vector of length one containing a single character or an empty string. 
     *  Use "" to turn off the interpretation of comments altogether.
     */
    char commentChar = '#';

    /**
     *  Should C-style escapes such as \n be processed or read verbatim 
     *  (the default)? Note that if not within quotes these could be 
     *  interpreted as a delimiter (but not as a comment character).
     */
    boolean allowEscapes = false;

    /**
     * if TRUE, scan will flush to the end of the line after reading the last of the
     * fields requested. This allows putting comments after the last field.
     */
    boolean flush = false;
    boolean stringsAsFactors;
    String fileEncoding = "";
    
    public TableOptions() {
    }



    LineSplitter createSplitter() {
        char quoteChar = 0;
        if(quote.length() > 0) {
            quoteChar = quote.charAt(0);
        }
        if(fieldSep.length() == 0) {
            return new WhitespaceLineSplitter(quoteChar);
        } else {
            return new DelimitedSplitter(quoteChar, fieldSep.charAt(0));
        }
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public boolean isAutoDetectHeader() {
        return autoDetectHeader;
    }

    public void setAutoDetectHeader(boolean autoDetectHeader) {
        this.autoDetectHeader = autoDetectHeader;
    }

    public String getFieldSep() {
        return fieldSep;
    }

    public void setFieldSep(String fieldSep) {
        this.fieldSep = fieldSep;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getDec() {
        return String.valueOf(dec);
    }

    public void setDec(String dec) {
        if(dec.length() != 1) {
            throw new EvalException("Expected single character for the 'dec' argument");
        }
        this.dec = dec.charAt(0);
    }

    public boolean isExplicitRowNames() {
        return explicitRowNames;
    }

    public void setExplicitRowNames(boolean explicitRowNames) {
        this.explicitRowNames = explicitRowNames;
    }

    public SEXP getRowNames() {
        return rowNames;
    }

    public void setRowNames(SEXP rowNames) {
        this.rowNames = rowNames;
    }

    public SEXP getColNames() {
        return colNames;
    }

    public void setColNames(SEXP colNames) {
        this.colNames = colNames;
    }

    public boolean isAsIs() {
        return asIs;
    }

    public void setAsIs(boolean asIs) {
        this.asIs = asIs;
    }

    public StringVector getNaStrings() {
        return naStrings;
    }

    public void setNaStrings(StringVector naStrings) {
        this.naStrings = naStrings;
    }

    public SEXP getColClasses() {
        return colClasses;
    }

    public void setColClasses(SEXP colClasses) {
        this.colClasses = colClasses;
    }


    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public boolean isCheckNames() {
        return checkNames;
    }

    public void setCheckNames(boolean checkNames) {
        this.checkNames = checkNames;
    }

    public boolean isBlankLinesSkip() {
        return blankLinesSkip;
    }

    public void setBlankLinesSkip(boolean blankLinesSkip) {
        this.blankLinesSkip = blankLinesSkip;
    }

    public boolean isFill() {
        return fill;
    }

    public void setFill(boolean fill) {
        this.fill = fill;
    }

    public String getCommentChar() {
        if(commentChar == 0) {
            return "";
        } else {
            return String.valueOf(commentChar);
        }
    }

    public void setCommentChar(String commentChar) {
        if(commentChar.isEmpty()) {
            this.commentChar = 0;
        } else {
            Preconditions.checkArgument(commentChar.length() == 1, 
                    "commentChar must be a single character");
            this.commentChar = commentChar.charAt(0);
        }
    }

    public boolean isAllowEscapes() {
        return allowEscapes;
    }

    public void setAllowEscapes(boolean allowEscapes) {
        this.allowEscapes = allowEscapes;
    }

    public boolean isFlush() {
        return flush;
    }

    public void setFlush(boolean flush) {
        this.flush = flush;
    }

    public boolean isStringsAsFactors() {
        return stringsAsFactors;
    }

    public void setStringsAsFactors(boolean stringsAsFactors) {
        this.stringsAsFactors = stringsAsFactors;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    public int getRowLimit() {
        return rowLimit;
    }

    public void setRowLimit(int rowLimit) {
        if(rowLimit < 1) {
            this.rowLimit = Integer.MAX_VALUE;
        } else {
            this.rowLimit = rowLimit;
        }
    }

    public boolean isStripWhitespace() {
        return stripWhitespace;
    }

    public void setStripWhitespace(boolean stripWhitespace) {
        this.stripWhitespace = stripWhitespace;
    }
}
