/*
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
package org.renjin.maven;

import junit.framework.TestCase;
import org.apache.maven.plugin.MojoFailureException;


public class NamespaceMojoTest extends TestCase {

  public void testSourceVersion() throws MojoFailureException {
    assertEquals("2.9-12", NamespaceMojo.sourceVersion("2.9-12"));
    assertEquals("2.9-12", NamespaceMojo.sourceVersion("2.9-12-b343"));

    assertEquals("0.13", NamespaceMojo.sourceVersion("0.13-renjin-SNAPSHOT"));
  }
}