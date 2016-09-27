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
package org.renjin.primitives.io.connections;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


/**
 * Verifies that readLines, writeLines, and socketConnection work for simple cases.
 * 
 * @author jamie
 *
 */
public class NetworkConnectionTest extends EvalTestCase {

  private static final int port = 4448;

  private static class SocketReadTestDriver implements Runnable {

    private final String x;

    public SocketReadTestDriver(String x) {
      this.x = x;
    }

    @Override
    public void run() {
      ServerSocket serverSocket = null;
      try {
        serverSocket = new ServerSocket(port);
        Socket clientSocket = null;
        clientSocket = serverSocket.accept();
        new PrintStream(clientSocket.getOutputStream()).println(x);
        clientSocket.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class SocketWriteTestDriver implements Runnable {

    public static Object READY_FOR_CONNECTIONS = new Object();
    public static Object READY_FOR_READING = new Object();

    private String x;

    public String getResult() {
      return x;
    }

    @Override
    public void run() {
      ServerSocket serverSocket = null;
      try {
        serverSocket = new ServerSocket(port);
        Socket clientSocket = null;
        System.out.println("Accepting connections");
        synchronized (READY_FOR_CONNECTIONS) {
          READY_FOR_CONNECTIONS.notify();
        }
        clientSocket = serverSocket.accept();
        System.out.println("Connected on server.");
        System.out.println("Ready to read");
        x = new BufferedReader(new InputStreamReader(
            clientSocket.getInputStream())).readLine();
        System.out.println("Read: " + x);
        
        synchronized (READY_FOR_READING) {
          READY_FOR_READING.notify();
        }
        clientSocket.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  @Ignore
  public void read() {
    assumingBasePackagesLoad();
    new Thread(new SocketReadTestDriver("hello network")).start();
    eval("con <- socketConnection(port=" + port + ", host=\"localhost\")");
    assertThat(eval("readLines(con)").toString(), equalTo("\"hello network\""));
  }

  @Test
  @Ignore
  public void write() throws InterruptedException {
    assumingBasePackagesLoad();
    SocketWriteTestDriver writer = new SocketWriteTestDriver();
    new Thread(writer).start();
    synchronized (SocketWriteTestDriver.READY_FOR_CONNECTIONS) {
      SocketWriteTestDriver.READY_FOR_CONNECTIONS.wait();
    }
    eval("con <- socketConnection(port=" + port + ", host=\"localhost\")");
    System.out.println("Connected on client");
    System.out.println("Ready to write");
    System.out.println("Writing");
    eval("writeLines(\"foobar\",con)");
    synchronized (SocketWriteTestDriver.READY_FOR_READING) {
      SocketWriteTestDriver.READY_FOR_READING.wait();
    }
    assertThat(writer.getResult(), equalTo("foobar"));
  }

}
