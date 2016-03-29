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

package org.renjin.primitives.text.regex;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * A "Fixed" regular expression that matches the pattern literally.
 */
public class FixedRE implements RE {

	private String pattern;
	private int matchStart;

	public FixedRE(String pattern) {
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
	public String subst(String substituteIn, String substitution) {
		return substituteIn.replace(pattern, substitution);
	}

	@Override
	public String subst(String substituteIn, String substitution, int flags) {
		if ((flags & ExtendedRE.REPLACE_FIRSTONLY) != 0) {
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
