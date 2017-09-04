/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.graphics;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.graphics.GraphicsDevice;
import org.renjin.graphics.GraphicsDevices;
import org.renjin.graphics.device.AwtGraphicsDevice;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertThat;


@Ignore("need to work out which platform dep code to use -- unix/linux in the R sources ")
public class GraphicsTest extends EvalTestCase {
  
  @Before
  public void setupPackages() {

  }
  
  @Test
  public void coordinateSystems() throws IOException {

    // compared to output from R2.12
    // with png(filename='test.png', width=420, height=340)
    
    
    GraphicsDeviceDriverStub driver = new GraphicsDeviceDriverStub(420, 340);
    GraphicsDevice device = new GraphicsDevice(driver);
    topLevelContext.getSingleton(GraphicsDevices.class).setActive(device); 
    
    assertThat("din", eval("par('din')"), elementsIdenticalTo(c(driver.getSize().getWidth(), driver.getSize().getHeight())));
    assertThat("fig", eval("par('fig')"), elementsIdenticalTo(c(0, 1, 0, 1)));
    assertThat("mar", eval("par('mar')"), elementsIdenticalTo(c(5.1,4.1,4.1,2.1)));
    assertThat("cra", eval("par('cra')"), elementsIdenticalTo(c(10.8, 14.4)));
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

    // compared to output from R2.12
    // with png(filename='test.png', width=420, height=340)
    
    GraphicsDeviceDriverStub driver = new GraphicsDeviceDriverStub(420, 340);
    GraphicsDevice device = new GraphicsDevice(driver);
    topLevelContext.getSingleton(GraphicsDevices.class).setActive(device); 
   
    eval("barplot(c(1,2,3), main='Distribution', xlab='Number')");
  
    assertThat("usr", eval("par('usr')"), closeTo(c(0.064, 3.736, -0.030, 3.00), 0.001));
    assertThat("pin", eval("par('pin')"), closeTo(c(4.59333, 2.88222), 0.001));
    assertThat(eval("grconvertX(0, from='user', to='device')"), closeTo(c(53.27582), 0.0001));
    assertThat(eval("grconvertY(0, from='user', to='device')"), closeTo(c(264.5053), 0.0001));
    assertThat(eval("grconvertY(1, from='user', to='device')"), closeTo(c(196.0169), 0.0001));
  }
  

  @Test
  public void awtIntegrationTest() throws IOException {
    BufferedImage image = new BufferedImage(420, 340, ColorSpace.TYPE_RGB);

    Graphics2D g2d = (Graphics2D) image.getGraphics();
    g2d.setColor(Color.WHITE);
    g2d.setBackground(Color.WHITE);
    g2d.fill(g2d.getDeviceConfiguration().getBounds());
    
    AwtGraphicsDevice driver = new AwtGraphicsDevice(g2d);
    topLevelContext.getSingleton(GraphicsDevices.class).setActive(new GraphicsDevice(driver));
    
    try {
      eval("barplot(c(1,2,3), main='Distribution', xlab='Number')");
    } finally {
      FileOutputStream fos = new FileOutputStream("target/simplestPossible.png");
      ImageIO.write(image, "PNG", fos);
      fos.close();
      
    }
  }

}
