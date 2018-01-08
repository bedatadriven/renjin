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
package org.renjin.aether;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;

import java.util.List;
import java.util.Set;


/**
 * Excludes core Renjin dependencies and packages that have already been loaded
 * from the classpath.
 */
public class AetherExclusionFilter implements DependencyFilter {

  private Set<String> loaded;

  public AetherExclusionFilter(Set<String> loaded) {
    this.loaded = loaded;
  }

  @Override
  public boolean accept(DependencyNode node, List<DependencyNode> parents) {
    // Exclude this if previously loaded, either directly from the classpath
    // or previously via Aether
    if(isLoaded(node.getArtifact())) {
      return false;
    }

    if(isCoreDependency(node.getArtifact())) {
      return false;
    }

    // Exclude this library if it is a transitive dependency of a
    // a renjin core dependency
    for (DependencyNode parent : parents) {
      if(isCoreDependency(parent.getArtifact())) {
        return false;
      }
    }
    return true;
  }

  private boolean isLoaded(Artifact artifact) {
    return loaded.contains(artifact.getGroupId() + ":" + artifact.getArtifactId());
  }

  private boolean isCoreDependency(Artifact artifact) {
    if(artifact.getGroupId().equals("org.renjin") &&
        artifact.getArtifactId().equals("renjin-core")) {
      return true;
    }
    return false;
  }
}
