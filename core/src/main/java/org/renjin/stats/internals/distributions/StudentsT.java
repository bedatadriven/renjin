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
package org.renjin.stats.internals.distributions;


import org.renjin.eval.Session;


public class StudentsT {

  /*
   * if df is infinite then a normal random should be returned.
   * I don't know the way of getting whether df is infinite or not
   * so i am leaving it as is. Something needed:
   * 
   * if (df is infinite){
   * return(Normal.norm_rand());
   * }else{
   * return Normal.norm_rand() / Math.sqrt(ChiSquare.rchisq(df) / df);
   * }
   */
  public static double rt(Session context, double df) {
    if (Double.isNaN(df) || df <= 0.0) {
      return (Double.NaN);
    }

    return Normal.norm_rand(context) / Math.sqrt(ChiSquare.rchisq(context, df) / df);

  }

}
