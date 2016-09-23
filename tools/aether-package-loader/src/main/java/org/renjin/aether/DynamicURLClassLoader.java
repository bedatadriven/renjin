/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.aether;

import org.eclipse.aether.resolution.ArtifactResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * {@code URLClassLoader} subclass which allows URLs to be added at runtime
 */
public class DynamicURLClassLoader extends URLClassLoader {
  public DynamicURLClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
  }

  public void addArtifact(ArtifactResult artifactResult) {
    try {
      addURL(artifactResult.getArtifact().getFile().toURI().toURL());
    } catch (MalformedURLException e) {
      throw new RuntimeException("Malformed url from " + artifactResult.getArtifact(), e);
    }
  }
}
