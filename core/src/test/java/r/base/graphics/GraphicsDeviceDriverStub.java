package r.base.graphics;

import r.lang.graphics.Color;
import r.lang.graphics.GraphicParameters;
import r.lang.graphics.GraphicsDeviceDriver;
import r.lang.graphics.geom.Dimension;
import r.lang.graphics.geom.Rectangle;

public class GraphicsDeviceDriverStub implements GraphicsDeviceDriver {

  private Rectangle deviceRegion;
  private Dimension size;
  
  public GraphicsDeviceDriverStub(Rectangle deviceRegion, Dimension size) {
    super();
    this.deviceRegion = deviceRegion;
    this.size = size;
  }

  public GraphicsDeviceDriverStub(int widthPixels, int heightPixels) {
    deviceRegion = new Rectangle(0, widthPixels, 0, heightPixels);
    size = new Dimension(widthPixels / 72d, heightPixels / 72d);
  }

  @Override
  public Dimension getInchesPerPixel() {
    return new Dimension(size.getWidth() / deviceRegion.getWidth(), 
                         size.getHeight() / deviceRegion.getHeight());
  }

  @Override
  public Dimension getCharacterSize() {
    return new Dimension(10.8, 14.4);
  }

  @Override
  public void drawRectangle(Rectangle bounds, Color fillColor,
      Color borderColor, GraphicParameters parameters) {
      
    
  }

  @Override
  public Rectangle getDeviceRegion() {
    return deviceRegion;
  }

  public Dimension getSize() {
    return size;
  }
  
}
