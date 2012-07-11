package org.renjin.primitives.io.connections;

import com.google.common.base.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Specialized reader class to support R character streams
 * which have pushback support. We need this because
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
