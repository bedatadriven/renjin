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
package org.renjin.compiler.ir.tac;

import org.renjin.compiler.ir.tac.expressions.ReadParam;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.*;
import java.util.Map.Entry;

public class IRBody {

  private Statement[] statements;
  private int labels[];
  
  private List<ReadParam> params = Collections.emptyList();
  
  public IRBody(List<Statement> statements, Map<IRLabel, Integer> labels) {
    this.statements = statements.toArray(new Statement[statements.size()]);
    this.labels = new int[labels.size()];
  
    Arrays.fill(this.labels, -1);
    
    for(Entry<IRLabel,Integer> label : labels.entrySet()) {
      this.labels[label.getKey().getIndex()] = label.getValue();
    }
  }


  public List<ReadParam> getParams() {
    return params;
  }
  
  public void setParams(List<ReadParam> params) {
    this.params = params;
  }

  public List<Statement> getStatements() {
    return Lists.newArrayList(statements);
  }

  public Set<IRLabel> getInstructionLabels(int instructionIndex) {
    Set<IRLabel> set = Sets.newHashSet();
    for(int i=0;i!=labels.length;++i) {
      if(labels[i]==instructionIndex) {
        set.add(new IRLabel(i));
      }
    }
    return set;
  }
 
  public boolean isLabeled(int instructionIndex) {
    for(int i=0;i!=labels.length;++i) {
      if(labels[i] == instructionIndex) {
        return true;
      }
    }
    return false;
  }
  
  private String labelAt(int instructionIndex) {
    Set<IRLabel> labels = getInstructionLabels(instructionIndex);
    
    return  labels.isEmpty() ? Strings.repeat(" ", 5) :
      Strings.padEnd(labels.iterator().next().toString(), 5, ' ');
  } 
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i!=statements.length;++i) {
      appendLineTo(sb, i);
    }
    return sb.toString();
  }

  public void appendLineTo(StringBuilder sb, int i) {
    sb.append(labelAt(i))
      .append(Strings.padEnd(i + ":", 4, ' '))
      .append(statements[i])
      .append("\n");
  }

}
