/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.aether;

import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;

import java.net.URL;
import java.util.List;

/**
 * Exclude artifacts that are already on the classpath.
 */
public class ClasspathExclusionFilter implements DependencyFilter {
  @Override
  public boolean accept(DependencyNode node, List<DependencyNode> parents) {

    // Check for "null" root node
    if(node.getArtifact() == null) {
      return true;
    }

    String mavenResource = String.format("META-INF/maven/%s/%s/pom.properties",
        node.getArtifact().getGroupId(),
        node.getArtifact().getArtifactId());

    URL resourceUrl = getClass().getClassLoader().getResource(mavenResource);
    if(resourceUrl == null) {
      // The dependency is already on our classpath
      return false;
    }

    return true;
  }
}
