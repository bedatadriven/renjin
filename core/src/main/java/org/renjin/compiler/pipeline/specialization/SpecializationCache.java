/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.compiler.pipeline.specialization;

import org.renjin.compiler.pipeline.fusion.kernel.CompiledKernel;
import org.renjin.compiler.pipeline.node.DeferredNode;
import org.renjin.repackaged.guava.util.concurrent.SettableFuture;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Maintains a cache of recently used JITted classes.
 */
public class SpecializationCache {

  public static final SpecializationCache INSTANCE = new SpecializationCache();

  private final ConcurrentHashMap<SpecializationKey, Future<CompiledKernel>> cache;

  private SpecializationCache() {
    cache = new ConcurrentHashMap<SpecializationKey, Future<CompiledKernel>>();
  }

  public CompiledKernel compile(DeferredNode node) {
    SpecializationKey key = node.jitKey();
    try {

      Future<CompiledKernel> existingSpecialization = cache.get(key);
      if(existingSpecialization != null) {
        return existingSpecialization.get();
      }
      // Immediately set the Future so that other threads that need this specialization
      // wait for this compilation to finish before starting compilation on their own
      SettableFuture<CompiledKernel> newlyCompiledSpecialization = SettableFuture.create();
      existingSpecialization = cache.putIfAbsent(key, newlyCompiledSpecialization);
      if (existingSpecialization != null) {
        return existingSpecialization.get();
      }

      throw new UnsupportedOperationException();
//      
//      //
//      LoopKernelCompiler jitter = new LoopKernelCompiler();
//      newlyCompiledSpecialization.set(jitter.compile(node));
//
//      return newlyCompiledSpecialization.get();
    } catch (Exception e) {
      throw new RuntimeException("Failed to compile " + key, e);
    }
  }

}
