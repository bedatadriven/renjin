package org.renjin.aether;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;

import java.util.List;
import java.util.Set;


/**
 * Excludes core Renjin dependencies and packages that have already been loaded
 * from the 
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
