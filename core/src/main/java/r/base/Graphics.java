package r.base;

import static r.util.CDefines.*;
import r.jvmi.annotations.Current;
import r.lang.CHARSEXP;
import r.lang.Context;
import r.lang.GraphicsDevice;
import r.lang.ListVector;
import r.lang.Null;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Vector;
import r.lang.Vector.Builder;

public class Graphics {
  
  
  public static SEXP par(@Current Context context, SEXP args) {
    Vector.Builder value;
    SEXP originalArgs = args;
    GraphicsDevice dd;
    int new_spec, nargs;

    dd = GEcurrentDevice(context);
    new_spec = 0;
    nargs = length(args);
    if (isNewList(args)) {
        SEXP oldnames, tag, val;
        Vector.Builder newnames;
        int i;
        PROTECT(newnames = allocVector(STRSXP, nargs));
        PROTECT(value = allocVector(VECSXP, nargs));
        oldnames = getAttrib(args, R_NamesSymbol);
        for (i = 0 ; i < nargs ; i++) {
            if (oldnames != R_NilValue)
                tag = STRING_ELT(oldnames, i);
            else
                tag = R_NilValue;
            val = VECTOR_ELT(args, i);
            /* tags are all ASCII */
            if (tag != R_NilValue && CHAR(tag)[0] != 0) {
                new_spec = 1;
                SET_VECTOR_ELT(value, i, Query(CHAR(tag), dd));
                SET_STRING_ELT(newnames, i, tag);
                throw new UnsupportedOperationException("nyi");
                //    Specify(CHAR(tag), val, dd, call);
            }
            else if (isString(val) && length(val) > 0) {
                tag = STRING_ELT(val, 0);
                if (tag != R_NilValue && CHAR(tag)[0] != 0) {
                    SET_VECTOR_ELT(value, i, Query(CHAR(tag), dd));
                    SET_STRING_ELT(newnames, i, tag);
                }
            }
            else {
                SET_VECTOR_ELT(value, i, R_NilValue);
                SET_STRING_ELT(newnames, i, R_BlankString);
            }
        }
        setAttrib(value, R_NamesSymbol, newnames);
        UNPROTECT(2);
    }
    else {
        error(_("invalid argument passed to par()"));
        return R_NilValue/* -Wall */;
    }
    /* should really only do this if specifying new pars ?  yes! [MM] */
//    if (new_spec && GRecording(call, dd))
//        GErecordGraphicOperation(op, originalArgs, dd);
    return value.build();
  }


  
  public static GraphicsDevice GEcurrentDevice(@Current Context context) {
    /* If there are no active devices
     * check the options for a "default device".
     * If there is one, start it up. */
    if (!context.getGlobals().hasGraphicsDevice()) {
        SEXP defdev = context.getGlobals().getOption("device");
        if (isString(defdev) && length(defdev) > 0) {
            SEXP devName = install(CHAR(STRING_ELT(defdev, 0)));
            /*  Not clear where this should be evaluated, since
                grDevices need not be in the search path.
                So we look for it first on the global search path.
            */
            defdev = findVar(devName, context.getGlobalEnvironment());
            if(defdev != R_UnboundValue) {
                PROTECT(defdev = lang1(devName));
                eval(defdev, context, context.getGlobalEnvironment());
                UNPROTECT(1);
            } else {
                /* Not globally visible:
                   try grDevices namespace if loaded.
                   The option is unlikely to be set if it is not loaded,
                   as the default setting is in grDevices:::.onLoad.
                */
                SEXP ns = findVarInFrame(context.getGlobals().namespaceRegistry,
                                         install("grDevices"));
                if(ns != R_UnboundValue &&
                   findVar(devName, ns) != R_UnboundValue) {
                    PROTECT(defdev = lang1(devName));
                    eval(defdev, context, ns);
                    UNPROTECT(1);
                } else
                    error(_("no active or default device"));
            }
        } else if(TYPEOF(defdev) == CLOSXP) {
            PROTECT(defdev = lang1(defdev));
            eval(defdev, context, context.getGlobalEnvironment());
            UNPROTECT(1);
        } else
            error(_("no active or default device"));
    }
    return context.getGlobals().getCurrentDevice();
  }
  



/* Do NOT forget to update  ../library/base/R/par.R */
/* if you  ADD a NEW  par !! */

public static SEXP Query(char[] what, GraphicsDevice dd)
{
    SEXP value;
    String name = new String(what);

//    if (streql(what, "adj")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->adj;
//    }
//    else if (streql(what, "ann")) {
//        value = allocVector(LGLSXP, 1);
//        LOGICAL(value)[0] = (dpptr(dd)->ann != 0);
//    }
//    else if (streql(what, "ask")) {
//        value = allocVector(LGLSXP, 1);
//        LOGICAL(value)[0] = dd->ask;
//    }
//    else if (streql(what, "bg")) {
//        value = mkString(col2name(dpptr(dd)->bg));
//    }
//    else if (streql(what, "bty")) {
//        char buf[2];
//        buf[0] = dpptr(dd)->bty;
//        buf[1] = '\0';
//        value = mkString(buf);
//    }
//    else if (streql(what, "cex")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->cexbase;
//    }
//    else if (streql(what, "cex.main")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->cexmain;
//    }
//    else if (streql(what, "cex.lab")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->cexlab;
//    }
//    else if (streql(what, "cex.sub")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->cexsub;
//    }
//    else if (streql(what, "cex.axis")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->cexaxis;
//    }
//    else if (streql(what, "cin")) {
//        value = allocVector(REALSXP, 2);
//        REAL(value)[0] = dpptr(dd)->scale * dd->dev->cra[0] * dd->dev->ipr[0];
//        REAL(value)[1] = dpptr(dd)->scale * dd->dev->cra[1] * dd->dev->ipr[1];
//    }
//    else if (streql(what, "col")) {
//        value = mkString(col2name(dpptr(dd)->col));
//    }
//    else if (streql(what, "col.main")) {
//        value = mkString(col2name(dpptr(dd)->colmain));
//    }
//    else if (streql(what, "col.lab")) {
//        value = mkString(col2name(dpptr(dd)->collab));
//    }
//    else if (streql(what, "col.sub")) {
//        value = mkString(col2name(dpptr(dd)->colsub));
//    }
//    else if (streql(what, "col.axis")) {
//        value = mkString(col2name(dpptr(dd)->colaxis));
//    }
//    else if (streql(what, "cra")) {
//        value = allocVector(REALSXP, 2);
//        REAL(value)[0] = dpptr(dd)->scale * dd->dev->cra[0];
//        REAL(value)[1] = dpptr(dd)->scale * dd->dev->cra[1];
//    }
//    else if (streql(what, "crt")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->crt;
//    }
//    else if (streql(what, "csi")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = GConvertYUnits(1.0, CHARS, INCHES, dd);
//    }
//    else if (streql(what, "cxy")) {
//        value = allocVector(REALSXP, 2);
//        /* == par("cin") / par("pin") : */
//        REAL(value)[0] = dpptr(dd)->scale * dd->dev->cra[0]
//            * dd->dev->ipr[0] / dpptr(dd)->pin[0]
//            * (dpptr(dd)->usr[1] - dpptr(dd)->usr[0]);
//        REAL(value)[1] = dpptr(dd)->scale * dd->dev->cra[1]
//            * dd->dev->ipr[1] / dpptr(dd)->pin[1]
//            * (dpptr(dd)->usr[3] - dpptr(dd)->usr[2]);
//    }
//    else if (streql(what, "din")) {
//        value = allocVector(REALSXP, 2);
//        REAL(value)[0] = GConvertXUnits(1.0, NDC, INCHES, dd);
//        REAL(value)[1] = GConvertYUnits(1.0, NDC, INCHES, dd);
//    }
//    else if (streql(what, "err")) {
//        value = allocVector(INTSXP, 1);
//        INTEGER(value)[0] = dpptr(dd)->err;
//    }
//    else if (streql(what, "family")) {
//        value = mkString(dpptr(dd)->family);
//    }
//    else if (streql(what, "fg")) {
//        value = mkString(col2name(dpptr(dd)->fg));
//    }
//    else if (streql(what, "fig")) {
//        value = allocVector(REALSXP, 4);
//        REAL(value)[0] = dpptr(dd)->fig[0];
//        REAL(value)[1] = dpptr(dd)->fig[1];
//        REAL(value)[2] = dpptr(dd)->fig[2];
//        REAL(value)[3] = dpptr(dd)->fig[3];
//    }
//    else if (streql(what, "fin")) {
//        value = allocVector(REALSXP, 2);
//        REAL(value)[0] = dpptr(dd)->fin[0];
//        REAL(value)[1] = dpptr(dd)->fin[1];
//    }
//    else if (streql(what, "font")) {
//        value = allocVector(INTSXP, 1);
//        INTEGER(value)[0] = dpptr(dd)->font;
//    }
//    else if (streql(what, "font.main")) {
//        value = allocVector(INTSXP, 1);
//        INTEGER(value)[0] = dpptr(dd)->fontmain;
//    }
//    else if (streql(what, "font.lab")) {
//        value = allocVector(INTSXP, 1);
//        INTEGER(value)[0] = dpptr(dd)->fontlab;
//    }
//    else if (streql(what, "font.sub")) {
//        value = allocVector(INTSXP, 1);
//        INTEGER(value)[0] = dpptr(dd)->fontsub;
//    }
//    else if (streql(what, "font.axis")) {
//        value = allocVector(INTSXP, 1);
//        INTEGER(value)[0] = dpptr(dd)->fontaxis;
//    }
//    else if (streql(what, "lab")) {
//        value = allocVector(INTSXP, 3);
//        INTEGER(value)[0] = dpptr(dd)->lab[0];
//        INTEGER(value)[1] = dpptr(dd)->lab[1];
//        INTEGER(value)[2] = dpptr(dd)->lab[2];
//    }
//    else if (streql(what, "las")) {
//        value = allocVector(INTSXP, 1);
//        INTEGER(value)[0] = dpptr(dd)->las;
//    }
//    else if (streql(what, "lend")) {
//        value = GE_LENDget(dpptr(dd)->lend);
//    }
//    else if (streql(what, "lheight")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->lheight;
//    }
//    else if (streql(what, "ljoin")) {
//        value = GE_LJOINget(dpptr(dd)->ljoin);
//    }
//    else if (streql(what, "lmitre")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->lmitre;
//    }
//    else if (streql(what, "lty")) {
//        value = GE_LTYget(dpptr(dd)->lty);
//    }
//    else if (streql(what, "lwd")) {
//        value =  allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->lwd;
//    }
//    else if (streql(what, "mai")) {
//        value = allocVector(REALSXP, 4);
//        REAL(value)[0] = dpptr(dd)->mai[0];
//        REAL(value)[1] = dpptr(dd)->mai[1];
//        REAL(value)[2] = dpptr(dd)->mai[2];
//        REAL(value)[3] = dpptr(dd)->mai[3];
//    }
//    else if (streql(what, "mar")) {
//        value = allocVector(REALSXP, 4);
//        REAL(value)[0] = dpptr(dd)->mar[0];
//        REAL(value)[1] = dpptr(dd)->mar[1];
//        REAL(value)[2] = dpptr(dd)->mar[2];
//        REAL(value)[3] = dpptr(dd)->mar[3];
//    }
//    else if (streql(what, "mex")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->mex;
//    }
//    /* NOTE that if a complex layout has been specified */
//    /* then this simple information may not be very useful. */
//    else if (streql(what, "mfrow") || streql(what, "mfcol")) {
//        value = allocVector(INTSXP, 2);
//        INTEGER(value)[0] = dpptr(dd)->numrows;
//        INTEGER(value)[1] = dpptr(dd)->numcols;
//    }
//    else if (streql(what, "mfg")) {
//        int row, col;
//        value = allocVector(INTSXP, 4);
//        currentFigureLocation(&row, &col, dd);
//        INTEGER(value)[0] = row+1;
//        INTEGER(value)[1] = col+1;
//        INTEGER(value)[2] = dpptr(dd)->numrows;
//        INTEGER(value)[3] = dpptr(dd)->numcols;
//    }
//    else if (streql(what, "mgp")) {
//        value = allocVector(REALSXP, 3);
//        REAL(value)[0] = dpptr(dd)->mgp[0];
//        REAL(value)[1] = dpptr(dd)->mgp[1];
//        REAL(value)[2] = dpptr(dd)->mgp[2];
//    }
//    else if (streql(what, "mkh")) {
//        /* Unused in R, but settable */
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->mkh;
//    }
//    else if (streql(what, "new")) {
//        value = allocVector(LGLSXP, 1);
//        LOGICAL(value)[0] = dpptr(dd)->new;
//    }
//    else if (streql(what, "oma")) {
//        value = allocVector(REALSXP, 4);
//        REAL(value)[0] = dpptr(dd)->oma[0];
//        REAL(value)[1] = dpptr(dd)->oma[1];
//        REAL(value)[2] = dpptr(dd)->oma[2];
//        REAL(value)[3] = dpptr(dd)->oma[3];
//    }
//    else if (streql(what, "omd")) {
//        value = allocVector(REALSXP, 4);
//        REAL(value)[0] = dpptr(dd)->omd[0];
//        REAL(value)[1] = dpptr(dd)->omd[1];
//        REAL(value)[2] = dpptr(dd)->omd[2];
//        REAL(value)[3] = dpptr(dd)->omd[3];
//    }
//    else if (streql(what, "omi")) {
//        value = allocVector(REALSXP, 4);
//        REAL(value)[0] = dpptr(dd)->omi[0];
//        REAL(value)[1] = dpptr(dd)->omi[1];
//        REAL(value)[2] = dpptr(dd)->omi[2];
//        REAL(value)[3] = dpptr(dd)->omi[3];
//    }
//    else if (streql(what, "pch")) {
//        int val = dpptr(dd)->pch;
//        /* we need to be careful that par("pch") is converted back
//           to the same value */
//        if (known_to_be_latin1 && val <= -32 && val >= -255) val = -val;
//        if(val >= ' ' && val <= (mbcslocale ? 127 : 255)) {
//            char buf[2];
//            buf[0] = val;
//            buf[1] = '\0';
//            value = mkString(buf);
//        } else {
//            /* Could return as UTF-8 string */
//            value = ScalarInteger(val);
//        }
//    }
//    else if (streql(what, "pin")) {
//        value = allocVector(REALSXP, 2);
//        REAL(value)[0] = dpptr(dd)->pin[0];
//        REAL(value)[1] = dpptr(dd)->pin[1];
//    }
//    else if (streql(what, "plt")) {
//        value = allocVector(REALSXP, 4);
//        REAL(value)[0] = dpptr(dd)->plt[0];
//        REAL(value)[1] = dpptr(dd)->plt[1];
//        REAL(value)[2] = dpptr(dd)->plt[2];
//        REAL(value)[3] = dpptr(dd)->plt[3];
//    }
//    else if (streql(what, "ps")) {
//        value = allocVector(INTSXP, 1);
//        /* was reporting unscaled prior to 2.7.0 */
//        INTEGER(value)[0] = dpptr(dd)->ps * dpptr(dd)->scale;
//    }
//    else if (streql(what, "pty")) {
//        char buf[2];
//        buf[0] = dpptr(dd)->pty;
//        buf[1] = '\0';
//        value = mkString(buf);
//    }
//    else if (streql(what, "smo")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->smo;
//    }
//    else if (streql(what, "srt")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->srt;
//    }
//    else if (streql(what, "tck")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->tck;
//    }
//    else if (streql(what, "tcl")) {
//        value = allocVector(REALSXP, 1);
//        REAL(value)[0] = dpptr(dd)->tcl;
//    }
//    else if (streql(what, "usr")) {
//        value = allocVector(REALSXP, 4);
//        if (gpptr(dd)->xlog) {
//            REAL(value)[0] = gpptr(dd)->logusr[0];
//            REAL(value)[1] = gpptr(dd)->logusr[1];
//        }
//        else {
//            REAL(value)[0] = dpptr(dd)->usr[0];
//            REAL(value)[1] = dpptr(dd)->usr[1];
//        }
//        if (gpptr(dd)->ylog) {
//            REAL(value)[2] = gpptr(dd)->logusr[2];
//            REAL(value)[3] = gpptr(dd)->logusr[3];
//        }
//        else {
//            REAL(value)[2] = dpptr(dd)->usr[2];
//            REAL(value)[3] = dpptr(dd)->usr[3];
//        }
//    }
//    else if (streql(what, "xaxp")) {
//        value = allocVector(REALSXP, 3);
//        REAL(value)[0] = dpptr(dd)->xaxp[0];
//        REAL(value)[1] = dpptr(dd)->xaxp[1];
//        REAL(value)[2] = dpptr(dd)->xaxp[2];
//    }
//    else if (streql(what, "xaxs")) {
//        char buf[2];
//        buf[0] = dpptr(dd)->xaxs;
//        buf[1] = '\0';
//        value = mkString(buf);
//    }
//    else if (streql(what, "xaxt")) {
//        char buf[2];
//        buf[0] = dpptr(dd)->xaxt;
//        buf[1] = '\0';
//        value = mkString(buf);
//    }
//    else if (streql(what, "xlog")) {
//        value = allocVector(LGLSXP, 1);
//        LOGICAL(value)[0] = dpptr(dd)->xlog;
//    }
//    else if (streql(what, "xpd")) {
//        value = allocVector(LGLSXP, 1);
//        if (dpptr(dd)->xpd == 2)
//            LOGICAL(value)[0] = NA_LOGICAL;
//        else
//            LOGICAL(value)[0] = dpptr(dd)->xpd;
//    }
//    else if (streql(what, "yaxp")) {
//        value = allocVector(REALSXP, 3);
//        REAL(value)[0] = dpptr(dd)->yaxp[0];
//        REAL(value)[1] = dpptr(dd)->yaxp[1];
//        REAL(value)[2] = dpptr(dd)->yaxp[2];
//    }
//    else if (streql(what, "yaxs")) {
//        char buf[2];
//        buf[0] = dpptr(dd)->yaxs;
//        buf[1] = '\0';
//        value = mkString(buf);
//    }
//    else if (streql(what, "yaxt")) {
//        char buf[2];
//        buf[0] = dpptr(dd)->yaxt;
//        buf[1] = '\0';
//        value = mkString(buf);
//    }
//    else if (streql(what, "ylog")) {
//        value = allocVector(LGLSXP, 1);
//        LOGICAL(value)[0] = dpptr(dd)->ylog;
//    }
//    if(new String(what).equals("in")) {
//      return dd.getIn();
//    } else {
//      return Null.INSTANCE;
//    }
    return Null.INSTANCE;
  }
}
