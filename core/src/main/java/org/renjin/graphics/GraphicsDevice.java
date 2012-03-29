package org.renjin.graphics;

import org.renjin.graphics.geom.Dimension;
import org.renjin.graphics.geom.Margins;
import org.renjin.graphics.geom.Point;
import org.renjin.graphics.geom.Rectangle;


public final class GraphicsDevice {

  
  private GraphicsDeviceDriver driver;
  private GraphicParameters parameters = new GraphicParameters();
  private GraphicParameters savedParameters = null;
 


  /**
   * The position of the innerRegion (device minus outerMargins) 
   * in device coordinates
   */
  private Rectangle innerRegion;
  
  
  /**
   * The current position of the figureRegion in normalized device coordinates
   * (NDC)
   */
  private Rectangle figureRegion = Rectangle.UNIT_RECT;
 
  /**
   * The the margins between the figure Region and the plot region
   */
  private Margins innerMargins = new Margins(5.1, 4.1, 4.1, 2.1);
  
  /**
   * The current position of the plot region in device coordinates
   *
   */
  private Rectangle plotRegion;
  
  private Margins outerMargins = new Margins(0, 0, 0, 0);
  private OuterMarginUnits units = OuterMarginUnits.LINES;
    
  /**
   *  An internal "zoom" factor to apply to ps and lwd 
   *  (for fit-to-window resizing in Windows) 
   */
  private double scale = 1.0; // TODO(alex): wtf?
   
  /**
   * Defines the coordinate system used to draw in {@code plotRegion}
   */
  private UserWindow userWindow = new UserWindow();


  public GraphicsDevice(GraphicsDeviceDriver driver) {
    super();
    this.driver = driver;
    
    /* For new devices, have to check the device's idea of its size
    * in case there has been a resize.
    */
    Dimension inchesPerPixel = driver.getInchesPerPixel();

    
//    
//    
//    dp->fig[0] = 0.0;
//      dp->fig[1] = 1.0;
//      dp->fig[2] = 0.0;
//      dp->fig[3] = 1.0;
//      dp->fUnits = NIC;
//      dp->defaultFigure = TRUE;   /* the figure region is calculated from */
//                                  /* the layout by default */
//      dp->pUnits = NFC;
//      dp->defaultPlot = TRUE;     /* the plot region is calculated as */
//                                  /* figure-margin by default */

  }

  /**
   * Calculates the size of a "line" unit in device coordinates.
   */
  private Dimension getLineSize() {
    
    double lineHeight = driver.getCharacterSize().getHeight() * 
        scale *
        parameters.getCexBase() *
        parameters.getMex();
    
    return new Dimension(
        lineHeight * driver.getInchesPerPixel().getAspectRatio(),
        lineHeight);
       
  }
  
  public Dimension getDefaultCharacterSize() {
    return new Dimension( driver.getCharacterSize().getWidth() * scale, 
                          driver.getCharacterSize().getHeight() * scale);
  }
  
  public GraphicParameters getParameters() {
    return parameters;
  }

  public final void saveParameters() {
    this.savedParameters = parameters.clone();
  }

  public final void restoreParameters() {
    if(savedParameters == null) {
      throw new IllegalStateException("saveParameters() has not previously been called");
    }
    this.parameters = savedParameters;
  }
 

  /**
   * The (NDC) coordinates of the figure region in the
   * display region of the device. If you set this, unlike S, you
   * start a new plot, so to add to an existing plot use
   * ‘new=TRUE’ as well.
   */
  public Rectangle getFigureRegion() {
    return figureRegion;
  }
  
  public void setFigureRegion(Rectangle figureCoordinates) {
    this.figureRegion = figureCoordinates;
  }
  
  /**
   * A vector of the form ‘c(x1, x2, y1, y2)’ giving the
   * coordinates of the plot region as fractions of the current
   * figure region.
   */
  public Rectangle getPlotRegion() {
    return getFigureRect().normalize(getPlotRegionRect());
  }

  private Rectangle getPlotRegionRect() {
    Rectangle figureInDevice = getFigureRect();
    Margins innerMarginsInDeviceUnits = innerMargins.multiplyBy(getLineSize());
    Rectangle plotRegionInDeviceUnits = figureInDevice.apply(innerMarginsInDeviceUnits);
    return plotRegionInDeviceUnits;
  }

  private Rectangle getFigureRect() {
    return driver.getDeviceRegion().denormalize(getFigureRegion());
  }
  
  public Dimension getPlotDimensions() {
    Rectangle plotRegionInDeviceUnits = getPlotRegionRect();
    
    return plotRegionInDeviceUnits.size().multiplyBy(driver.getInchesPerPixel());
  }

  public Point userToDevice(Point point) {
    return getPlotRegionRect().denormalize(
                    userWindow.normalize(point));
  }
  
  public Rectangle userToDevice(Rectangle rectangle) {
    return getPlotRegionRect().denormalize(userWindow.normalize(rectangle));
  }

  
  public void setPlotRegion(Rectangle plotRegion) {
    this.plotRegion = driver.getDeviceRegion().denormalize(plotRegion);
  }
  
  public Rectangle getUserCoordinates() {
    return userWindow.getUserCoordinates();
  }
  
  public void setUserCoordinates(Rectangle coords) {
    userWindow.setUserCoordinates(coords);
  }
  
  public void setUserLimits(Rectangle limits) {
    userWindow.setLimits(limits, parameters);
  }
  
  public Margins getInnerMargins() {
    return innerMargins;
  }

  public void setInnerMargins(Margins innerMargins) {
    this.innerMargins = innerMargins;
  }
  

  /**
   * Draws a rectangle, provided in user coordinates
   */
  public void drawRectangle(Rectangle bounds, Color fillColor, Color borderColor) {
    driver.drawRectangle(userToDevice(bounds), fillColor, borderColor, parameters);
  }
  
  public Margins getOuterMargins() {
    return outerMargins;
  }

  public void setOuterMargins(Margins outerMargins) {
    this.outerMargins = outerMargins;
  }

  public Dimension getDeviceSizeInInches() {
    return driver.getDeviceRegion()
            .size()
            .multiplyBy(driver.getInchesPerPixel());
  }

  public void text(Point anchor, double where, String text,
      Point center, double rotation) {
    
    
  }
}
