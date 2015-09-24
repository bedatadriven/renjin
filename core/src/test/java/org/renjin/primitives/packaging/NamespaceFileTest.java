package org.renjin.primitives.packaging;

import com.google.common.collect.Iterables;
import com.google.common.io.CharSource;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class NamespaceFileTest {

  
  @Test
  public void testJvmImports() throws IOException {
    
    String NAMESPACE = 
        "importClass(org.renjin.stats.dist.Distance)\n";
    
    NamespaceFile file = new NamespaceFile(CharSource.wrap(NAMESPACE));
    
    NamespaceFile.JvmClassImportEntry entry = Iterables.getOnlyElement(file.getJvmImports());
    assertThat(entry.isClassImported(), equalTo(true));
    assertThat(entry.getClassName(), equalTo("org.renjin.stats.dist.Distance"));
  }
  
  @Test
  public void testDynLib() throws IOException {
    String NAMESPACE = 
        "useDynLib(stats, .registration = TRUE, .fixes='C_',\n" +
        "    kmeans_Lloyd,\n" +
        "    kmeans_MacQueen,\n" +
        "    kmns = kmns_)\n";
    
    NamespaceFile file = new NamespaceFile(CharSource.wrap(NAMESPACE));

    NamespaceFile.DynLibEntry dynLib = Iterables.getOnlyElement(file.getDynLibEntries());
    assertThat(dynLib.getPrefix(), equalTo("C_"));
    assertThat(dynLib.isRegistration(), equalTo(true));

    NamespaceFile.DynLibSymbol kmns = dynLib.getSymbols().get(2);
    assertThat(kmns.getSymbolName(), equalTo("kmns_"));
    assertThat(kmns.getAlias(), equalTo("kmns"));
  }
  
}