package org.renjin.util;


import java.io.File;
import java.io.PrintWriter;

public class DebugGraph {


  private final PrintWriter writer;
  private File tempFile;

  public DebugGraph(final String compute)  {
    try {
      tempFile = File.createTempFile("deferred", ".dot");
      writer = new PrintWriter(tempFile);
      writer.println("digraph G { ");
      System.out.println("Dumping " + compute + " graph to " + tempFile.getAbsolutePath());
    } catch(Exception e) {
      throw new RuntimeException();
    }
  }

  public DebugGraph() {
    this("compute");
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

      try {
        Runtime.getRuntime().exec(new String[]{"dot", "-T", "png", "-o", tempPng.getAbsolutePath(), tempFile.getAbsolutePath()});

        System.out.println("Graph drawn to " + tempPng);
      } catch (Exception e) {
        System.out.println("Failed to render graph: " + e.getMessage());
      }
      //tempFile.delete();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }
}
