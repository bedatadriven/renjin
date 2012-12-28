package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.renjin.eval.Context;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.Environment;

import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;

public class Packages {

  @Primitive
  public static void library2(@Current Context context, @Current Environment rho, String packageName) throws IOException {
    
    String fqPackageName; 
    if(packageName.indexOf('.') != -1) {
      fqPackageName = packageName;
    } else {
      fqPackageName = "org.r-project.cran." + packageName;
    }
    
    int lastDot = fqPackageName.lastIndexOf('.');
    String namespace = fqPackageName.substring(lastDot+1);
    
    // resolve dependencies
    // TODO
    
    // find and deserialize the package environment 
    InputSupplier<InputStream> inSup = getSerializedEnvironment(fqPackageName);
    Environment packageEnv = deserializePackageEnvironment(context, inSup);
   
    // Register namespace
    Namespaces.registerNamespace(context, namespace, packageEnv);
    
    // Attach the exports to the search path
    // TODO: attach the exports instead of the environment
    rho.insertAbove(packageEnv.getFrame());
    
    
  }

  private static Environment deserializePackageEnvironment(Context context,
      InputSupplier<InputStream> inSup) throws IOException {
    InputStream in = inSup.getInput();
    RDataReader reader = new RDataReader(context, in);
    Environment packageEnv = (Environment) reader.readFile();
    in.close();
    return packageEnv;
  }

  private static InputSupplier<InputStream> getSerializedEnvironment(String fqPackageName) {
    String environmentFile = "/" + fqPackageName.replace('.', '/') + "/environment";
    URL resource = Resources.getResource(environmentFile);
    return Resources.newInputStreamSupplier(resource);
  } 
  
}
