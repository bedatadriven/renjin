package org.renjin.primitives.graphics;


import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.graphics.GraphicsDevice;
import org.renjin.graphics.GraphicsDevices;
import org.renjin.graphics.geom.Point;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Recycle;


public class Graphics {

  public static final int DEVICE = 1;
  public static final int NDC = 2;
  public static final int NIC = 7;
  public static final int NFC = 8;
  public static final int USER = 13;
  public static final int INCHES = 14;
  public static final int NPC = 17;
  
  
  public static double grconvertX(@Current Context context, @Recycle double x, int from, int to) {
    return grConvert(context, new Point(x, Double.NaN), from, to).getX();
  }
  
  public static double grconvertY(@Current Context context, @Recycle double y, int from, int to) {
    return grConvert(context, new Point(Double.NaN, y), from, to).getY();
  }
 
  private static Point grConvert(Context context, Point point, int from, int to) {
    GraphicsDevice active = context.getSession().getSingleton(GraphicsDevices.class).getActive();
    Point device = toDeviceCoordinates(active, point, from);
    return fromDeviceCoordinates(active, device, to);
  }

  private static Point toDeviceCoordinates(GraphicsDevice active, Point point, int from) {
    switch(from) {
    case USER:
      return active.userToDevice(point);
    default:
      throw new EvalException("Invalid 'from' argument");
    }
  }
  
  private static Point fromDeviceCoordinates(GraphicsDevice active, Point point, int to) {
    switch(to) {
    case DEVICE:
       return point;
    default:
      throw new EvalException("Invalid 'to' argument");
    }
  }
  
}
