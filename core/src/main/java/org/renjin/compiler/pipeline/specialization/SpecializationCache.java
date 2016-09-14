package org.renjin.compiler.pipeline.specialization;

import org.renjin.repackaged.guava.util.concurrent.SettableFuture;
import org.renjin.compiler.pipeline.DeferredNode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Maintains a cache of recently used JITted classes.
 */
public class SpecializationCache {

  public static final SpecializationCache INSTANCE = new SpecializationCache();

  private final ConcurrentHashMap<SpecializationKey, Future<SpecializedComputer>> cache;

  private SpecializationCache() {
    cache = new ConcurrentHashMap<SpecializationKey, Future<SpecializedComputer>>();
  }

  public SpecializedComputer compile(DeferredNode node) {
    SpecializationKey key = node.jitKey();
    try {

      Future<SpecializedComputer> existingSpecialization = cache.get(key);
      if(existingSpecialization != null) {
        return existingSpecialization.get();
      }
      // Immediately set the Future so that other threads that need this specialization
      // wait for this compilation to finish before starting compilation on their own
      SettableFuture<SpecializedComputer> newlyCompiledSpecialization = SettableFuture.create();
      existingSpecialization = cache.putIfAbsent(key, newlyCompiledSpecialization);
      if (existingSpecialization != null) {
        return existingSpecialization.get();
      }

      //
      JitSpecializer jitter = new JitSpecializer();
      newlyCompiledSpecialization.set(jitter.compile(node));

      return newlyCompiledSpecialization.get();
    } catch (Exception e) {
      throw new RuntimeException("Failed to compile " + key, e);
    }
  }

}
