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

package r.base.regex;

import com.google.common.collect.Lists;

import java.util.List;

public class FixedRE implements RE {

  private String pattern;

  public FixedRE(String pattern) {
    this.pattern = pattern;
  }

  @Override
  public boolean match(String search) {
    return search.indexOf(pattern) != -1;
  }

  @Override
  public String subst(String substituteIn, String substitution) {
    throw new UnsupportedOperationException("subst not yet implemented for fixed RE");
  }

  @Override
  public String subst(String substituteIn, String substitution, int flags) {
    throw new UnsupportedOperationException("subst not yet implemented for fixed RE");
  }

  @Override
  public String[] split(String s) {
    List<String> splits = Lists.newArrayList();
    int i=0;
    int j;
    while(i < s.length() && (j=s.indexOf(pattern,i))!=-1) {
      splits.add(s.substring(i,j));
      i = j+pattern.length();
    }
    splits.add(s.substring(i));
    return splits.toArray(new String[splits.size()]);
  }
}
