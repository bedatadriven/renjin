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
package org.renjin.compiler.cfg;

/**
 * Control flow edge from one basic block to another.
 */
public class FlowEdge {
  private final BasicBlock predecessor;
  private final BasicBlock successor;
  
  public FlowEdge(BasicBlock predecessor, BasicBlock successor) {
    this.predecessor = predecessor;
    this.successor = successor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    FlowEdge flowEdge = (FlowEdge) o;
    return flowEdge.predecessor == this.predecessor &&
           flowEdge.successor == this.successor;
  }

  public BasicBlock getPredecessor() {
    return predecessor;
  }

  public BasicBlock getSuccessor() {
    return successor;
  }


  @Override
  public int hashCode() {
    int result = predecessor.hashCode();
    result = 31 * result + successor.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return getPredecessor().getDebugId() + " -> " + getSuccessor().getDebugId();
  }
}
