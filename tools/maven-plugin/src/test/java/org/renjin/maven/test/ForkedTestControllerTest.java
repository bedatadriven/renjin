package org.renjin.maven.test;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import junit.framework.TestCase;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ForkedTestControllerTest extends TestCase {

  private File testOutputDirectory;
  private ForkedTestController forkedTestController;

  @Override
  protected void setUp() throws Exception {
    testOutputDirectory = Files.createTempDirectory("testOutput").toFile();

    forkedTestController = new ForkedTestController();
    forkedTestController.setClassPath(getCurrentClassPath());
    forkedTestController.setNamespaceUnderTest("base");
    forkedTestController.setTestReportDirectory(testOutputDirectory);
    
  }

  public void testRdFileWithError() throws MojoExecutionException, IOException {

    File testFile = testFile("man/mean.Rd");

    forkedTestController.executeTest(testFile);

    assertTestCaseSucceeded(testFile, false);
    assertOutputContains(testFile, "ERROR: foo");
  }

  public void testSuccessfulTests() throws MojoExecutionException, IOException {

    File testFile = testFile("successful.R");

    forkedTestController.executeTest(testFile);

    Document document = parseXmlReport(testFile);
    NodeList testCases = document.getElementsByTagName("testcase");

    assertEquals("test count", 3, testCases.getLength());
    
    findTestCaseElement(document, "(root)");
    findTestCaseElement(document, "to.upper");
    findTestCaseElement(document, "to.lower");
  }
  
  public void testInfiniteOutput() throws MojoExecutionException, IOException {
    File testFile = testFile("infiniteOutput.R");

    forkedTestController.setTimeout(3, TimeUnit.SECONDS);
    forkedTestController.executeTest(testFile);

    String output = parseOutput(testFile);
    
    assertTrue(output.contains("----MAX OUTPUT REACHED----"));
    
    if(output.length() > CappedOutputStream.MAX_BYTES + 100) {
      throw new AssertionError(String.format("Output should be limited to %d bytes, output size is %d", 
          CappedOutputStream.MAX_BYTES, output.length()));
    }
    
  }
  
  public void testTimeout() throws MojoExecutionException, IOException {
    File testFile = testFile("timeout.R");
    
    forkedTestController.setTimeout(1, TimeUnit.SECONDS);
    forkedTestController.executeTest(testFile);

    assertTestCaseSucceeded(testFile, false);
  }
  
  public void testOutOfMemory() throws MojoExecutionException, IOException {
    File badTestFile = testFile("outOfMemory.R");

    forkedTestController.executeTest(badTestFile);

    assertTestCaseSucceeded(badTestFile, false);
    assertOutputContains(badTestFile, "gobs of memory");
    
    System.out.println(parseOutput(badTestFile));
    
    // ensure that a new JVM is created for the subsequent test
    File goodTestFile = testFile("good.R");
    
    forkedTestController.executeTest(goodTestFile);
    assertTestCaseSucceeded(goodTestFile, true);
  }

  private String getCurrentClassPath() {
    List<String> paths = new ArrayList<String>();
    try {
      URLClassLoader classLoader = (URLClassLoader) getClass().getClassLoader();
      for (URL url : classLoader.getURLs()) {
        paths.add(url.getFile());
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception computing classpath for fork", e);
    }
    
    return Joiner.on(File.pathSeparator).join(paths);
  }
  
  private File testFile(String name) {
    URL resource = Resources.getResource(name);
    return new File(resource.getFile());
  }
  
  public void assertTestCaseSucceeded(File file, boolean expectSuccess) {
    
    Document document = parseXmlReport(file);
    NodeList testCases = document.getElementsByTagName("testcase");
    if(testCases.getLength() != 1) {
      throw new AssertionError("Expected a single test case: found " + testCases.getLength() + " test case(s)");
    }
    Element testCase = (Element) testCases.item(0);
    boolean succeeded = testCase.getElementsByTagName("error").getLength() == 0;
    if(expectSuccess && !succeeded) {
      throw new AssertionError("Expected test to succeed, but marked as error");
    } 
    if(!expectSuccess && succeeded) {
      throw new AssertionError("Expected test to fail, but marked as success");
    }
  }
  

  private Document parseXmlReport(File testFile) {

    File xmlFile = new File(testOutputDirectory, "TEST-" + TestReporter.suiteName(testFile) + ".xml");
    if(!xmlFile.exists()) {
      throw new AssertionError("XML test report does not exist at " + xmlFile.getAbsolutePath());
    }
    
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      return dBuilder.parse(xmlFile);
    } catch (Exception e) {
      throw new RuntimeException("Exception parsing " + xmlFile, e);
    }
  }
  
  private Element findTestCaseElement(Document resultDoc, String name) {
    NodeList cases = resultDoc.getElementsByTagName("testcase");
    List<String> found = new ArrayList<String>();
    for (int i = 0; i < cases.getLength(); i++) {
      Element testCase = (Element) cases.item(i);
      String testCaseName = testCase.getAttribute("name");
      if(testCaseName.equals(name)) {
        return testCase;        
      }
      found.add(testCaseName);
    }
    throw new AssertionError(String.format("No test case with name %s. Found: %s", name, found));
  }
  
  protected void assertOutputContains(File file, String expectedText) throws IOException {
    String output = parseOutput(file);
    if(!output.contains(expectedText)) {
      throw new AssertionError(String.format("Expected output to contain '%s', but found only:\n%s", expectedText, output));
    }
  }

  private String parseOutput(File file) throws IOException {
    File outputFile = new File(testOutputDirectory, TestReporter.suiteName(file) + "-output.txt");
    if(!outputFile.exists()) {
      throw new AssertionError("Output text does not exist at " + outputFile.getAbsolutePath());
    }
    return com.google.common.io.Files.toString(outputFile, Charsets.UTF_8);
  }
}