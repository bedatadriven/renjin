/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.parser;

import org.junit.Test;
import r.lang.ExpExp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class BaseTest {
  private RParser parser;

  @Test
  public void rev() throws IOException {
    parsesWithoutError("rev.R");
  }

  @Test
  public void character() throws IOException {
    parsesWithoutError("character.R");
  }


  private void parsesWithoutError(String script) throws IOException {
    Reader reader = openReader("/r/library/base/R/" + script);
    ExpExp s = RParser.parseAll(reader);

    System.out.println(s);
  }


  private Reader openReader(String path) {
    InputStream inputStream = getClass().getResourceAsStream(path);
    assertThat(inputStream, is(not(nullValue())));

    Reader reader = new InputStreamReader(inputStream);
    return reader;
  }

}
