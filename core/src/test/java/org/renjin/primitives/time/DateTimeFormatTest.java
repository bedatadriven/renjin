package org.renjin.primitives.time;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.renjin.primitives.time.DateTimeFormat;

public class DateTimeFormatTest {

  @Test
  public void formatBuilder() {
    verifyFormat("2009-07-01 00:00:00", "%Y-%m-%d %H:%M:%OS", new DateTime(2009,7,1,0,0,0));
  }


  private void verifyFormat(String x, String format, DateTime dateTime) {
    DateTimeFormatter formatter = DateTimeFormat.forPattern(format);
    assertThat(formatter.print(dateTime), equalTo(x));
    assertThat(formatter.parseDateTime(x), equalTo(dateTime));
  }
  
}
