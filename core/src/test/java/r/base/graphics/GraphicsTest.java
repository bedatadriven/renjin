package r.base.graphics;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import r.EvalTestCase;
import r.graphics.AwtGraphicsDevice;
import r.lang.graphics.GraphicsDevice;

public class GraphicsTest extends EvalTestCase {
  
  
  @Test
  public void coordinateSystems() throws IOException {
    topLevelContext.init();

    // compared to output from R2.12
    // with png(filename='test.png', width=420, height=340)
    
    
    GraphicsDeviceDriverStub driver = new GraphicsDeviceDriverStub(420, 340);
    GraphicsDevice device = new GraphicsDevice(driver);
    topLevelContext.getGlobals().getGraphicsDevices().setActive(device); 
    
    assertThat("din", eval("par('din')"), equalTo(c(driver.getSize().getWidth(), driver.getSize().getHeight())));
    assertThat("fig", eval("par('fig')"), equalTo(c(0, 1, 0, 1)));
    assertThat("mar", eval("par('mar')"), equalTo(c(5.1,4.1,4.1,2.1)));
    assertThat("cra", eval("par('cra')"), equalTo(c(10.8, 14.4)));
    assertThat("pin", eval("par('pin')"), closeTo(c( 4.593333, 2.882222), 0.0001));
    assertThat("plt", eval("par('plt')"), closeTo(c( 0.1405714, 0.9280000, 0.2160000, 0.8263529), 0.0001));
      
    eval("plot.new()");
  
    assertThat("usr", eval("par('usr')"), closeTo(c(-0.04,1.04,-0.04,1.04), 0.001));
    assertThat(eval("grconvertX(0, from='user', to='device')"), closeTo(c(71.28889), 0.0001));
    assertThat(eval("grconvertY(0, from='user', to='device')"), closeTo(c(258.8741), 0.0001));
    assertThat(eval("grconvertY(1, from='user', to='device')"), closeTo(c(66.72593), 0.0001));
  }
 
  @Test
  public void plotWindow() throws IOException {
    topLevelContext.init();

    // compared to output from R2.12
    // with png(filename='test.png', width=420, height=340)
    
    GraphicsDeviceDriverStub driver = new GraphicsDeviceDriverStub(420, 340);
    GraphicsDevice device = new GraphicsDevice(driver);
    topLevelContext.getGlobals().getGraphicsDevices().setActive(device); 
   
    eval("barplot(c(1,2,3), main='Distribution', xlab='Number')");
  
    assertThat("usr", eval("par('usr')"), closeTo(c(0.064, 3.736, -0.030, 3.00), 0.001));
    assertThat("pin", eval("par('pin')"), closeTo(c(4.59333, 2.88222), 0.001));
    assertThat(eval("grconvertX(0, from='user', to='device')"), closeTo(c(53.27582), 0.0001));
    assertThat(eval("grconvertY(0, from='user', to='device')"), closeTo(c(264.5053), 0.0001));
    assertThat(eval("grconvertY(1, from='user', to='device')"), closeTo(c(196.0169), 0.0001));
  }
  

  @Test
  public void awtIntegrationTest() throws IOException {
    topLevelContext.init();
    BufferedImage image = new BufferedImage(420, 340, ColorSpace.TYPE_RGB);

    Graphics2D g2d = (Graphics2D) image.getGraphics();
    g2d.setColor(Color.WHITE);
    g2d.setBackground(Color.WHITE);
    g2d.fill(g2d.getDeviceConfiguration().getBounds());
    
    AwtGraphicsDevice driver = new AwtGraphicsDevice(g2d);
    topLevelContext.getGlobals().getGraphicsDevices().setActive(new GraphicsDevice(driver));
    
    try {
      eval("barplot(c(1,2,3), main='Distribution', xlab='Number')");
    } finally {
      FileOutputStream fos = new FileOutputStream("target/simplestPossible.png");
      ImageIO.write(image, "PNG", fos);
      fos.close();
      
    
      
    }
  }

}
