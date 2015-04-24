package org.renjin.aether;

import org.eclipse.aether.resolution.DependencyResolutionException;
import org.renjin.primitives.packaging.FqPackageName;


public interface PackageListener {
    
    void packageLoading(FqPackageName packageName);
    
    void packageResolved(FqPackageName packageName, String version);

    void packageVersionResolutionFailed(FqPackageName packageName);

    void packageLoadSucceeded(FqPackageName name, String version);

    void packageResolveFailed(DependencyResolutionException name);
}
