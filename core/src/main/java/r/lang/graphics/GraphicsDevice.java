package r.lang.graphics;

public abstract class GraphicsDevice {

  private GraphicParameters parameters = new GraphicParameters();
  private GraphicParameters savedParameters = null;

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
 
  public abstract void setPlotWindow(double x1, double y1, double x2, double y2);

  public abstract void drawRectangle(double xleft, double ybottom, double xright, double ytop, Color fillColor, Color borderColor);
}
