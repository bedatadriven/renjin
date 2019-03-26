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
package org.renjin.studio.console;

/**
	The interface for name completion.
*/
public interface NameCompletion {
	/**
		Return an array containing a string element of the maximum
		unambiguous namespace completion or, if there is no common prefix,
		return the list of ambiguous names.
		e.g.
			input: "java.l"
			output: [ "java.lang." ]
			input: "java.lang."
			output: [ "java.lang.Thread", "java.lang.Integer", ... ]

		Note: Alternatively, make a NameCompletionResult object someday...
	*/
  public String [] completeName( String part );

}