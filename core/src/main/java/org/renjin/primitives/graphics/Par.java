package org.renjin.primitives.graphics;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.graphics.*;
import org.renjin.graphics.geom.Margins;
import org.renjin.graphics.geom.Rectangle;
import org.renjin.primitives.annotations.Current;
import org.renjin.sexp.*;


public class Par {


  /**
   * Gets or sets graphical parameters.
   */
  public static SEXP par(@Current Context context, ListVector args) {
    GraphicsDevice dd;
    
    ListVector.NamedBuilder result = new ListVector.NamedBuilder();
    
    dd = Devices.GEcurrentDevice(context);
    for(NamedValue namedValue : args.namedValues()) {
      if(namedValue.hasName()) {
        Parameter parameter = findParameter(namedValue.getName());

        // add old value to return value
        result.add(namedValue.getName(), parameter.query(dd));

        // set new value
        parameter.specify(context, dd, namedValue.getValue());
      
      } else if(namedValue.getValue() instanceof StringVector) {
        StringVector vector = (StringVector)namedValue.getValue();
        String parName = vector.getElementAsString(0);
        result.add(namedValue.getName(),
            findParameter(parName).query(dd));
      
      } else {
        result.add("", Null.INSTANCE);
      }
    }
              /* should really only do this if specifying new pars ?  yes! [MM] */
//    if (new_spec && GRecording(call, dd))
//        GErecordGraphicOperation(op, originalArgs, dd);
    return result.build();
  }

  private static abstract class Parameter {
    private final String name;

    protected Parameter(String name) {
      this.name = name;
    }

    public final String getName() {
      return name;
    }

    public SEXP query(GraphicsDevice dd) {
      throw new EvalException("implement me: " + getName());
    }

    public void specify(Context context, GraphicsDevice dd, SEXP exp) {
      throw new EvalException("implement me" + getName());
    }

    public void specifyInline(Context context, GraphicsDevice dd, SEXP exp) {
      specify(context, dd, null);
    }

    protected final Color toColor(Context context, GraphicsDevice dd, SEXP exp) {
      if(!(exp instanceof Vector) || exp.length() < 1) {
        throw new EvalException("invalid rgb specification: " + exp.toString());
      }
      return Color.fromExp(context.getSession().getSingleton(ColorPalette.class),
                           dd.getParameters().getBackground(),
                            (Vector)exp, 0);
    }
    
    protected final double toDouble(SEXP exp) {
      if(!(exp instanceof Vector) || exp.length() < 1) {
        throw new EvalException("invalid par value for '%s'", getName());
      }
      return ((Vector)exp).getElementAsDouble(0);
    }
  }

  private static abstract class ReadOnlyParameter extends Parameter {
    protected ReadOnlyParameter(String name) {
      super(name);
    }

    @Override
    public final void specify(Context context, GraphicsDevice dd, SEXP exp) {
      // todo
    }

    @Override
    public final void specifyInline(Context context, GraphicsDevice dd, SEXP exp) {
      // todo
    }
  }

  private static abstract class NonInlineParameter extends Parameter {
    protected NonInlineParameter(String name) {
      super(name);
    }
  }

    private static abstract class ObsoleteParameter extends Parameter {
      protected ObsoleteParameter(String name) {
        super(name);
      }
    }

  private static abstract class GraphicalArg extends Parameter {
    protected GraphicalArg(String name) {
      super(name);
    }
  }

  private static Parameter findParameter(String name) {
   for(Parameter param : ParTable) {
     if(param.getName().equals(name)) {
       return param;
     }
   }
   throw new EvalException("no parameter found by name " + name);

  }

