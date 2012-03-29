package org.renjin.graphics;

public class TextStyle {
  private double fontSizeFactor = 1.0;
  private Color color = Color.BLACK;
  private String fontName;
  
  public TextStyle() {
  }
  
  public TextStyle(double fontSizeFactor, Color color) {
    super();
    this.fontSizeFactor = fontSizeFactor;
    this.color = color;
  }
  
  public double getFontSizeFactor() {
    return fontSizeFactor;
  }

  public void setFontSizeFactor(double fontSizeFactor) {
    this.fontSizeFactor = fontSizeFactor;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }
  
  @Override
  public TextStyle clone() {
    return new TextStyle(fontSizeFactor, color);
  }
}
