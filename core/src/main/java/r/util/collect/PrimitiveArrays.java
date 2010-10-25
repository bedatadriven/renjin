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

package r.util.collect;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

import java.util.*;

public class PrimitiveArrays {


  public static Iterator<Double> asUnmodifiableIterator(double values[]) {
    return new DoubleArrayIterator(values);
  }

  public static List<Double> asImmutableList(double values[]) {
    return new DoubleArrayList(values);
  }


  private static class DoubleArrayIterator extends UnmodifiableIterator<Double> {
    private int i = 0;
    private double[] values;

    public DoubleArrayIterator(double values[]) {
      this.values = values;
    }

    @Override
    public boolean hasNext() {
      return i < values.length;
    }

    @Override
    public Double next() {
      return values[i++];
    }
  }

  private static class DoubleArrayList implements List<Double> {
    private final double values[];

    private DoubleArrayList(double[] values) {
      this.values = values;
    }

    @Override
    public int size() {
      return values.length;
    }

    @Override
    public boolean isEmpty() {
      return values.length == 0;
    }

    @Override
    public boolean contains(Object o) {
      return Iterators.contains(iterator(), o);
    }

    @Override
    public Iterator<Double> iterator() {
      return new DoubleArrayIterator(values);
    }

    @Override
    public Object[] toArray() {
      Double boxed[] = new Double[values.length];
      for(int i=0;i!=values.length; ++i) {
        boxed[i] = values[i];
      }
      return boxed;
    }

    @Override
    public <T> T[] toArray(T[] a) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Double aDouble) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      for(Object o : c) {
        if(!contains(o)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean addAll(Collection<? extends Double> c) {
      return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Double> c) {
      return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean equals(Object o) {
      return false;
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(values);
    }

    @Override
    public Double get(int index) {
      return values[index];
    }

    @Override
    public Double set(int index, Double element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, Double element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Double remove(int index) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
      return Iterators.indexOf(iterator(), Predicates.equalTo(o));
    }

    @Override
    public int lastIndexOf(Object o) {
      return Lists.reverse(this).indexOf(o);
    }

    @Override
    public ListIterator<Double> listIterator() {
      return null;
    }

    @Override
    public ListIterator<Double> listIterator(int index) {
      return null;
    }

    @Override
    public List<Double> subList(int fromIndex, int toIndex) {
      return null;
    }
  }
}
