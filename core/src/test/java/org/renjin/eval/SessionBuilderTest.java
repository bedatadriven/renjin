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
package org.renjin.eval;

import org.junit.Ignore;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class SessionBuilderTest {

  @Ignore
  public void documentation() throws MalformedURLException {

    URLClassLoader classLoader = new URLClassLoader(
        new URL[] {
            new File("/home/alex/my_dir_with_jars").toURI().toURL(),
            new File("/home/alex/my_other_dir_with_jars").toURI().toURL()
        });

    Session session = new SessionBuilder()
        .setClassLoader(classLoader)
        .build();

  }

}