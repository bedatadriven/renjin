package org.renjin.repl;

import jline.console.ConsoleReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

public class JlineReader extends Reader {
  private ConsoleReader reader;
  private String currentLine = "";
  private int pos = 0;
  private boolean eof = false;
  
  private boolean echo;
  private Writer echoOut = new PrintWriter(System.out);
  
  public JlineReader(ConsoleReader reader) {
    this.reader = reader;
  }

  public boolean isEcho() {
    return echo;
  }

  public void setEcho(boolean echo) {
    this.echo = echo;
  }
  
  public void setEchoOut(Writer echoOut) {
    this.echoOut = echoOut;
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

      if(echo) {
        echoOut.append(currentLine);
        echoOut.flush();
      }

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
  
  public boolean isEndOfLine() {
    return pos == currentLine.length();
  }
  
  @Override
  public void close() throws IOException {
    
  }
}
