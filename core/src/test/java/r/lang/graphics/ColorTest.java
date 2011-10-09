package r.lang.graphics;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ColorTest {

  @Test
  public void names() {
    assertThat(Color.fromRGB(255,0,0).toString(), equalTo("red"));
    assertThat(Color.fromName("blue").getBlue(), equalTo(0xFF));
  }

}
