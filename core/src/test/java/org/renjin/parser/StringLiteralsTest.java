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
package org.renjin.parser;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class StringLiteralsTest {

    @Test
    public void unicodeEscapes() {
        // Start of Heading should be escaped
        assertThat(StringLiterals.format("\u0001", "NA"), equalTo("\"\\u0001\""));
        // newlines should be escaped
        assertThat(StringLiterals.format("\n", "NA"), equalTo("\"\\n\""));
        // umlauts should NOT be escaped
        assertThat(StringLiterals.format("åäö", "NA"), equalTo("\"åäö\""));

        // Inverted Exclamation Mark should NOT be escaped
        assertThat(StringLiterals.format("\u00a1", "NA"), equalTo("\"\u00a1\""));
        // Latin Small Letter O with horn (ơ) should NOT be escaped
        assertThat(StringLiterals.format("\u01a1", "NA"), equalTo("\"\u01a1\""));
        // greek small letter omega with dasia and ypogegrammeni (ᾡ) i.e. U+1FA1 should NOT be escaped
        assertThat(StringLiterals.format("\u1fa1", "NA"), equalTo("\"\u1fa1\""));

        // Codepoint 149, Message Waiting, should be escaped
        assertThat(StringLiterals.format("\u0095", "NA"), equalTo("\"\\u0095\""));
    }
}