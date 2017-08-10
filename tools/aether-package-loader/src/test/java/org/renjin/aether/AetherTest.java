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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.repackaged.guava.base.Joiner;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertThat;

/**
 * This test uses the {@code alpha} and {@code beta} packages in {@code test-packages} to test
 *
 */
public class AetherTest {

  @Test
  public void alpha() throws IOException {

    String script =
        "library(org.renjin.test.alpha);" +
            "alphaVersion()\n";

    assertThat(evaluate(script), equalTo("2.5.1"));
  }

  @Test
  public void beta() throws IOException {

    String script =
        "library(org.renjin.test.beta);" +
            "betaVersion()\n";

    assertThat(evaluate(script), equalTo("2.0.0"));
  }

  @Test
  public void remote() {
    AetherPackageLoader loader = new AetherPackageLoader();
    loader.setRepositoryListener(new ConsoleRepositoryListener());
    loader.setTransferListener(new ConsoleTransferListener());
    loader.load(new FqPackageName("org.renjin.cran", "ACD"));

    System.out.println(Joiner.on("\n").join(loader.getClassLoader().getURLs()));


  }

  @Test
  public void renjinCoreExcluded() {
    AetherPackageLoader loader = new AetherPackageLoader();
    loader.load(new FqPackageName("org.renjin.test", "beta"));

    // Verify that none of the core libraries are loaded, they are already on
    // the classpath
    assertThat(loader.getClassLoader().getURLs(), not(hasItemInArray(containing("renjin-core"))));
    assertThat(loader.getClassLoader().getURLs(), not(hasItemInArray(containing("gcc-runtime"))));
    assertThat(loader.getClassLoader().getURLs(), not(hasItemInArray(containing("lapack"))));
  }

  @Test
  public void singleVersionLoaded() {
    AetherPackageLoader loader = new AetherPackageLoader();
    loader.load(new FqPackageName("org.renjin.test", "alpha"));
    loader.load(new FqPackageName("org.renjin.test", "beta"));

    // Verify that none of the core libraries are loaded, they are already on
    // the classpath
    assertThat(loader.getClassLoader().getURLs(), hasItemInArray(containing("jackson-core-2.5.1.jar")));
    assertThat(loader.getClassLoader().getURLs(), not(hasItemInArray(containing("jackson-core-2.0.0.jar"))));
  }

  @Test
  public void transitiveDependencyConflict() throws IOException {

    // In an interactive sessions, the best we can do let the version
    // version of a transitive dependency win...
    // We have no way of knowing what will be loaded next.

    String alphaFirst =
        "library(org.renjin.test.alpha);" +
            "library(org.renjin.test.beta);" +
            "stopifnot(identical(alphaVersion(), betaVersion()));" +
            "alphaVersion();";

    assertThat(evaluate(alphaFirst), equalTo("2.5.1"));

    String betaFirst =
        "library(org.renjin.test.beta);" +
            "library(org.renjin.test.alpha);" +
            "stopifnot(identical(alphaVersion(), betaVersion()));" +
            "alphaVersion();";

    assertThat(evaluate(betaFirst), equalTo("2.0.0"));
  }

  private String evaluate(String script) throws IOException {
    AetherPackageLoader aetherLoader = new AetherPackageLoader();
    aetherLoader.setTransferListener(new ConsoleTransferListener());
    aetherLoader.setRepositoryListener(new ConsoleRepositoryListener(System.out));

    Session session = new SessionBuilder()
        .setClassLoader(aetherLoader.getClassLoader())
        .setPackageLoader(aetherLoader)
        .build();


    ExpressionVector sexp = RParser.parseAllSource(new StringReader(script));

    SEXP result = session.getTopLevelContext().evaluate(sexp);

    if(result instanceof StringVector) {
      return ((StringVector) result).getElementAsString(0);
    } else {
      throw new AssertionError("Expected string, got: " + result);
    }
  }


  private Matcher<URL> containing(final String string) {
    return new TypeSafeMatcher<URL>() {
      @Override
      public boolean matchesSafely(URL url) {
        return url.toString().contains(string);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("URL containing string ").appendValue(string);
      }
    };
  }

}
