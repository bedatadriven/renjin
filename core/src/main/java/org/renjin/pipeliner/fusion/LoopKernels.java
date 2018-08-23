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
package org.renjin.pipeliner.fusion;

import org.renjin.pipeliner.fusion.kernel.ColSumKernel;
import org.renjin.pipeliner.fusion.kernel.LoopKernel;
import org.renjin.pipeliner.fusion.kernel.RowMeanKernel;
import org.renjin.pipeliner.fusion.kernel.SumMeanKernel;
import org.renjin.pipeliner.node.DeferredNode;
import org.renjin.pipeliner.node.FunctionNode;

import java.util.HashMap;
import java.util.Map;


public enum LoopKernels {

  INSTANCE;

  private Map<String, LoopKernel> map;

  LoopKernels() {
    map = new HashMap<>();
    if (System.getProperty("renjin.vp.disablejit") == null) {
      map.put("mean", SumMeanKernel.mean());
      map.put("sum", SumMeanKernel.sum());
      map.put("rowMeans", new RowMeanKernel());
      map.put("colSums", new ColSumKernel());
    } else {
      System.err.println("Specializers are disabled");
    }
  }

  public boolean supports(DeferredNode node) {
    if(node instanceof FunctionNode) {
      return map.containsKey(((FunctionNode) node).getComputationName());
    }
    return false;
  }

  public LoopKernel get(FunctionNode node) {
    return map.get(node.getComputationName());
  }
}
