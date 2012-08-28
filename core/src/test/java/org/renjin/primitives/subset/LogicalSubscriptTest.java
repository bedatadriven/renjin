package org.renjin.primitives.subset;

public class LogicalSubscriptTest {
//
//  @Test
//  public void test() {
//
//    LogicalSubscript subscript = new LogicalSubscript(5, new LogicalArrayVector(true, false, false, true, false));
//    IndexIterator it = subscript.indexIterator();
//    assertTrue(it.hasNext());
//    assertThat(it.nextInt(), equalTo(0));
//    assertTrue(it.hasNext());
//    assertThat(it.nextInt(), equalTo(3));
//    assertFalse(it.hasNext());
//  }
//
//  @Test
//  public void repeating() {
//    LogicalSubscript subscript = new LogicalSubscript(3, new LogicalArrayVector(true));
//    IndexIterator it = subscript.indexIterator();
//    assertTrue(it.hasNext());
//    assertThat(it.nextInt(), equalTo(0));
//    assertTrue(it.hasNext());
//    assertThat(it.nextInt(), equalTo(1));
//    assertTrue(it.hasNext());
//    assertThat(it.nextInt(), equalTo(2));
//    assertFalse(it.hasNext());
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
