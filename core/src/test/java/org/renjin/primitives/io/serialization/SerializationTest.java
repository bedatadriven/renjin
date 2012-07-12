package org.renjin.primitives.io.serialization;

import org.junit.Test;
import org.renjin.EvalTestCase;

public class SerializationTest extends EvalTestCase {

  @Test
  public void saveToConn() {
    
    eval("x <- 1:10");
    eval("y <- 42");
    eval("attr(y,'foo') <- 'bar' ");
    eval("f <- function(x) x*2 ");
    eval("con <- .Internal(file('target/saved.RData', open='', blocking=TRUE, encoding='UTF8'))");
    eval(".Internal(saveToConn(c('x','y','f'), con, ascii=FALSE, version=NULL, globalenv(), eval.promises=TRUE))");
    
    // check for interoperability with C-R externally...
    
  }
}
