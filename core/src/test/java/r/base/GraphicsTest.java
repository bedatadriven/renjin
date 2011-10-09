package r.base;


import org.junit.Ignore;
import org.junit.Test;
import r.EvalTestCase;
import r.graphics.AwtGraphicsDevice;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

public class GraphicsTest extends EvalTestCase {
  

  @Test
  @Ignore("work in progress")
  public void simplestPossible() throws IOException {
    topLevelContext.init();
    BufferedImage image = new BufferedImage(300, 300, ColorSpace.TYPE_RGB);

    Graphics2D g2d = (Graphics2D) image.getGraphics();
    g2d.setColor(Color.WHITE);
    g2d.setBackground(Color.WHITE);
    g2d.fill(g2d.getDeviceConfiguration().getBounds());
    
    AwtGraphicsDevice device = new AwtGraphicsDevice(g2d);
    topLevelContext.getGlobals().getGraphicsDevices().setActive(device);

    try {
      eval("barplot(c(1,2,3), main='Distribution', xlab='Number')");
    } finally {
      FileOutputStream fos = new FileOutputStream("simplestPossible.png");
      ImageIO.write(image, "PNG", fos);
      fos.close();
    }
  }

}
