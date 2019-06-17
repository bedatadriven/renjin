package org.renjin.grDevices;

import java.awt.*;

class GDFont implements GDObject {

  private Font font;

  /**
   * this is to work around a bug in Java on Windows where the Symbol font is incorrectly mapped and
   * requires us to force another font for the Symbol characters. According to
   * Simon Urbanek, Mac OS X is fine with Symbol, Windows is not, so we'll fix this for Windows only
   */
  private static final boolean USE_SYMBOL_FONT = !System.getProperty("os.name", "").startsWith("Win");

  public GDFont(double cex, double ps, double lineheight, int face, String family) {
    int jFT = Font.PLAIN;
    if (face == 2) {
      jFT = Font.BOLD;
    }
    if (face == 3) {
      jFT = Font.ITALIC;
    }
    if (face == 4) {
      jFT = Font.BOLD | Font.ITALIC;
    }
    if (face == 5 && USE_SYMBOL_FONT) {
      family = "Symbol";
    }
    font = new Font(family.equals("") ? null : family, jFT, (int) (cex * ps + 0.5));
  }

  public Font getFont() {
    return font;
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    g.setFont(font);
    gs.setFont(font);
  }
}
