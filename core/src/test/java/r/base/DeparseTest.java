package r.base;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import r.EvalTestCase;

public class DeparseTest extends EvalTestCase {

  @Before
  public void defineDeparse() {
    eval("deparse<-function(expr) .Internal(deparse(expr, 60L, FALSE, 1L, -1L))");    
  }
  
  @Test
  public void deparse() {
    
    assertThat(eval("deparse(1)"), equalTo(c("1")));
    assertThat(eval("deparse(c(1,2,3,4))"), equalTo(c("c(1, 2, 3, 4)")));
    assertThat(eval("deparse(c(1L,3L,4L))"), equalTo(c("c(1L, 3L, 4L)")));
    assertThat(eval("deparse(c(1L,NA,4L))"), equalTo(c("c(1L, NA, 4L)")));
    assertThat(eval("deparse(c(99L))"), equalTo(c("99L")));
    assertThat(eval("deparse(c(x=99L,y=45L))"), equalTo(c("c(x = 99L, y = 45L)")));
    assertThat(eval("deparse(NA_real_)"), equalTo(c("NA_real_")));
    assertThat(eval("deparse(1:10)"), equalTo(c("1:10")));
    assertThat(eval("deparse(5:1)"), equalTo(c("5:1")));
    assertThat(eval("deparse(list(1,'s',3L))"), equalTo(c("list(1, \"s\", 3L)")));
    assertThat(eval("deparse(list(a=1,b='foo'))"), equalTo(c("list(a = 1, b = \"foo\")")));
  } 
  
  @Test
  public void emptyVectors() {
    assertThat(eval("deparse(c(1L)[-1])"), equalTo(c("integer(0)")));
    assertThat(eval("deparse(c(1)[-1])"), equalTo(c("numeric(0)")));
  }
  
  @Test
  public void deparseCalls() {
    assertThat(eval("deparse(quote(if(x) y else  z))"), equalTo(c("if (x) y else z")));
    assertThat(eval("deparse(quote(for(i in x) z()))"), equalTo(c("for(i in x) z()")));
    assertThat(eval("deparse(quote(repeat x))"), equalTo(c("repeat x")));
    assertThat(eval("deparse(quote(while(x) y))"), equalTo(c("while (x) y")));
    assertThat(eval("deparse(quote(f(x=1,3)))"), equalTo(c("f(x = 1, 3)")));
    assertThat(eval("deparse(quote({ x+1 }))"), equalTo(c("{ x + 1 }")));
    assertThat(eval("deparse(quote(x:y))"), equalTo(c("x:y")));
    assertThat(eval("deparse(quote(2*(1+x)))"), equalTo(c("2 * (1 + x)")));
    assertThat(eval("deparse(quote(!x))"), equalTo(c("!x")));
    assertThat(eval("deparse(quote(x[1,drop=TRUE]))"), equalTo(c("x[1, drop = TRUE]")));
    assertThat(eval("deparse(quote(x[[1]]))"), equalTo(c("x[[1]]")));
    assertThat(eval("deparse(quote(x$y))"), equalTo(c("x$y")));
  }
}
