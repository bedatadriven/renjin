/*
 * Renjin Regular Expression Library, based on gnu-regexp
 * Copyright (C) 1998-2001 Wes Biggs
 * Copyright (C) 2016 BeDataDriven Groep BV
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.renjin.regexp;

import java.io.Serializable;


class CharUnit implements Serializable {
  public char ch;
  public boolean bk;
}
