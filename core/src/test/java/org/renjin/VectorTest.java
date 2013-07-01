package org.renjin;

import org.apache.commons.vfs2.FileSystemException;
import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class VectorTest {

  @Test
  public void test() throws IOException {
    Session session = SessionBuilder.buildDefault();
    session.setWorkingDirectory(session.getFileSystemManager().resolveFile("F:\\dev\\Renjin-Benchmarks"));
    ExpressionVector source = RParser.parseAllSource(new FileReader("F:\\dev\\Renjin-Benchmarks\\dcor.R"));
    session.getTopLevelContext().evaluate(source);
  }


  public static void main(String[] args) throws IOException {
    new VectorTest().test();
  }
}

