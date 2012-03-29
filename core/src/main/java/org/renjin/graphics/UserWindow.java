package org.renjin.graphics;

import org.renjin.eval.EvalException;
import org.renjin.graphics.geom.Point;
import org.renjin.graphics.geom.Rectangle;
import org.renjin.sexp.DoubleVector;



public class UserWindow {
  private static final int EPS_FAC_1 = 16;
  private static final int EPS_FAC_2 = 100;
  
  private Rectangle coordinates;
  private Rectangle logCoordinates;
  
  private boolean xLog;
  private boolean yLog;
  
  public UserWindow() {
    setUserCoordinates(new Rectangle(0, 1, 0, 1));
  }
  
  
  public Rectangle getUserCoordinates() {
    return coordinates;
  }
  
  public void setUserCoordinates(Rectangle coords) {
    double x1, x2, logX1, logX2;
  
    if(xLog) {
      logX1 = coords.getX1();
      logX2 = coords.getX2();
      x1 = Math.pow(logX1, 10);
      x2 = Math.pow(logX2, 10);
    } else {
      x1 = coords.getX1();
      x2 = coords.getX2();
      logX1 = Math.log10(x1);
      logX2 = Math.log10(x2);
    }
  
    double y1, y2, logY1, logY2;
  
    if(yLog) {
      logY1 = coords.getY1();
      logY2 = coords.getY2();
      y1 = Math.pow(logY1, 10);
      y2 = Math.pow(logY2, 10);
    } else {
      y1 = coords.getY1();
      y2 = coords.getY2();
      logY1 = Math.log10(y1);
      logY2 = Math.log10(y2);
    }
  
    coordinates = new Rectangle(x1, x2, y1, y2);
    logCoordinates = new Rectangle(logX1, logX2, logY1, logY2);
  }
  
  public Rectangle normalize(Rectangle rect) {
    //TODO: log stuff
    return new Rectangle(
        (rect.getX1() - coordinates.getX1()) / coordinates.getWidth(),
        (rect.getX2() - coordinates.getX1()) / coordinates.getWidth(),
        (rect.getY1() - coordinates.getY1()) / coordinates.getHeight(),
        (rect.getY2() - coordinates.getY1()) / coordinates.getHeight());
  }
  
  public Point normalize(Point point) {
    return new Point(
        (point.getX() - coordinates.getX1()) / coordinates.getWidth(),
        (point.getY() - coordinates.getY1()) / coordinates.getHeight());
  }
  
  public boolean isLog(Axis axis) {
    switch(axis) {
    case X:
      return xLog;
    case Y:
      return yLog;
    }
    throw new IllegalArgumentException("" + axis);
  }
  

  public void setLimits(Rectangle limits, GraphicParameters parameters) {
    computeScale(Axis.X, limits.getX1(), limits.getX2(), parameters);
    computeScale(Axis.Y, limits.getY1(), limits.getY2(), parameters);
  }  
  
