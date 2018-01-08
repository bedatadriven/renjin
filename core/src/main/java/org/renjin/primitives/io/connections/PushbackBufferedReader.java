/*
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
package org.renjin.primitives.io.connections;

import org.renjin.repackaged.guava.base.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Reader implementation with pushback <em>and</em> buffered line support for R character streams.
 *
 * <p>We need this because
 * the JRE's PusbackReader and BufferedReader do not 
 * play well together.
 */
public class PushbackBufferedReader extends Reader {

  private BufferedReader reader;
  private StringBuilder pushbackStack = new StringBuilder();
  
  
  public PushbackBufferedReader(Reader reader) {
    this.reader = new BufferedReader(reader);
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if(pushbackStack.length() == 0) {
      return reader.read(cbuf, off, len);
    } else {
      int toRead = Math.min( pushbackStack.length(), len);
      pushbackStack.getChars(0, toRead, cbuf, off);
      pushbackStack.delete(0, toRead);
      return toRead;
    } 
  }
  
  public void pushBack(String string) {
    pushbackStack.insert(0, string);
  }
  
  public String readLine() throws IOException {
    if(pushbackStack.length()==0) {
      return reader.readLine();
    } else {
      int newLine = nextNewline(pushbackStack);
      if(newLine == -1) {
        return popStack() + Strings.nullToEmpty(reader.readLine());
      } else {
        return popStack(newLine);
      }
    }
  }

  private int nextNewline(StringBuilder stack) {
    for(int i=0;i!=stack.length();++i) {
      if(stack.charAt(i) == '\n') {
        return i;
      }
    }
    return -1;
  }
  
  private String popStack() {
    String line = pushbackStack.toString();
    pushbackStack.setLength(0);
    return line;
  }
  
  private String popStack(int newLinePos) {
    int eol = newLinePos;
    if(newLinePos != 0 && pushbackStack.charAt(newLinePos-1) == '\r') {
      eol --;
    }
    String line = pushbackStack.substring(0, eol);
    pushbackStack.delete(0, newLinePos+1);
    return line;
  }

  public int countLinesPushedBack() {
    int count = 0;
    for(int i=0;i!=pushbackStack.length();++i) {
      if(pushbackStack.charAt(i) == '\n') {
        count ++;
      }
    }
    if(count == 0 && pushbackStack.length() > 0) {
      return 1;
    } else {
      return count;
    }
  }
}
