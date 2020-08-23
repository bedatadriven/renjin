package org.renjin.maven.test;

import org.renjin.packaging.test.TestListener;

import java.io.File;

class Listener implements TestListener {

  @Override
  public void debug(String message) {
    System.out.println(message);
  }

  @Override
  public void startFile(File testFile) {
  }

  @Override
  public void start(String testName) {
  }

  @Override
  public void pass() {
  }

  @Override
  public void fail() {
  }

  @Override
  public void done() {
  }

  @Override
  public void timeout() {
  }
}
