package org.renjin.primitives.packaging;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.renjin.eval.Context;
import org.renjin.packaging.LazyLoadFrame;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;

public class MavenPackage implements Package {

  private String groupId;
  private String artifactId;

  public MavenPackage(String name) {
    int dot = name.lastIndexOf('.');
    if(dot == -1) {
      this.groupId = "org.renjin";
      this.artifactId = name;
    } else {
      this.groupId = name.substring(0, dot);
      this.artifactId = name.substring(dot+1);
    }
  }

  @Override
  public Iterable<NamedValue> loadSymbols(Context context) throws IOException {
  
    LazyLoadFrame frame = new LazyLoadFrame(context, Resources.newInputStreamSupplier(getEnvironmentUrl()));
    List<NamedValue> values = Lists.newArrayList();
    for(Symbol name : frame.getNames()) {
      values.add(new PackageValue(name, frame.get(name)));
    }
    return values;
  }
  
  public URL getPomUrl() {
    return Resources.getResource("META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml");
  }
  
  public URL getNamespaceURL() {
    return Resources.getResource(groupId.replace('.', '/') + "/" + artifactId + "/NAMESPACE");
  }
  
  public URL getEnvironmentUrl() {
    String name = groupId.replace('.', '/') + "/" + artifactId + "/environment";
    System.err.println(name);
    return Resources.getResource(name);
  }
  
  @Override
  public NamespaceDef getNamespaceDef() {
    try {
      NamespaceDef def = new NamespaceDef();
      def.parse(Resources.newReaderSupplier(getNamespaceURL(), Charsets.UTF_8));
      return def;
    } catch(IOException e) {
      throw new RuntimeException("IOException while parsing NAMESPACE file");
    }
  }
  
  public boolean exists() {
    try {
      getEnvironmentUrl();
      return true;
    } catch(IllegalArgumentException e) {
      e.printStackTrace();
      return false;
    }
  }
  
  private static class PackageValue implements NamedValue {
    private Symbol name;
    private SEXP value;
    
    public PackageValue(Symbol name, SEXP value) {
      super();
      this.name = name;
      this.value = value;
    }

    @Override
    public boolean hasName() {
      return true;
    }

    @Override
    public String getName() {
      return name.getPrintName();
    }

    @Override
    public SEXP getValue() {
      return value;
    }
    
  }


}
