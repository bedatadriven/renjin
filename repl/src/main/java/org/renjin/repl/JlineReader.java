/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
