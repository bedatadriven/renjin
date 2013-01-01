package org.renjin.maven.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

public class TestSuiteResult {

  private String name;
  private List<TestCaseResult> results = Lists.newArrayList();
  
  private double time;
  
  

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
  

  public void addCase(TestCaseResult caseResult) {
    results.add(caseResult);
  }
  
  public Document toXML() throws ParserConfigurationException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.newDocument();

    Element testsuite = document.createElement("testsuite");
    testsuite.setAttribute("failures", "0");
    testsuite.setAttribute("time", Double.toString(time));
    testsuite.setAttribute("errors", Integer.toString(countOutcomes(TestOutcome.ERROR)));
    testsuite.setAttribute("skipped", Integer.toString(countOutcomes(TestOutcome.SKIPPED)));
    testsuite.setAttribute("tests", Integer.toString(results.size()));
    testsuite.setAttribute("name", name);
    
    for(TestCaseResult result : results) {
      Element testcase = document.createElement("testcase");
      testcase.setAttribute("time", Double.toString(result.getTime()));
      testcase.setAttribute("classname", result.getClassName());
      testcase.setAttribute("name", result.getName());
      
      if(result.getException() != null) {
        Element error = document.createElement("error");
        error.setAttribute("message", result.getException().getMessage());
        error.setAttribute("type", result.getException().getClass().getName());
        
        StringWriter stacktrace = new StringWriter();
        result.getException().printStackTrace(new PrintWriter(stacktrace));
        error.setTextContent(stacktrace.toString());
        testcase.appendChild(error);
      }
      testsuite.appendChild(testcase);
    }
    return document;
  }

  public void writeXml(File reportDir) {
    try {
      File xmlFile = new File(reportDir, "TEST-" + name + ".xml");
      
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
  
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD,"xml");
      // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
  
      StreamResult result = new StreamResult(xmlFile);
      DOMSource source = new DOMSource(toXML());
      transformer.transform(source, result);
      
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    
  }


}
