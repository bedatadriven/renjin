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
package org.renjin.compiler.pipeline;

import org.renjin.repackaged.guava.cache.Cache;
import org.renjin.repackaged.guava.cache.CacheBuilder;

/**
 * Maintains a cache of recently used JITted classes.
 */
public class DeferredJitCache {

  public static final DeferredJitCache INSTANCE = new DeferredJitCache();

  private final Cache<JitKey, JittedComputation> cache;

  private DeferredJitCache() {
    cache = CacheBuilder.newBuilder()
            .softValues()
            .maximumSize(100)
            .build();
  }

  public JittedComputation compile(DeferredNode node) {
    JitKey key = node.jitKey();
    JittedComputation computation = cache.getIfPresent(key);
    if(computation != null) {
      return computation;
    }
    DeferredJitter jitter = new DeferredJitter();
    computation = jitter.compile(node);
    cache.put(key, computation);

    return computation;
  }
}
