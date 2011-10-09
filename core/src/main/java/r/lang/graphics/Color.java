package r.lang.graphics;

import r.lang.Context;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Vector;

/**
 *      Some Notes on Color
 *
 *      R uses a 24-bit color model.  Colors are specified in 32-bit
 *      integers which are partitioned into 4 bytes as follows.
 *
 * <pre>
 *              <-- most sig        least sig -->
 *              +-------------------------------+
 *              |   0   | blue  | green |  red  |
 *              +-------------------------------+
 * </pre>
 *
 *      The red, green and blue bytes can be extracted as follows.
 *
 * <pre>
 *              red   = ((color      ) & 255)
 *              green = ((color >>  8) & 255)
 *              blue  = ((color >> 16) & 255)
 * </pre>
 *
 *      Changes as from 1.4.0: use top 8 bits as an alpha channel.
 *      0 = opaque, 255 = transparent.
 * Changes as from 2.0.0:  use top 8 bits as full alpha channel
 *      255 = opaque, 0 = transparent
 *      [to conform with SVG, PDF and others]
 *      and everything in between is used
 *      [which means that NA is not stored as an internal colour;
 *       it is converted to R_RGBA(255, 255, 255, 0)]
 */
public class Color {

  private int value;

  public static final int TRANSPARENT = 0;
  public static final int OPAQUE = 0xFF;

  public static final Color TRANSPARENT_WHITE = fromRGBA(0xFF,0xFF,0xFF, TRANSPARENT);
  public static final Color BLACK = fromRGB(0,0,0);

  public Color(int argb) {
    this.value = argb;
  }

  public static Color fromRGB(int r, int g, int b) {
    return  new Color( ((r)|((g)<<8)|((b)<<16)|0xFF000000) );
  }

  public static Color fromRGBA(int r, int g, int b, int a) {
    return new Color(((r)|((g)<<8)|((b)<<16)|((a)<<24)));
  }

  public static Color fromName(String name) {
    for(NamedColor namedColor : COLOR_DATABASE) {
      if(namedColor.name.equals(name)) {
        return namedColor.color;
      }
    }
    throw new IllegalArgumentException("No such color name: " + name);
  }

  public static Color fromExp(ColorPalette palette, Color background, Vector vector, int elementIndex) {
    if(vector.isElementNA(elementIndex)) {
      return TRANSPARENT_WHITE;
    } else if(vector instanceof StringVector) {
      String string = vector.getElementAsString(elementIndex);
      if(!Character.isDigit(string.codePointAt(0))) {
        return fromString(vector.getElementAsString(elementIndex));
      }
    } 
    int index = vector.getElementAsInt(elementIndex);
    if(index <= 0) {
      return background;
    } else {
      return palette.get(index);
    }
  }

  public static Color fromExp(Context context, GraphicsDevice device, Vector colorVector, int elementIndex) {
    return fromExp(context.getGlobals().getColorPalette(), device.getParameters().getBackground(), colorVector,
            elementIndex % colorVector.length());  
  }

  public static Color fromString(String string) {
    if(string.length() >= 1 && string.charAt(0) == '#') {
      return fromHexString(string);
    } else {
      return fromName(string);
    }
  }

  public static Color fromHexString(String rgb) {
    int r, g, b, a = OPAQUE;

    if(rgb.length() < 1 || rgb.charAt(0) != '#') {
      throw new IllegalArgumentException("invalid RGB specification: " + rgb + " (must begin with '#')");
    }

    switch (rgb.length()) {
    case 9:
        a = Integer.valueOf(rgb.substring(7, 9), 16);
    case 7:
        r = Integer.valueOf(rgb.substring(1, 3), 16);
        g = Integer.valueOf(rgb.substring(3, 5), 16);
        b = Integer.valueOf(rgb.substring(5, 7), 16);
        break;
    default:
      throw new IllegalArgumentException("invalid RGB specification: " + rgb);
    }
    return fromRGBA(r, g, b, a);
  }
  
  public int getRed() {
    return value & 255;
  }

  public int getGreen() {
    return (((value)>> 8)&255);
  }

  public int getBlue() {
    return (((value)>>16)&255);
  }

  public int getAlpha() {
    return  (((value)>>24)&255);
  }

  public boolean isOpaque() {
    return getAlpha() == 255;
  }

  public boolean isTransparent() {
    return getAlpha() == 0;
  }

  private char hexDigit(int digit) {
    return Character.forDigit(digit, 16);
  }

  private static int valueOfHexDigit(char hex) {
    return Character.digit(hex, 16);
  }

  public SEXP toExp() {
    return new StringVector(toString());
  }

