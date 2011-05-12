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

package r.base;

import r.jvmi.annotations.Current;
import r.lang.*;
import r.lang.exception.EvalException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Scan {


  public static void scan(@Current Context context, SEXP file, Vector what, int nmax, String sep, String dec, String quote, int skip, int nlines,
        StringVector naStrings, boolean flush, boolean fill, boolean stripWhite, boolean quiet, boolean blankLinesSkip,
        boolean multiLine, String commentChar, boolean allowEscapes, String encoding) throws IOException {
    InputStream in;
    if(file instanceof StringVector) {
      String fileName = ((StringVector) file).getElementAsString(0);
      if(fileName.length() == 0) {
        in = java.lang.System.in;
      } else {
        in = Connections.file(context,fileName,"o",true,encoding).getValue().getInputStream();
      }
    } else if(file instanceof ExternalExp && ((ExternalExp) file).getValue() instanceof Connection) {
        in = ((Connection) ((ExternalExp) file).getValue()).getInputStream();
    } else {
      throw new EvalException("illegal file argument");
    }

    BufferedReader reader = new BufferedReader( new InputStreamReader(in) );
    String line;

    while( (line=reader.readLine())!=null) {
      java.lang.System.out.println(line);

    }

    throw new EvalException("scan not finished");

  }

}
