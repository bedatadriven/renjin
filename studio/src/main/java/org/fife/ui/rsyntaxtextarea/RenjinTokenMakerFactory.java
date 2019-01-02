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
package org.fife.ui.rsyntaxtextarea;

import java.util.Collections;
import java.util.Set;

public class RenjinTokenMakerFactory extends TokenMakerFactory {

  public static final String SYNTAX_STYLE_R = "R";
  
  @Override
  protected TokenMaker getTokenMakerImpl(String key) {
    return new RenjinTokenMaker();
  }

  @Override
  public Set<String> keySet() {
    return Collections.singleton(SYNTAX_STYLE_R);
  }

}
