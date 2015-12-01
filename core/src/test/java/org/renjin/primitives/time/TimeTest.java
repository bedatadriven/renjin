package org.renjin.primitives.time;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class TimeTest extends EvalTestCase {


  
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
    
    /**
     * FIXME Unless I'm mistaken, this is not worth keeping the build broken. This could depend on where the code
     * is running from, since not all places use DST.
     */
//    assertThat(eval("t$isdst"), equalTo(c_i(1)));
  }
  
  @Test
  public void seq() {
    eval("length(seq(from=Sys.Date(),to = as.Date('2017-01-01'), by = 'month'))");
    
  }
  
  @Test
  public void timeZones() {
    eval("t <- .Internal(strptime('2011-11-06 09:27', '%Y-%m-%d %H:%M', tz='HST'))");
    assertThat(eval("t$hour"), equalTo(c_i(9)));
    assertThat(eval(".Internal(as.POSIXct(t, 'HST'))"), equalTo(c(1320607620d)));
  }
  
  @Ignore("This test seems to depend on the location or time settings.")
  @Test
  public void asPosixLt() throws IOException {
    topLevelContext.init();
    eval("ct <- as.POSIXct('2009-07-01 00:00:00')");
    assertThat(eval("ct"), equalTo(c(1246399200d)));
    assertThat(eval("format(ct)"), equalTo(c("2009-07-01")));
    assertThat(eval("names(format(ct))"), equalTo(NULL));
  }

  @Ignore("This test seems to depend on the location or time settings.")
  @Test
  public void printTime() throws IOException {
    topLevelContext.init();
    
    StringWriter stringWriter = new StringWriter();
    topLevelContext.getSession().setStdOut(new PrintWriter(stringWriter));

    eval("print(as.POSIXct('2009-07-01 00:00:00'))");

    assertThat(stringWriter.toString(), equalTo("[1] \"2009-07-01 CEST\"\n"));
  }

  @Test
  public void timeZoneProvided() {
    assumingBasePackagesLoad();
    
    eval("as.Date(as.POSIXct('2000-01-01 00:00:00 GMT', tz = 'GMT'))");
    
  }

  @Test
  public void strptimeExtra() {
    eval("t <- .Internal(strptime('2000-01-01 00:00:00 GMT', '%Y-%m-%d %H:%M:%OS', ''))");
    assertThat(eval("t$year"), equalTo(c_i(100)));


  }
  
  
  @Test
  public void strptimeBadInput() {
    eval("t <- .Internal(strptime('FOOOO!!!', '%Y-%m-%d %H:%M:%OS', ''))");
    
    assertThat(eval("is.na(t$sec)"), equalTo(c(true)));
    assertThat(eval("t$isdst"), equalTo(c_i(-1)));
  }

  @Test
  public void asDate() {
    assumingBasePackagesLoad();
    eval("x <- paste(2004, rep(1:4, 4:1), seq(1,20,2), sep = \"-\")");
    eval("print(x)");
    eval("y <- as.Date(x)");
    eval("print(y)");
    assertThat(eval("y"), equalTo(c(12418d, 12420d, 12422d, 12424d, 12457, 12459, 12461, 12492, 12494, 12527)));
  }
  
}
