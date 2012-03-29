package org.renjin.primitives.graphics;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.graphics.Color;
import org.renjin.graphics.GraphicsDevice;
import org.renjin.graphics.LineType;
import org.renjin.graphics.TextStyle;
import org.renjin.graphics.geom.Point;
import org.renjin.graphics.geom.Rectangle;
import org.renjin.primitives.annotations.ArgumentList;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Vector;


public class Plot {

  /**
   * Create a new plot "frame"
   */
  @Primitive("plot.new")
  public static void do_plot_new(@Current Context context)
  {
    GraphicsDevice dd = Devices.GEcurrentDevice(context);

    //    /* plot.new() - create a new plot "frame" */
    //
    //    GraphicsDevice dd = Graphics.GEcurrentDevice(context);
    //
    //    dd = Graphics.GEcurrentDevice(context);
    //    /*
    //     * If user is prompted before new page, user has opportunity
    //     * to kill current device.  GNewPlot returns (potentially new)
    //     * current device.
    //     */
    //    dd = GNewPlot(GRecording(call, dd));
    //
    //    dpptr(dd)->xlog = gpptr(dd)->xlog = FALSE;
    //    dpptr(dd)->ylog = gpptr(dd)->ylog = FALSE;
    //
    dd.setUserLimits(Rectangle.UNIT_RECT);

    //    GMapWin2Fig(dd);
    //    GSetState(1, dd);
    //
    //    if (GRecording(call, dd))
    //      GErecordGraphicOperation(op, args, dd);
    //    return R_NilValue;
  }



  @Primitive("plot.window")
  public static void plotWindow(@Current Context context, Vector xlim, Vector ylim, Vector log, Vector asp,
      @ArgumentList ListVector parameters) {

    GraphicsDevice dd = Devices.GEcurrentDevice(context);
    dd.setUserLimits(Rectangle.from(xlim, ylim));

    //    dd.setPlotWindow(xlim.getElementAsDouble(0), ylim.getElementAsDouble(0),
    //              xlim.getElementAsDouble(1), ylim.getElementAsDouble(1));


  }

  public static void rect(@Current Context context, Vector xleft, Vector ybottom, Vector xright, Vector ytop,
      Vector col,
      Vector border,
      Vector lty,
      Vector lwd,
      @ArgumentList ListVector parameters) {

    GraphicsDevice dd = Devices.GEcurrentDevice(context);
    dd.saveParameters();

    int numRects = maxLength(xleft, ybottom, xright, ytop);
    for(int i=0;i!=numRects; ++i) {

      Color fillColor = Color.fromExp(context, dd, col, i % col.length());
      Color borderColor = border == Null.INSTANCE ? Color.TRANSPARENT_WHITE :
        Color.fromExp(context, dd, border, i % border.length());



      dd.getParameters()
      .setLineType( LineType.valueOf(lty, i % lty.length()) )
      .setLineWidth( lwd.getElementAsDouble( i % lwd.length() ));

      dd.drawRectangle(new Rectangle(
          xleft.getElementAsDouble(i % xleft.length()),
          xright.getElementAsDouble(i % xright.length()),
          ybottom.getElementAsDouble(i % ybottom.length()),
          ytop.getElementAsDouble(i % ytop.length())),
          fillColor, borderColor);
    }
    dd.restoreParameters();
  }

