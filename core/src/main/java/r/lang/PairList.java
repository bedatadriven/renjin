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

package r.lang;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * Pairlists (LISTSXP, the name going back to the origins of R as a Scheme-like language) are
 *  rarely seen at R level, but are for example used for argument lists.
 *
 */
public interface PairList extends SEXP {
  public String TYPE_NAME = "pairlist";

  <S extends SEXP> S getElementAsSEXP(int i);
  Iterable<Node> nodes();
  Iterable<SEXP> values();

  /**
   * @return this {@code PairList} as a {@code Vector}, either {@code Null.INSTANCE} for an empty pairlist
   * or a {@code ListVector}
   */
  Vector toVector();

  /**
   * @return this expression's tag
   * @throws ClassCastException if this expression's tag is {@link Null#INSTANCE}
   */
  SEXP getRawTag();

  Symbol getTag();

  boolean hasTag();

  void setTag(SEXP tag);

  /**
   * Finds the first value associated with the given tag, or
   * {@code Null.INSTANCE} if no such element exists.
   *
   * @param tag the tag for which to search
   * @return  the first value, or {@code Null.INSTANCE} if no
   * such element exists.
   */
  SEXP findByTag(Symbol tag);

  PairList clone();

  public class Node extends AbstractSEXP implements Recursive, PairList, NamedValue, HasNamedValues {


    /**
     * The actual data for this node, .e.g {@code CAR} in
     * the C implementation
     */
    private SEXP tag = Null.INSTANCE;
    protected SEXP value = Null.INSTANCE;

    /**
     * The next node in the linked list, i.e. {@code CDR} in the
     * C implementation.
     */
    protected PairList nextNode = Null.INSTANCE;

    public Node(SEXP tag, SEXP value, PairList attributes, PairList nextNode) {
      super(attributes);
      this.tag = tag;
      this.value = value;
      if(nextNode instanceof Node) {
        this.nextNode = (Node) nextNode;
      }
    }

    public Node(SEXP tag, SEXP value, PairList nextNode) {
      super(Null.INSTANCE);
      this.tag = tag;
      this.value = value;
      if(nextNode instanceof Node) {
       this.nextNode = (Node) nextNode;
      }
    }

    public Node(SEXP value, PairList nextNode) {
      this(Null.INSTANCE, value, nextNode);
    }

    @Override
    public String getTypeName() {
      return TYPE_NAME;
    }

    /**
     * @return the next node in this linked list
     * @throws IllegalStateException if there is no next node
     */
    public Node getNextNode() {
      if(!(nextNode instanceof PairList.Node)) {
        throw new IllegalStateException("no next node. call hasNextNode() first or use getNext()");
      }
      return (PairList.Node)nextNode;
    }
    
    public PairList getNext() {
      return nextNode;
    }

    public boolean hasNextNode() {
      return nextNode != Null.INSTANCE;
    }

    public boolean tagEquals(String name) {
      return hasTag() && getTag().getPrintName().equals(name);
    }

    public static PairList fromIterable(Iterable<? extends SEXP> values) {
      Iterator<? extends SEXP> it = values.iterator();

      if (!it.hasNext()) {
        return Null.INSTANCE;
      } else {
        Node head = new Node(it.next(), Null.INSTANCE);
        Node node = head;
        while (it.hasNext()) {
          node.nextNode = new Node(it.next(), Null.INSTANCE);
          node = (Node) node.nextNode;
        }
        return head;
      }
    }

    public static PairList fromArray(SEXP... values) {
      return fromIterable(Arrays.asList(values));
    }

    public final SEXP getValue() {
      return value;
    }

    @Override
    public boolean hasName() {
      return hasTag();
    }

    @Override
    public String getName() {
      return hasTag() ? getTag().getPrintName() : "";
    }

    @Override
    public AtomicVector getNames() {
      StringVector.Builder names = new StringVector.Builder();
      boolean hasNames = false;
      for(PairList.Node node : nodes()) {
        if (node.hasTag()) {
          names.add(node.getTag().getPrintName());
          hasNames = true;
        } else {
          names.add("");
        }
      }
      return hasNames ? names.build() : Null.INSTANCE;
    }

