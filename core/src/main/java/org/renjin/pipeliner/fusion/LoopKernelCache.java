/**
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

import org.renjin.pipeliner.fusion.kernel.CompiledKernel;
import org.renjin.pipeliner.fusion.kernel.LoopKernel;
import org.renjin.pipeliner.fusion.node.LoopNode;
import org.renjin.repackaged.guava.cache.Cache;
import org.renjin.repackaged.guava.cache.CacheBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Caches loop kernels based on their shape.
 */
public class LoopKernelCache {


  private final Cache<String, Future<CompiledKernel>> cache;
  private ExecutorService executorService;

  public LoopKernelCache(ExecutorService executorService) {
    this.executorService = executorService;

    cache = CacheBuilder.newBuilder()
        .softValues()
        .maximumSize(100)
        .build();
  }

  public Future<CompiledKernel> get(LoopKernel kernel, LoopNode[] kernelOperands) {

    String key = kernelKey(kernel, kernelOperands);

    Future<CompiledKernel> compiledKernel = cache.getIfPresent(key);

    if(compiledKernel == null) {

      LoopKernelCompiler compiler = new LoopKernelCompiler(kernel, kernelOperands);
      compiledKernel = executorService.submit(compiler);

      cache.put(key, compiledKernel);
    }

    return compiledKernel;
  }

  private String kernelKey(LoopKernel kernel, LoopNode[] kernelOperands) {
    StringBuilder key = new StringBuilder();
    kernel.appendToKey(key);
    key.append(':');
    for (LoopNode kernelOperand : kernelOperands) {
      kernelOperand.appendToKey(key);
    }
    return key.toString();
  }


}
