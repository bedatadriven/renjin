/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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

import org.apache.maven.plugin.logging.Log;
import org.renjin.repackaged.guava.base.Charsets;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

class Fork {
  private final Log log;
  private final Process process;
  private final DataOutputStream processChannel;
  private final ResultListener listener;
  private final Thread listeningThread;

  private boolean readError = false;

  private final ArrayBlockingQueue<ForkMessage> incomingQueue = new ArrayBlockingQueue<>(100);

  public Fork(Log log, Process process) {
    this.log = log;
    this.process = process;
    processChannel = new DataOutputStream(process.getOutputStream());

    listener = new ResultListener(process.getInputStream());
    listeningThread = new Thread(listener);
    listeningThread.start();
  }

  /**
   * Sends a command to the forked JVM
   */
  public void sendCommand(String commandText) throws IOException {
    processChannel.writeUTF(commandText);
    processChannel.flush();
  }

  /**
   * Tries to read a message from the forked JVM.
   * @param timeout
   * @param timeUnit
   * @return the message read, or {@code null} if no message was received before the timeout.
   * @throws IOException
   * @throws InterruptedException
   */
  public ForkMessage readMessage(long timeout, TimeUnit timeUnit) throws IOException, InterruptedException {
    if(readError) {
      throw new IOException("Exception reading message from forked JVM");
    }
    return incomingQueue.poll(timeout, timeUnit);
  }

  public void shutdown() {
    try {
      process.destroy();
      listeningThread.interrupt();
    } catch (Exception e) {
      log.error("Exception shutting down forked JVM: " + e.getMessage(), e);
    }
  }

  /**
   * Runs in a separate thread, listening for putput from the forked JVM.
   */
  private class ResultListener implements Runnable {

    private final BufferedReader reader;

    private ResultListener(InputStream in) {
      this.reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
    }

    @Override
    public void run() {
      while(!Thread.interrupted()) {
        String line;
        try {
          line = reader.readLine();

        } catch (IOException e) {
          readError = true;
          log.error("Error reading from forked test executor: " + e.getMessage(), e);
          return;
        }
        if(line == null) {
          readError = true;
          log.error("End of input when reading from forked test executor.");
          return;
        }
        if(ForkedTestController.DEBUG_FORKING) {
          log.debug("[CHANNEL] " + line);
        }
        if (line.startsWith(TestExecutor.MESSAGE_PREFIX)) {
          incomingQueue.add(new ForkMessage(line));
        }
      }
    }
  }
}
