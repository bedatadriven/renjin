package org.renjin.primitives.io.connections;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PushbackBufferedReaderTest {
  
  @Test
  public void test() throws IOException {
    StringReader original = new StringReader("the quick\nsecond line\n");
    PushbackBufferedReader reader = new PushbackBufferedReader(original);
    
    assertThat(reader.readLine(), equalTo("the quick"));
    reader.pushBack("foo\r\n");
    reader.pushBack("doohoo\n");
    assertThat(reader.readLine(), equalTo("doohoo"));
    assertThat(reader.readLine(), equalTo("foo"));
    assertThat(reader.readLine(), equalTo("second line"));
  }

}
