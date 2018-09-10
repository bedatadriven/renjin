/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gnur.api;

import org.renjin.sexp.SEXP;

import java.lang.invoke.MethodHandle;

public class DevDesc {

  /********************************************************
   * Device physical characteristics
   ********************************************************/

  /**
   * left raster coordinate
   */
  public double[] left = new double[1];

  /** right raster coordinate */
  public double[] right = new double[1];

  /** bottom raster coordinate */
  public double[] bottom = new double[1];

  /** top raster coordinate */
  public double[] top = new double[1];

  /* R only has the notion of a rectangular clipping region
   */

  public double clipLeft;
  public double clipRight;
  public double clipBottom;
  public double clipTop;

  /* I hate these next three -- they seem like a real fudge
   * BUT I'm not sure what to replace them with so they stay for now.
   */
  public double xCharOffset;	        /* x character addressing offset - unused */
  public double yCharOffset;	        /* y character addressing offset */
  public double yLineBias;	        /* 1/2 interline space as frac of line height */
  public double ipr[] = new double[2];	        /* Inches per raster; [0]=x, [1]=y */
  /* I hate this guy too -- seems to assume that a device can only
   * have one font size during its lifetime
   * BUT removing/replacing it would take quite a lot of work
   * to design and insert a good replacement so it stays for now.
   */
  public double cra[] = new double[2];	        /* Character size in rasters; [0]=x, [1]=y */
  public double gamma;	        /* (initial) Device Gamma Correction */
  /********************************************************
   * Device capabilities
   ********************************************************/
  public int canClip;		/* Device-level clipping */
  public int canChangeGamma;    /* can the gamma factor be modified? */
  public int canHAdj;	        /* Can do at least some horiz adjust of text
			           0 = none, 1 = {0,0.5,1}, 2 = [0,1] */
  /********************************************************
   * Device initial settings
   ********************************************************/
    /* These are things that the device must set up when it is created.
     * The graphics system can modify them and track current values,
     */
  public double startps;
  public int startcol;  /* sets par("fg"), par("col") and gpar("col") */
  public int startfill; /* sets par("bg") and gpar("fill") */
  public int startlty;
  public int startfont;
  public double startgamma;
  /********************************************************
   * Device specific information
   ********************************************************/
  public Object deviceSpecific;	/* pointer to device specific parameters */
  /********************************************************
   * Device display list
   ********************************************************/
  public int displayListOn;     /* toggle for initial display list status */


  /********************************************************
   * Event handling entries
   ********************************************************/

    /* Used in do_setGraphicsEventEnv */

  public int canGenMouseDown; /* can the device generate mousedown events */
  public int canGenMouseMove; /* can the device generate mousemove events */
  public int canGenMouseUp;   /* can the device generate mouseup events */
  public int canGenKeybd;     /* can the device generate keyboard events */

  public int gettingEvent;    /* This is set while getGraphicsEvent
				 is actively looking for events */

  /********************************************************
   * Device procedures.
   ********************************************************/

    /*
     * ---------------------------------------
     * GENERAL COMMENT ON GRAPHICS PARAMETERS:
     * ---------------------------------------
     * Graphical parameters are now passed in a pointer to a 
     * graphics context structure (pGEcontext) rather than individually.
     * Each device action should extract the parameters it needs
     * and ignore the others.  Thought should be given to which
     * parameters are relevant in each case -- the graphics engine
     * does not REQUIRE that each parameter is honoured, but if
     * a parameter is NOT honoured, it might be a good idea to
     * issue a warning when a parameter is not honoured (or at
     * the very least document which parameters are not honoured
     * in the user-level documentation for the device).  [An example
     * of a parameter that may not be honoured by many devices is
     * transparency.]
     */

    /*
     * device_Activate is called when a device becomes the
     * active device.  For example, it can be used to change the
     * title of a window to indicate the active status of
     * the device to the user.  Not all device types will
     * do anything.
     * The only parameter is a device driver structure.
     * An example is ...
     *
     * static void   X11_Activate(pDevDesc dd);
     *
     * As from R 2.14.0 this can be omitted or set to NULL.
     */
  // void (*activate)(const pDevDesc );

  public MethodHandle activate;

