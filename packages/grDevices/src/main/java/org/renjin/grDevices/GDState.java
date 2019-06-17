package org.renjin.grDevices;

import java.awt.*;

/**
 * object storing the current graphics state
 */
class GDState {
  private Color col;
  private Color fill;
  private Font font;

  public Color getCol() {
    return col;
  }

  public void setCol(Color col) {
    this.col = col;
  }

  public Color getFill() {
    return fill;
  }

  public void setFill(Color fill) {
    this.fill = fill;
  }

  public Font getFont() {
    return font;
  }

  public void setFont(Font font) {
    this.font = font;
  }
}
