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
package org.renjin.sexp;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SymbolTest {

  @Test
  public void hashBits() {
    print("<-");
    print("+");
    print("*");
    print(".Internal");
    print("if");
    print("sep");
    print("...");
    print("collapse");
    print("aardvark");
    print("AAAA");
    print("list");
    print("b");
    print("c");
    print("zoo");
    print(".completeToken");
    print("?");
    print("{");
    
    System.out.println(toBinaryString(8388736));
    System.out.println(toBinaryString(128));
 
  }
  
  @Test
  public void reservedWord() {
    assertTrue(Symbol.get("*").isReservedWord());
  }

  private void print(String name) {
    System.out.println(toBinaryString(Symbol.get(name).hashBit()) + " " + name);
  }

  private String toBinaryString(int hashBits) {
    String bits = Integer.toBinaryString(hashBits);
    StringBuilder display = new StringBuilder();
    for(int i=0;i!=32-bits.length();++i) {
      display.append('0');
    }
    display.append(bits);
    String b = display.toString().replace('0', '.');
    return b;
  }
  
}
