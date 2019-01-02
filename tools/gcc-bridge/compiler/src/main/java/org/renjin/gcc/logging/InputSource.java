/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.logging;

import org.renjin.gcc.gimple.GimpleBasicBlock;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.statement.GimpleStatement;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.html.HtmlEscapers;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class InputSource {

  private List<String> lines;
  private String sourceFile;
  private Set<Integer> compiledLines = new HashSet<>();
  private int minLine = Integer.MAX_VALUE;
  private int maxLine = Integer.MIN_VALUE;

  public InputSource(GimpleFunction gimpleFunction, File sourceFile, String sourcePath) {
    this.lines = loadLines(sourceFile);
    this.sourceFile = sourcePath;

    // Find min/max line
    for (GimpleBasicBlock basicBlock : gimpleFunction.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if(sourcePath.equals(statement.getSourceFile())) {
          if(statement.getLineNumber() != null) {
            int lineNumber = statement.getLineNumber();
            compiledLines.add(lineNumber);
            if(lineNumber < minLine) {
              minLine = lineNumber;
            }
            if(lineNumber > maxLine) {
              maxLine = lineNumber;
            }
          }
        }
      }
    }

    // Expand the range a bit to include more context
    minLine = Math.max(1, minLine - 5);
    maxLine = maxLine + 5;
  }

  public static List<InputSource> from(GimpleFunction function) {
    Set<String> sourceFiles = findSources(function);
    List<InputSource> sources = new ArrayList<>();
    for (String sourceFile : sourceFiles) {
      sources.add(new InputSource(function, resolveSourceFile(function, sourceFile), sourceFile));
    }

    return sources;
  }

  private static File resolveSourceFile(GimpleFunction function, String sourceFile) {
    if(sourceFile.startsWith("..")) {
      return new File(function.getUnit().getSourceFile().getParentFile(), sourceFile);
    } else {
      return new File(sourceFile);
    }
  }

  private static List<String> loadLines(File file) {
    if(file.exists()) {
      try {
        return Files.readLines(file, Charsets.UTF_8);
      } catch (IOException e) {
        // ignore
      }
    }
    return Collections.emptyList();
  }


  private static Set<String> findSources(GimpleFunction function) {
    Set<String> sources = new HashSet<>();
    for (GimpleBasicBlock basicBlock : function.getBasicBlocks()) {
      for (GimpleStatement statement : basicBlock.getStatements()) {
        if(statement.getSourceFile() != null) {
          sources.add(statement.getSourceFile());
        }
      }
    }
    return sources;
  }

  public void render(StringBuilder html) {
    html.append("<div class=\"sourceFilename\">");
    html.append(HtmlEscapers.htmlEscaper().escape(this.sourceFile));
    html.append("</div>");
    html.append("<table>\n");
    for (int i = minLine; i < maxLine + 3; i++) {
      line(html, i, lineAt(i - 1));
    }
    html.append("</table>");
  }

  public void line(StringBuilder html, int lineNumber, String line) {
    html.append("<tr");
    if(compiledLines.contains(lineNumber)) {
      html.append(String.format(" class=\"SL SL%d\"", lineNumber));
    }
    html.append(">");
    html.append("<td class=\"lnum\">").append(lineNumber).append("</td>");
    html.append("<td class=\"line");
    html.append("\">").append(HtmlEscapers.htmlEscaper().escape(line)).append("</td></tr>\n");
  }

  private String lineAt(int i) {
    if(i < lines.size()) {
      return lines.get(i);
    }
    return "";
  }
}
