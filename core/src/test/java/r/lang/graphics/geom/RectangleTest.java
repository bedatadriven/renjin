package r.lang.graphics.geom;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RectangleTest {

  @Test
  public void denormalize() {
    
    Rectangle rectangle = new Rectangle(0, 150, 0, 200);
    Rectangle denormalized = rectangle.denormalize(rectangle.UNIT_RECT);
    
    assertThat(denormalized, equalTo(rectangle));
    
  }
  
}
