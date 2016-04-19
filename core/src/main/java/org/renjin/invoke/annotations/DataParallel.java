package org.renjin.invoke.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Indicates that this Java method is a data parallel
 * and should be applied to all elements of an R vector. The Java method
 * should be written to operate on a single element, for example:
 *
 * <code>
 *   @DataParallel
 *   public static double plus(double x, double y) {
 *     return x + y;
 *   }
 * </code>
 *
 * In this way it can be flexibly applied depending on the situation, for example it
 * may be:
 * <ul>
 *   <li>Applied serially to all elements of a vector in a generated for-loop</li>
 *   <li>Applied in parallel to blocks of a vector</li>
 *   <li>Translated to OpenCL and applied on device</li>
 * </ul>
 *
 * <p>For methods with several arguments, shorter vectors will be "recycled"  to match
 * the length of the longest vector. </p>
 *
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DataParallel {

  /**
   * Determines whether this method accepts NA values. If {@code true}, the method
   * will be applied to all elements, including those with NA values. If {@code false}, any element
   * combinations that have one or more NA elements will evaluate to NA.
   */
  boolean passNA() default false;


  /**
   * Determines which attributes if any should be copied from the longest argument of the vector
   */
  PreserveAttributeStyle value() default PreserveAttributeStyle.STRUCTURAL;
}