  /*
   * device_Circle should have the side-effect that a
   * circle is drawn, centred at the given location, with
   * the given radius.
   * (If the device has non-square pixels, 'radius' should
   * be interpreted in the units of the x direction.)
   * The border of the circle should be
   * drawn in the given "col", and the circle should be
   * filled with the given "fill" colour.
   * If "col" is NA_INTEGER then no border should be drawn
   * If "fill" is NA_INTEGER then the circle should not
   * be filled.
   * An example is ...
   *
   * static void X11_Circle(double x, double y, double r,
   *                        pGEcontext gc,
   *                        pDevDesc dd);
   *
   * R_GE_gcontext parameters that should be honoured (if possible):
   *   col, fill, gamma, lty, lwd
   */
  //void (*circle)(double x, double y, double r, const pGEcontext gc, pDevDesc dd);
  public MethodHandle circle;
    /*
     * device_Clip is given the left, right, bottom, and
     * top of a rectangle (in DEVICE coordinates).
     * It should have the side-effect that subsequent output
     * is clipped to the given rectangle.
     * NOTE that R's graphics engine already clips to the
     * extent of the device.
     * NOTE also that this will probably only be called if
     * the flag canClip is true.
     * An example is ...
     *
     * static void X11_Clip(double x0, double x1, double y0, double y1,
     *                      pDevDesc dd)
     */
//  void (*clip)(double x0, double x1, double y0, double y1, pDevDesc dd);

  public MethodHandle clip;

   /*
     * device_Close is called when the device is killed.
     * This function is responsible for destroying any
     * device-specific resources that were created in
     * device_Open and for FREEing the device-specific
     * parameters structure.
     * An example is ...
     *
     * static void X11_Close(pDevDesc dd)
     *
     */
//  void (*close)(pDevDesc dd);

  public MethodHandle close;
  /*
   * device_Deactivate is called when a device becomes
   * inactive.
   * This allows the device to undo anything it did in
   * dev_Activate.
   * Not all device types will do anything.
   * An example is ...
   *
   * static void X11_Deactivate(pDevDesc dd)
   *
   * As from R 2.14.0 this can be omitted or set to NULL.
   */
  public MethodHandle deactivate;
//  void (*deactivate)(pDevDesc );


  /*
   * device_Locator should return the location of the next
   * mouse click (in DEVICE coordinates)
   * Not all devices will do anything (e.g., postscript)
   * An example is ...
   *
   * static int X11_Locator(double *x, double *y, pDevDesc dd)
   *
   * As from R 2.14.0 this can be omitted or set to NULL.
   */
  public   MethodHandle locator;
//  int (*locator)(double *x, double *y, pDevDesc dd);

    /*
     * device_Line should have the side-effect that a single
     * line is drawn (from x1,y1 to x2,y2)
     * An example is ...
     *
     * static void X11_Line(double x1, double y1, double x2, double y2,
     *                      const pGEcontext gc,
     *                      pDevDesc dd);
     *
     * R_GE_gcontext parameters that should be honoured (if possible):
     *   col, gamma, lty, lwd
     */
//  void (*line)(double x1, double y1, double x2, double y2,
//		 const pGEcontext gc, pDevDesc dd);

  public  MethodHandle line;

  /*
   * device_MetricInfo should return height, depth, and
   * width information for the given character in DEVICE
   * units.
   * Note: in an 8-bit locale, c is 'char'.
   * In an mbcslocale, it is wchar_t, and at least some
   * of code assumes that is UCS-2 (Windows, true) or UCS-4.
   * This is used for formatting mathematical expressions
   * and for exact centering of text (see GText)
   * If the device cannot provide metric information then
   * it MUST return 0.0 for ascent, descent, and width.
   * An example is ...
   *
   * static void X11_MetricInfo(int c,
   *                            const pGEcontext gc,
   *                            double* ascent, double* descent,
   *                            double* width, pDevDesc dd);
   *
   * R_GE_gcontext parameters that should be honoured (if possible):
   *   font, cex, ps
   */
  public MethodHandle metricInfo;
//  void (*metricInfo)(int c, const pGEcontext gc,
//  double* ascent, double* descent, double* width,
//  pDevDesc dd);

  /*
   * device_Mode is called whenever the graphics engine
   * starts drawing (mode=1) or stops drawing (mode=0)
   * GMode (in graphics.c) also says that
   * mode = 2 (graphical input on) exists.
   * The device is not required to do anything
   * An example is ...
   *
   * static void X11_Mode(int mode, pDevDesc dd);
   *
   * As from R 2.14.0 this can be omitted or set to NULL.
   */
  public MethodHandle mode;
//  void (*mode)(int mode, pDevDesc dd);

  /*
   * device_NewPage is called whenever a new plot requires
   * a new page.
   * A new page might mean just clearing the
   * device (e.g., X11) or moving to a new page
   * (e.g., postscript)
   * An example is ...
   *
   *
   * static void X11_NewPage(const pGEcontext gc,
   *                         pDevDesc dd);
   *
   */
  public MethodHandle newPage;
//  void (*newPage)(const pGEcontext gc, pDevDesc dd);

