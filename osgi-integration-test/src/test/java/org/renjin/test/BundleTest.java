
/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.renjin.test;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;

import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.io.File;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * {@see http://felix.apache.org/site/apache-felix-ipojo-junit4osgi-tutorial.html}
 * @author Nicolas Fortin
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class BundleTest {
  @Inject
  BundleContext context;

  private File bundleFolder = new File("target/bundle");

  @Configuration
  public Option[] config() throws MalformedURLException {
    List<Option> options = new ArrayList<Option>();
    options.addAll(Arrays.asList(systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
        getBundle("org.osgi.compendium"),
        getBundle("renjin-script-engine"),
        junitBundles()));
    //options.addAll(getBundles());
    return options(options.toArray(new Option[options.size()]));
  }

  private UrlProvisionOption getBundle(String bundleName) throws MalformedURLException {
    return bundle(new File(bundleFolder, bundleName + ".jar").toURI().toURL().toString());
  }

  private List<Option> getBundles() {
    List<Option> bundles = new ArrayList<Option>();
    File bundleFolder = new File("target/bundle");
    for (File bundle : bundleFolder.listFiles()) {
      try {
        bundles.add(bundle(bundle.toURI().toURL().toString()).noStart());
      } catch (MalformedURLException ex) {
        // Ignore
      }
    }
    return bundles;
  }

  /**
   * Create data source
   */
  @Before
  public void setUp() throws SQLException {
  
  }

  /**
   * Validate integration of built-in bundles.
   */
  @Test
  public void testBuiltInBundleActivation() throws Exception {
    System.out.println("Built-In bundle list :");
    System.out.println("ID\t\tState\tBundle name");
    for (Bundle bundle : context.getBundles()) {
      System.out.println(
          "[" + String.format("%02d", bundle.getBundleId()) + "]\t"
              + getStateString(bundle.getState()) + "\t"
              + bundle.getSymbolicName() + "[" + bundle.getVersion() + "]");
      // Print services
      ServiceReference[] refs = bundle.getRegisteredServices();
      if (refs != null) {
        for (ServiceReference ref : refs) {
          String refDescr = ref.toString();
          if (!refDescr.contains("org.osgi") && !refDescr.contains("org.apache")) {
            System.out.println(
                "\t\t\t\t" + ref);
          }
        }
      }
    }
  }
  
  @Test
  public void basicScriptEngineUse() throws Exception {
    Bundle bundle = context.getBundle("org.renjin.script-engine");
    Assert.assertNotNull(bundle);

    Class<?> factoryClass = bundle.loadClass("org.renjin.script.RenjinScriptEngineFactory");
    Assert.assertNotNull(factoryClass);
    
    ScriptEngineFactory factory = (ScriptEngineFactory) factoryClass.newInstance();
    ScriptEngine engine = factory.getScriptEngine();
    engine.eval("print(1+1)");
  }

  private String getStateString(int i) {
    switch (i) {
      case Bundle.ACTIVE:
        return "Active   ";
      case Bundle.INSTALLED:
        return "Installed";
      case Bundle.RESOLVED:
        return "Resolved "; 
      case Bundle.STARTING:
        return "Starting ";
      case Bundle.STOPPING:
        return "Stopping ";
      default:
        return "Unknown  ";
    }
  }

  @After
  public void tearDown() {
  }

  @Test
  public void checkResolveState() throws BundleException {
    for (Bundle bundle : context.getBundles()) {
      if (bundle.getState() == Bundle.INSTALLED) {
        throw new BundleException("Bundle " + bundle.getSymbolicName() + " not resolved");
      }
    }
  }
}