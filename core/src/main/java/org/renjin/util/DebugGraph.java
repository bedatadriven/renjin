package org.renjin.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class DebugGraph {


  private final PrintWriter writer;
  private File tempFile;

  public DebugGraph()  {
    try {
      tempFile = File.createTempFile("deferred", ".dot");
      writer = new PrintWriter(tempFile);
      writer.println("digraph G { ");
      System.out.println("Dumping compute graph to " + tempFile.getAbsolutePath());
    } catch(Exception e) {
      throw new RuntimeException();
    }
  }

  public void printEdge(String fromId, String toId) {
    writer.println(fromId + " -> " + toId);
  }

  public void printNode(String nodeId, String label) {
    writer.println(nodeId + " [ label=\"" + label + "\"]");
  }

  public void close() {
    try {
      writer.println("}");
      writer.close();

      File tempPng = File.createTempFile("graph", ".png");

      Process process = Runtime.getRuntime().exec(new String[] { "dot", "-T", "png", "-o", tempPng.getAbsolutePath(), tempFile.getAbsolutePath() });

      System.out.println("Graph drawn to " + tempPng);
      //tempFile.delete();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
