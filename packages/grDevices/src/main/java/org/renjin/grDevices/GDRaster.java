package org.renjin.grDevices;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.Ptr;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;

class GDRaster implements GDObject {
  private boolean interpolate;
  private Image image;
  private AffineTransform atrans;

  public GDRaster(Ptr image, int imageWidth, int imageHeight, double x, double y, double w, double h, double rot, boolean interpolate) {
    this.interpolate = interpolate;
    atrans = new AffineTransform();
    // R seems to use flipped y coordinates
    y += h;
    h = -h;

    double sx = w / (double) imageWidth;
    double sy = h / (double) imageHeight;
    atrans.translate(x, y);
    atrans.rotate(-rot / 180 * Math.PI, 0, y);
    atrans.scale(sx, sy);

    DataBuffer dbuf = toDataBuffer(image, imageWidth * imageHeight * 4);

    int[] compOffsets = {0, 1, 2, 3};
    SampleModel sm = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, imageWidth, imageHeight,
        4, imageWidth * 4, compOffsets);
    WritableRaster raster = Raster.createWritableRaster(sm, dbuf, null);
    ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
        true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    this.image = new BufferedImage(cm, raster, false, null);
  }

  private DataBuffer toDataBuffer(Ptr image, int size) {
    if(image instanceof BytePtr) {
      // Fast path
      BytePtr bytePtr = (BytePtr) image;
      return new DataBufferByte(bytePtr.array, size, bytePtr.offset);

    } else {
      // Need to make a copy...
      byte[] buffer = new byte[size];
      for (int i = 0; i < buffer.length; i++) {
        buffer[i] = image.getByte(i);
      }
      return new DataBufferByte(buffer, size, 0);
    }
  }

  @Override
  public void paint(Component c, GDState gs, Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    Object oh = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    try {
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolate ?
          RenderingHints.VALUE_INTERPOLATION_BILINEAR :
          RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      g2.drawImage(image, atrans, null);

    } finally {
      if (oh != null) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oh);
      }
    }
  }
}
