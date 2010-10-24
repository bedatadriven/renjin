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

package r.lang;

/**
 * Marker interface that restricts the type of a parameter or member to
 * either a ListExp or NilExp.
 *
 * Renjin copies faithfully the structure of ListExp which means that we
 * can't have an empty ListExp. In the C-implementation of R, the NilExp serves
 * as an empty list. We keep this convention (for now), but use this supertype
 * for NilExp and ListExp to enforce types.
 */
public interface NillOrListExp extends Iterable<SEXP> {

  int length();
  SEXP getFirst();
  SEXP getSecond();
  SEXP getThird();
  <S extends SEXP> S get(int i);


}