/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import java.awt.*;

/**
 * Additional capabilities of an interactive console for R.
 *
 * @author Patrick Niemeyer (pat@pat.net)
 */
public interface RichConsole extends Console {
  void print(Object o, Color color);
  void setNameCompletion(NameCompletion nc);

  /** e.g. the wait cursor */
  void setWaitFeedback(boolean on);
}