  static final Parameter ParTable  [] = new Parameter[] {
    new Parameter("adj") {},
    new Parameter("ann") {},
    new NonInlineParameter("ask") {

    },
    new Parameter("bg") {},
    new Parameter("bty") {},
    new Parameter("cex") {

      @Override
      public SEXP query(GraphicsDevice dd) {
        return new DoubleArrayVector(dd.getParameters().getCexBase());
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().setCexBase(toDouble(exp));
      }
    },
    new Parameter("cex.axis") {

      @Override
      public SEXP query(GraphicsDevice dd) {
        return new DoubleArrayVector( dd.getParameters().getAxisAnnotationStyle().getFontSizeFactor() );
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().getAxisAnnotationStyle().setFontSizeFactor( toDouble(exp) );
      }
    },
    new Parameter("cex.lab") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return new DoubleArrayVector( dd.getParameters().getXyLabelStyle().getFontSizeFactor() );
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().getXyLabelStyle().setFontSizeFactor( toDouble(exp) );
      }
    },
    new Parameter("cex.main") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return new DoubleArrayVector( dd.getParameters().getMainTitleStyle().getFontSizeFactor() );
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().getMainTitleStyle().setFontSizeFactor( toDouble(exp) );
      }
    },
    new Parameter("cex.sub") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return new DoubleArrayVector( dd.getParameters().getSubTitleStyle().getFontSizeFactor() );
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().getSubTitleStyle().setFontSizeFactor( toDouble(exp) );
      }
    },
    new ReadOnlyParameter("cin") {},
    new Parameter("col") {},
    new Parameter("col.axis") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getParameters().getAxisAnnotationStyle().getColor().toExp();
      }
      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().getAxisAnnotationStyle().setColor( toColor(context, dd, exp) );
      }
    },
    new Parameter("col.lab") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getParameters().getXyLabelStyle().getColor().toExp();
      }
      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().getXyLabelStyle().setColor( toColor(context, dd, exp) );
      }
    },
    new Parameter("col.main") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getParameters().getMainTitleStyle().getColor().toExp();
      }
      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().getMainTitleStyle().setColor( toColor(context, dd, exp) );
      }
    },
    new Parameter("col.sub") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getParameters().getSubTitleStyle().getColor().toExp();
      }
      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().getSubTitleStyle().setColor( toColor(context, dd, exp) );
      }
    },
    new ReadOnlyParameter("cra") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getDefaultCharacterSize().toVector();
      }
    },
    new Parameter("crt") {},
    new ReadOnlyParameter("csi") {},
    new Parameter("csy") {},
    new ReadOnlyParameter("cxy") {},
    new ReadOnlyParameter("din") {

      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getDeviceSizeInInches().toVector();
      }
      
    },
    new Parameter("err") {},
    new Parameter("family") {},
    new Parameter("fg") {

      @Override
      public SEXP query(GraphicsDevice dd) {
         return StringVector.valueOf(dd.getParameters().getForeground().toString());
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        /* par(fg=) sets BOTH "fg" and "col" */
        Color color = toColor(context, dd, exp);
        dd.getParameters().setForeground(color);
        dd.getParameters().setColor(color);
      }

      @Override
      public void specifyInline(Context context, GraphicsDevice dd, SEXP exp) {
        Color color = toColor(context, dd, exp);
        dd.getParameters().setForeground(color);
      }
    },
    new NonInlineParameter("fig") {

      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getFigureRegion().toVector();
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.setFigureRegion(Rectangle.from(exp));
      }
    },
    new NonInlineParameter("fin") {},
    new Parameter("font") {},
    new Parameter("font.axis") {},
    new Parameter("font.lab") {},
    new Parameter("font.main") {},
    new Parameter("font.sub") {},
    new Parameter("lab") {},
    new Parameter("las") {},
    new Parameter("lend") {},
    new NonInlineParameter("lheight") {},
    new Parameter("ljoin") {},
    new Parameter("lmitre") {},
    new Parameter("lty") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getParameters().getLineType().toExpression();
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().setLineType(LineType.valueOf(exp));
      }
    },
    new Parameter("lwd") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return new DoubleArrayVector(dd.getParameters().getLineWidth());
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        if(exp instanceof Vector && exp.length() >= 1) {
          dd.getParameters().setLineWidth(((Vector) exp).getElementAsDouble(0));
        }
        throw new EvalException("invalid lwd parameter: " + exp.toString());
      }
    },
    new NonInlineParameter("mai") {},
    new NonInlineParameter("mar") {

      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getInnerMargins().toVector();
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.setInnerMargins(Margins.fromExp(exp));
      }
      
    },
    new NonInlineParameter("mex") {},
    new NonInlineParameter("mfcol") {},
    new NonInlineParameter("mfg") {},
    new NonInlineParameter("mfrow") {},
    new Parameter("mgp") {},
    new Parameter("mkh") {},
    new NonInlineParameter("new") {},
    new NonInlineParameter("oma") {},
    new NonInlineParameter("omd") {},
    new NonInlineParameter("omi") {},
    new Parameter("pch") {},
    new NonInlineParameter("pin") {

      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getPlotDimensions().toVector();
      }
      
      
    },
    new NonInlineParameter("plt") {

      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getPlotRegion().toVector();
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.setPlotRegion(Rectangle.from(exp));
      }      
    },
    new NonInlineParameter("ps") {},
    new NonInlineParameter("pty") {},
    new Parameter("smo") {},
    new Parameter("srt") {},
    new Parameter("tck") {},
    new Parameter("tcl") {},
    new NonInlineParameter("usr") {

      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getUserCoordinates().toVector();
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
      
        dd.setUserCoordinates(
            Rectangle.from(exp));
      }
    },
    new Parameter("xaxp") {},
    new Parameter("xaxs") {},
    new Parameter("xaxt") {},
    new NonInlineParameter("xlog") {},
    new Parameter("xpd") {
      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getParameters().getClippingMode().toExp();
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().setClippingMode(ClippingMode.fromExp(exp));
      }
    },
    new Parameter("yaxp") {},
    new Parameter("yaxs") {

      @Override
      public SEXP query(GraphicsDevice dd) {
        return dd.getParameters().getYAxisStyle().getCalculationStyle().toExp();
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().getYAxisStyle().setCalculationStyle(
                AxisIntervalCalculationStyle.fromExp(exp));
      }
    },
    new Parameter("yaxt") {},
    new NonInlineParameter("ylog") {},
    /* Obsolete pars */
    new ObsoleteParameter("gamma") {},
    new ObsoleteParameter("type") {},
    new ObsoleteParameter("tmag") {},
    /* Non-pars that might get passed to Specify2 */
    new GraphicalArg("asp") {},
    new GraphicalArg("main") {},
    new GraphicalArg("sub") {},
    new GraphicalArg("xlab") {},
    new GraphicalArg("ylab") {},
    new GraphicalArg("xlim") {},
    new GraphicalArg("ylim") {}

};



}