  private void computeScale(Axis axis, double min, double max, GraphicParameters parameters) {
/* GScale: used to default axis information
 *         i.e., if user has NOT specified par(usr=...)
 * NB: can have min > max !
 */

    boolean swap;
    double temp, min_o = 0., max_o = 0., tmp2 = 0.;/*-Wall*/

    int n = parameters.getAxisStyle(axis).getTickmarkCount();
    AxisIntervalCalculationStyle style = parameters.getAxisStyle(axis).getCalculationStyle();
    
    if (isLog(axis)) {
        /*  keep original  min, max - to use in extremis */
        min_o = min; max_o = max;
        min = Math.log10(min);
        max = Math.log10(max);
    }
    if(!DoubleVector.isFinite(min) || !DoubleVector.isFinite(max)) {
//        warning(_("nonfinite axis limits [GScale(%g,%g,%d, .); log=%d]"),
//                min, max, axis, log);
        if(!DoubleVector.isFinite(min)) {
          min = - .45 * Double.MAX_VALUE;
        }
        if(!DoubleVector.isFinite(max)) {
          max = + .45 * Double.MAX_VALUE;
        }
        /* max - min is now finite */
    }
    /* Version <= 1.2.0 had
       if (min == max)   -- exact equality for real numbers */
    temp = Math.max(Math.abs(max), Math.abs(min));
    if(temp == 0) {/* min = max = 0 */
        min = -1;
        max =  1;
    } else if(Math.abs(max - min) < temp * EPS_FAC_1 * DoubleVector.EPSILON) {
        temp *= (min == max) ? .4 : 1e-2;
        min -= temp;
        max += temp;
    }

    switch(style) {
    case REGULAR:
        temp = 0.04 * (max-min);
        min -= temp;
        max += temp;
        break;
    case INTERNAL:
        break;
    case STANDARD:/* FIXME --- implement  's' and 'e' axis styles ! */
    case EXTENDED:
    default:
        throw new EvalException("axis style \"%c\" unimplemented", style.name());
    }

    if (isLog(axis)) { /* 10^max may have gotten +Inf ; or  10^min has become 0 */
        if((temp = Math.pow(10., min)) == 0.) {/* or < 1.01*DBL_MIN */
            temp = Math.min(min_o, 1.01* Double.MIN_VALUE); /* allow smaller non 0 */
            min = Math.log10(temp);
        }
        if((tmp2 =  Math.pow(10., max)) == Double.POSITIVE_INFINITY) { /* or  > .95*DBL_MAX */
            tmp2 = Math.max(max_o, .99 * Double.MAX_VALUE);
            max = Math.log10(tmp2);
        }
    }
    if(isLog(axis)) {
      updateLimits(axis, temp, tmp2);
      updateLogLimits(axis, min, max);
    } else {
      updateLimits(axis, min, max);
    }
    
//    /* ------  The following : Only computation of [xy]axp[0:2] ------- */
//
//    /* This is not directly needed when [xy]axt = "n",
//     * but may later be different in another call to axis(), e.g.:
//      > plot(1, xaxt = "n");  axis(1)
//     * In that case, do_axis() should do the following.
//     * MM: May be we should modularize and put the following into another
//     * subroutine which could be called by do_axis {when [xy]axt != 'n'} ..
//     */
//
//    swap = min > max;
//    if(swap) { /* Feature: in R, something like  xlim = c(100,0)  just works */
//        temp = min; min = max; max = temp;
//    }
//    /* save only for the extreme case (EPS_FAC_2): */
//    min_o = min; max_o = max;
//
//    if(isLog(axis)) {
//        /* Avoid infinities */
//        if(max > 308) max = 308;
//        if(min < -307) min = -307;
//        min = Math.pow(10., min);
//        max = Math.pow(10., max);
//        GLPretty(&min, &max, &n);
//    } else {
//      GPretty(&min, &max, &n);
//    }
//
//    tmp2 = EPS_FAC_2 * DBL_EPSILON;/* << prevent overflow in product below */
//    if(fabs(max - min) < (temp = fmax2(fabs(max), fabs(min)))* tmp2) {
//        /* Treat this case somewhat similar to the (min ~= max) case above */
//        /* Too much accuracy here just shows machine differences */
//        warning(_("relative range of values =%4.0f * EPS, is small (axis %d)")
//                /*"to compute accurately"*/,
//                fabs(max - min) / (temp*DBL_EPSILON), axis);
//
//        /* No pretty()ing anymore */
//        min = min_o;
//        max = max_o;
//        temp = .005 * fabs(max - min);/* .005: not to go to DBL_MIN/MAX */
//        min += temp;
//        max -= temp;
//        if(log) {
//            min = pow(10., min);
//            max = pow(10., max);
//        }
//        n = 1;
//    }
//
//    if(swap) {
//        temp = min; min = max; max = temp;
//    }
////
////#define G_Store_AXP(is_X)                       \
////    if(is_X) {                                  \
////        gpptr(dd)->xaxp[0] = dpptr(dd)->xaxp[0] = min;  \
////        gpptr(dd)->xaxp[1] = dpptr(dd)->xaxp[1] = max;  \
////        gpptr(dd)->xaxp[2] = dpptr(dd)->xaxp[2] = n;    \
////    }                                           \
////    else {                                      \
////        gpptr(dd)->yaxp[0] = dpptr(dd)->yaxp[0] = min;  \
////        gpptr(dd)->yaxp[1] = dpptr(dd)->yaxp[1] = max;  \
////        gpptr(dd)->yaxp[2] = dpptr(dd)->yaxp[2] = n;    \
////    }
//
//    G_Store_AXP(is_xaxis);
}

  private void updateLimits(Axis axis, double min, double max) {
    switch(axis) {
    case X:
      coordinates = coordinates.withXLimits(min, max);
      break;
    case Y:
      coordinates = coordinates.withYLimits(min, max);
      break;
    default:
      throw new IllegalArgumentException();
    }
  }
  
  private void updateLogLimits(Axis axis, double min, double max) {
    switch(axis) {
    case X:
      logCoordinates = logCoordinates.withXLimits(min, max);
      break;
    case Y:
      logCoordinates = logCoordinates.withYLimits(min, max);
      break;
    default:
      throw new IllegalArgumentException();
    }    
  }


}