    public final void setValue(SEXP value) {
      this.value = value;
    }

    /**
     * @return this expression's tag
     * @throws ClassCastException if this expression's tag is NullExp
     */
    @Override
    public final SEXP getRawTag() {
      return tag;
    }

    @Override
    public final Symbol getTag() {
      return (Symbol)tag;
    }

    @Override
    public final boolean hasTag() {
      return tag != Null.INSTANCE;
    }

    @Override
    public void setTag(SEXP tag) {
      this.tag = tag;
    }

    public void setNextNode(Node nextNode) {
      this.nextNode = nextNode;
    }

    @Override
    public ListVector toVector() {
      ListVector.Builder builder = new ListVector.Builder();
      builder.copyAttributesFrom(this);
      for(Node node : nodes()) {
        if(node.hasTag()) {
          builder.add(node.getTag().getPrintName(), node.getValue());
        } else {
          builder.add(node.getValue());
        }
      }
      return builder.build();
    }

    public Iterator<SEXP> valueIterator() {
      return new ValueIterator(this);
    }

    public Iterable<SEXP> values() {
      return new Iterable<SEXP>() {
        @Override
        public Iterator<SEXP> iterator() {
          return new ValueIterator(Node.this);
        }
      };
    }

    @Override
    public final int length() {
      return Iterators.size(valueIterator());
    }

    @Override
    public <X extends SEXP> X getElementAsSEXP(int i) {
      return (X) Iterators.get(valueIterator(), i);
    }

    public Node getNode(int i) {
      return Iterators.get(nodeIterator(), i);
    }

    @Override
    public String getName(int index) {
      Node node = getNode(index);
      if(node.hasTag()) {
        return node.getTag().getPrintName();
      } else {
        return "";
      }
    }


    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Node node = (Node) o;

      if (nextNode != null ? !nextNode.equals(node.nextNode) : node.nextNode != null) return false;
      if (tag != null ? !tag.equals(node.tag) : node.tag != null) return false;
      if (value != null ? !value.equals(node.value) : node.value != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = tag != null ? tag.hashCode() : 0;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      result = 31 * result + (nextNode != null ? nextNode.hashCode() : 0);
      return result;
    }

    /**
     * @return a shallow clone of the ListExp from this point on
     */
    @Override
    public Node clone() {
      Builder builder = new Builder();
      for(Node node : nodes()) {
        builder.add(node.getRawTag(), node.getValue());
      }
      return builder.buildNode();
    }

    public String toString() {
      if (value == this) {
        // so-called "stretchy lists" used by the parser
        return "[ CAR=this, CDR=" + nextNode + "]";
      } else {
        StringBuilder sb = new StringBuilder("pairlist(");
        appendValuesTo(sb);
        sb.append(")");
        return sb.toString();
      }
    }

    public void appendValuesTo(StringBuilder sb) {
      for (Node node : nodes()) {
        if (node != Node.this) {
          sb.append(", ");
        }
        if (node.hasTag()) {
          sb.append(node.getRawTag()).append("=");
        }
        sb.append(node.getValue());
      }
    }
    
    public static Node singleton(Symbol tag, SEXP value) {
      return new Node(tag, value, Null.INSTANCE);
    }

    public static Node singleton(String tag, SEXP value) {
      return singleton(new Symbol(tag), value);
    }

    /**
     * Iterator that iterators over the {@code ListExp}'s values
     */
    private static class ValueIterator extends UnmodifiableIterator<SEXP> {

      private PairList next = Null.INSTANCE;

      private ValueIterator(Node next) {
        this.next = next;
      }

      @Override
      public boolean hasNext() {
        return next != Null.INSTANCE;
      }

      @Override
      public SEXP next() {
        SEXP value = ((PairList.Node)next).value;
        next = ((PairList.Node)next).nextNode;
        return value;
      }



    }

