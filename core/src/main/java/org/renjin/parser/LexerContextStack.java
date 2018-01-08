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