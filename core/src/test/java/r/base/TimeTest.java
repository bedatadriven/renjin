package r.base;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import r.EvalTestCase;

public class TimeTest extends EvalTestCase {

  @Test
  public void formatBuilder() {
    verifyFormat("2009-07-01 00:00:00", "%Y-%m-%d %H:%M:%OS", new DateTime(2009,7,1,0,0,0));
  }
  
  @Test
  public void strptime() {
    eval("t <- .Internal(strptime('2009-07-01 18:14:05', '%Y-%m-%d %H:%M:%OS', ''))");
    
    assertThat(eval("t$sec"), equalTo(c_i(5)));
    assertThat(eval("t$min"), equalTo(c_i(14)));
    assertThat(eval("t$hour"), equalTo(c_i(18)));
    assertThat(eval("t$mday"), equalTo(c_i(1)));
    assertThat(eval("t$mon"), equalTo(c_i(6)));
    assertThat(eval("t$year"), equalTo(c_i(109)));
    assertThat(eval("t$wday"), equalTo(c_i(3)));
    assertThat(eval("t$yday"), equalTo(c_i(181)));
    assertThat(eval("t$isdst"), equalTo(c_i(1)));
  }
  
  @Test
  public void asPosixLt() throws IOException {
    topLevelContext.init();
    eval("ct <- as.POSIXct('2009-07-01 00:00:00')");
    assertThat(eval("ct"), equalTo(c(1246399200d)));
  }
  
  @Test
  public void strptimeBadInput() {
    eval("t <- .Internal(strptime('FOOOO!!!', '%Y-%m-%d %H:%M:%OS', ''))");
    
    assertThat(eval("is.na(t$sec)"), equalTo(c(true)));
    assertThat(eval("t$isdst"), equalTo(c_i(-1)));
  }
  
  private void verifyFormat(String x, String format, DateTime dateTime) {
    DateTimeFormatter formatter = Time.formatterFromRSpecification(format);
    assertThat(formatter.print(dateTime), equalTo(x));
    assertThat(formatter.parseDateTime(x), equalTo(dateTime));
  }
  
  
}
