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
package org.renjin.maven.test;

import org.renjin.repackaged.guava.collect.Lists;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;
import java.util.Map.Entry;

public class TestSuiteResult {

  /**
   * The R script file containing the tests
   */
  private File scriptFile;
 
  /**
   * Fake class name based on the name of the script 
   */
  private String className;
  
  /**
   * Results of individual test functions
   */
  private List<TestCaseResult> results = Lists.newArrayList();
  
  private double time;
  
  
  public String getClassName() {
    return className;
  }
  
  public File getScriptFile() {
    return scriptFile;
  }

  public void setScriptFile(File scriptFile) {
    this.scriptFile = scriptFile;
  }

  public void setClassName(String name) {
    this.className = name;
  }

  public List<TestCaseResult> getResults() {
    return results;
  }

  public void setResults(List<TestCaseResult> results) {
    this.results = results;
  }

  public double getTime() {
    return time;
  }

  public void setTime(double time) {
    this.time = time;
  }

  public int countOutcomes(TestOutcome outcome) {
    int count = 0;
    for(TestCaseResult result : results) {
      if(result.getOutcome() == outcome) {
        count++;
      }
    }
    return count;
  }


  public boolean hasFailures() {
    return countOutcomes(TestOutcome.FAILURE) > 0 ||
        countOutcomes(TestOutcome.ERROR) > 0;
  }

  public void addCase(TestCaseResult caseResult) {
    results.add(caseResult);
  }
  
  /**
   * 
   * @return creates a JUnit-style XML document representing these results
   * @throws ParserConfigurationException
   */
  public Document toJunitXML() throws ParserConfigurationException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.newDocument();

    Element testsuite = document.createElement("testsuite");
    testsuite.setAttribute("failures", "0");
    testsuite.setAttribute("time", Double.toString(time));
    testsuite.setAttribute("errors", Integer.toString(countOutcomes(TestOutcome.ERROR)));
    testsuite.setAttribute("skipped", Integer.toString(countOutcomes(TestOutcome.SKIPPED)));
    testsuite.setAttribute("tests", Integer.toString(results.size()));
    testsuite.setAttribute("name", className);
    
    Element properties = document.createElement("properties");
    for(Entry entry : System.getProperties().entrySet()) {
      Element property = document.createElement("property");
      property.setAttribute("name", (String)entry.getKey());
      property.setAttribute("value", (String)entry.getValue());
      properties.appendChild(property);
    }
    testsuite.appendChild(properties);
    
    for(TestCaseResult result : results) {
      Element testCase = document.createElement("testcase");
      testCase.setAttribute("time", Double.toString(result.getTime()));
      testCase.setAttribute("classname", result.getClassName());
      testCase.setAttribute("name", result.getName());
      
      if(result.getOutcome() == TestOutcome.ERROR) {
        Element error = document.createElement("error");
        if(result.getErrorMessage() != null) {
          error.setAttribute("message", result.getErrorMessage());
        }
        testCase.appendChild(error);
      }
      testsuite.appendChild(testCase);
    }
    document.appendChild(testsuite);
    
    return document;
  }

  /**
   * Writes the JUnit XML file to a reports directory
   * @param reportDir
   */
  public void writeXml(File reportDir) {
    try {
      File xmlFile = new File(reportDir, "TEST-" + className + ".xml");
      
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
  
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD,"xml");
      // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
  
      StreamResult result = new StreamResult(xmlFile);
      DOMSource source = new DOMSource(toJunitXML());
      transformer.transform(source, result);
      
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

}
