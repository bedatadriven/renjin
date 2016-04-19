/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.parser;

public class LexerContextStack {
  private static final int SIZE = 50;

  public static final char IF_BLOCK = 'i';

  public static class OverflowException extends RuntimeException {

  }

  private char stack[] = new char[SIZE];
  private int currentIndex;


  void push(int i) {
    push((char) i);
  }

  void push(char c) {
    if (currentIndex >= SIZE) {
      throw new OverflowException();
    }
    ++currentIndex;
    stack[currentIndex] = c;
  }

  void pop() {
    stack[currentIndex] = 0;
    currentIndex--;
  }

  char peek() {
    return stack[currentIndex];
  }

  void ifPush() {
    if (peek() == Tokens.LBRACE ||
        peek() == '[' ||
        peek() == '(' ||
        peek() == IF_BLOCK) {
      push(IF_BLOCK);
    }
  }

  void ifPop() {
    if (peek() == IF_BLOCK) {
      stack[currentIndex] = 0;
      currentIndex--;
    }
  }
}