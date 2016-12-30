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
package org.renjin.regexp;

import org.renjin.primitives.text.regex.ExtendedPattern;
import org.renjin.regexp.Pattern;
import org.renjin.repackaged.guava.collect.Lists;

import java.util.List;

/**
 * A "Fixed" regular expression that matches the pattern literally.
 */
public class FixedPattern implements Pattern {

	private String pattern;
	private int matchStart;

	public FixedPattern(String pattern) {
		assert pattern != null;
		assert pattern.length() > 0;
		this.pattern = pattern;
	}

	@Override
	public boolean match(String search) {
		int matchStart = search.indexOf(pattern);
		return matchStart != -1;
	}

  @Override
	public String subst(String substituteIn, String substitution, int flags) {
		if ((flags & ExtendedPattern.REPLACE_FIRSTONLY) != 0) {
			int io = substituteIn.indexOf(pattern);
			if (io < 0) {
				return substituteIn;
			}
			return substituteIn.substring(0, io) + substitution
					+ substituteIn.substring(io + pattern.length());
		} else {
			return substituteIn.replace(pattern, substitution);
		}
	}

	@Override
	public String[] split(String s) {
		List<String> splits = Lists.newArrayList();
		int i = 0;
		int j;
		while (i < s.length() && (j = nextMatch(s, i)) != -1) {
			splits.add(s.substring(i, j));
			i = j + pattern.length();
		}
		if(i<s.length()) {
			splits.add(s.substring(i));
		}
		return splits.toArray(new String[splits.size()]);
	}

	private int nextMatch(String s, int startingIndex) {
		return s.indexOf(pattern, startingIndex);
	}

	@Override
	public int getGroupStart(int groupIndex) {
		if (groupIndex != 0) {
			throw new IllegalArgumentException(
					"groupIndex out of bounds: fixed REs have no sub groups.");
		}
		return matchStart;
	}

	@Override
	public int getGroupEnd(int groupIndex) {
		int start = getGroupStart(groupIndex);
		if (start == -1) {
			return -1;
		} else {
			return start + pattern.length();
		}
	}
}
