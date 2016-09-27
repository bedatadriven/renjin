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
package org.renjin.graphics;


public class GraphicParameters {

  private AxisStyle xAxisStyle = new AxisStyle();
  private AxisStyle yAxisStyle = new AxisStyle();

  private ClippingMode clippingMode = ClippingMode.PLOT;
  private Color foreground = Color.BLACK;
  private Color background = Color.TRANSPARENT_WHITE;
  private Color color = Color.TRANSPARENT_WHITE;
  private LineType lineType = LineType.SOLID;
  private double lineWidth = 1;

  
  private double cexBase = 1.0;
  
  private TextStyle mainTitleStyle = new TextStyle(1.2, Color.BLACK);
  private TextStyle axisAnnotationStyle = new TextStyle();
  private TextStyle xyLabelStyle = new TextStyle();
  private TextStyle subTitleStyle = new TextStyle();

  /**
   * ‘mex’ is a character size expansion factor which is used to
          describe coordinates in the margins of plots. Note that this
          does not change the font size, rather specifies the size of
          font (as a multiple of ‘csi’) used to convert between ‘mar’
          and ‘mai’, and between ‘oma’ and ‘omi’.

          This starts as ‘1’ when the device is opened, and is reset
          when the layout is changed (alongside resetting ‘cex’).

   */
  private double mex = 1.0;

  public ClippingMode getClippingMode() {
    return clippingMode;
  }

  public AxisStyle getxAxisStyle() {
    return xAxisStyle;
  }

  public AxisStyle getyAxisStyle() {
    return yAxisStyle;
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

  public double getMex() {
    return mex;
  }

  public void setMex(double mex) {
    this.mex = mex;
  }

  /**
   * A numerical value giving the amount by which plotting text
   * and symbols should be magnified relative to the default.
   * Note that some graphics functions such as ‘plot.default’ have
   * an _argument_ of this name which _multiplies_ this graphical
   * parameter, and some functions such as ‘points’ accept a
   * vector of values which are recycled.  Other uses will take
   * just the first value if a vector of length greater than one
   * is supplied.
   *
   * <p>This starts as ‘1’ when a device is opened, and is reset when
   * the layout is changed, e.g. by setting ‘mfrow’.
   */
  public double getCexBase() {
    return cexBase;
  }

  public void setCexBase(double cexBase) {
    this.cexBase = cexBase;
  }

  public TextStyle getMainTitleStyle() {
    return mainTitleStyle;
  }

  public void setMainTitleStyle(TextStyle mainTitleStyle) {
    this.mainTitleStyle = mainTitleStyle;
  }

  public TextStyle getAxisAnnotationStyle() {
    return axisAnnotationStyle;
  }

  public void setAxisAnnotationStyle(TextStyle axisAnnotationStyle) {
    this.axisAnnotationStyle = axisAnnotationStyle;
  }

  public TextStyle getXyLabelStyle() {
    return xyLabelStyle;
  }

  public void setXyLabelStyle(TextStyle xyLabelStyle) {
    this.xyLabelStyle = xyLabelStyle;
  }

  public TextStyle getSubTitleStyle() {
    return subTitleStyle;
  }

  public void setSubTitleStyle(TextStyle subTitleStyle) {
    this.subTitleStyle = subTitleStyle;
  }

  public void setxAxisStyle(AxisStyle xAxisStyle) {
    this.xAxisStyle = xAxisStyle;
  }

  public void setyAxisStyle(AxisStyle yAxisStyle) {
    this.yAxisStyle = yAxisStyle;
  }

  public AxisStyle getAxisStyle(Axis axis) {
    switch(axis) {
    case X:
      return xAxisStyle;
    case Y:
      return yAxisStyle;
    }
    throw new IllegalArgumentException("" + axis);
  }
  
  public AxisStyle getXAxisStyle() {
    return xAxisStyle;
  }
  
  public AxisStyle getYAxisStyle() {
    return yAxisStyle;
  }
  
  /**
   * The value of ‘adj’ determines the way in which text strings are justified
   * in ‘text’, ‘mtext’ and ‘title’. A value of ‘0’ produces left-justified
   * text, ‘0.5’ (the default) centered text and ‘1’ right-justified text. (Any
   * value in [0, 1] is allowed, and on most devices values outside that
   * interval will also work.) Note that the ‘adj’ argument of ‘text’ also
   * allows ‘adj = c(x, y)’ for different adjustment in x- and y- directions.
   * Note that whereas for ‘text’ it refers to positioning of text about a
   * point, for ‘mtext’ and ‘title’ it controls placement within the plot or
   * device region.
   */
  public double getTextJustification() {
    // TODO !
    return 0.5;
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
    clone.xAxisStyle = xAxisStyle.clone();
    clone.yAxisStyle = yAxisStyle.clone();
    clone.cexBase = cexBase;
    clone.mainTitleStyle = mainTitleStyle.clone();
    clone.subTitleStyle = subTitleStyle.clone();
   
    return clone;
  }
}