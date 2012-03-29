package org.renjin.graphics;

import org.renjin.graphics.geom.Dimension;
import org.renjin.graphics.geom.Rectangle;

public interface GraphicsDeviceDriver {
  
  /**
   * 
   * @return the size of a pixel, in inches.
   */
  Dimension getInchesPerPixel();
  
  /**
   * 
   * @return the size of the default character, in pixels
   */
  Dimension getCharacterSize();
  
  
  void drawRectangle(Rectangle bounds, Color fillColor, Color borderColor, 
      GraphicParameters parameters);
  
  Rectangle getDeviceRegion();
  
  

}