  /*
   * device_Polygon should have the side-effect that a
   * polygon is drawn using the given x and y values
   * the polygon border should be drawn in the "col"
   * colour and filled with the "fill" colour.
   * If "col" is NA_INTEGER don't draw the border
   * If "fill" is NA_INTEGER don't fill the polygon
   * An example is ...
   *
   * static void X11_Polygon(int n, double *x, double *y,
   *                         const pGEcontext gc,
   *                         pDevDesc dd);
   *
   * R_GE_gcontext parameters that should be honoured (if possible):
   *   col, fill, gamma, lty, lwd
   */
//  void (*polygon)(int n, double *x, double *y, const pGEcontext gc, pDevDesc dd);
  public MethodHandle polygon;

  /*
   * device_Polyline should have the side-effect that a
   * series of line segments are drawn using the given x
   * and y values.
   * An example is ...
   *
   * static void X11_Polyline(int n, double *x, double *y,
   *                          const pGEcontext gc,
   *                          pDevDesc dd);
   *
   * R_GE_gcontext parameters that should be honoured (if possible):
   *   col, gamma, lty, lwd
   */
  public MethodHandle polyline;
  //  void (*polyline)(int n, double *x, double *y, const pGEcontext gc, pDevDesc dd);
    /*
     * device_Rect should have the side-effect that a
     * rectangle is drawn with the given locations for its
     * opposite corners.  The border of the rectangle
     * should be in the given "col" colour and the rectangle
     * should be filled with the given "fill" colour.
     * If "col" is NA_INTEGER then no border should be drawn
     * If "fill" is NA_INTEGER then the rectangle should not
     * be filled.
     * An example is ...
     *
     * static void X11_Rect(double x0, double y0, double x1, double y1,
     *                      const pGEcontext gc,
     *                      pDevDesc dd);
     *
     */
//  void (*rect)(double x0, double y0, double x1, double y1,
//		 const pGEcontext gc, pDevDesc dd);
  public MethodHandle rect;

    /*
     * device_Path should draw one or more sets of points 
     * as a single path
     * 
     * 'x' and 'y' give the points
     *
     * 'npoly' gives the number of polygons in the path
     * MUST be at least 1
     *
     * 'nper' gives the number of points in each polygon
     * each value MUST be at least 2
     *
     * 'winding' says whether to fill using the nonzero 
     * winding rule or the even-odd rule
     *
     * Added 2010-06-27
     *
     * As from R 2.13.2 this can be left unimplemented as NULL.
     */
//  void (*path)(double *x, double *y,
//  int npoly, int *nper,
//  int winding,
//                 const pGEcontext gc, pDevDesc dd);

  public MethodHandle path;
    /*
     * device_Raster should draw a raster image justified 
     * at the given location,
     * size, and rotation (not all devices may be able to rotate?)
     * 
     * 'raster' gives the image data BY ROW, with every four bytes
     * giving one R colour (ABGR).
     *
     * 'x and 'y' give the bottom-left corner.
     *
     * 'rot' is in degrees (as per device_Text), with positive
     * rotation anticlockwise from the positive x-axis.
     *
     * As from R 2.13.2 this can be left unimplemented as NULL.
     */
//  void (*raster)(unsigned int *raster, int w, int h,
//  double x, double y,
//  double width, double height,
//  double rot,
//  int interpolate,
//                   const pGEcontext gc, pDevDesc dd);


  public MethodHandle raster;
    /*
     * device_Cap should return an integer matrix (R colors)
     * representing the current contents of the device display.
     * 
     * The result is expected to be ROW FIRST.
     *
     * This will only make sense for raster devices and can 
     * probably only be implemented for screen devices.
     *
     * added 2010-06-27
     *
     * As from R 2.13.2 this can be left unimplemented as NULL.
     * For earlier versions of R it should return R_NilValue.
     */

  public MethodHandle cap;
  //#if R_USE_PROTOTYPES
//  SEXP (*cap)(pDevDesc dd);
    /*
     * device_Size is called whenever the device is
     * resized.
     * The function returns (left, right, bottom, and top) for the
     * new device size.
     * This is not usually called directly by the graphics
     * engine because the detection of device resizes
     * (e.g., a window resize) are usually detected by
     * device-specific code.
     * An example is ...
     *
     * static void X11_Size(double *left, double *right,
     *                      double *bottom, double *top,
     *                      pDevDesc dd);
     *
     * R_GE_gcontext parameters that should be honoured (if possible):
     *   col, fill, gamma, lty, lwd
     *
     * As from R 2.13.2 this can be left unimplemented as NULL.
     */
  public MethodHandle size;
//  void (*size)(double *left, double *right, double *bottom, double *top,
//  pDevDesc dd);
    /*
     * device_StrWidth should return the width of the given
     * string in DEVICE units.
     * An example is ...
     *
     * static double X11_StrWidth(const char *str,
     *                            const pGEcontext gc,
     *                            pDevDesc dd)
     *
     * R_GE_gcontext parameters that should be honoured (if possible):
     *   font, cex, ps
     */
//  double (*strWidth)(const char *str, const pGEcontext gc, pDevDesc dd);

