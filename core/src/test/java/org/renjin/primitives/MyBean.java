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
package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MyBean {

  private String name = "fred";
  private int count;
  private List<String> children = Lists.newArrayList();

  private static final long LONG_VALUE = 214748364700L;

  public enum Membership {
    PENDING,
    ACTIVE,
    EXPIRED
  }
  
  private Membership membership = Membership.PENDING;
  
  public MyBean() {
    
  }
  
  public MyBean(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
  
  public Membership getMembershipStatus() {
    return membership;
  }
  
  public void setMembershipStatus(Membership membership) {
    this.membership = membership;
  }
  
  public List<String> getChildren() {
    return Arrays.asList("Bob", "Sue");
  }

  public String sayHello(String name) {
    return "Hello " + name;
  }
  
  public String sayHelloToEveryone(String[] args) {
    return "Hello " + Joiner.on(", ").join(args);
  }
  
  public String sayHello(int count) {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i!=count;++i) {
      sb.append("Hello");
    }
    return sb.toString();
  }
  
  // should be mapped as method, not property
  // because there is no getter
  public void setLocked(boolean locked) {
    
  }
  
  public DoubleVector compute() {
    return new DoubleArrayVector(1,2,3);
  }
  
  public List<MyChildBean> getChildBeans() {
    return Arrays.asList(new MyChildBean(), new MyChildBean());
  }
  
  public int intVarArg(String dummy, int... toSum) {
    int sum = 0;
    for(int x : toSum) {
      sum += x;
    }
    return sum;
  }
  
  public static class MyChildBean {
    public int getCount() {
      return 42;
    } 
  }

  public void methodWithContext(@Current Context context, String name) {
    java.lang.System.out.println(context);
  }
  
  public void overloadedWithVarArgs(String label, String... sources) {
  
  }

  public void overloadedWithVarArgs(String label, Collection<String> sources) {
  
  }

  public void overloadedWithVarArgs(Object mapping) {

  }
  
  public static double sum(double... values) {
    double sum = 0;
    for(int i=0;i!=values.length;++i) {
      sum += values[i];
    }
    return sum;
  }

  public static float sum32(float x, float y) {
    return x + y;
  }

  public static float sumArray32(float[] values) {
    float sum = 0;
    for (int i = 0; i < values.length; i++) {
      sum += values[i];
    }
    return sum;
  }

  public static long sumArrayLong(long[] values) {
    long sum = 0;
    for (int i = 0; i < values.length; i++) {
      sum += values[i];
    }
    return sum;
  }

  public static long calculateLong() {
    return LONG_VALUE;
  }

  public static void useLongValue(long value) {
    if(value != LONG_VALUE) {
      throw new AssertionError("long has been mangled by R");
    }
  }



}
