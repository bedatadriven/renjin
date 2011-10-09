package r.graphics;

import org.junit.Test;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class AwtGraphicsDeviceTest {

  @Test
  public void transform() {

    BufferedImage image = new BufferedImage(300, 300, ColorSpace.TYPE_RGB);
    Graphics2D g2d = (Graphics2D) image.getGraphics();
    AwtGraphicsDevice device = new AwtGraphicsDevice(g2d);

    device.setPlotWindow(1, 0, 10, 30);

    Point2D src = new Point2D.Float(1, 30);
    Point2D dst = new Point2D.Float();

    g2d.getTransform().transform(src, dst);

    assertThat("scale Y", g2d.getTransform().getScaleY(), equalTo(-10d));


    assertThat(dst.getX(), equalTo(0d));
    assertThat(dst.getY(), equalTo(0d));
  }

}
