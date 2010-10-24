/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.lang;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import r.util.ArgChecker;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Linked list of SEXP values
 */
public class ListExp extends SEXP implements Iterable<SEXP>, NillOrListExp {

  public static final int TYPE_CODE = 2;
  public static final String TYPE_NAME = "pairlist";

  /**
   * The actual data for this node, .e.g {@code CAR} in
   * the C implementation
   */
  protected SEXP value = NilExp.INSTANCE;

  /**
   * The next node in the linked list, i.e. {@code CDR} in the
   * C implementation.
   * <p/>
   * Contrary to the C impl, {@code nextNode} is either a subclass
   * of {@code ListExp} or {@code null}
   */
  protected ListExp nextNode = null;


  public ListExp(SEXP value, ListExp nextNode) {
    this.value = value;
    this.nextNode = nextNode;
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  /**
   * @return the next node in this linked list
   * @throws IllegalStateException if there is no next node
   */
  public final ListExp getNextNode() {
    if (nextNode == null) {
      throw new IllegalStateException("this list has no nextNode. Call hasNextNode() to check first.");
    }
    return nextNode;
  }

  public final boolean hasNextNode() {
    return nextNode != null;
  }

  public void removeNextNode() {
    nextNode = null;
  }


  public static ListExp fromIterable(Iterable<? extends SEXP> values) {
    Iterator<? extends SEXP> it = values.iterator();

    if (!it.hasNext()) {
      throw new IllegalArgumentException("Cannot create a zero-length list");
    }
    ListExp head = new ListExp(it.next(), null);
    ListExp node = head;
    while (it.hasNext()) {
      node.nextNode = new ListExp(it.next(), null);
      node = (ListExp) node.nextNode;
    }
    return head;
  }

  public static ListExp fromArray(SEXP... values) {
    return fromIterable(Arrays.asList(values));
  }

  public final SEXP getValue() {
    return value;
  }

  public final void setValue(SEXP value) {
    this.value = value;
  }

  @Override
  public SEXP getAttribute(String name) {
    /* pre-test to avoid expensive operations if clearly not needed -- LT */

    return NilExp.INSTANCE;
  }

  public void setNextNode(ListExp nextNode) {
    ArgChecker.notNull(nextNode);
    this.nextNode = nextNode;
  }

  @Override
  public Iterator<SEXP> iterator() {
    return new ValueIterator(this);
  }

  /**
   * Creates an iterator for the {@code ListExp}, or returns the
   * empty iterator if exp is {@code null}
   *
   * @param exp  a {@code ListExp}, or {@code null}
   * @return  an iterator
   */
  public static Iterator<SEXP> iterator(ListExp exp) {
    if(exp == null) {
      return Iterators.emptyIterator();
    } else {
      return exp.iterator();
    }
  }

  /**
   * Returns an (immutable) list of all {@code ListExp} nodes tagged
   * with the given {@code SymbolExp}
   */
  public List<ListExp> findAllNodesTaggedWith(SymbolExp symbol) {
    return new ImmutableList.Builder<ListExp>()
        .addAll(Iterators.filter(nodeIterator(), new Tagged(symbol)))
        .build();
  }

  @Override
  public final int length() {
    return Iterators.size(iterator());
  }

  @Override
  public SEXP subset(int from, int to) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  public static SEXP ofLength(int length) {
    return ListExp.fromIterable(Iterables.limit(Iterables.cycle(NilExp.INSTANCE), length));
  }

  public <X extends SEXP> X get(int i) {
    return (X) Iterators.get(iterator(), i);
  }

  public ListExp getNode(int i) {
    return Iterators.get(nodeIterator(), i);
  }

  public String toString() {
    if (value == this) {
      return "[ CAR=this, CDR=" + nextNode + "]";
    } else {
      StringBuilder sb = new StringBuilder("[");
      for (ListExp node : listNodes()) {
        if (node != ListExp.this) {
          sb.append(", ");
        }
        if (node.hasTag()) {
          sb.append(node.getTag()).append("=");
        }
        sb.append(node.getValue());
      }
      return sb.append("]").toString();
    }
  }

  public SEXP getFirst() {
    return value;
  }

  public SEXP getSecond() {
    return nextNode.value;
  }

  public SEXP getThird() {
    return nextNode.nextNode.value;
  }

  /**
   * Iterator that iterators over the {@code ListExp}'s values
   */
  private static class ValueIterator extends UnmodifiableIterator<SEXP> {

    private ListExp next;

    private ValueIterator(ListExp next) {
      this.next = next;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public SEXP next() {
      SEXP value = next.value;
      next = next.nextNode;
      return value;
    }
  }

  public Iterable<SEXP> values() {
    return new Iterable<SEXP>() {
      @Override
      public Iterator<SEXP> iterator() {
        return new ValueIterator(ListExp.this);
      }
    };
  }

  private static class NodeIterator extends UnmodifiableIterator<ListExp> {
    private ListExp next;

    private NodeIterator(ListExp next) {
      this.next = next;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public ListExp next() {
      ListExp value = next;
      next = next.nextNode;
      return value;
    }
  }

  /**
   * Creates an {@code Iterable} of the succession of {@code ListExp}s.
   *
   * For a {@code ListExp} {@code L} with three nodes, the sequence will include
   * {@code L}, {@code L.nextNode}, and {@code L.nextNode.nextNode}.
   * 
   *
   * @return an {@code Iterable} of the sucession of {@code ListExp}s.
   */
  public Iterable<ListExp> listNodes() {
    return new Iterable<ListExp>() {
      @Override
      public Iterator<ListExp> iterator() {
        return nodeIterator();
      }
    };
  }

  /**
   * Returns an iterator over the individual ListExp nodes in this list,
   * or an empty iterator if exp is the NilExp.

   * @param exp  A ListExp or null
   * @throws IllegalArgumentException if the exp is not of type ListExp or NillExp
   */
  public static Iterable<ListExp> listNodes(NillOrListExp exp) {
    if(exp instanceof ListExp) {
      return ((ListExp) exp).listNodes();
    } else {
      return Collections.emptySet();
    }
  }

  private Iterator<ListExp> nodeIterator() {
    return new NodeIterator(this);
  }

  private class Tagged implements Predicate<ListExp> {
    private SymbolExp symbolToMatch;

    private Tagged(SymbolExp symbolToMatch) {
      this.symbolToMatch = symbolToMatch;
    }

    @Override
    public boolean apply(ListExp listExp) {
      return symbolToMatch.equals(listExp.getTag());
    }
  }


  public static class Builder {
    private ListExp head = null;
    private ListExp tail;

    public Builder() {
    }

    public void add(SEXP s) {
      if (head == null) {
        head = new ListExp(s, null);
        tail = head;
      } else {
        ListExp next = new ListExp(s, null);
        tail.nextNode = next;
        tail = next;
      }
    }

    public ListExp list() {
      Preconditions.checkState(head != null, "ListExp cannot be empty");

      return head;
    }
  }

  @Override
  public SEXP evaluate(EnvExp rho) {
    Builder builder = new Builder();

    return builder.list();
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  public abstract static class Predicates {
    
    public static Predicate<ListExp> hasTag() {
      return new Predicate<ListExp>() {
        @Override
        public boolean apply(ListExp listExp) {
          return listExp.hasTag();
        }
      };
    }

  }

}