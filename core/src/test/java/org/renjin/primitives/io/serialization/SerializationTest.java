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
    eval("con <- .Internal(file('target/saved.RData', open='', blocking=TRUE, encoding='UTF8', raw=FALSE))");
    eval(".Internal(saveToConn(c('x','y','f'), con, ascii=FALSE, version=NULL, globalenv(), eval.promises=TRUE))");
    
    // check for interoperability with C-R externally...
    
  }
  
  @Test
  public void serialize() {
    eval("identical(unserialize(serialize(c(seq(1,5),NA), NULL)), c(seq(1,5),NA))");
    eval("identical(unserialize(serialize(c(1.2,3.4,NA), NULL)), c(1.2,3.4,NA))");
    eval("identical(unserialize(serialize(c('1.2','3.4',NA), NULL)), c('1.2','3.4',NA))");
    eval("identical(unserialize(serialize(list('1.2',3.4), NULL)), list('1.2',3.4))");
    eval("identical(unserialize(serialize(.GlobalEnv, NULL)), .GlobalEnv)");
    
    eval("env <- new.env()");
    eval("assign('x', list(1,'2'), env)");
    eval("identical(get('x',unserialize(serialize(env, NULL)), ), list(1,'2'))");
    
    eval("f <- function(x) {x+1}");
    eval("identical(unserialize(serialize(f, NULL))(2), 3)");
    
    // check for interoperability with C-R externally...
    
  }
}