    private static class NodeIterator extends UnmodifiableIterator<Node> {
      private PairList next;

      private NodeIterator(Node next) {
        this.next = next;
      }

      @Override
      public boolean hasNext() {
        return next != Null.INSTANCE;
      }

      @Override
      public Node next() {
        Node value = (PairList.Node)next;
        next = value.getNext();
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
    public Iterable<Node> nodes() {
      return new Iterable<Node>() {
        @Override
        public Iterator<Node> iterator() {
          return nodeIterator();
        }
      };
    }

    @Override
    public Iterable<NamedValue> namedValues() {
      return (Iterable)nodes();
    }

    /**
     * Returns an iterator over the individual ListExp nodes in this list,
     * or an empty iterator if exp is the NilExp.

     * @param exp  A ListExp or null
     * @throws IllegalArgumentException if the exp is not of type ListExp or NillExp
     */
    public static Iterable<Node> listNodes(PairList exp) {
      if(exp instanceof Node) {
        return ((Node) exp).nodes();
      } else {
        return Collections.emptySet();
      }
    }

    private Iterator<Node> nodeIterator() {
      return new NodeIterator(this);
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static Builder buildList(Symbol tag, SEXP value) {
      return new Builder().add(tag, value);
    }

    @Override
    public EvalResult evaluate(Context context, Environment rho) {
      return new EvalResult(this);
    }

    @Override
    public void accept(SexpVisitor visitor) {
      visitor.visit(this);
    }

    @Override
    public SEXP findByTag(Symbol symbol) {
      for(Node node : nodes()) {
        if(node.hasTag() && node.getTag().equals(symbol)) {
          return node.getValue();
        }
      }
      return Null.INSTANCE;
    }

  }

  public class Builder {
    private Node head = null;
    private Node tail;

    public Builder() {
    }

    public Builder add(SEXP tag, SEXP s) {
      if (head == null) {
        head = new Node(tag, s, Null.INSTANCE);
        tail = head;
      } else {
        Node next = new Node(tag, s, Null.INSTANCE);
        tail.nextNode = next;
        tail = next;
      }
      return this;
    }

    public Builder addAll(PairList list) {
      for(Node node : list.nodes()) {
        add(node.getRawTag(), node.getValue());
      }
      return this;
    }

    public Builder addAll(ListVector list) {
      for(NamedValue namedValue : list.namedValues()) {
        add(namedValue.getName(), namedValue.getValue());
      }
      return this;
    }

    public Builder add(SEXP s) {
      return add(Null.INSTANCE, s);
    }

    public Builder add(String name, SEXP value) {
      SEXP tag = Null.INSTANCE;
      if(!name.isEmpty()) {
        tag = new Symbol(name);
      }
      return add(tag, value);
    }

    public PairList build() {
      if(head == null) {
        return Null.INSTANCE;
      } else {
        return head;
      }
    }

    Node buildNode() {
      if(head == null) {
        throw new IllegalStateException("no SEXPs have been added");
      }
      return head;
    }
  }

  abstract class Predicates {

    public static Predicate<Node> hasTag() {
      return new Predicate<Node>() {
        @Override
        public boolean apply(Node listExp) {
          return listExp.hasTag();
        }
      };
    }

    public static Predicate<Node> matches(final String name) {
      return new Predicate<Node>() {
        @Override
        public boolean apply(Node input) {
          if(input.getRawTag() instanceof Symbol) {
            return ((Symbol) input.getRawTag()).getPrintName().equals(name);
          } else {
            return false;
          }
        }
      };
    }

    public static Predicate<Node> matches(SEXP tag) {
      assert tag instanceof Symbol;
      return matches( ((Symbol) tag).getPrintName() );
    }

    public static Predicate<Node> startsWith(final Symbol name) {
      return new Predicate<Node>() {
        @Override
        public boolean apply(Node input) {
          return input.hasTag() && input.getTag().getPrintName().startsWith(name.getPrintName());
        }
      };
    }
  }
}