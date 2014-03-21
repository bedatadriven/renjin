package org.renjin.primitives.io.serialization;

import java.io.*;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.renjin.util.CDefines.R_NilValue;

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
}
