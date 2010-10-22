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

/**
 * Opaque position class emitted by the Lexer
 * and consumed by the Parser.
 * <p/>
 * Corresponds to the C class {@code yyltype }
 */
public class Position {

  public int line;
  public int column;
  public int byteIndex;

  public Position() {
  }

  public Position(int line, int column, int byteIndex) {
    this.line = line;
    this.column = column;
    this.byteIndex = byteIndex;
  }

  @Override
  public Position clone() {
    return new Position(line, column, byteIndex);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Position position = (Position) o;

    if (byteIndex != position.byteIndex) return false;
    if (column != position.column) return false;
    if (line != position.line) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = line;
    result = 31 * result + column;
    result = 31 * result + byteIndex;
    return result;
  }

  @Override
  public String toString() {
    return "line " + (line + 1) + " char " + column;
  }
}
