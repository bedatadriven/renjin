package org.renjin.primitives.io.serialization;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repackaged.guava.io.Resources;
import org.renjin.repackaged.guava.primitives.UnsignedBytes;
import org.renjin.sexp.StringArrayVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SerializationTest extends EvalTestCase {

  @Test
  public void saveToConn() {
    
    eval("x <- 1:10");
    eval("y <- 42");
    eval("attr(y,'foo') <- 'bar' ");
    eval("f <- function(x) x*2 ");
    eval("con <- .Internal(file('target/saved.RData', open='', blocking=TRUE, encoding='UTF8', raw=FALSE))");
    eval(".Internal(saveToConn(c('x','y','f'), con, ascii=FALSE, version=NULL, globalenv(), eval.promises=TRUE))");
    
    // check for interoperability with C-R externally...
    
  }
  
  @Test
  public void serialize() throws IOException {
    assertThat(eval("unserialize(serialize(c(seq(1,5),NA), NULL))"), equalTo(eval("c(seq(1,5),NA)")));
    assertThat(eval("unserialize(serialize(c(1.2,3.4,NA), NULL))"), equalTo(eval("c(1.2,3.4,NA)")));
    assertThat(eval("unserialize(serialize(c('1.2','3.4',NA), NULL))"), equalTo(eval("c('1.2','3.4',NA)")));
    assertThat(eval("unserialize(serialize(list('1.2',3.4), NULL))"), equalTo(eval("list('1.2',3.4)")));
    assertThat(eval("unserialize(serialize(.GlobalEnv, NULL))"), equalTo(eval(".GlobalEnv")));
    
    eval("env <- new.env()");
    eval("assign('x', list(1,'2'), env)");
    assertThat(eval("get('x',unserialize(serialize(env, NULL)))"), equalTo(eval("list(1,'2')")));
    
    eval("f <- function(x) {x+1}");
    assertThat(eval("unserialize(serialize(f, NULL))(2)"), equalTo( c(3) ));
    
    // check for interoperability with C-R externally...
    BufferedReader reader = new BufferedReader(new InputStreamReader(
            getClass().getResourceAsStream("test_gnur.txt")));
    assertRead(reader, "seq(1,5)");
    assertRead(reader, "c(seq(1,5),NA)");
    assertRead(reader, "c(1.2,3.4)");
    assertRead(reader, "c(1.2,3.4,NA)");
    assertRead(reader, "c('1.2','3.4')");
    assertRead(reader, "c('1.2','3.4',NA)");
    assertRead(reader, "list('1.2',3.4)");
    assertRead(reader, ".GlobalEnv");
    
    evalRead(reader);
    assertThat(eval("get('x', test)"), equalTo(eval("list(1,'2')")));
    
    evalRead(reader);
    assertThat(eval("test(2)"), equalTo(c(3)));
    
    reader.close();
  }
  
  private void evalRead(BufferedReader reader) throws IOException {
    String fromGnur;
    if((fromGnur = reader.readLine()) != null) {
      eval("test <- unserialize(charToRaw('" + fromGnur + "'))");
    }    
  }
  
  private void assertRead(BufferedReader reader, String sexp) throws IOException {
    evalRead(reader);
    assertThat(eval("test"), equalTo(eval(sexp)));
  }
  
  @Test
  public void testSaveRdsBitwiseMatch() throws IOException {
    assumingBasePackagesLoad();
    
    File tempFile = File.createTempFile("renjin", "rds");
    global.setVariable("tempFile", new StringArrayVector(tempFile.getAbsolutePath()));
    
    eval("saveRDS('A', file=tempFile, compress=FALSE)");

    assertBitwiseMatch(tempFile, "expectedSimple.rds");
  }

  @Test
  public void readRds() {
    assumingBasePackagesLoad();

    String rdsFile = Resources.getResource("expectedSimple.rds").getFile();
    global.setVariable("file" , new StringArrayVector(rdsFile));
    
    eval("x <- readRDS(file)");
    
    assertThat(eval("x"), equalTo(c("A")));
  }
  
  
  @Test
  @Ignore("todo: version seems to be different")
  public void testSaveBitwiseMatch() throws IOException {
    assumingBasePackagesLoad();

    eval("x <- 'اختيارات'");
    eval("y <- 1:5");
    eval("z <- .GlobalEnv");

    File tempFile = File.createTempFile("renjin", "RData");
    global.setVariable("tempFile", new StringArrayVector(tempFile.getAbsolutePath()));

    eval("save(x,y,z ,file=tempFile, compress=FALSE)");

    assertBitwiseMatch(tempFile, "expectedSave.RData");
  }

  private void assertBitwiseMatch(File tempFile, String resourceName) throws IOException {

    byte[] expected = Resources.toByteArray(Resources.getResource(resourceName));
    byte[] actual = Files.toByteArray(tempFile);

    System.out.println(String.format("    %10s %10s", "GNU R", "Renjin"));

    for(int i=0; i< Math.max(expected.length, actual.length); i++) {
      String expectedByte = toHex(expected, i);
      String actualByte = toHex(actual, i);

      System.out.println(String.format("%2X: %10s %10s %s", i, expectedByte, actualByte,
          Objects.equals(expectedByte, actualByte) ? "" : "*"));

    }

    if(!Arrays.equals(expected, actual)) {
      throw new AssertionError("saveRDS() output does not match GNU R 3.2.2");
    }
  }

  private String toHex(byte[] expected, int i) {
    if (i < expected.length) {
      return Integer.toHexString(UnsignedBytes.toInt(expected[i])).toUpperCase();
    } else {
      return "";
    }
  }
}