  public static void title(@Current Context context, SEXP main, SEXP sub, SEXP xlab, SEXP ylab,
      double line, boolean outer, @ArgumentList ListVector arguments) {
    GraphicsDevice dd = Devices.GEcurrentDevice(context);
    dd.saveParameters();   

    double adj, adjy, offset, hpos, vpos, where;
    int i, n;
    

    //  GCheckState(dd);
    //TODO:    ProcessInlinePars(args, dd, call);

    /* override par("xpd") and force clipping to figure region
       NOTE: don't override to _reduce_ clipping region */
//    if (gpptr(dd)->xpd < 1)
//            gpptr(dd)->xpd = 1;
//        if (outer)
//            gpptr(dd)->xpd = 2;

    adj = dd.getParameters().getTextJustification();

    //    GMode(1, dd);
    if (main != Null.INSTANCE) {
      dd.getParameters().getMainTitleStyle();

      /* GetTextArg may coerce, so protect the result */
      //GetTextArg(call, Main, &Main, &col, &cex, &font);

      //   dd.getParameters().setCurrentTextStyle(style);

      if (outer) {
//        if (DoubleVector.isFinite(line)) {
//          vpos = line;
//          adjy = 0;
//        }
//        else {
//          vpos = 0.5 * dd.getOuterMargins().getTop();
//          adjy = 0.5;
//        }
//        hpos = adj;
//        where = 0; //OMA3;
        throw new EvalException("main title in outer margins not yet implemented");
      } else {
        if (DoubleVector.isFinite(line)) {
          vpos = line;
          adjy = 0;
        }
        else {
          vpos = 0.5 * dd.getInnerMargins().getTop();
          adjy = 0.5;
        }
        hpos = 0; // GConvertX(adj, NPC, USER, dd);
        where = 0; //MAR3;
      }
      if (main instanceof ExpressionVector) {
        //            GMathText(hpos, vpos, where, main.getElementAsSEXP(0),
        //                      adj, 0.5, 0.0, dd);
      } else {
        n = main.length();
        offset = 0.5 * (n - 1) + vpos;
        for (i = 0; i < n; i++) {
          String text = ((Vector)main).getElementAsString(i);
          if(!StringVector.isNA(text)) {
            dd.text(new Point(hpos, offset - i), where, text,
                new Point(adj, adjy), 0.0);
          }
        }
      }
    }
      //    if (sub != R_NilValue) {
      //        cex = gpptr(dd)->cexsub;
      //        col = gpptr(dd)->colsub;
      //        font = gpptr(dd)->fontsub;
      //        /* GetTextArg may coerce, so protect the result */
      //        GetTextArg(call, sub, &sub, &col, &cex, &font);
      //        PROTECT(sub);
      //        gpptr(dd)->col = col;
      //        gpptr(dd)->cex = gpptr(dd)->cexbase * cex;
      //        gpptr(dd)->font = font;
      //        if (R_FINITE(line))
      //            vpos = line;
      //        else
      //            vpos = gpptr(dd)->mgp[0] + 1;
      //        if (outer) {
      //            hpos = adj;
      //            where = 1;
      //        }
      //        else {
      //            hpos = GConvertX(adj, NPC, USER, dd);
      //            where = 0;
      //        }
      //        if (isExpression(sub))
      //            GMMathText(VECTOR_ELT(sub, 0), 1, vpos, where,
      //                       hpos, 0, 0.0, dd);
      //        else {
      //            n = length(sub);
      //            for (i = 0; i < n; i++) {
      //                string = STRING_ELT(sub, i);
      //                if(string != NA_STRING)
      //                    GMtext(CHAR(string), getCharCE(string), 1, vpos, where,
      //                           hpos, 0, 0.0, dd);
      //            }
      //        }
      //        UNPROTECT(1);
      //    }
      //    if (xlab != R_NilValue) {
      //        cex = gpptr(dd)->cexlab;
      //        col = gpptr(dd)->collab;
      //        font = gpptr(dd)->fontlab;
      //        /* GetTextArg may coerce, so protect the result */
      //        GetTextArg(call, xlab, &xlab, &col, &cex, &font);
      //        PROTECT(xlab);
      //        gpptr(dd)->cex = gpptr(dd)->cexbase * cex;
      //        gpptr(dd)->col = col;
      //        gpptr(dd)->font = font;
      //        if (R_FINITE(line))
      //            vpos = line;
      //        else
      //            vpos = gpptr(dd)->mgp[0];
      //        if (outer) {
      //            hpos = adj;
      //            where = 1;
      //        }
      //        else {
      //            hpos = GConvertX(adj, NPC, USER, dd);
      //            where = 0;
      //        }
      //        if (isExpression(xlab))
      //            GMMathText(VECTOR_ELT(xlab, 0), 1, vpos, where,
      //                       hpos, 0, 0.0, dd);
      //        else {
      //            n = length(xlab);
      //            for (i = 0; i < n; i++) {
      //                string = STRING_ELT(xlab, i);
      //                if(string != NA_STRING)
      //                    GMtext(CHAR(string), getCharCE(string), 1, vpos + i,
      //                           where, hpos, 0, 0.0, dd);
      //            }
      //        }
      //        UNPROTECT(1);
      //    }
      //    if (ylab != R_NilValue) {
      //        cex = gpptr(dd)->cexlab;
      //        col = gpptr(dd)->collab;
      //        font = gpptr(dd)->fontlab;
      //        /* GetTextArg may coerce, so protect the result */
      //        GetTextArg(call, ylab, &ylab, &col, &cex, &font);
      //        PROTECT(ylab);
      //        gpptr(dd)->cex = gpptr(dd)->cexbase * cex;
      //        gpptr(dd)->col = col;
      //        gpptr(dd)->font = font;
      //        if (R_FINITE(line))
      //            vpos = line;
      //        else
      //            vpos = gpptr(dd)->mgp[0];
      //        if (outer) {
      //            hpos = adj;
      //            where = 1;
      //        }
      //        else {
      //            hpos = GConvertY(adj, NPC, USER, dd);
      //            where = 0;
      //        }
      //        if (isExpression(ylab))
      //            GMMathText(VECTOR_ELT(ylab, 0), 2, vpos, where,
      //                       hpos, 0, 0.0, dd);
      //        else {
      //            n = length(ylab);
      //            for (i = 0; i < n; i++) {
      //                string = STRING_ELT(ylab, i);
      //                if(string != NA_STRING)
      //                    GMtext(CHAR(string), getCharCE(string), 2, vpos - i,
      //                           where, hpos, 0, 0.0, dd);
      //            }
      //        }
      //        UNPROTECT(1);
      //    }
      //    

      dd.restoreParameters();

    }

    public static void axis(SEXP side, SEXP at, SEXP labels, SEXP tick, 
        SEXP line, SEXP pos, SEXP outer, SEXP font, SEXP lty, SEXP lwd, SEXP lwdTicks, 
        SEXP col, SEXP colTicks, 
        SEXP hadj, SEXP padj, @ArgumentList ListVector arguments) {


    }

    private static int maxLength(Vector... vectors) {
      int n = 0;
      for(Vector vector : vectors) {
        if(vector.length() > n) {
          n = vector.length();
        }
      }
      return n;
    }


  }
