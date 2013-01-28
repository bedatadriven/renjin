package org.renjin.repl;

import jline.console.ConsoleReader;

import java.io.IOException;
import java.io.Reader;

public class JlineReader extends Reader {
  private ConsoleReader reader;
  private String currentLine = "";
  private int pos = 0;
  private boolean eof = false;

  public JlineReader(ConsoleReader reader) {
    this.reader = reader;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if(pos == currentLine.length()) {
      String line = reader.readLine();
      if(line == null) {
        eof = true;
        return -1;
      }
      currentLine = line + "\n";
      reader.setPrompt("+ ");
      pos = 0;
    }
    int nchars = Math.min(len, currentLine.length() - pos);
    currentLine.getChars(pos, pos + nchars, cbuf, off);
    pos += nchars;
    return nchars;
  }

  public boolean isEof() {
    return eof;
  }
  
  @Override
  public void close() throws IOException {
    
  }
}
