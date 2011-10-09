package r.lang.graphics;

public class GraphicParameters {

  private AxisIntervalCalculationStyle xAxisIntervalCalculationStyle =
            AxisIntervalCalculationStyle.REGULAR;
  private AxisIntervalCalculationStyle yAxisIntervalCalculationStyle =
            AxisIntervalCalculationStyle.REGULAR;

  private ClippingMode clippingMode = ClippingMode.PLOT;
  private Color foreground = Color.BLACK;
  private Color background = Color.TRANSPARENT_WHITE;
  private Color color = Color.TRANSPARENT_WHITE;
  private LineType lineType = LineType.SOLID;
  private double lineWidth = 1;

  /**
   *
   * @return the X axis calculation style ("xaxs")
   */
  public AxisIntervalCalculationStyle getxAxisIntervalCalculationStyle() {
    return xAxisIntervalCalculationStyle;
  }

  public GraphicParameters setxAxisIntervalCalculationStyle(AxisIntervalCalculationStyle xAxisIntervalCalculationStyle) {
    this.xAxisIntervalCalculationStyle = xAxisIntervalCalculationStyle;
    return this;
  }

  public AxisIntervalCalculationStyle getyAxisIntervalCalculationStyle() {
    return yAxisIntervalCalculationStyle;
  }

  public GraphicParameters setyAxisIntervalCalculationStyle(AxisIntervalCalculationStyle yAxisIntervalCalculationStyle) {
    this.yAxisIntervalCalculationStyle = yAxisIntervalCalculationStyle;
    return this;
  }

  public ClippingMode getClippingMode() {
    return clippingMode;
  }

  public void setClippingMode(ClippingMode clippingMode) {
    this.clippingMode = clippingMode;
  }

  public Color getForeground() {
    return foreground;
  }

  public GraphicParameters setForeground(Color foreground) {
    this.foreground = foreground;
    return this;
  }

  public LineType getLineType() {
    return lineType;
  }

  public GraphicParameters setLineType(LineType lineType) {
    this.lineType = lineType;
    return this;
  }

  public double getLineWidth() {
    return lineWidth;
  }

  public GraphicParameters setLineWidth(double lineWidth) {
    this.lineWidth = lineWidth;
    return this;
  }

  public Color getBackground() {
    return background;
  }

  public GraphicParameters setBackground(Color background) {
    this.background = background;
    return this;
  }

  public Color getColor() {
    return color;
  }

  public GraphicParameters setColor(Color color) {
    this.color = color;
    return this;
  }

  @Override
  protected GraphicParameters clone() {
    GraphicParameters clone = new GraphicParameters();
    clone.clippingMode = clippingMode;
    clone.foreground = foreground;
    clone.background = background;
    clone.color = color;
    clone.lineType = lineType;
    clone.lineWidth = lineWidth;
    clone.xAxisIntervalCalculationStyle = xAxisIntervalCalculationStyle;
    clone.yAxisIntervalCalculationStyle = yAxisIntervalCalculationStyle;
    return clone;
  }
}
