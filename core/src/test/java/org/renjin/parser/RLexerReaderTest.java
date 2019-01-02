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

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class RLexerReaderTest {

  @Test
  public void positions() throws IOException {

    RLexerReader reader = new RLexerReader(new StringReader("foobar"));

    assertThat(reader.read(), equalTo((int)'f'));
    assertThat(reader.getCharacterIndex(), equalTo(0));
    assertThat(reader.getLineNumber(), equalTo(0));
    assertThat(reader.getColumnNumber(), equalTo(0));


    assertThat(reader.read(), equalTo((int)'o'));
    assertThat(reader.getCharacterIndex(), equalTo(1));
    assertThat(reader.getLineNumber(), equalTo(0));
    assertThat(reader.getColumnNumber(), equalTo(1));

    assertThat(reader.read(), equalTo((int)'o'));
    assertThat(reader.read(), equalTo((int)'b'));
    assertThat(reader.read(), equalTo((int)'a'));
    assertThat(reader.read(), equalTo((int)'r'));

    assertThat(reader.getCharacterIndex(), equalTo(5));
    assertThat(reader.getLineNumber(), equalTo(0));
    assertThat(reader.getColumnNumber(), equalTo(5));
  }

  @Test
  public void newLine() throws IOException {

    RLexerReader reader = new RLexerReader(new StringReader("1\n2\n"));

    assertThat(reader.read(), equalTo((int)'1'));
    assertThat(reader.getCharacterIndex(), equalTo(0));
    assertThat(reader.getLineNumber(), equalTo(0));
    assertThat(reader.getColumnNumber(), equalTo(0));

    assertThat(reader.read(), equalTo((int)'\n'));
    assertThat(reader.getCharacterIndex(), equalTo(-1));
    assertThat(reader.getLineNumber(), equalTo(1));
    assertThat(reader.getColumnNumber(), equalTo(-1));

    assertThat(reader.read(), equalTo((int)'2'));
    assertThat(reader.getCharacterIndex(), equalTo(0));
    assertThat(reader.getLineNumber(), equalTo(1));
    assertThat(reader.getColumnNumber(), equalTo(0));

    assertThat(reader.read(), equalTo((int)'\n'));
    assertThat(reader.getCharacterIndex(), equalTo(-1));
    assertThat(reader.getLineNumber(), equalTo(2));
    assertThat(reader.getColumnNumber(), equalTo(-1));
  }
}