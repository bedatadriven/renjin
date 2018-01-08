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
package org.renjin.primitives.packaging;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.renjin.eval.Context;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.sexp.Symbol;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class NamespaceFileTest {


  private Context context;

  @Before
  public void startSession() {
    Session session = new SessionBuilder().build();
    context = session.getTopLevelContext();
  }
  
  @Test
  public void testJvmImports() throws IOException {
    
    String NAMESPACE = 
        "importClass(org.renjin.stats.dist.Distance)\n";
    
    NamespaceFile file = NamespaceFile.parse(context, CharSource.wrap(NAMESPACE));
    
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
      
    NamespaceFile file = NamespaceFile.parse(context, CharSource.wrap(NAMESPACE));

    NamespaceFile.DynLibEntry dynLib = Iterables.getOnlyElement(file.getDynLibEntries());
    assertThat(dynLib.getPrefix(), equalTo("C_"));
    assertThat(dynLib.isRegistration(), equalTo(true));

    NamespaceFile.DynLibSymbol kmns = dynLib.getSymbols().get(2);
    assertThat(kmns.getSymbolName(), equalTo("kmns_"));
    assertThat(kmns.getAlias(), equalTo("kmns"));
  }
  
  @Test
  public void exportPatterns() throws IOException {
    String NAMESPACE = 
        "exportPattern(cell_effect_mult_or,cell_effect_or,Cloglin,Cloglin_mult,exp_par,exp_par_mult)\n";

    NamespaceFile file = NamespaceFile.parse(context, CharSource.wrap(NAMESPACE));

    assertThat(file.getExportedPatterns(), hasItems(
        "cell_effect_mult_or",
        "cell_effect_or",
        "Cloglin",
        "Cloglin_mult",
        "exp_par",
        "exp_par_mult"));
    
  }
  
  @Test
  public void ifWithBraces() throws IOException {
    String NAMESPACE = "if (getRversion() >= \"2.13.0\") {\n" +
        "    importFrom(\"stats\", \"nobs\")\n" +
        "} else {\n" +
        "    export(nobs)\n" +
        "}\n";

    NamespaceFile file = NamespaceFile.parse(context, CharSource.wrap(NAMESPACE));

    assertThat(file.getPackageImports().size(), equalTo(1));
    NamespaceFile.PackageImportEntry statsImport = Iterables.getOnlyElement(file.getPackageImports());
    assertThat(statsImport.getSymbols(), hasItems(Symbol.get("nobs")));
    
    assertTrue((file.getExportedSymbols().isEmpty()));
  }

  @Test
  public void elseWithBraces() throws IOException {
    String NAMESPACE = "if (getRversion() < \"2.13.0\") {\n" +
        "    importFrom(\"stats\", \"nobs\")\n" +
        "} else {\n" +
        "    export(nobs)\n" +
        "}\n";

    NamespaceFile file = NamespaceFile.parse(context, CharSource.wrap(NAMESPACE));

    assertThat(file.getExportedSymbols(), hasItem(Symbol.get("nobs")));
  }
  
  @Test
  public void ifRVersion() throws IOException {
    String NAMESPACE = 
        "if(getRversion() > \"2.15.0\")\n" +
            "    export(\".M.classEnv\")\n";


    NamespaceFile file = NamespaceFile.parse(context, CharSource.wrap(NAMESPACE));

    assertThat(file.getExportedSymbols(), Matchers.hasItem(Symbol.get(".M.classEnv")));
  }
  
  @Test
  public void malformedImportsFrom() throws IOException {
    
    // The following actually has no effect, but is included in at least one
    // CRAN package. The fact that it has no effect was balanced out in practice by
    // listing survival also in the Depends clause of the DESCRIPTION file.
    String NAMESPACE = 
        "importFrom(survival)\n";

    NamespaceFile file = NamespaceFile.parse(context, CharSource.wrap(NAMESPACE));

    NamespaceFile.PackageImportEntry entry = Iterables.getOnlyElement(file.getPackageImports());
    assertThat(entry.getPackageName(), equalTo("survival"));
    assertTrue(entry.getSymbols().isEmpty());
    assertTrue(entry.getClasses().isEmpty());
  }
} 