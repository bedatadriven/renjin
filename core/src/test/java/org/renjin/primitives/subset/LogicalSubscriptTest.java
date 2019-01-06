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
package org.renjin.primitives.subset;

public class LogicalSubscriptTest {
//
//  @Test
//  public void test() {
//
//    LogicalSubscript subscript = new LogicalSubscript(5, new LogicalArrayVector(true, false, false, true, false));
//    IndexIterator it = subscript.indexIterator();
//    assertTrue(it.hasSuccessor());
//    assertThat(it.nextInt(), equalTo(0));
//    assertTrue(it.hasSuccessor());
//    assertThat(it.nextInt(), equalTo(3));
//    assertFalse(it.hasSuccessor());
//  }
//
//  @Test
//  public void repeating() {
//    LogicalSubscript subscript = new LogicalSubscript(3, new LogicalArrayVector(true));
//    IndexIterator it = subscript.indexIterator();
//    assertTrue(it.hasSuccessor());
//    assertThat(it.nextInt(), equalTo(0));
//    assertTrue(it.hasSuccessor());
//    assertThat(it.nextInt(), equalTo(1));
//    assertTrue(it.hasSuccessor());
//    assertThat(it.nextInt(), equalTo(2));
//    assertFalse(it.hasSuccessor());
//  }
//  @Test
//  public void test() {
//    while(true) {
//      long start = System.nanoTime();
//
//      double values[] = new double[25000000];
//      for(int i=0;i!=values.length;++i) {
//        values[i] = Math.sqrt(i)+1;
//      }
//
//      double sum = 0;
//      for(int i=0;i!=5000;++i) {
//        for(int j=0;j!=5000;++j) {
//          sum+= values[(j*5000)+i];
//        }
//      }
////      for(int i=0;i!=(5000*5000);++i) {
////        sum += values[i];
////      }
//      long timeElapsed = System.nanoTime() - start;
//      System.out.println(sum + " in " + timeElapsed/1000000d + "ms");
//    }
//  }
}
