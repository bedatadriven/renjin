/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.stats.internals.distributions;

import org.apache.commons.math.distribution.ContinuousDistribution;

/**
 * The uniform probability distribution.
 */
public class UniformDistribution implements ContinuousDistribution {
  private final double min;
  private final double max;
  private final double range;

  public UniformDistribution(double min, double max) {
    this.min = min;
    this.max = max;
    this.range = max - min;
  }

  @Override
  public double cumulativeProbability(double x)  {
    if(x < min || x > max) {
      return 0;
    }
    return (x-min)/ range;
  }

  public double density(double x) {
    if(x < min || x > max) {
      return 0;
    } else {
      return 1.0/range;
    }
  }

  @Override
  public double inverseCumulativeProbability(double p)  {
    return min + (p * range);
  }

  @Override
  public double cumulativeProbability(double x0, double x1)  {
    if(x0 < min) {
      x0 = min;
    }
    if(x0 > max) {
      x0 = max;
    }
    if(x1 < min) {
      x1 = min;
    }
    if(x1 > max) {
      x1 = max;
    }
    return (x1-x0)/range;
  }
}
