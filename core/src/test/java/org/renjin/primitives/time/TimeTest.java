package org.renjin.primitives.time;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class TimeTest extends EvalTestCase {
  
  @Test
  public void strptime() {
    
    eval("t <- .Internal(strptime('2009-07-01 18:14:05', '%Y-%m-%d %H:%M:%OS', 'Europe/Amsterdam'))");
    
    assertThat(eval("t$sec"), equalTo(c_i(5)));
    assertThat(eval("t$min"), equalTo(c_i(14)));
    assertThat(eval("t$hour"), equalTo(c_i(18)));
    assertThat(eval("t$mday"), equalTo(c_i(1)));
    assertThat(eval("t$mon"), equalTo(c_i(6)));
    assertThat(eval("t$year"), equalTo(c_i(109)));
    assertThat(eval("t$wday"), equalTo(c_i(3)));
    assertThat(eval("t$yday"), equalTo(c_i(181)));
    
    
    // Verify that the daylight savings flag is set 
    // Because we specified timezone above, this should not be dependent on local settings
    assertThat(eval("t$isdst"), equalTo(c_i(1)));
  }
  
  @Test
  public void strptimeDst() {
    // 2015
    // NL: DST (CET) ended on 25 Oct
    // US: DST (EDT) ended on 2 Nov


    assertThat(eval("as.POSIXlt('2015-10-30', tz='Europe/Amsterdam')$isdst"), equalTo(c_i(0)));
    assertThat(eval("as.POSIXlt('2015-10-30', tz='America/New_York')$isdst"), equalTo(c_i(1)));


    assertThat(eval("as.POSIXlt('2015-12-30', tz='Europe/Amsterdam')$isdst"), equalTo(c_i(0)));
    assertThat(eval("as.POSIXlt('2015-12-30', tz='America/New_York')$isdst"), equalTo(c_i(0)));
  }
  
  @Test
  public void strpTimeWithZone() {
    eval("lt <- strptime('2015-15");
  }
  
  @Test
  public void strptimeWithOffset() {
    eval("t <- strptime('24/Aug/2014:17:57:26 +0200', '%d/%b/%Y:%H:%M:%S %z')");
    assertThat(eval("t$sec"), equalTo(c_i(26)));
    assertThat(eval("t$min"), equalTo(c_i(57)));
    assertThat(eval("t$hour"), equalTo(c_i(17)));
    assertThat(eval("t$mday"), equalTo(c_i(24)));
    assertThat(eval("t$mon"), equalTo(c_i(7)));
    assertThat(eval("t$year"), equalTo(c_i(114)));
    assertThat(eval("t$wday"), equalTo(c_i(0)));
    assertThat(eval("t$yday"), equalTo(c_i(235)));
    assertThat(eval("t$isdst"), equalTo(c_i(1)));
    assertThat(eval("t$gmtoff"), equalTo(c_i(7200)));
    assertThat(eval("attr(t, 'tzone')"), equalTo((SEXP)Null.INSTANCE));
  }


  @Test
  public void strptimeWithOffsetWithTzParam() {
    eval("t <- strptime('24/Aug/2014:17:57:26 +0200', '%d/%b/%Y:%H:%M:%S %z', tz = 'Pacific/Honolulu')");
    assertThat(eval("t$sec"), equalTo(c_i(26)));
    assertThat(eval("t$min"), equalTo(c_i(57)));
    assertThat(eval("t$hour"), equalTo(c_i(5)));
    assertThat(eval("t$mday"), equalTo(c_i(24)));
    assertThat(eval("t$mon"), equalTo(c_i(7)));
    assertThat(eval("t$year"), equalTo(c_i(114)));
    assertThat(eval("t$wday"), equalTo(c_i(0)));
    assertThat(eval("t$yday"), equalTo(c_i(235)));
    assertThat(eval("t$isdst"), equalTo(c_i(0)));
    assertThat(eval("attr(t, 'tzone')"), equalTo(c( "Pacific/Honolulu")));

    // GNU R 3.2.2 says this should be -7200, but that doesn't make much sense to me. 
    // Waiting for response from https://bugs.r-project.org/bugzilla/show_bug.cgi?id=16621
    assertThat(eval("t$gmtoff"), equalTo(c_i(-36000))); 

  }
  
  @Test
  public void seqByMonth() {
    assertThat(eval("s <- seq(from=as.Date('2015-01-01'), to = as.Date('2015-04-01'), by = 'month')"),
        equalTo(c(16436, 16467, 16495, 16526)));
  }
  
  @Test
  public void seqByDay() {
    assertThat(eval("seq(from= as.Date('2005-01-01'), to= as.Date('2005-01-05'), by= 'day')"),
        equalTo(c(12784, 12785, 12786, 12787, 12788)));
  }
  
  @Test
  public void cutByWeek() {
    eval("everyday = seq(from= as.Date('2005-1-1'), to= as.Date('2005-1-14'), by= 'day')");
    eval("res<-cut(everyday, breaks= 'week')");
    assertThat(eval("res"), equalTo(c_i( 1, 1, 2, 2, 2, 2 ,2, 2, 2, 3, 3, 3, 3, 3)));
    assertThat(eval("levels(res)"), equalTo(c("2004-12-27", "2005-01-03", "2005-01-10")));
  }
  
  @Test
  public void timeZones() {
    eval("t <- .Internal(strptime('2011-11-06 09:27', '%Y-%m-%d %H:%M', tz='HST'))");
    assertThat(eval("t$hour"), equalTo(c_i(9)));
    assertThat(eval("attr(t, 'tzone')"), equalTo(c("HST")));
    
    // _t_ now contains a time value expressed as calander date + time offset + time zone
    // as.POSIXct must convert this value to milliseconds since 1970-01-01 UTC.
    assertThat(eval(".Internal(as.POSIXct(t, 'HST'))"), equalTo(c(1320607620d)));
  }
  
  @Test
  public void posixLtWithNegativeFields() {
    eval("lt <-  list(sec = 0L, min = 0L, hour = 0L, mday = -4L, mon = 0L, year = 105L, wday = 6L, yday = 0L, isdst = -1L)");
    eval("class(lt) <- c('POSIXlt', 'POSIXt')");
    
    assertThat(eval("as.character(lt)"), equalTo(c("2004-12-27")));
  }
  
  @Test
  public void asPosixLt() throws IOException {
    topLevelContext.init();
    eval("ct <- as.POSIXct('2009-07-01 00:00:00', tz = 'UTC')");
    assertThat(eval("ct"), equalTo(c(1246406400d)));
    assertThat(eval("attr(ct, 'tzone')"), equalTo(c("UTC")));
    
    eval("lt <- as.POSIXlt(ct)");
    assertThat(eval("lt$sec"), equalTo(c_i(0)));
    assertThat(eval("lt$min"), equalTo(c_i(0)));
    assertThat(eval("lt$hour"), equalTo(c_i(0)));
    assertThat(eval("lt$mday"), equalTo(c_i(1)));
    assertThat(eval("lt$mon"), equalTo(c_i(7-1)));
    assertThat(eval("lt$year"), equalTo(c_i(2009-1900)));
    assertThat(eval("lt$wday"), equalTo(c_i(3)));
    assertThat(eval("lt$yday"), equalTo(c_i(181)));
    assertThat(eval("attr(lt, 'tzone')"), equalTo(c("UTC")));
    
    assertThat(eval("format(ct)"), equalTo(c("2009-07-01")));
    assertThat(eval("names(format(ct))"), equalTo(NULL));
  }
  
  @Test
  public void dateAsPosixLt() {
    eval("d <- as.Date(c('2015-02-15', '2015-04-29'))");
    eval("lt <- as.POSIXlt(d)");
    assertThat(eval("class(lt)"), equalTo(c("POSIXlt", "POSIXt")));
    assertThat(eval("names(unclass(lt))"), 
        equalTo(c("sec", "min", "hour", "mday", "mon", "year", "wday", "yday", "isdst")));

    assertThat(eval("lt$sec"), equalTo(c_i(0, 0)));
    assertThat(eval("lt$min"), equalTo(c_i(0, 0)));
    assertThat(eval("lt$hour"), equalTo(c_i(0, 0)));
    assertThat(eval("lt$mday"), equalTo(c_i(15, 29)));
    assertThat(eval("lt$mon"), equalTo(c_i(1, 3)));
    assertThat(eval("lt$year"), equalTo(c_i(115, 115)));
    assertThat(eval("lt$wday"), equalTo(c_i(0, 3)));
    assertThat(eval("lt$yday"), equalTo(c_i(45, 118)));
  }
  
  @Test
  public void naPosixLtToDate() {
    eval("lt <- as.POSIXlt(strptime(c('2014-01-01','xxx'), format='%Y-%m-%d'))");

    assertThat(eval("lt$year"), equalTo(c_i(114, IntVector.NA)));
    ;

    eval("d <- as.Date(lt)");
    eval("print(d)");
    assertThat(eval("is.na(d)"), equalTo(c(false, true)));
    assertThat(eval("is.na(d[2])"), equalTo(c(true)));
  }

  @Test
  public void printTime() throws IOException {
    topLevelContext.init();
    
    StringWriter stringWriter = new StringWriter();
    topLevelContext.getSession().setStdOut(new PrintWriter(stringWriter));

    eval("print(as.POSIXct('2009-07-01 00:00:00', tz = 'Europe/Amsterdam'))");

    assertThat(stringWriter.toString(), equalTo("[1] \"2009-07-01 CEST\"\n"));
  }

  @Test
  public void strpTimeSeconds() {

    eval("tt <-  strptime('1978-02-16 12:30:15', format='%Y-%m-%d %H:%M:%S', tz='GMT')");
    assertThat(eval("tt$sec"), equalTo(c_i(15)));
    assertThat(eval("tt$min"), equalTo(c_i(30)));
    assertThat(eval("tt$hour"), equalTo(c_i(12)));
    assertThat(eval("tt$mday"), equalTo(c_i(16)));
    assertThat(eval("tt$mon"), equalTo(c_i(1)));
    assertThat(eval("tt$year"), equalTo(c_i(78)));
    assertThat(eval("tt$wday"), equalTo(c_i(4)));
    assertThat(eval("tt$yday"), equalTo(c_i(46)));
  }
  
  @Test
  public void timeZoneProvided() {
    assumingBasePackagesLoad();
    
    eval("d <- as.Date(as.POSIXct('2000-01-01 00:00:00 GMT', tz = 'GMT'))");
    assertThat(eval("d"), equalTo(c(10957)));
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
