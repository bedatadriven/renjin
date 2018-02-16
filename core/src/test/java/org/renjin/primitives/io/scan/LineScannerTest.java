/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.primitives.io.scan;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.renjin.primitives.io.connections.CharProcessing;
import org.renjin.primitives.io.connections.CharProcessor;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Stopwatch;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;


public class LineScannerTest {


  @Test
  public void simpleTest() throws IOException {

    LineScanner scanner = new LineScanner();
    String text = "Hello World!\n\nThis is a line!\r\nAnd Another line!";
    ReadableByteChannel inputChannel = Channels.newChannel(new ByteArrayInputStream(text.getBytes(Charsets.UTF_8)));
    CharProcessing.read(inputChannel, Charsets.UTF_8, 1024, scanner);

    assertThat(scanner.getLines(), contains("Hello World!", "", "This is a line!", "And Another line!"));

  }



  @Test
  public void simpleTestBuffer() throws IOException {

    LineScanner scanner = new LineScanner();
    String text = "Hello World!\n\nThis is a line!\r\nAnd Another line!";
    ByteBuffer inputBuffer = ByteBuffer.wrap(text.getBytes(Charsets.UTF_8));

    CharProcessing.read(inputBuffer, Charsets.UTF_8, 1024, scanner);

    assertThat(scanner.getLines(), contains("Hello World!", "", "This is a line!", "And Another line!"));

  }


  /**
   * Tests the scenario where the input text is larger than the buffer.
   */
  @Test
  public void biggerText() throws IOException {

    LineScanner scanner = new LineScanner();
    String text = "Hello World!\n\nThis is a line!\r\nAnd Another line!\nSo many lines!\n";
    ByteBuffer inputBuffer = ByteBuffer.wrap(text.getBytes(Charsets.UTF_8));

    CharProcessing.read(inputBuffer, Charsets.UTF_8, 20, scanner);

    assertThat(scanner.getLines(),
        contains("Hello World!", "", "This is a line!", "And Another line!", "So many lines!"));

  }

  /**
   * Tests the scenario where the input text is larger than the buffer.
   */
  @Test
  public void biggerTextBuffer() throws IOException {

    LineScanner scanner = new LineScanner();
    String text = "Hello World!\n\nThis is a line!\r\nAnd Another line!\nSo many lines!\n";
    ReadableByteChannel inputChannel = Channels.newChannel(new ByteArrayInputStream(text.getBytes(Charsets.UTF_8)));

    CharProcessing.read(inputChannel, Charsets.UTF_8, 20, scanner);

    assertThat(scanner.getLines(),
        contains("Hello World!", "", "This is a line!", "And Another line!", "So many lines!"));

  }

  @Test
  public void performanceReader() throws IOException {

    Stopwatch stopwatch = Stopwatch.createStarted();
    InputStream inputStream = new FileInputStream("/home/alex/dev/tpch-dbgen/lineitem.tbl");
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
    String line;
    List<String> lines = new ArrayList<>();
    while((line = reader.readLine()) != null) {
      lines.add(line);
    }

    System.out.println(lines.get(0));
    System.out.println(lines.get(10001));
    System.out.println(stopwatch);
  }

  @Test
  public void performanceScanner() throws IOException {

    Stopwatch stopwatch = Stopwatch.createStarted();
    RandomAccessFile file = new RandomAccessFile("/home/alex/dev/tpch-dbgen/lineitem.tbl", "r");
    FileChannel channel = file.getChannel();

    LineScanner scanner = new LineScanner();
    CharProcessing.read(channel, Charsets.UTF_8, 200 * 1024, scanner);

    List<String> lines = scanner.getLines();

    System.out.println(lines.get(0));
    System.out.println(lines.get(10001));
    System.out.println(stopwatch);
  }

  @Test
  public void countLinesMapped() throws IOException {

    while(true) {
      Stopwatch stopwatch = Stopwatch.createStarted();
      RandomAccessFile file = new RandomAccessFile("/home/alex/dev/tpch-dbgen/lineitem.tbl", "r");
      MappedByteBuffer map = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
      int length = map.remaining();
      int count = 0;
      for (int i = 0; i < length; i++) {
        if (map.get(i) == (byte)'\n') {
          count++;
        }
      }
      System.out.println(count);
      System.out.println(stopwatch);
    }
  }

  @Test
  public void countLinesReader() throws IOException {

    while(true) {
      Stopwatch stopwatch = Stopwatch.createStarted();
      RandomAccessFile file = new RandomAccessFile("/home/alex/dev/tpch-dbgen/lineitem.tbl", "r");
      final byte newline = '\n';
      int count = 0;
      byte[] buffer = new byte[64 * 1024];
      while(true) {
        int bytesRead = file.read(buffer);
        if(bytesRead < 0) {
          break;
        }
        for (int i = 0; i < bytesRead; ++i) {
          if(buffer[i] == (byte)'\n') {
            count++;
          }
        }
      }
      System.out.println(count);
      System.out.println(stopwatch);
    }
  }


  @Test
  public void performanceScannerMmap() throws IOException {

    for (int i = 0; i < 1000; i++) {

      Stopwatch stopwatch = Stopwatch.createStarted();
      RandomAccessFile file = new RandomAccessFile("/home/alex/dev/tpch-dbgen/lineitem.tbl", "r");

      LineScanner scanner = new LineScanner();
      CharProcessing.read(file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length()), Charsets.UTF_8, 100 * 1024, scanner);

      List<String> lines = scanner.getLines();

      System.out.println(lines.get(0));
      System.out.println(lines.get(lines.size() - i - 10));
      System.out.println(stopwatch);
    }
  }

}