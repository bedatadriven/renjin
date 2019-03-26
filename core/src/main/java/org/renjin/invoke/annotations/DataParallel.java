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
package org.renjin.invoke.annotations;

import java.lang.annotation.Documented;
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
@Documented
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