  @Override
  public String toString() {
    if(isOpaque()) {
      for(NamedColor namedColor : COLOR_DATABASE) {
        if(namedColor.color.equals(this)) {
          return namedColor.name;
        }
      }
      char hexString[] = new char[7];
      hexString[0] = '#';
      hexString[1] = hexDigit((value >>  4) & 15);
      hexString[2] = hexDigit((value      ) & 15);
      hexString[3] = hexDigit((value >> 12) & 15);
      hexString[4] = hexDigit((value >>  8) & 15);
      hexString[5] = hexDigit((value >> 20) & 15);
      hexString[6] = hexDigit((value >> 16) & 15);
      return new String(hexString);

    } else if (isTransparent()) {
      return "transparent";

    } else {
      char hexString[] = new char[8];
      hexString[0] = '#';
      hexString[1] = hexDigit((value >>  4) & 15);
      hexString[2] = hexDigit((value      ) & 15);
      hexString[3] = hexDigit((value >> 12) & 15);
      hexString[4] = hexDigit((value >>  8) & 15);
      hexString[5] = hexDigit((value >> 20) & 15);
      hexString[6] = hexDigit((value >> 16) & 15);
      hexString[7] = hexDigit((value >> 28) & 15);
      hexString[8] = hexDigit((value >> 24) & 15);
      return new String(hexString);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Color color = (Color) o;

    if (value != color.value) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return value;
  }

  private static class NamedColor {
    private String name;
    private Color color;

    private NamedColor(String name, Color color) {
      this.name = name;
      this.color = color;
    }
  }

  private static final NamedColor COLOR_DATABASE[] = new NamedColor[] {
      new NamedColor("white", fromRGB(0xFF, 0xFF, 0xFF)),
      new NamedColor("aliceblue", fromRGB(0xF0, 0xF8, 0xFF)),
      new NamedColor("antiquewhite", fromRGB(0xFA, 0xEB, 0xD7)),
      new NamedColor("antiquewhite1", fromRGB(0xFF, 0xEF, 0xDB)),
      new NamedColor("antiquewhite2", fromRGB(0xEE, 0xDF, 0xCC)),
      new NamedColor("antiquewhite3", fromRGB(0xCD, 0xC0, 0xB0)),
      new NamedColor("antiquewhite4", fromRGB(0x8B, 0x83, 0x78)),
      new NamedColor("aquamarine", fromRGB(0x7F, 0xFF, 0xD4)),
      new NamedColor("aquamarine1", fromRGB(0x7F, 0xFF, 0xD4)),
      new NamedColor("aquamarine2", fromRGB(0x76, 0xEE, 0xC6)),
      new NamedColor("aquamarine3", fromRGB(0x66, 0xCD, 0xAA)),
      new NamedColor("aquamarine4", fromRGB(0x45, 0x8B, 0x74)),
      new NamedColor("azure", fromRGB(0xF0, 0xFF, 0xFF)),
      new NamedColor("azure1", fromRGB(0xF0, 0xFF, 0xFF)),
      new NamedColor("azure2", fromRGB(0xE0, 0xEE, 0xEE)),
      new NamedColor("azure3", fromRGB(0xC1, 0xCD, 0xCD)),
      new NamedColor("azure4", fromRGB(0x83, 0x8B, 0x8B)),
      new NamedColor("beige", fromRGB(0xF5, 0xF5, 0xDC)),
      new NamedColor("bisque", fromRGB(0xFF, 0xE4, 0xC4)),
      new NamedColor("bisque1", fromRGB(0xFF, 0xE4, 0xC4)),
      new NamedColor("bisque2", fromRGB(0xEE, 0xD5, 0xB7)),
      new NamedColor("bisque3", fromRGB(0xCD, 0xB7, 0x9E)),
      new NamedColor("bisque4", fromRGB(0x8B, 0x7D, 0x6B)),
      new NamedColor("black", fromRGB(0x00, 0x00, 0x00)),
      new NamedColor("blanchedalmond", fromRGB(0xFF, 0xEB, 0xCD)),
      new NamedColor("blue", fromRGB(0x00, 0x00, 0xFF)),
      new NamedColor("blue1", fromRGB(0x00, 0x00, 0xFF)),
      new NamedColor("blue2", fromRGB(0x00, 0x00, 0xEE)),
      new NamedColor("blue3", fromRGB(0x00, 0x00, 0xCD)),
      new NamedColor("blue4", fromRGB(0x00, 0x00, 0x8B)),
      new NamedColor("blueviolet", fromRGB(0x8A, 0x2B, 0xE2)),
      new NamedColor("brown", fromRGB(0xA5, 0x2A, 0x2A)),
      new NamedColor("brown1", fromRGB(0xFF, 0x40, 0x40)),
      new NamedColor("brown2", fromRGB(0xEE, 0x3B, 0x3B)),
      new NamedColor("brown3", fromRGB(0xCD, 0x33, 0x33)),
      new NamedColor("brown4", fromRGB(0x8B, 0x23, 0x23)),
      new NamedColor("burlywood", fromRGB(0xDE, 0xB8, 0x87)),
      new NamedColor("burlywood1", fromRGB(0xFF, 0xD3, 0x9B)),
      new NamedColor("burlywood2", fromRGB(0xEE, 0xC5, 0x91)),
      new NamedColor("burlywood3", fromRGB(0xCD, 0xAA, 0x7D)),
      new NamedColor("burlywood4", fromRGB(0x8B, 0x73, 0x55)),
      new NamedColor("cadetblue", fromRGB(0x5F, 0x9E, 0xA0)),
      new NamedColor("cadetblue1", fromRGB(0x98, 0xF5, 0xFF)),
      new NamedColor("cadetblue2", fromRGB(0x8E, 0xE5, 0xEE)),
      new NamedColor("cadetblue3", fromRGB(0x7A, 0xC5, 0xCD)),
      new NamedColor("cadetblue4", fromRGB(0x53, 0x86, 0x8B)),
      new NamedColor("chartreuse", fromRGB(0x7F, 0xFF, 0x00)),
      new NamedColor("chartreuse1", fromRGB(0x7F, 0xFF, 0x00)),
      new NamedColor("chartreuse2", fromRGB(0x76, 0xEE, 0x00)),
      new NamedColor("chartreuse3", fromRGB(0x66, 0xCD, 0x00)),
      new NamedColor("chartreuse4", fromRGB(0x45, 0x8B, 0x00)),
      new NamedColor("chocolate", fromRGB(0xD2, 0x69, 0x1E)),
      new NamedColor("chocolate1", fromRGB(0xFF, 0x7F, 0x24)),
      new NamedColor("chocolate2", fromRGB(0xEE, 0x76, 0x21)),
      new NamedColor("chocolate3", fromRGB(0xCD, 0x66, 0x1D)),
      new NamedColor("chocolate4", fromRGB(0x8B, 0x45, 0x13)),
      new NamedColor("coral", fromRGB(0xFF, 0x7F, 0x50)),
      new NamedColor("coral1", fromRGB(0xFF, 0x72, 0x56)),
      new NamedColor("coral2", fromRGB(0xEE, 0x6A, 0x50)),
      new NamedColor("coral3", fromRGB(0xCD, 0x5B, 0x45)),
      new NamedColor("coral4", fromRGB(0x8B, 0x3E, 0x2F)),
      new NamedColor("cornflowerblue", fromRGB(0x64, 0x95, 0xED)),
      new NamedColor("cornsilk", fromRGB(0xFF, 0xF8, 0xDC)),
      new NamedColor("cornsilk1", fromRGB(0xFF, 0xF8, 0xDC)),
      new NamedColor("cornsilk2", fromRGB(0xEE, 0xE8, 0xCD)),
      new NamedColor("cornsilk3", fromRGB(0xCD, 0xC8, 0xB1)),
      new NamedColor("cornsilk4", fromRGB(0x8B, 0x88, 0x78)),
      new NamedColor("cyan", fromRGB(0x00, 0xFF, 0xFF)),
      new NamedColor("cyan1", fromRGB(0x00, 0xFF, 0xFF)),
      new NamedColor("cyan2", fromRGB(0x00, 0xEE, 0xEE)),
      new NamedColor("cyan3", fromRGB(0x00, 0xCD, 0xCD)),
      new NamedColor("cyan4", fromRGB(0x00, 0x8B, 0x8B)),
      new NamedColor("darkblue", fromRGB(0x00, 0x00, 0x8B)),
      new NamedColor("darkcyan", fromRGB(0x00, 0x8B, 0x8B)),
      new NamedColor("darkgoldenrod", fromRGB(0xB8, 0x86, 0x0B)),
      new NamedColor("darkgoldenrod1", fromRGB(0xFF, 0xB9, 0x0F)),
      new NamedColor("darkgoldenrod2", fromRGB(0xEE, 0xAD, 0x0E)),
      new NamedColor("darkgoldenrod3", fromRGB(0xCD, 0x95, 0x0C)),
      new NamedColor("darkgoldenrod4", fromRGB(0x8B, 0x65, 0x08)),
      new NamedColor("darkgray", fromRGB(0xA9, 0xA9, 0xA9)),
      new NamedColor("darkgreen", fromRGB(0x00, 0x64, 0x00)),
      new NamedColor("darkgrey", fromRGB(0xA9, 0xA9, 0xA9)),
      new NamedColor("darkkhaki", fromRGB(0xBD, 0xB7, 0x6B)),
      new NamedColor("darkmagenta", fromRGB(0x8B, 0x00, 0x8B)),
      new NamedColor("darkolivegreen", fromRGB(0x55, 0x6B, 0x2F)),
      new NamedColor("darkolivegreen1", fromRGB(0xCA, 0xFF, 0x70)),
      new NamedColor("darkolivegreen2", fromRGB(0xBC, 0xEE, 0x68)),
      new NamedColor("darkolivegreen3", fromRGB(0xA2, 0xCD, 0x5A)),
      new NamedColor("darkolivegreen4", fromRGB(0x6E, 0x8B, 0x3D)),
      new NamedColor("darkorange", fromRGB(0xFF, 0x8C, 0x00)),
      new NamedColor("darkorange1", fromRGB(0xFF, 0x7F, 0x00)),
      new NamedColor("darkorange2", fromRGB(0xEE, 0x76, 0x00)),
      new NamedColor("darkorange3", fromRGB(0xCD, 0x66, 0x00)),
      new NamedColor("darkorange4", fromRGB(0x8B, 0x45, 0x00)),
      new NamedColor("darkorchid", fromRGB(0x99, 0x32, 0xCC)),
      new NamedColor("darkorchid1", fromRGB(0xBF, 0x3E, 0xFF)),
      new NamedColor("darkorchid2", fromRGB(0xB2, 0x3A, 0xEE)),
      new NamedColor("darkorchid3", fromRGB(0x9A, 0x32, 0xCD)),
      new NamedColor("darkorchid4", fromRGB(0x68, 0x22, 0x8B)),
      new NamedColor("darkred", fromRGB(0x8B, 0x00, 0x00)),
      new NamedColor("darksalmon", fromRGB(0xE9, 0x96, 0x7A)),
      new NamedColor("darkseagreen", fromRGB(0x8F, 0xBC, 0x8F)),
      new NamedColor("darkseagreen1", fromRGB(0xC1, 0xFF, 0xC1)),
      new NamedColor("darkseagreen2", fromRGB(0xB4, 0xEE, 0xB4)),
      new NamedColor("darkseagreen3", fromRGB(0x9B, 0xCD, 0x9B)),
      new NamedColor("darkseagreen4", fromRGB(0x69, 0x8B, 0x69)),
      new NamedColor("darkslateblue", fromRGB(0x48, 0x3D, 0x8B)),
      new NamedColor("darkslategray", fromRGB(0x2F, 0x4F, 0x4F)),
      new NamedColor("darkslategray1", fromRGB(0x97, 0xFF, 0xFF)),
      new NamedColor("darkslategray2", fromRGB(0x8D, 0xEE, 0xEE)),
      new NamedColor("darkslategray3", fromRGB(0x79, 0xCD, 0xCD)),
      new NamedColor("darkslategray4", fromRGB(0x52, 0x8B, 0x8B)),
      new NamedColor("darkslategrey", fromRGB(0x2F, 0x4F, 0x4F)),
      new NamedColor("darkturquoise", fromRGB(0x00, 0xCE, 0xD1)),
      new NamedColor("darkviolet", fromRGB(0x94, 0x00, 0xD3)),
      new NamedColor("deeppink", fromRGB(0xFF, 0x14, 0x93)),
      new NamedColor("deeppink1", fromRGB(0xFF, 0x14, 0x93)),
      new NamedColor("deeppink2", fromRGB(0xEE, 0x12, 0x89)),
      new NamedColor("deeppink3", fromRGB(0xCD, 0x10, 0x76)),
      new NamedColor("deeppink4", fromRGB(0x8B, 0x0A, 0x50)),
      new NamedColor("deepskyblue", fromRGB(0x00, 0xBF, 0xFF)),
      new NamedColor("deepskyblue1", fromRGB(0x00, 0xBF, 0xFF)),
      new NamedColor("deepskyblue2", fromRGB(0x00, 0xB2, 0xEE)),
      new NamedColor("deepskyblue3", fromRGB(0x00, 0x9A, 0xCD)),
      new NamedColor("deepskyblue4", fromRGB(0x00, 0x68, 0x8B)),
      new NamedColor("dimgray", fromRGB(0x69, 0x69, 0x69)),
      new NamedColor("dimgrey", fromRGB(0x69, 0x69, 0x69)),
      new NamedColor("dodgerblue", fromRGB(0x1E, 0x90, 0xFF)),
      new NamedColor("dodgerblue1", fromRGB(0x1E, 0x90, 0xFF)),
      new NamedColor("dodgerblue2", fromRGB(0x1C, 0x86, 0xEE)),
      new NamedColor("dodgerblue3", fromRGB(0x18, 0x74, 0xCD)),
      new NamedColor("dodgerblue4", fromRGB(0x10, 0x4E, 0x8B)),
      new NamedColor("firebrick", fromRGB(0xB2, 0x22, 0x22)),
      new NamedColor("firebrick1", fromRGB(0xFF, 0x30, 0x30)),
      new NamedColor("firebrick2", fromRGB(0xEE, 0x2C, 0x2C)),
      new NamedColor("firebrick3", fromRGB(0xCD, 0x26, 0x26)),
      new NamedColor("firebrick4", fromRGB(0x8B, 0x1A, 0x1A)),
      new NamedColor("floralwhite", fromRGB(0xFF, 0xFA, 0xF0)),
      new NamedColor("forestgreen", fromRGB(0x22, 0x8B, 0x22)),
      new NamedColor("gainsboro", fromRGB(0xDC, 0xDC, 0xDC)),
      new NamedColor("ghostwhite", fromRGB(0xF8, 0xF8, 0xFF)),
      new NamedColor("gold", fromRGB(0xFF, 0xD7, 0x00)),
      new NamedColor("gold1", fromRGB(0xFF, 0xD7, 0x00)),
      new NamedColor("gold2", fromRGB(0xEE, 0xC9, 0x00)),
      new NamedColor("gold3", fromRGB(0xCD, 0xAD, 0x00)),
      new NamedColor("gold4", fromRGB(0x8B, 0x75, 0x00)),
      new NamedColor("goldenrod", fromRGB(0xDA, 0xA5, 0x20)),
      new NamedColor("goldenrod1", fromRGB(0xFF, 0xC1, 0x25)),
      new NamedColor("goldenrod2", fromRGB(0xEE, 0xB4, 0x22)),
      new NamedColor("goldenrod3", fromRGB(0xCD, 0x9B, 0x1D)),
      new NamedColor("goldenrod4", fromRGB(0x8B, 0x69, 0x14)),
      new NamedColor("gray", fromRGB(0xBE, 0xBE, 0xBE)),
      new NamedColor("gray0", fromRGB(0x00, 0x00, 0x00)),
      new NamedColor("gray1", fromRGB(0x03, 0x03, 0x03)),
      new NamedColor("gray2", fromRGB(0x05, 0x05, 0x05)),
      new NamedColor("gray3", fromRGB(0x08, 0x08, 0x08)),
      new NamedColor("gray4", fromRGB(0x0A, 0x0A, 0x0A)),
      new NamedColor("gray5", fromRGB(0x0D, 0x0D, 0x0D)),
      new NamedColor("gray6", fromRGB(0x0F, 0x0F, 0x0F)),
      new NamedColor("gray7", fromRGB(0x12, 0x12, 0x12)),
      new NamedColor("gray8", fromRGB(0x14, 0x14, 0x14)),
      new NamedColor("gray9", fromRGB(0x17, 0x17, 0x17)),
      new NamedColor("gray10", fromRGB(0x1A, 0x1A, 0x1A)),
      new NamedColor("gray11", fromRGB(0x1C, 0x1C, 0x1C)),
      new NamedColor("gray12", fromRGB(0x1F, 0x1F, 0x1F)),
      new NamedColor("gray13", fromRGB(0x21, 0x21, 0x21)),
      new NamedColor("gray14", fromRGB(0x24, 0x24, 0x24)),
      new NamedColor("gray15", fromRGB(0x26, 0x26, 0x26)),
      new NamedColor("gray16", fromRGB(0x29, 0x29, 0x29)),
      new NamedColor("gray17", fromRGB(0x2B, 0x2B, 0x2B)),
      new NamedColor("gray18", fromRGB(0x2E, 0x2E, 0x2E)),
      new NamedColor("gray19", fromRGB(0x30, 0x30, 0x30)),
      new NamedColor("gray20", fromRGB(0x33, 0x33, 0x33)),
      new NamedColor("gray21", fromRGB(0x36, 0x36, 0x36)),
      new NamedColor("gray22", fromRGB(0x38, 0x38, 0x38)),
      new NamedColor("gray23", fromRGB(0x3B, 0x3B, 0x3B)),
      new NamedColor("gray24", fromRGB(0x3D, 0x3D, 0x3D)),
      new NamedColor("gray25", fromRGB(0x40, 0x40, 0x40)),
      new NamedColor("gray26", fromRGB(0x42, 0x42, 0x42)),
      new NamedColor("gray27", fromRGB(0x45, 0x45, 0x45)),
      new NamedColor("gray28", fromRGB(0x47, 0x47, 0x47)),
      new NamedColor("gray29", fromRGB(0x4A, 0x4A, 0x4A)),
      new NamedColor("gray30", fromRGB(0x4D, 0x4D, 0x4D)),
      new NamedColor("gray31", fromRGB(0x4F, 0x4F, 0x4F)),
      new NamedColor("gray32", fromRGB(0x52, 0x52, 0x52)),
      new NamedColor("gray33", fromRGB(0x54, 0x54, 0x54)),
      new NamedColor("gray34", fromRGB(0x57, 0x57, 0x57)),
      new NamedColor("gray35", fromRGB(0x59, 0x59, 0x59)),
      new NamedColor("gray36", fromRGB(0x5C, 0x5C, 0x5C)),
      new NamedColor("gray37", fromRGB(0x5E, 0x5E, 0x5E)),
      new NamedColor("gray38", fromRGB(0x61, 0x61, 0x61)),
      new NamedColor("gray39", fromRGB(0x63, 0x63, 0x63)),
      new NamedColor("gray40", fromRGB(0x66, 0x66, 0x66)),
      new NamedColor("gray41", fromRGB(0x69, 0x69, 0x69)),
      new NamedColor("gray42", fromRGB(0x6B, 0x6B, 0x6B)),
      new NamedColor("gray43", fromRGB(0x6E, 0x6E, 0x6E)),
      new NamedColor("gray44", fromRGB(0x70, 0x70, 0x70)),
      new NamedColor("gray45", fromRGB(0x73, 0x73, 0x73)),
      new NamedColor("gray46", fromRGB(0x75, 0x75, 0x75)),
      new NamedColor("gray47", fromRGB(0x78, 0x78, 0x78)),
      new NamedColor("gray48", fromRGB(0x7A, 0x7A, 0x7A)),
      new NamedColor("gray49", fromRGB(0x7D, 0x7D, 0x7D)),
      new NamedColor("gray50", fromRGB(0x7F, 0x7F, 0x7F)),
      new NamedColor("gray51", fromRGB(0x82, 0x82, 0x82)),
      new NamedColor("gray52", fromRGB(0x85, 0x85, 0x85)),
      new NamedColor("gray53", fromRGB(0x87, 0x87, 0x87)),
      new NamedColor("gray54", fromRGB(0x8A, 0x8A, 0x8A)),
      new NamedColor("gray55", fromRGB(0x8C, 0x8C, 0x8C)),
      new NamedColor("gray56", fromRGB(0x8F, 0x8F, 0x8F)),
      new NamedColor("gray57", fromRGB(0x91, 0x91, 0x91)),
      new NamedColor("gray58", fromRGB(0x94, 0x94, 0x94)),
      new NamedColor("gray59", fromRGB(0x96, 0x96, 0x96)),
      new NamedColor("gray60", fromRGB(0x99, 0x99, 0x99)),
      new NamedColor("gray61", fromRGB(0x9C, 0x9C, 0x9C)),
      new NamedColor("gray62", fromRGB(0x9E, 0x9E, 0x9E)),
      new NamedColor("gray63", fromRGB(0xA1, 0xA1, 0xA1)),
      new NamedColor("gray64", fromRGB(0xA3, 0xA3, 0xA3)),
      new NamedColor("gray65", fromRGB(0xA6, 0xA6, 0xA6)),
      new NamedColor("gray66", fromRGB(0xA8, 0xA8, 0xA8)),
      new NamedColor("gray67", fromRGB(0xAB, 0xAB, 0xAB)),
      new NamedColor("gray68", fromRGB(0xAD, 0xAD, 0xAD)),
      new NamedColor("gray69", fromRGB(0xB0, 0xB0, 0xB0)),
      new NamedColor("gray70", fromRGB(0xB3, 0xB3, 0xB3)),
      new NamedColor("gray71", fromRGB(0xB5, 0xB5, 0xB5)),
      new NamedColor("gray72", fromRGB(0xB8, 0xB8, 0xB8)),
      new NamedColor("gray73", fromRGB(0xBA, 0xBA, 0xBA)),
      new NamedColor("gray74", fromRGB(0xBD, 0xBD, 0xBD)),
      new NamedColor("gray75", fromRGB(0xBF, 0xBF, 0xBF)),
      new NamedColor("gray76", fromRGB(0xC2, 0xC2, 0xC2)),
      new NamedColor("gray77", fromRGB(0xC4, 0xC4, 0xC4)),
      new NamedColor("gray78", fromRGB(0xC7, 0xC7, 0xC7)),
      new NamedColor("gray79", fromRGB(0xC9, 0xC9, 0xC9)),
      new NamedColor("gray80", fromRGB(0xCC, 0xCC, 0xCC)),
      new NamedColor("gray81", fromRGB(0xCF, 0xCF, 0xCF)),
      new NamedColor("gray82", fromRGB(0xD1, 0xD1, 0xD1)),
      new NamedColor("gray83", fromRGB(0xD4, 0xD4, 0xD4)),
      new NamedColor("gray84", fromRGB(0xD6, 0xD6, 0xD6)),
      new NamedColor("gray85", fromRGB(0xD9, 0xD9, 0xD9)),
      new NamedColor("gray86", fromRGB(0xDB, 0xDB, 0xDB)),
      new NamedColor("gray87", fromRGB(0xDE, 0xDE, 0xDE)),
      new NamedColor("gray88", fromRGB(0xE0, 0xE0, 0xE0)),
      new NamedColor("gray89", fromRGB(0xE3, 0xE3, 0xE3)),
      new NamedColor("gray90", fromRGB(0xE5, 0xE5, 0xE5)),
      new NamedColor("gray91", fromRGB(0xE8, 0xE8, 0xE8)),
      new NamedColor("gray92", fromRGB(0xEB, 0xEB, 0xEB)),
      new NamedColor("gray93", fromRGB(0xED, 0xED, 0xED)),
      new NamedColor("gray94", fromRGB(0xF0, 0xF0, 0xF0)),
      new NamedColor("gray95", fromRGB(0xF2, 0xF2, 0xF2)),
      new NamedColor("gray96", fromRGB(0xF5, 0xF5, 0xF5)),
      new NamedColor("gray97", fromRGB(0xF7, 0xF7, 0xF7)),
      new NamedColor("gray98", fromRGB(0xFA, 0xFA, 0xFA)),
      new NamedColor("gray99", fromRGB(0xFC, 0xFC, 0xFC)),
      new NamedColor("gray100", fromRGB(0xFF, 0xFF, 0xFF)),
      new NamedColor("green", fromRGB(0x00, 0xFF, 0x00)),
      new NamedColor("green1", fromRGB(0x00, 0xFF, 0x00)),
      new NamedColor("green2", fromRGB(0x00, 0xEE, 0x00)),
      new NamedColor("green3", fromRGB(0x00, 0xCD, 0x00)),
      new NamedColor("green4", fromRGB(0x00, 0x8B, 0x00)),
      new NamedColor("greenyellow", fromRGB(0xAD, 0xFF, 0x2F)),
      new NamedColor("grey", fromRGB(0xBE, 0xBE, 0xBE)),
      new NamedColor("grey0", fromRGB(0x00, 0x00, 0x00)),
      new NamedColor("grey1", fromRGB(0x03, 0x03, 0x03)),
      new NamedColor("grey2", fromRGB(0x05, 0x05, 0x05)),
      new NamedColor("grey3", fromRGB(0x08, 0x08, 0x08)),
      new NamedColor("grey4", fromRGB(0x0A, 0x0A, 0x0A)),
      new NamedColor("grey5", fromRGB(0x0D, 0x0D, 0x0D)),
      new NamedColor("grey6", fromRGB(0x0F, 0x0F, 0x0F)),
      new NamedColor("grey7", fromRGB(0x12, 0x12, 0x12)),
      new NamedColor("grey8", fromRGB(0x14, 0x14, 0x14)),
      new NamedColor("grey9", fromRGB(0x17, 0x17, 0x17)),
      new NamedColor("grey10", fromRGB(0x1A, 0x1A, 0x1A)),
      new NamedColor("grey11", fromRGB(0x1C, 0x1C, 0x1C)),
      new NamedColor("grey12", fromRGB(0x1F, 0x1F, 0x1F)),
      new NamedColor("grey13", fromRGB(0x21, 0x21, 0x21)),
      new NamedColor("grey14", fromRGB(0x24, 0x24, 0x24)),
      new NamedColor("grey15", fromRGB(0x26, 0x26, 0x26)),
      new NamedColor("grey16", fromRGB(0x29, 0x29, 0x29)),
      new NamedColor("grey17", fromRGB(0x2B, 0x2B, 0x2B)),
      new NamedColor("grey18", fromRGB(0x2E, 0x2E, 0x2E)),
      new NamedColor("grey19", fromRGB(0x30, 0x30, 0x30)),
      new NamedColor("grey20", fromRGB(0x33, 0x33, 0x33)),
      new NamedColor("grey21", fromRGB(0x36, 0x36, 0x36)),
      new NamedColor("grey22", fromRGB(0x38, 0x38, 0x38)),
      new NamedColor("grey23", fromRGB(0x3B, 0x3B, 0x3B)),
      new NamedColor("grey24", fromRGB(0x3D, 0x3D, 0x3D)),
      new NamedColor("grey25", fromRGB(0x40, 0x40, 0x40)),
      new NamedColor("grey26", fromRGB(0x42, 0x42, 0x42)),
      new NamedColor("grey27", fromRGB(0x45, 0x45, 0x45)),
      new NamedColor("grey28", fromRGB(0x47, 0x47, 0x47)),
      new NamedColor("grey29", fromRGB(0x4A, 0x4A, 0x4A)),
      new NamedColor("grey30", fromRGB(0x4D, 0x4D, 0x4D)),
      new NamedColor("grey31", fromRGB(0x4F, 0x4F, 0x4F)),
      new NamedColor("grey32", fromRGB(0x52, 0x52, 0x52)),
      new NamedColor("grey33", fromRGB(0x54, 0x54, 0x54)),
      new NamedColor("grey34", fromRGB(0x57, 0x57, 0x57)),
      new NamedColor("grey35", fromRGB(0x59, 0x59, 0x59)),
      new NamedColor("grey36", fromRGB(0x5C, 0x5C, 0x5C)),
      new NamedColor("grey37", fromRGB(0x5E, 0x5E, 0x5E)),
      new NamedColor("grey38", fromRGB(0x61, 0x61, 0x61)),
      new NamedColor("grey39", fromRGB(0x63, 0x63, 0x63)),
      new NamedColor("grey40", fromRGB(0x66, 0x66, 0x66)),
      new NamedColor("grey41", fromRGB(0x69, 0x69, 0x69)),
      new NamedColor("grey42", fromRGB(0x6B, 0x6B, 0x6B)),
      new NamedColor("grey43", fromRGB(0x6E, 0x6E, 0x6E)),
      new NamedColor("grey44", fromRGB(0x70, 0x70, 0x70)),
      new NamedColor("grey45", fromRGB(0x73, 0x73, 0x73)),
      new NamedColor("grey46", fromRGB(0x75, 0x75, 0x75)),
      new NamedColor("grey47", fromRGB(0x78, 0x78, 0x78)),
      new NamedColor("grey48", fromRGB(0x7A, 0x7A, 0x7A)),
      new NamedColor("grey49", fromRGB(0x7D, 0x7D, 0x7D)),
      new NamedColor("grey50", fromRGB(0x7F, 0x7F, 0x7F)),
      new NamedColor("grey51", fromRGB(0x82, 0x82, 0x82)),
      new NamedColor("grey52", fromRGB(0x85, 0x85, 0x85)),
      new NamedColor("grey53", fromRGB(0x87, 0x87, 0x87)),
      new NamedColor("grey54", fromRGB(0x8A, 0x8A, 0x8A)),
      new NamedColor("grey55", fromRGB(0x8C, 0x8C, 0x8C)),
      new NamedColor("grey56", fromRGB(0x8F, 0x8F, 0x8F)),
      new NamedColor("grey57", fromRGB(0x91, 0x91, 0x91)),
      new NamedColor("grey58", fromRGB(0x94, 0x94, 0x94)),
      new NamedColor("grey59", fromRGB(0x96, 0x96, 0x96)),
      new NamedColor("grey60", fromRGB(0x99, 0x99, 0x99)),
      new NamedColor("grey61", fromRGB(0x9C, 0x9C, 0x9C)),
      new NamedColor("grey62", fromRGB(0x9E, 0x9E, 0x9E)),
      new NamedColor("grey63", fromRGB(0xA1, 0xA1, 0xA1)),
      new NamedColor("grey64", fromRGB(0xA3, 0xA3, 0xA3)),
      new NamedColor("grey65", fromRGB(0xA6, 0xA6, 0xA6)),
      new NamedColor("grey66", fromRGB(0xA8, 0xA8, 0xA8)),
      new NamedColor("grey67", fromRGB(0xAB, 0xAB, 0xAB)),
      new NamedColor("grey68", fromRGB(0xAD, 0xAD, 0xAD)),
      new NamedColor("grey69", fromRGB(0xB0, 0xB0, 0xB0)),
      new NamedColor("grey70", fromRGB(0xB3, 0xB3, 0xB3)),
      new NamedColor("grey71", fromRGB(0xB5, 0xB5, 0xB5)),
      new NamedColor("grey72", fromRGB(0xB8, 0xB8, 0xB8)),
      new NamedColor("grey73", fromRGB(0xBA, 0xBA, 0xBA)),
      new NamedColor("grey74", fromRGB(0xBD, 0xBD, 0xBD)),
      new NamedColor("grey75", fromRGB(0xBF, 0xBF, 0xBF)),
      new NamedColor("grey76", fromRGB(0xC2, 0xC2, 0xC2)),
      new NamedColor("grey77", fromRGB(0xC4, 0xC4, 0xC4)),
      new NamedColor("grey78", fromRGB(0xC7, 0xC7, 0xC7)),
      new NamedColor("grey79", fromRGB(0xC9, 0xC9, 0xC9)),
      new NamedColor("grey80", fromRGB(0xCC, 0xCC, 0xCC)),
      new NamedColor("grey81", fromRGB(0xCF, 0xCF, 0xCF)),
      new NamedColor("grey82", fromRGB(0xD1, 0xD1, 0xD1)),
      new NamedColor("grey83", fromRGB(0xD4, 0xD4, 0xD4)),
      new NamedColor("grey84", fromRGB(0xD6, 0xD6, 0xD6)),
      new NamedColor("grey85", fromRGB(0xD9, 0xD9, 0xD9)),
      new NamedColor("grey86", fromRGB(0xDB, 0xDB, 0xDB)),
      new NamedColor("grey87", fromRGB(0xDE, 0xDE, 0xDE)),
      new NamedColor("grey88", fromRGB(0xE0, 0xE0, 0xE0)),
      new NamedColor("grey89", fromRGB(0xE3, 0xE3, 0xE3)),
      new NamedColor("grey90", fromRGB(0xE5, 0xE5, 0xE5)),
      new NamedColor("grey91", fromRGB(0xE8, 0xE8, 0xE8)),
      new NamedColor("grey92", fromRGB(0xEB, 0xEB, 0xEB)),
      new NamedColor("grey93", fromRGB(0xED, 0xED, 0xED)),
      new NamedColor("grey94", fromRGB(0xF0, 0xF0, 0xF0)),
      new NamedColor("grey95", fromRGB(0xF2, 0xF2, 0xF2)),
      new NamedColor("grey96", fromRGB(0xF5, 0xF5, 0xF5)),
      new NamedColor("grey97", fromRGB(0xF7, 0xF7, 0xF7)),
      new NamedColor("grey98", fromRGB(0xFA, 0xFA, 0xFA)),
      new NamedColor("grey99", fromRGB(0xFC, 0xFC, 0xFC)),
      new NamedColor("grey100", fromRGB(0xFF, 0xFF, 0xFF)),
      new NamedColor("honeydew", fromRGB(0xF0, 0xFF, 0xF0)),
      new NamedColor("honeydew1", fromRGB(0xF0, 0xFF, 0xF0)),
      new NamedColor("honeydew2", fromRGB(0xE0, 0xEE, 0xE0)),
      new NamedColor("honeydew3", fromRGB(0xC1, 0xCD, 0xC1)),
      new NamedColor("honeydew4", fromRGB(0x83, 0x8B, 0x83)),
      new NamedColor("hotpink", fromRGB(0xFF, 0x69, 0xB4)),
      new NamedColor("hotpink1", fromRGB(0xFF, 0x6E, 0xB4)),
      new NamedColor("hotpink2", fromRGB(0xEE, 0x6A, 0xA7)),
      new NamedColor("hotpink3", fromRGB(0xCD, 0x60, 0x90)),
      new NamedColor("hotpink4", fromRGB(0x8B, 0x3A, 0x62)),
      new NamedColor("indianred", fromRGB(0xCD, 0x5C, 0x5C)),
      new NamedColor("indianred1", fromRGB(0xFF, 0x6A, 0x6A)),
      new NamedColor("indianred2", fromRGB(0xEE, 0x63, 0x63)),
      new NamedColor("indianred3", fromRGB(0xCD, 0x55, 0x55)),
      new NamedColor("indianred4", fromRGB(0x8B, 0x3A, 0x3A)),
      new NamedColor("ivory", fromRGB(0xFF, 0xFF, 0xF0)),
      new NamedColor("ivory1", fromRGB(0xFF, 0xFF, 0xF0)),
      new NamedColor("ivory2", fromRGB(0xEE, 0xEE, 0xE0)),
      new NamedColor("ivory3", fromRGB(0xCD, 0xCD, 0xC1)),
      new NamedColor("ivory4", fromRGB(0x8B, 0x8B, 0x83)),
      new NamedColor("khaki", fromRGB(0xF0, 0xE6, 0x8C)),
      new NamedColor("khaki1", fromRGB(0xFF, 0xF6, 0x8F)),
      new NamedColor("khaki2", fromRGB(0xEE, 0xE6, 0x85)),
      new NamedColor("khaki3", fromRGB(0xCD, 0xC6, 0x73)),
      new NamedColor("khaki4", fromRGB(0x8B, 0x86, 0x4E)),
      new NamedColor("lavender", fromRGB(0xE6, 0xE6, 0xFA)),
      new NamedColor("lavenderblush", fromRGB(0xFF, 0xF0, 0xF5)),
      new NamedColor("lavenderblush1", fromRGB(0xFF, 0xF0, 0xF5)),
      new NamedColor("lavenderblush2", fromRGB(0xEE, 0xE0, 0xE5)),
      new NamedColor("lavenderblush3", fromRGB(0xCD, 0xC1, 0xC5)),
      new NamedColor("lavenderblush4", fromRGB(0x8B, 0x83, 0x86)),
      new NamedColor("lawngreen", fromRGB(0x7C, 0xFC, 0x00)),
      new NamedColor("lemonchiffon", fromRGB(0xFF, 0xFA, 0xCD)),
      new NamedColor("lemonchiffon1", fromRGB(0xFF, 0xFA, 0xCD)),
      new NamedColor("lemonchiffon2", fromRGB(0xEE, 0xE9, 0xBF)),
      new NamedColor("lemonchiffon3", fromRGB(0xCD, 0xC9, 0xA5)),
      new NamedColor("lemonchiffon4", fromRGB(0x8B, 0x89, 0x70)),
      new NamedColor("lightblue", fromRGB(0xAD, 0xD8, 0xE6)),
      new NamedColor("lightblue1", fromRGB(0xBF, 0xEF, 0xFF)),
      new NamedColor("lightblue2", fromRGB(0xB2, 0xDF, 0xEE)),
      new NamedColor("lightblue3", fromRGB(0x9A, 0xC0, 0xCD)),
      new NamedColor("lightblue4", fromRGB(0x68, 0x83, 0x8B)),
      new NamedColor("lightcoral", fromRGB(0xF0, 0x80, 0x80)),
      new NamedColor("lightcyan", fromRGB(0xE0, 0xFF, 0xFF)),
      new NamedColor("lightcyan1", fromRGB(0xE0, 0xFF, 0xFF)),
      new NamedColor("lightcyan2", fromRGB(0xD1, 0xEE, 0xEE)),
      new NamedColor("lightcyan3", fromRGB(0xB4, 0xCD, 0xCD)),
      new NamedColor("lightcyan4", fromRGB(0x7A, 0x8B, 0x8B)),
      new NamedColor("lightgoldenrod", fromRGB(0xEE, 0xDD, 0x82)),
      new NamedColor("lightgoldenrod1", fromRGB(0xFF, 0xEC, 0x8B)),
      new NamedColor("lightgoldenrod2", fromRGB(0xEE, 0xDC, 0x82)),
      new NamedColor("lightgoldenrod3", fromRGB(0xCD, 0xBE, 0x70)),
      new NamedColor("lightgoldenrod4", fromRGB(0x8B, 0x81, 0x4C)),
      new NamedColor("lightgoldenrodyellow", fromRGB(0xFA, 0xFA, 0xD2)),
      new NamedColor("lightgray", fromRGB(0xD3, 0xD3, 0xD3)),
      new NamedColor("lightgreen", fromRGB(0x90, 0xEE, 0x90)),
      new NamedColor("lightgrey", fromRGB(0xD3, 0xD3, 0xD3)),
      new NamedColor("lightpink", fromRGB(0xFF, 0xB6, 0xC1)),
      new NamedColor("lightpink1", fromRGB(0xFF, 0xAE, 0xB9)),
      new NamedColor("lightpink2", fromRGB(0xEE, 0xA2, 0xAD)),
      new NamedColor("lightpink3", fromRGB(0xCD, 0x8C, 0x95)),
      new NamedColor("lightpink4", fromRGB(0x8B, 0x5F, 0x65)),
      new NamedColor("lightsalmon", fromRGB(0xFF, 0xA0, 0x7A)),
      new NamedColor("lightsalmon1", fromRGB(0xFF, 0xA0, 0x7A)),
      new NamedColor("lightsalmon2", fromRGB(0xEE, 0x95, 0x72)),
      new NamedColor("lightsalmon3", fromRGB(0xCD, 0x81, 0x62)),
      new NamedColor("lightsalmon4", fromRGB(0x8B, 0x57, 0x42)),
      new NamedColor("lightseagreen", fromRGB(0x20, 0xB2, 0xAA)),
      new NamedColor("lightskyblue", fromRGB(0x87, 0xCE, 0xFA)),
      new NamedColor("lightskyblue1", fromRGB(0xB0, 0xE2, 0xFF)),
      new NamedColor("lightskyblue2", fromRGB(0xA4, 0xD3, 0xEE)),
      new NamedColor("lightskyblue3", fromRGB(0x8D, 0xB6, 0xCD)),
      new NamedColor("lightskyblue4", fromRGB(0x60, 0x7B, 0x8B)),
      new NamedColor("lightslateblue", fromRGB(0x84, 0x70, 0xFF)),
      new NamedColor("lightslategray", fromRGB(0x77, 0x88, 0x99)),
      new NamedColor("lightslategrey", fromRGB(0x77, 0x88, 0x99)),
      new NamedColor("lightsteelblue", fromRGB(0xB0, 0xC4, 0xDE)),
      new NamedColor("lightsteelblue1", fromRGB(0xCA, 0xE1, 0xFF)),
      new NamedColor("lightsteelblue2", fromRGB(0xBC, 0xD2, 0xEE)),
      new NamedColor("lightsteelblue3", fromRGB(0xA2, 0xB5, 0xCD)),
      new NamedColor("lightsteelblue4", fromRGB(0x6E, 0x7B, 0x8B)),
      new NamedColor("lightyellow", fromRGB(0xFF, 0xFF, 0xE0)),
      new NamedColor("lightyellow1", fromRGB(0xFF, 0xFF, 0xE0)),
      new NamedColor("lightyellow2", fromRGB(0xEE, 0xEE, 0xD1)),
      new NamedColor("lightyellow3", fromRGB(0xCD, 0xCD, 0xB4)),
      new NamedColor("lightyellow4", fromRGB(0x8B, 0x8B, 0x7A)),
      new NamedColor("limegreen", fromRGB(0x32, 0xCD, 0x32)),
      new NamedColor("linen", fromRGB(0xFA, 0xF0, 0xE6)),
      new NamedColor("magenta", fromRGB(0xFF, 0x00, 0xFF)),
      new NamedColor("magenta1", fromRGB(0xFF, 0x00, 0xFF)),
      new NamedColor("magenta2", fromRGB(0xEE, 0x00, 0xEE)),
      new NamedColor("magenta3", fromRGB(0xCD, 0x00, 0xCD)),
      new NamedColor("magenta4", fromRGB(0x8B, 0x00, 0x8B)),
      new NamedColor("maroon", fromRGB(0xB0, 0x30, 0x60)),
      new NamedColor("maroon1", fromRGB(0xFF, 0x34, 0xB3)),
      new NamedColor("maroon2", fromRGB(0xEE, 0x30, 0xA7)),
      new NamedColor("maroon3", fromRGB(0xCD, 0x29, 0x90)),
      new NamedColor("maroon4", fromRGB(0x8B, 0x1C, 0x62)),
      new NamedColor("mediumaquamarine", fromRGB(0x66, 0xCD, 0xAA)),
      new NamedColor("mediumblue", fromRGB(0x00, 0x00, 0xCD)),
      new NamedColor("mediumorchid", fromRGB(0xBA, 0x55, 0xD3)),
      new NamedColor("mediumorchid1", fromRGB(0xE0, 0x66, 0xFF)),
      new NamedColor("mediumorchid2", fromRGB(0xD1, 0x5F, 0xEE)),
      new NamedColor("mediumorchid3", fromRGB(0xB4, 0x52, 0xCD)),
      new NamedColor("mediumorchid4", fromRGB(0x7A, 0x37, 0x8B)),
      new NamedColor("mediumpurple", fromRGB(0x93, 0x70, 0xDB)),
      new NamedColor("mediumpurple1", fromRGB(0xAB, 0x82, 0xFF)),
      new NamedColor("mediumpurple2", fromRGB(0x9F, 0x79, 0xEE)),
      new NamedColor("mediumpurple3", fromRGB(0x89, 0x68, 0xCD)),
      new NamedColor("mediumpurple4", fromRGB(0x5D, 0x47, 0x8B)),
      new NamedColor("mediumseagreen", fromRGB(0x3C, 0xB3, 0x71)),
      new NamedColor("mediumslateblue", fromRGB(0x7B, 0x68, 0xEE)),
      new NamedColor("mediumspringgreen", fromRGB(0x00, 0xFA, 0x9A)),
      new NamedColor("mediumturquoise", fromRGB(0x48, 0xD1, 0xCC)),
      new NamedColor("mediumvioletred", fromRGB(0xC7, 0x15, 0x85)),
      new NamedColor("midnightblue", fromRGB(0x19, 0x19, 0x70)),
      new NamedColor("mintcream", fromRGB(0xF5, 0xFF, 0xFA)),
      new NamedColor("mistyrose", fromRGB(0xFF, 0xE4, 0xE1)),
      new NamedColor("mistyrose1", fromRGB(0xFF, 0xE4, 0xE1)),
      new NamedColor("mistyrose2", fromRGB(0xEE, 0xD5, 0xD2)),
      new NamedColor("mistyrose3", fromRGB(0xCD, 0xB7, 0xB5)),
      new NamedColor("mistyrose4", fromRGB(0x8B, 0x7D, 0x7B)),
      new NamedColor("moccasin", fromRGB(0xFF, 0xE4, 0xB5)),
      new NamedColor("navajowhite", fromRGB(0xFF, 0xDE, 0xAD)),
      new NamedColor("navajowhite1", fromRGB(0xFF, 0xDE, 0xAD)),
      new NamedColor("navajowhite2", fromRGB(0xEE, 0xCF, 0xA1)),
      new NamedColor("navajowhite3", fromRGB(0xCD, 0xB3, 0x8B)),
      new NamedColor("navajowhite4", fromRGB(0x8B, 0x79, 0x5E)),
      new NamedColor("navy", fromRGB(0x00, 0x00, 0x80)),
      new NamedColor("navyblue", fromRGB(0x00, 0x00, 0x80)),
      new NamedColor("oldlace", fromRGB(0xFD, 0xF5, 0xE6)),
      new NamedColor("olivedrab", fromRGB(0x6B, 0x8E, 0x23)),
      new NamedColor("olivedrab1", fromRGB(0xC0, 0xFF, 0x3E)),
      new NamedColor("olivedrab2", fromRGB(0xB3, 0xEE, 0x3A)),
      new NamedColor("olivedrab3", fromRGB(0x9A, 0xCD, 0x32)),
      new NamedColor("olivedrab4", fromRGB(0x69, 0x8B, 0x22)),
      new NamedColor("orange", fromRGB(0xFF, 0xA5, 0x00)),
      new NamedColor("orange1", fromRGB(0xFF, 0xA5, 0x00)),
      new NamedColor("orange2", fromRGB(0xEE, 0x9A, 0x00)),
      new NamedColor("orange3", fromRGB(0xCD, 0x85, 0x00)),
      new NamedColor("orange4", fromRGB(0x8B, 0x5A, 0x00)),
      new NamedColor("orangered", fromRGB(0xFF, 0x45, 0x00)),
      new NamedColor("orangered1", fromRGB(0xFF, 0x45, 0x00)),
      new NamedColor("orangered2", fromRGB(0xEE, 0x40, 0x00)),
      new NamedColor("orangered3", fromRGB(0xCD, 0x37, 0x00)),
      new NamedColor("orangered4", fromRGB(0x8B, 0x25, 0x00)),
      new NamedColor("orchid", fromRGB(0xDA, 0x70, 0xD6)),
      new NamedColor("orchid1", fromRGB(0xFF, 0x83, 0xFA)),
      new NamedColor("orchid2", fromRGB(0xEE, 0x7A, 0xE9)),
      new NamedColor("orchid3", fromRGB(0xCD, 0x69, 0xC9)),
      new NamedColor("orchid4", fromRGB(0x8B, 0x47, 0x89)),
      new NamedColor("palegoldenrod", fromRGB(0xEE, 0xE8, 0xAA)),
      new NamedColor("palegreen", fromRGB(0x98, 0xFB, 0x98)),
      new NamedColor("palegreen1", fromRGB(0x9A, 0xFF, 0x9A)),
      new NamedColor("palegreen2", fromRGB(0x90, 0xEE, 0x90)),
      new NamedColor("palegreen3", fromRGB(0x7C, 0xCD, 0x7C)),
      new NamedColor("palegreen4", fromRGB(0x54, 0x8B, 0x54)),
      new NamedColor("paleturquoise", fromRGB(0xAF, 0xEE, 0xEE)),
      new NamedColor("paleturquoise1", fromRGB(0xBB, 0xFF, 0xFF)),
      new NamedColor("paleturquoise2", fromRGB(0xAE, 0xEE, 0xEE)),
      new NamedColor("paleturquoise3", fromRGB(0x96, 0xCD, 0xCD)),
      new NamedColor("paleturquoise4", fromRGB(0x66, 0x8B, 0x8B)),
      new NamedColor("palevioletred", fromRGB(0xDB, 0x70, 0x93)),
      new NamedColor("palevioletred1", fromRGB(0xFF, 0x82, 0xAB)),
      new NamedColor("palevioletred2", fromRGB(0xEE, 0x79, 0x9F)),
      new NamedColor("palevioletred3", fromRGB(0xCD, 0x68, 0x89)),
      new NamedColor("palevioletred4", fromRGB(0x8B, 0x47, 0x5D)),
      new NamedColor("papayawhip", fromRGB(0xFF, 0xEF, 0xD5)),
      new NamedColor("peachpuff", fromRGB(0xFF, 0xDA, 0xB9)),
      new NamedColor("peachpuff1", fromRGB(0xFF, 0xDA, 0xB9)),
      new NamedColor("peachpuff2", fromRGB(0xEE, 0xCB, 0xAD)),
      new NamedColor("peachpuff3", fromRGB(0xCD, 0xAF, 0x95)),
      new NamedColor("peachpuff4", fromRGB(0x8B, 0x77, 0x65)),
      new NamedColor("peru", fromRGB(0xCD, 0x85, 0x3F)),
      new NamedColor("pink", fromRGB(0xFF, 0xC0, 0xCB)),
      new NamedColor("pink1", fromRGB(0xFF, 0xB5, 0xC5)),
      new NamedColor("pink2", fromRGB(0xEE, 0xA9, 0xB8)),
      new NamedColor("pink3", fromRGB(0xCD, 0x91, 0x9E)),
      new NamedColor("pink4", fromRGB(0x8B, 0x63, 0x6C)),
      new NamedColor("plum", fromRGB(0xDD, 0xA0, 0xDD)),
      new NamedColor("plum1", fromRGB(0xFF, 0xBB, 0xFF)),
      new NamedColor("plum2", fromRGB(0xEE, 0xAE, 0xEE)),
      new NamedColor("plum3", fromRGB(0xCD, 0x96, 0xCD)),
      new NamedColor("plum4", fromRGB(0x8B, 0x66, 0x8B)),
      new NamedColor("powderblue", fromRGB(0xB0, 0xE0, 0xE6)),
      new NamedColor("purple", fromRGB(0xA0, 0x20, 0xF0)),
      new NamedColor("purple1", fromRGB(0x9B, 0x30, 0xFF)),
      new NamedColor("purple2", fromRGB(0x91, 0x2C, 0xEE)),
      new NamedColor("purple3", fromRGB(0x7D, 0x26, 0xCD)),
      new NamedColor("purple4", fromRGB(0x55, 0x1A, 0x8B)),
      new NamedColor("red", fromRGB(0xFF, 0x00, 0x00)),
      new NamedColor("red1", fromRGB(0xFF, 0x00, 0x00)),
      new NamedColor("red2", fromRGB(0xEE, 0x00, 0x00)),
      new NamedColor("red3", fromRGB(0xCD, 0x00, 0x00)),
      new NamedColor("red4", fromRGB(0x8B, 0x00, 0x00)),
      new NamedColor("rosybrown", fromRGB(0xBC, 0x8F, 0x8F)),
      new NamedColor("rosybrown1", fromRGB(0xFF, 0xC1, 0xC1)),
      new NamedColor("rosybrown2", fromRGB(0xEE, 0xB4, 0xB4)),
      new NamedColor("rosybrown3", fromRGB(0xCD, 0x9B, 0x9B)),
      new NamedColor("rosybrown4", fromRGB(0x8B, 0x69, 0x69)),
      new NamedColor("royalblue", fromRGB(0x41, 0x69, 0xE1)),
      new NamedColor("royalblue1", fromRGB(0x48, 0x76, 0xFF)),
      new NamedColor("royalblue2", fromRGB(0x43, 0x6E, 0xEE)),
      new NamedColor("royalblue3", fromRGB(0x3A, 0x5F, 0xCD)),
      new NamedColor("royalblue4", fromRGB(0x27, 0x40, 0x8B)),
      new NamedColor("saddlebrown", fromRGB(0x8B, 0x45, 0x13)),
      new NamedColor("salmon", fromRGB(0xFA, 0x80, 0x72)),
      new NamedColor("salmon1", fromRGB(0xFF, 0x8C, 0x69)),
      new NamedColor("salmon2", fromRGB(0xEE, 0x82, 0x62)),
      new NamedColor("salmon3", fromRGB(0xCD, 0x70, 0x54)),
      new NamedColor("salmon4", fromRGB(0x8B, 0x4C, 0x39)),
      new NamedColor("sandybrown", fromRGB(0xF4, 0xA4, 0x60)),
      new NamedColor("seagreen", fromRGB(0x2E, 0x8B, 0x57)),
      new NamedColor("seagreen1", fromRGB(0x54, 0xFF, 0x9F)),
      new NamedColor("seagreen2", fromRGB(0x4E, 0xEE, 0x94)),
      new NamedColor("seagreen3", fromRGB(0x43, 0xCD, 0x80)),
      new NamedColor("seagreen4", fromRGB(0x2E, 0x8B, 0x57)),
      new NamedColor("seashell", fromRGB(0xFF, 0xF5, 0xEE)),
      new NamedColor("seashell1", fromRGB(0xFF, 0xF5, 0xEE)),
      new NamedColor("seashell2", fromRGB(0xEE, 0xE5, 0xDE)),
      new NamedColor("seashell3", fromRGB(0xCD, 0xC5, 0xBF)),
      new NamedColor("seashell4", fromRGB(0x8B, 0x86, 0x82)),
      new NamedColor("sienna", fromRGB(0xA0, 0x52, 0x2D)),
      new NamedColor("sienna1", fromRGB(0xFF, 0x82, 0x47)),
      new NamedColor("sienna2", fromRGB(0xEE, 0x79, 0x42)),
      new NamedColor("sienna3", fromRGB(0xCD, 0x68, 0x39)),
      new NamedColor("sienna4", fromRGB(0x8B, 0x47, 0x26)),
      new NamedColor("skyblue", fromRGB(0x87, 0xCE, 0xEB)),
      new NamedColor("skyblue1", fromRGB(0x87, 0xCE, 0xFF)),
      new NamedColor("skyblue2", fromRGB(0x7E, 0xC0, 0xEE)),
      new NamedColor("skyblue3", fromRGB(0x6C, 0xA6, 0xCD)),
      new NamedColor("skyblue4", fromRGB(0x4A, 0x70, 0x8B)),
      new NamedColor("slateblue", fromRGB(0x6A, 0x5A, 0xCD)),
      new NamedColor("slateblue1", fromRGB(0x83, 0x6F, 0xFF)),
      new NamedColor("slateblue2", fromRGB(0x7A, 0x67, 0xEE)),
      new NamedColor("slateblue3", fromRGB(0x69, 0x59, 0xCD)),
      new NamedColor("slateblue4", fromRGB(0x47, 0x3C, 0x8B)),
      new NamedColor("slategray", fromRGB(0x70, 0x80, 0x90)),
      new NamedColor("slategray1", fromRGB(0xC6, 0xE2, 0xFF)),
      new NamedColor("slategray2", fromRGB(0xB9, 0xD3, 0xEE)),
      new NamedColor("slategray3", fromRGB(0x9F, 0xB6, 0xCD)),
      new NamedColor("slategray4", fromRGB(0x6C, 0x7B, 0x8B)),
      new NamedColor("slategrey", fromRGB(0x70, 0x80, 0x90)),
      new NamedColor("snow", fromRGB(0xFF, 0xFA, 0xFA)),
      new NamedColor("snow1", fromRGB(0xFF, 0xFA, 0xFA)),
      new NamedColor("snow2", fromRGB(0xEE, 0xE9, 0xE9)),
      new NamedColor("snow3", fromRGB(0xCD, 0xC9, 0xC9)),
      new NamedColor("snow4", fromRGB(0x8B, 0x89, 0x89)),
      new NamedColor("springgreen", fromRGB(0x00, 0xFF, 0x7F)),
      new NamedColor("springgreen1", fromRGB(0x00, 0xFF, 0x7F)),
      new NamedColor("springgreen2", fromRGB(0x00, 0xEE, 0x76)),
      new NamedColor("springgreen3", fromRGB(0x00, 0xCD, 0x66)),
      new NamedColor("springgreen4", fromRGB(0x00, 0x8B, 0x45)),
      new NamedColor("steelblue", fromRGB(0x46, 0x82, 0xB4)),
      new NamedColor("steelblue1", fromRGB(0x63, 0xB8, 0xFF)),
      new NamedColor("steelblue2", fromRGB(0x5C, 0xAC, 0xEE)),
      new NamedColor("steelblue3", fromRGB(0x4F, 0x94, 0xCD)),
      new NamedColor("steelblue4", fromRGB(0x36, 0x64, 0x8B)),
      new NamedColor("tan", fromRGB(0xD2, 0xB4, 0x8C)),
      new NamedColor("tan1", fromRGB(0xFF, 0xA5, 0x4F)),
      new NamedColor("tan2", fromRGB(0xEE, 0x9A, 0x49)),
      new NamedColor("tan3", fromRGB(0xCD, 0x85, 0x3F)),
      new NamedColor("tan4", fromRGB(0x8B, 0x5A, 0x2B)),
      new NamedColor("thistle", fromRGB(0xD8, 0xBF, 0xD8)),
      new NamedColor("thistle1", fromRGB(0xFF, 0xE1, 0xFF)),
      new NamedColor("thistle2", fromRGB(0xEE, 0xD2, 0xEE)),
      new NamedColor("thistle3", fromRGB(0xCD, 0xB5, 0xCD)),
      new NamedColor("thistle4", fromRGB(0x8B, 0x7B, 0x8B)),
      new NamedColor("tomato", fromRGB(0xFF, 0x63, 0x47)),
      new NamedColor("tomato1", fromRGB(0xFF, 0x63, 0x47)),
      new NamedColor("tomato2", fromRGB(0xEE, 0x5C, 0x42)),
      new NamedColor("tomato3", fromRGB(0xCD, 0x4F, 0x39)),
      new NamedColor("tomato4", fromRGB(0x8B, 0x36, 0x26)),
      new NamedColor("turquoise", fromRGB(0x40, 0xE0, 0xD0)),
      new NamedColor("turquoise1", fromRGB(0x00, 0xF5, 0xFF)),
      new NamedColor("turquoise2", fromRGB(0x00, 0xE5, 0xEE)),
      new NamedColor("turquoise3", fromRGB(0x00, 0xC5, 0xCD)),
      new NamedColor("turquoise4", fromRGB(0x00, 0x86, 0x8B)),
      new NamedColor("violet", fromRGB(0xEE, 0x82, 0xEE)),
      new NamedColor("violetred", fromRGB(0xD0, 0x20, 0x90)),
      new NamedColor("violetred1", fromRGB(0xFF, 0x3E, 0x96)),
      new NamedColor("violetred2", fromRGB(0xEE, 0x3A, 0x8C)),
      new NamedColor("violetred3", fromRGB(0xCD, 0x32, 0x78)),
      new NamedColor("violetred4", fromRGB(0x8B, 0x22, 0x52)),
      new NamedColor("wheat", fromRGB(0xF5, 0xDE, 0xB3)),
      new NamedColor("wheat1", fromRGB(0xFF, 0xE7, 0xBA)),
      new NamedColor("wheat2", fromRGB(0xEE, 0xD8, 0xAE)),
      new NamedColor("wheat3", fromRGB(0xCD, 0xBA, 0x96)),
      new NamedColor("wheat4", fromRGB(0x8B, 0x7E, 0x66)),
      new NamedColor("whitesmoke", fromRGB(0xF5, 0xF5, 0xF5)),
      new NamedColor("yellow", fromRGB(0xFF, 0xFF, 0x00)),
      new NamedColor("yellow1", fromRGB(0xFF, 0xFF, 0x00)),
      new NamedColor("yellow2", fromRGB(0xEE, 0xEE, 0x00)),
      new NamedColor("yellow3", fromRGB(0xCD, 0xCD, 0x00)),
      new NamedColor("yellow4", fromRGB(0x8B, 0x8B, 0x00)),
      new NamedColor("yellowgreen", fromRGB(0x9A, 0xCD, 0x32)),
  };
}