  public MethodHandle strWidth;

    /*
     * device_Text should have the side-effect that the
     * given text is drawn at the given location.
     * The text should be rotated according to rot (degrees)
     * An example is ...
     *
     * static void X11_Text(double x, double y, const char *str,
     *                      double rot, double hadj,
     *                      const pGEcontext gc,
     * 	                    pDevDesc dd);
     *
     * R_GE_gcontext parameters that should be honoured (if possible):
     *   font, cex, ps, col, gamma
     */

  public MethodHandle text;
//
//  void (*text)(double x, double y, const char *str, double rot,
//  double hadj, const pGEcontext gc, pDevDesc dd);
    /*
     * device_onExit is called by GEonExit when the user has aborted
     * some operation, and so an R_ProcessEvents call may not return normally.
     * It need not be set to any value; if null, it will not be called.
     *
     * An example is ...
     *
     * static void GA_onExit(pDevDesc dd);
    */

  public MethodHandle onExit;

  //  void (*onExit)(pDevDesc dd);
    /*
     * device_getEvent is no longer used, but the slot is kept for back
     * compatibility of the structure.
     */
  public MethodHandle getEvent;
//  SEXP (*getEvent)(SEXP, const char *);

    /* --------- Optional features introduced in 2.7.0 --------- */

    /* Does the device have a device-specific way to confirm a 
       new frame (for e.g. par(ask=TRUE))?
       This should be NULL if it does not.
       If it does, it returns TRUE if the device handled this, and
       FALSE if it wants the engine to do so. 

       There is an example in the windows() device.

       Can be left unimplemented as NULL.
    */

  public MethodHandle newFrameConfirm;
  //  int (*newFrameConfirm)(pDevDesc dd);

  /* Some devices can plot UTF-8 text directly without converting
     to the native encoding, e.g. windows(), quartz() ....

     If this flag is true, all text *not in the symbol font* is sent
     in UTF8 to the textUTF8/strWidthUTF8 entry points.

     If the flag is TRUE, the metricInfo entry point should
     accept negative values for 'c' and treat them as indicating
     Unicode points (as well as positive values in a MBCS locale).
  */
  public int hasTextUTF8; /* and strWidthUTF8 */

  public MethodHandle textUTF8;

//  void (*textUTF8)(double x, double y, const char *str, double rot,
//  double hadj, const pGEcontext gc, pDevDesc dd);
//  double (*strWidthUTF8)(const char *str, const pGEcontext gc, pDevDesc dd);

  public int wantSymbolUTF8;

  /* Is rotated text good enough to be preferable to Hershey in
     contour labels?  Old default was FALSE.
  */
  public int useRotatedTextInContour;

    /* --------- Post-2.7.0 features --------- */

    /* Added in 2.12.0:  Changed graphics event handling. */

  public SEXP eventEnv;   /* This is an environment holding event handlers. */
    /*
     * eventHelper(dd, 1) is called by do_getGraphicsEvent before looking for a 
     * graphics event.  It will then call R_ProcessEvents() and eventHelper(dd, 2)
     * until this or another device returns sets a non-null result value in eventEnv,
     * at which time eventHelper(dd, 0) will be called.
     * 
     * An example is ...
     *
     * static SEXP GA_eventHelper(pDevDesc dd, int code);

     * Can be left unimplemented as NULL
     */
//  void (*eventHelper)(pDevDesc dd, int code);

  public MethodHandle eventHelper;

    /* added in 2.14.0, only used by screen devices.

       Allows graphics devices to have multiple levels of suspension: 
       when this reaches zero output is flushed.

       Can be left unimplemented as NULL.
     */
//  int (*holdflush)(pDevDesc dd, int level);

  public MethodHandle holdflush;

  /* added in 2.14.0, for dev.capabilities.
     In all cases 0 means NA (unset).
  */
  public int haveTransparency; /* 1 = no, 2 = yes */
  public int haveTransparentBg; /* 1 = no, 2 = fully, 3 = semi */
  public int haveRaster; /* 1 = no, 2 = yes, 3 = except for missing values */
  public int haveCapture, haveLocator;  /* 1 = no, 2 = yes */


  public void setLeft(double left) {
    this.left[0] = left;
  }

  public void setRight(double right) {
    this.right[0] = right;
  }

  public void setTop(double top) {
    this.top[0] = top;
  }

  public void setBottom(double bottom) {
    this.bottom[0] = bottom;
  }

  public void setClipLeft(double clipLeft) {
    this.clipLeft = clipLeft;
  }

  public void setClipRight(double clipRight) {
    this.clipRight = clipRight;
  }

  public void setClipBottom(double clipBottom) {
    this.clipBottom = clipBottom;
  }

  public void setClipTop(double clipTop) {
    this.clipTop = clipTop;
  }
}
