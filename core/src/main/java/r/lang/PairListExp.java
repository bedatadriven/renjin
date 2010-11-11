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
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import r.util.ArgChecker;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * Pairlists (LISTSXP, the name going back to the origins of R as a Scheme-like language) are
 *  rarely seen at R level, but are for example used for argument lists.
 *
 */
public class PairListExp extends SEXP implements RecursiveExp, Iterable<SEXP>, PairList {

  public static final int TYPE_CODE = 2;
  public static final String TYPE_NAME = "pairlist";

  /**
   * The actual data for this node, .e.g {@code CAR} in
   * the C implementation
   */
  protected SEXP value = NullExp.INSTANCE;

  /**
   * The next node in the linked list, i.e. {@code CDR} in the
   * C implementation.
   * <p/>
   * Contrary to the C impl, {@code nextNode} is either a subclass
   * of {@code ListExp} or {@code null}
   */
  protected PairListExp nextNode = null;


  public PairListExp(SEXP value, PairList nextNode) {
    this.value = value;
    if(nextNode instanceof PairListExp) {
     this.nextNode = (PairListExp) nextNode;
    }
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
  public final PairListExp getNextNode() {
    if (nextNode == null) {
      throw new IllegalStateException("this list has no nextNode. Call hasNextNode() to check first.");
    }
    return nextNode;
  }

  public final boolean hasNextNode() {
    return nextNode != null;
  }

  public static PairListExp fromIterable(Iterable<? extends SEXP> values) {
    Iterator<? extends SEXP> it = values.iterator();

    if (!it.hasNext()) {
      throw new IllegalArgumentException("Cannot create a zero-length list");
    }
    PairListExp head = new PairListExp(it.next(), null);
    PairListExp node = head;
    while (it.hasNext()) {
      node.nextNode = new PairListExp(it.next(), null);
      node = (PairListExp) node.nextNode;
    }
    return head;
  }

  public static PairListExp fromArray(SEXP... values) {
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
    return NullExp.INSTANCE;
  }

  public void setNextNode(PairListExp nextNode) {
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
  public static Iterator<SEXP> iterator(PairList exp) {
    if(exp == null || exp == NullExp.INSTANCE)  {
      return Iterators.emptyIterator();
    } else {
      return exp.iterator();
    }
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
    return PairListExp.fromIterable(Iterables.limit(Iterables.cycle(NullExp.INSTANCE), length));
  }

  public <X extends SEXP> X get(int i) {
    return (X) Iterators.get(iterator(), i);
  }

  public PairListExp getNode(int i) {
    return Iterators.get(nodeIterator(), i);
  }

  /**
   * @return a shallow clone of the ListExp from this point on
   */
  @Override
  public PairListExp clone() {
    Builder builder = new Builder();
    for(PairListExp node : listNodes()) {
      builder.add(node.getValue()).taggedWith(node.getRawTag());
    }
    return builder.list();
  }

  public String toString() {
    if (value == this) {
      return "[ CAR=this, CDR=" + nextNode + "]";
    } else {
      StringBuilder sb = new StringBuilder("[");
      for (PairListExp node : listNodes()) {
        if (node != PairListExp.this) {
          sb.append(", ");
        }
        if (node.hasTag()) {
          sb.append(node.getRawTag()).append("=");
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

    private PairListExp next;

    private ValueIterator(PairListExp next) {
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
        return new ValueIterator(PairListExp.this);
      }
    };
  }

  private static class NodeIterator extends UnmodifiableIterator<PairListExp> {
    private PairListExp next;

    private NodeIterator(PairListExp next) {
      this.next = next;
    }

    @Override
    public boolean hasNext() {
      return next != null;
    }

    @Override
    public PairListExp next() {
      PairListExp value = next;
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
   * @return an {@code Iterable} of the succession of {@code ListExp}s.
   */
  public Iterable<PairListExp> listNodes() {
    return new Iterable<PairListExp>() {
      @Override
      public Iterator<PairListExp> iterator() {
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
  public static Iterable<PairListExp> listNodes(PairList exp) {
    if(exp instanceof PairListExp) {
      return ((PairListExp) exp).listNodes();
    } else {
      return Collections.emptySet();
    }
  }

  private Iterator<PairListExp> nodeIterator() {
    return new NodeIterator(this);
  }

  public static Builder buildList() {
    return new Builder();
  }

  public static Builder buildList(SymbolExp tag, SEXP value) {
    return new Builder().add(value).taggedWith(tag);  
  }

  public static Builder buildList(SEXP value) {
    return new Builder().add(value);
  }

  public static class Builder {
    private PairListExp head = null;
    private PairListExp tail;

    public Builder() {
    }

    public Builder add(SEXP s) {
      if (head == null) {
        head = new PairListExp(s, null);
        tail = head;
      } else {
        PairListExp next = new PairListExp(s, null);
        tail.nextNode = next;
        tail = next;
      }
      return this;
    }

    public Builder taggedWith(SEXP tag) {
      tail.setTag(tag);
      return this;
    }

    public PairListExp list() {
      Preconditions.checkState(head != null, "ListExp cannot be empty");
      return head;
    }
  }

  @Override
  public EvalResult evaluate(EnvExp rho) {
    return new EvalResult(this);
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public SEXP findByTag(SymbolExp symbol) {
    for(PairListExp node : listNodes()) {
      if(node.hasTag() && node.getTag().equals(symbol)) {
        return node.getValue();
      }
    }
    return NullExp.INSTANCE;

  }

  public abstract static class Predicates {
    
    public static Predicate<PairListExp> hasTag() {
      return new Predicate<PairListExp>() {
        @Override
        public boolean apply(PairListExp listExp) {
          return listExp.hasTag();
        }
      };
    }

    public static Predicate<PairListExp> matches(final String name) {
      return new Predicate<PairListExp>() {
        @Override
        public boolean apply(PairListExp input) {
          if(input.getRawTag() instanceof SymbolExp) {
            return ((SymbolExp) input.getRawTag()).getPrintName().equals(name);
          } else {
            return false;
          }
        }
      };
    }

    public static Predicate<PairListExp> matches(SEXP tag) {
      assert tag instanceof SymbolExp;
      return matches( ((SymbolExp) tag).getPrintName() );
    }

    public static Predicate<PairListExp> startsWith(final SymbolExp name) {
      return new Predicate<PairListExp>() {
        @Override
        public boolean apply(PairListExp input) {
          return input.hasTag() && input.getTag().getPrintName().startsWith(name.getPrintName());
        }
      };
    }
  }


}
