package org.renjin.methods;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.special.ForFunction;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class MethodsTest {

  private Session session;

  @Before
  public void setUp() throws Exception {
    session = new SessionBuilder().build();
    eval("library(methods)");
  }

  private SEXP eval(String source) throws IOException {
    ExpressionVector sexp = RParser.parseAllSource(new StringReader(source + "\n"));
    try {
      return session.getTopLevelContext().evaluate(sexp);
    } catch (EvalException e) {
      e.printRStackTrace(System.out);
      throw new RuntimeException(e);
    }
  }

  @Test
  public void hadley() throws IOException {
    eval("setClass('Person', representation(name = 'character', age = 'numeric'))");
    eval("setClass('Employee', representation(boss = 'Person'), contains = 'Person')");
    eval("hadley <- new(\"Person\", name = \"Hadley\", age = 31)");

    eval("print(hadley@age)");
  }

  @Ignore
  @Test
  public void failure() throws IOException {
    eval("setClass('M', contains = 'matrix', representation(fuzz = 'numeric'))");
    eval("m <- new('M', 1:12, ncol = 3, fuzz = c(1.2,3.2,3.3))");
    eval("m2 <- as(m, 'matrix')");
    eval("print(m2)");

    IntVector dim = (IntVector) eval("dim(m2)");

    assertThat(dim.getElementAsInt(0), equalTo(12));
    assertThat(dim.getElementAsInt(1), equalTo(1));
  }
  
  @Ignore
  public void loopS4MethodCall() throws IOException {
    ForFunction.COMPILE_LOOPS = false;
    eval(" set.seed(101)                                                                   ");
    eval(" setClass('Seq', representation(seq = 'character') )                             ");
    eval(" reads=character(1000)                                                          ");
    eval(" aa=c('A','T','C','G')                                                           ");
    eval(" seqs=list()                                                                     ");
    eval(" for(i in 1:1e3) reads[i] <- paste0(sample(aa, 36, replace = TRUE), collapse='') ");
    eval(" for(i in 1:1e3) seqs[[i]] <- new('Seq', seq=reads[i])                           ");
    eval(" setClass('SeqSum', representation(seq = 'character', value = 'numeric') )       ");
    eval(" setMethod('+', signature(e1 = 'SeqSum', e2 = 'Seq'), function(e1, e2) { hasSeq <- grep(e1@seq, e2@seq); if(length(hasSeq) > 0) e1@value <- e1@value + 1; return(e1) }) ");
    eval(" atg <- new('SeqSum', seq = 'ATG', value = 0)                                    ");
    ForFunction.COMPILE_LOOPS = true;
    ForFunction.FAIL_ON_COMPILATION_ERROR = true;
    eval(" for(i in 1:1e4) { atg <- atg + seqs[[ i ]] }                                    ");
    eval("                                                                                 ");
    
    ForFunction.COMPILE_LOOPS = false;
    
    DoubleArrayVector count = (DoubleArrayVector) eval("atg@value");
    assertThat(count.getElementAsInt(0), equalTo( 4316 ));
  }
  
  @Ignore
  public void loopS4MethodCallSimple() throws IOException {
    ForFunction.COMPILE_LOOPS = false;
    eval(" set.seed(101)                                                                   ");
    eval(" reads=character(250)                                                          ");
    eval(" aa=c('A','T','C','G')                                                           ");
    eval(" for(i in 1:250) reads[i] <- paste0(sample(aa, 36, replace = TRUE), collapse='') ");
    eval(" setClass('SeqSum', representation(seq = 'character', value = 'numeric') )       ");
    eval(" setMethod('+', signature(e1 = 'SeqSum', e2 = 'character'), function(e1, e2) { hasSeq <- grep(e1@seq, seq); if(length(hasSeq) > 0) e1@value <- e1@value + 1; return(e1) }) ");
    eval(" atg <- new('SeqSum', seq = 'ATG', value = 0)                                    ");
    ForFunction.COMPILE_LOOPS = true;
    ForFunction.FAIL_ON_COMPILATION_ERROR = true;
    eval(" for(i in 1:250) { atg <- atg + reads[ i ] }                                    ");
    
    ForFunction.COMPILE_LOOPS = false;
    
    DoubleArrayVector count = (DoubleArrayVector) eval("atg@value");
    assertThat(count.getElementAsInt(0), equalTo( 4316 ));
  }
}
