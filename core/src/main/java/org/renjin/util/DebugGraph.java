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
