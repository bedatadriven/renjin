package org.renjin.cli;

import jline.console.ConsoleReader;

import java.io.IOException;
import java.io.Reader;

public class JlineReader extends Reader {
  private ConsoleReader reader;
  private String currentLine = "";
  private int pos = 0;

  public JlineReader(ConsoleReader reader) {
    this.reader = reader;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if(pos == currentLine.length()) {
      currentLine = reader.readLine() + "\n";
      reader.setPrompt("");
      pos = 0;
    }
    int nchars = Math.min(len, currentLine.length() - pos);
    currentLine.getChars(pos, pos + nchars, cbuf, off);
    pos += nchars;
    return nchars;
  }
  
  @Override
  public void close() throws IOException {
    
  }
}
