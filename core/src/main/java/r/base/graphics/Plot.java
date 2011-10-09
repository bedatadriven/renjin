package r.base.graphics;

import r.jvmi.annotations.ArgumentList;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.Context;
import r.lang.ListVector;
import r.lang.Null;
import r.lang.Vector;
import r.lang.graphics.Color;
import r.lang.graphics.GraphicsDevice;
import r.lang.graphics.LineType;

public class Plot {

  /**
   * Create a new plot "frame"
   */
  @Primitive("plot.new")
  public static void do_plot_new(@Current Context context)
  {
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
//    GScale(0.0, 1.0, 1, dd);
//    GScale(0.0, 1.0, 2, dd);
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
    dd.setPlotWindow(xlim.getElementAsDouble(0), ylim.getElementAsDouble(0),
              xlim.getElementAsDouble(1), ylim.getElementAsDouble(1));


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

      dd.drawRectangle(xleft.getElementAsDouble(i % xleft.length()),
                      ybottom.getElementAsDouble(i % ybottom.length()),
                      xright.getElementAsDouble(i % xright.length()),
                      ytop.getElementAsDouble(i % ytop.length()),
                      fillColor, borderColor);
    }
    dd.restoreParameters();
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
