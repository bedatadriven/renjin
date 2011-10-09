package r.base.graphics;

import r.jvmi.annotations.Current;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.graphics.*;

import static r.util.CDefines.*;

public class Par {


  /**
   * Gets or sets graphical parameters.
   */
  public static SEXP par(@Current Context context, SEXP args) {
    Vector.Builder value;
    SEXP originalArgs = args;
    GraphicsDevice dd;
    int new_spec, nargs;

    dd = Devices.GEcurrentDevice(context);
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
          Specify(context, CHAR(tag), val, dd);
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




  public static void Specify(Context context, char[] what, SEXP value, GraphicsDevice dd)
  {
    findParameter(new String(what)).specify(context, dd, value);
/* If you ADD a NEW par, then do NOT forget to update the code in
 *                       ../library/base/R/par.R

 * Parameters in Specify(),
 * which can*not* be specified in high-level functions,
 * i.e., by Specify2() [below]:
 *      this list is in \details{.} of ../library/base/man/par.Rd
 *      ------------------------
 *      "ask",
 *      "family", "fig", "fin",
 *      "lheight",
 *      "mai", "mar", "mex", "mfrow", "mfcol", "mfg",
 *      "new",
 *      "oma", "omd", "omi",
 *      "pin", "plt", "ps", "pty"
 *      "usr",
 *      "xlog", "ylog"
// */
//      double x;
//      int ix = 0;
//
//      /* If we get here, Query has already checked that 'what' is valid */
//
//      if (ParCode(what) == 2) {
//          warning(_("graphical parameter \"%s\" cannot be set"), what);
//          return;
//      }
//  #include "par-common.c"
///*        ------------
// *--- now, these are *different* from  "Specify2() use" : */
//      else if (streql(what, "bg")) {
//          lengthCheck(what, value, 1, call);
//          ix = RGBpar3(value, 0, dpptr(dd)->bg);
//          /*      naIntCheck(ix, what); */
//          R_DEV__(bg) = ix;
//          R_DEV__(new) = FALSE;
//      }
//      else if (streql(what, "cex")) {
//          lengthCheck(what, value, 1, call);      x = asReal(value);
//          posRealCheck(x, what);
//          R_DEV__(cex) = 1.0; /* ! (highlevel par, i.e.  Specify2(), set x ! */
//          R_DEV__(cexbase) = x;
//      }
//
//      else if (streql(what, "fg")) {
//          /* par(fg=) sets BOTH "fg" and "col" */
//          lengthCheck(what, value, 1, call);
//          ix = RGBpar3(value, 0, dpptr(dd)->bg);
//          /*      naIntCheck(ix, what); */
//          R_DEV__(col) = R_DEV__(fg) = ix;
//      }
//
//
///*--- and these are "Specify() only" {i.e. par(nam = val)} : */
//      else if (streql(what, "ask")) {
//          lengthCheck(what, value, 1, call);      ix = asLogical(value);
//          dd->ask = (ix == 1);/* NA |-> FALSE */
//      }
//      else if (streql(what, "fig")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 4, call);
//          if (0.0 <= REAL(value)[0] && REAL(value)[0] < REAL(value)[1] &&
//              REAL(value)[1] <= 1.0 &&
//              0.0 <= REAL(value)[2] && REAL(value)[2] < REAL(value)[3] &&
//              REAL(value)[3] <= 1.0) {
//              R_DEV_2(defaultFigure) = 0;
//              R_DEV_2(fUnits) = NIC;
//              R_DEV_2(numrows) = 1;
//              R_DEV_2(numcols) = 1;
//              R_DEV_2(heights[0]) = 1;
//              R_DEV_2(widths[0]) = 1;
//              R_DEV_2(cmHeights[0]) = 0;
//              R_DEV_2(cmWidths[0]) = 0;
//              R_DEV_2(order[0]) = 1;
//              R_DEV_2(currentFigure) = 1;
//              R_DEV_2(lastFigure) = 1;
//              R_DEV__(rspct) = 0;
//
//              R_DEV_2(fig[0]) = REAL(value)[0];
//              R_DEV_2(fig[1]) = REAL(value)[1];
//              R_DEV_2(fig[2]) = REAL(value)[2];
//              R_DEV_2(fig[3]) = REAL(value)[3];
//              GReset(dd);
//          }
//          else par_error(what);
//      }
//      else if (streql(what, "family")) {
//          const char *ss;
//          value = coerceVector(value, STRSXP);
//          lengthCheck(what, value, 1, call);
//          ss = translateChar(STRING_ELT(value, 0));
//          if(strlen(ss) > 200)
//              error(_("graphical parameter 'family' has a maximum length of 200 bytes"));
//          strncpy(dpptr(dd)->family, ss, 201);
//          strncpy(gpptr(dd)->family, ss, 201);
//      }
//      else if (streql(what, "fin")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 2, call);
//          R_DEV_2(defaultFigure) = 0;
//          R_DEV_2(fUnits) = INCHES;
//          R_DEV_2(numrows) = 1;
//          R_DEV_2(numcols) = 1;
//          R_DEV_2(heights[0]) = 1;
//          R_DEV_2(widths[0]) = 1;
//          R_DEV_2(cmHeights[0]) = 0;
//          R_DEV_2(cmWidths[0]) = 0;
//          R_DEV_2(order[0]) = 1;
//          R_DEV_2(currentFigure) = 1;
//          R_DEV_2(lastFigure) = 1;
//          R_DEV__(rspct) = 0;
//          R_DEV_2(fin[0]) = REAL(value)[0];
//          R_DEV_2(fin[1]) = REAL(value)[1];
//          GReset(dd);
//      }
//      /* -- */
//      else if (streql(what, "lheight")) {
//          lengthCheck(what, value, 1, call);
//          x = asReal(value);
//          posRealCheck(x, what);
//          R_DEV__(lheight) = x;
//      }
//      else if (streql(what, "mai")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 4, call);
//          nonnegRealCheck(REAL(value)[0], what);
//          nonnegRealCheck(REAL(value)[1], what);
//          nonnegRealCheck(REAL(value)[2], what);
//          nonnegRealCheck(REAL(value)[3], what);
//          R_DEV__(mai[0]) = REAL(value)[0];
//          R_DEV__(mai[1]) = REAL(value)[1];
//          R_DEV__(mai[2]) = REAL(value)[2];
//          R_DEV__(mai[3]) = REAL(value)[3];
//          R_DEV__(mUnits) = INCHES;
//          R_DEV__(defaultPlot) = TRUE;
//          GReset(dd);
//      }
//      else if (streql(what, "mar")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 4, call);
//          nonnegRealCheck(REAL(value)[0], what);
//          nonnegRealCheck(REAL(value)[1], what);
//          nonnegRealCheck(REAL(value)[2], what);
//          nonnegRealCheck(REAL(value)[3], what);
//          R_DEV__(mar[0]) = REAL(value)[0];
//          R_DEV__(mar[1]) = REAL(value)[1];
//          R_DEV__(mar[2]) = REAL(value)[2];
//          R_DEV__(mar[3]) = REAL(value)[3];
//          R_DEV__(mUnits) = LINES;
//          R_DEV__(defaultPlot) = TRUE;
//          GReset(dd);
//      }
//      else if (streql(what, "mex")) {
//          lengthCheck(what, value, 1, call);      x = asReal(value);
//          posRealCheck(x, what);
//          R_DEV__(mex) = x;
//          GReset(dd);
//      }
//      else if (streql(what, "mfrow")) {
//          int nrow, ncol;
//          value = coerceVector(value, INTSXP);
//          lengthCheck(what, value, 2, call);
//          posIntCheck(INTEGER(value)[0], what);
//          posIntCheck(INTEGER(value)[1], what);
//          nrow = INTEGER(value)[0];
//          ncol = INTEGER(value)[1];
//          R_DEV_2(numrows) = nrow;
//          R_DEV_2(numcols) = ncol;
//          R_DEV_2(currentFigure) = nrow*ncol;
//          R_DEV_2(lastFigure) = nrow*ncol;
//          R_DEV_2(defaultFigure) = TRUE;
//          R_DEV_2(layout) = FALSE;
//          if (nrow > 2 || ncol > 2) {
//              R_DEV_2(cexbase) = 0.66;
//              R_DEV_2(mex) = 1.0;
//          }
//          else if (nrow == 2 && ncol == 2) {
//              R_DEV_2(cexbase) = 0.83;
//              R_DEV_2(mex) = 1.0;
//          }
//          else {
//              R_DEV_2(cexbase) = 1.0;
//              R_DEV_2(mex) = 1.0;
//          }
//          R_DEV__(mfind) = 0;
//          GReset(dd);
//      }
//      else if (streql(what, "mfcol")) {
//          int nrow, ncol;
//          value = coerceVector(value, INTSXP);
//          lengthCheck(what, value, 2, call);
//          posIntCheck(INTEGER(value)[0], what);
//          posIntCheck(INTEGER(value)[1], what);
//          nrow = INTEGER(value)[0];
//          ncol = INTEGER(value)[1];
//          R_DEV_2(numrows) = nrow;
//          R_DEV_2(numcols) = ncol;
//          R_DEV_2(currentFigure) = nrow*ncol;
//          R_DEV_2(lastFigure) = nrow*ncol;
//          R_DEV_2(defaultFigure) = TRUE;
//          R_DEV_2(layout) = FALSE;
//          if (nrow > 2 || ncol > 2) {
//              R_DEV_2(cexbase) = 0.66;
//              R_DEV_2(mex) = 1.0;
//          }
//          else if (nrow == 2 && ncol == 2) {
//              R_DEV_2(cexbase) = 0.83;
//              R_DEV_2(mex) = 1.0;
//          }
//          else {
//              R_DEV__(cexbase) = 1.0;
//              R_DEV__(mex) = 1.0;
//          }
//          R_DEV__(mfind) = 1;
//          GReset(dd);
//      }
//      else if (streql(what, "mfg")) {
//          int row, col, nrow, ncol, np;
//          value = coerceVector(value, INTSXP);
//          np = length(value);
//          if(np != 2 && np != 4)
//              error(_("parameter \"mfg\" has the wrong length"));
//          posIntCheck(INTEGER(value)[0], what);
//          posIntCheck(INTEGER(value)[1], what);
//          row = INTEGER(value)[0];
//          col = INTEGER(value)[1];
//          nrow = dpptr(dd)->numrows;
//          ncol = dpptr(dd)->numcols;
//          if(row <= 0 || row > nrow)
//              error(_("parameter \"i\" in \"mfg\" is out of range"));
//          if(col <= 0 || col > ncol)
//              error(_("parameter \"j\" in \"mfg\" is out of range"));
//          if(np == 4) {
//              posIntCheck(INTEGER(value)[2], what);
//              posIntCheck(INTEGER(value)[3], what);
//              if(nrow != INTEGER(value)[2])
//                  warning(_("value of nr in \"mfg\" is wrong and will be ignored"));
//              if(ncol != INTEGER(value)[3])
//                  warning(_("value of nc in \"mfg\" is wrong and will be ignored"));
//          }
//          R_DEV_2(lastFigure) = nrow*ncol;
//          /*R_DEV__(mfind) = 1;*/
//          /* currentFigure is 1-based */
//          if(gpptr(dd)->mfind)
//              dpptr(dd)->currentFigure = (col-1)*nrow + row;
//          else dpptr(dd)->currentFigure = (row-1)*ncol + col;
//          /*
//            if (dpptr(dd)->currentFigure == 0)
//            dpptr(dd)->currentFigure = dpptr(dd)->lastFigure;
//          */
//          R_DEV_2(currentFigure);
//          /* R_DEV_2(defaultFigure) = TRUE;
//             R_DEV_2(layout) = FALSE; */
//          R_DEV_2(new) = TRUE;
//          GReset(dd);
//          /* Force a device clip */
//          if (dd->dev->canClip) GForceClip(dd);
//      } /* mfg */
//
//      else if (streql(what, "new")) {
//          lengthCheck(what, value, 1, call);
//          ix = asLogical(value);
//          if(!gpptr(dd)->state) {
//              /* no need to warn with new=FALSE and no plot */
//              if(ix != 0) warning(_("calling par(new=TRUE) with no plot"));
//          } else R_DEV__(new) = (ix != 0);
//      }
//      /* -- */
//
//      else if (streql(what, "oma")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 4, call);
//          nonnegRealCheck(REAL(value)[0], what);
//          nonnegRealCheck(REAL(value)[1], what);
//          nonnegRealCheck(REAL(value)[2], what);
//          nonnegRealCheck(REAL(value)[3], what);
//          R_DEV__(oma[0]) = REAL(value)[0];
//          R_DEV__(oma[1]) = REAL(value)[1];
//          R_DEV__(oma[2]) = REAL(value)[2];
//          R_DEV__(oma[3]) = REAL(value)[3];
//          R_DEV__(oUnits) = LINES;
//          /* !!! Force eject of multiple figures !!! */
//          R_DEV__(currentFigure) = gpptr(dd)->lastFigure;
//          GReset(dd);
//      }
//      else if (streql(what, "omd")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 4, call);
//          BoundsCheck(REAL(value)[0], 0.0, 1.0, what);
//          BoundsCheck(REAL(value)[1], 0.0, 1.0, what);
//          BoundsCheck(REAL(value)[2], 0.0, 1.0, what);
//          BoundsCheck(REAL(value)[3], 0.0, 1.0, what);
//          R_DEV__(omd[0]) = REAL(value)[0];
//          R_DEV__(omd[1]) = REAL(value)[1];
//          R_DEV__(omd[2]) = REAL(value)[2];
//          R_DEV__(omd[3]) = REAL(value)[3];
//          R_DEV__(oUnits) = NDC;
//          /* Force eject of multiple figures */
//          R_DEV__(currentFigure) = gpptr(dd)->lastFigure;
//          GReset(dd);
//      }
//      else if (streql(what, "omi")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 4, call);
//          nonnegRealCheck(REAL(value)[0], what);
//          nonnegRealCheck(REAL(value)[1], what);
//          nonnegRealCheck(REAL(value)[2], what);
//          nonnegRealCheck(REAL(value)[3], what);
//          R_DEV__(omi[0]) = REAL(value)[0];
//          R_DEV__(omi[1]) = REAL(value)[1];
//          R_DEV__(omi[2]) = REAL(value)[2];
//          R_DEV__(omi[3]) = REAL(value)[3];
//          R_DEV__(oUnits) = INCHES;
//          /* Force eject of multiple figures */
//          R_DEV__(currentFigure) = gpptr(dd)->lastFigure;
//          GReset(dd);
//      }
//      /* -- */
//
//      else if (streql(what, "pin")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 2, call);
//          nonnegRealCheck(REAL(value)[0], what);
//          nonnegRealCheck(REAL(value)[1], what);
//          R_DEV__(pin[0]) = REAL(value)[0];
//          R_DEV__(pin[1]) = REAL(value)[1];
//          R_DEV__(pUnits) = INCHES;
//          R_DEV__(defaultPlot) = FALSE;
//          GReset(dd);
//      }
//      else if (streql(what, "plt")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 4, call);
//          nonnegRealCheck(REAL(value)[0], what);
//          nonnegRealCheck(REAL(value)[1], what);
//          nonnegRealCheck(REAL(value)[2], what);
//          nonnegRealCheck(REAL(value)[3], what);
//          R_DEV__(plt[0]) = REAL(value)[0];
//          R_DEV__(plt[1]) = REAL(value)[1];
//          R_DEV__(plt[2]) = REAL(value)[2];
//          R_DEV__(plt[3]) = REAL(value)[3];
//          R_DEV__(pUnits) = NFC;
//          R_DEV__(defaultPlot) = FALSE;
//          GReset(dd);
//      }
//      else if (streql(what, "ps")) {
//          lengthCheck(what, value, 1, call);      ix = asInteger(value);
//          nonnegIntCheck(ix, what);
//          R_DEV__(ps) = ix;
//      }
//      else if (streql(what, "pty")) {
//          if (!isString(value) || LENGTH(value) < 1)
//              par_error(what);
//          ix = CHAR(STRING_ELT(value, 0))[0];
//          if (ix == 'm' || ix == 's') {
//              R_DEV__(pty) = ix;
//              R_DEV__(defaultPlot) = TRUE;
//          }
//          else par_error(what);
//      }
//      /* -- */
//      else if (streql(what, "usr")) {
//          value = coerceVector(value, REALSXP);
//          lengthCheck(what, value, 4, call);
//          naRealCheck(REAL(value)[0], what);
//          naRealCheck(REAL(value)[1], what);
//          naRealCheck(REAL(value)[2], what);
//          naRealCheck(REAL(value)[3], what);
//          if (REAL(value)[0] == REAL(value)[1] ||
//              REAL(value)[2] == REAL(value)[3])
//              par_error(what);
//          if (gpptr(dd)->xlog) {
//              R_DEV_2(logusr[0]) = REAL(value)[0];
//              R_DEV_2(logusr[1]) = REAL(value)[1];
//              R_DEV_2(usr[0]) = pow(10., REAL(value)[0]);
//              R_DEV_2(usr[1]) = pow(10., REAL(value)[1]);
//          }
//          else {
//              R_DEV_2(usr[0]) = REAL(value)[0];
//              R_DEV_2(usr[1]) = REAL(value)[1];
//              R_DEV_2(logusr[0]) = R_Log10(REAL(value)[0]);
//              R_DEV_2(logusr[1]) = R_Log10(REAL(value)[1]);
//          }
//          if (gpptr(dd)->ylog) {
//              R_DEV_2(logusr[2]) = REAL(value)[2];
//              R_DEV_2(logusr[3]) = REAL(value)[3];
//              R_DEV_2(usr[2]) = pow(10., REAL(value)[2]);
//              R_DEV_2(usr[3]) = pow(10., REAL(value)[3]);
//          }
//          else {
//              R_DEV_2(usr[2]) = REAL(value)[2];
//              R_DEV_2(usr[3]) = REAL(value)[3];
//              R_DEV_2(logusr[2]) = R_Log10(REAL(value)[2]);
//              R_DEV_2(logusr[3]) = R_Log10(REAL(value)[3]);
//          }
//          /* Reset Mapping and Axis Parameters */
//          GMapWin2Fig(dd);
//          GSetupAxis(1, dd);
//          GSetupAxis(2, dd);
//      }/* usr */
//
//      else if (streql(what, "xlog")) {
//          lengthCheck(what, value, 1, call);      ix = asLogical(value);
//          if (ix == NA_LOGICAL)
//              par_error(what);
//          R_DEV__(xlog) = (ix != 0);
//      }
//      else if (streql(what, "ylog")) {
//          lengthCheck(what, value, 1, call);      ix = asLogical(value);
//          if (ix == NA_LOGICAL)
//              par_error(what);
//          R_DEV__(ylog) = (ix != 0);
//      }
//      /* We do not need these as Query will already have warned.
//      else if (streql(what, "type")) {
//          warning(_("graphical parameter \"%s\" is obsolete"), what);
//      }
//      else warning(_("unknown graphical parameter \"%s\""), what);
//      */

      return;
  } /* Specify */


/* Specify2 -- parameters as arguments from higher-level graphics functions
 * --------
 * Many things in PARALLEL to Specify(.)
 * for par()s not valid here, see comment there.
 */

  static void Specify2(Context context, char[] what, SEXP value, GraphicsDevice dd)
  {
    findParameter(new String(what)).specifyInline(context, dd, null);
//      double x;
//      int ix = 0, ptype = ParCode(what);
//
//      if (ptype == 1 || ptype == -3) {
//          /* 1: these are valid, but not settable inline
//             3: arguments, not pars
//          */
//          return;
//      }
//      if (ptype == -2) {
//          warning(_("graphical parameter \"%s\" is obsolete"), what);
//          return;
//      }
//      if (ptype < 0) {
//          warning(_("\"%s\" is not a graphical parameter"), what);
//          return;
//      }
//      if (ptype == 2) {
//          warning(_("graphical parameter \"%s\" cannot be set"), what);
//          return;
//      }
//
//  #include "par-common.c"
///*        ------------
// *  these are *different* from Specify() , i.e., par(<NAM> = .) use : */
//      else if (streql(what, "bg")) {
//          /* bg can be a vector of length > 1, so pick off first value
//             (as e.g. pch always did) */
//          if (!isVector(value) || LENGTH(value) < 1)
//              par_error(what);
//          R_DEV__(bg) = RGBpar3(value, 0, dpptr(dd)->bg);
//      }
//      else if (streql(what, "cex")) {
//          /* cex can be a vector of length > 1, so pick off first value
//             (as e.g. pch always did) */
//          x = asReal(value);
//          posRealCheck(x, what);
//          R_DEV__(cex) = x;
//          /* not setting cexbase here (but in Specify()) */
//      }
//      else if (streql(what, "family")) {
//          const char *ss;
//          value = coerceVector(value, STRSXP);
//          lengthCheck(what, value, 1, call);
//          ss = translateChar(STRING_ELT(value, 0));
//          if(strlen(ss) > 200)
//              error(_("graphical parameter 'family' has a maximum length of 200 bytes"));
//          strncpy(gpptr(dd)->family, ss, 201);
//      }
//      else if (streql(what, "fg")) {
//          /* highlevel arg `fg = ' does *not* set `col' (as par(fg=.) does!*/
//          lengthCheck(what, value, 1, call);
//          ix = RGBpar3(value, 0, dpptr(dd)->bg);
//          /*      naIntCheck(ix, what); */
//          R_DEV__(fg) = ix;
//      }
  } /* Specify2 */



/* Do NOT forget to update  ../library/base/R/par.R */
/* if you  ADD a NEW  par !! */

  public static SEXP Query(char[] what, GraphicsDevice dd)
  {
    SEXP value;
    String name = new String(what);

    return findParameter(new String(what)).query(dd);


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
      return Color.fromExp(context.getGlobals().getColorPalette(),
                           dd.getParameters().getBackground(),
                            (Vector)exp, 0);
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

  static final Parameter
    ParTable  [] = new Parameter[] {
    new Parameter("adj") {},
    new Parameter("ann") {},
    new NonInlineParameter("ask") {

    },
    new Parameter("bg") {},
    new Parameter("bty") {},
    new Parameter("cex") {},
    new Parameter("cex.axis") {},
    new Parameter("cex.lab") {},
    new Parameter("cex.main") {},
    new Parameter("cex.sub") {},
    new ReadOnlyParameter("cin") {},
    new Parameter("col") {},
    new Parameter("col.axis") {},
    new Parameter("col.lab") {},
    new Parameter("col.main") {},
    new Parameter("col.sub") {},
    new ReadOnlyParameter("cra") {},
    new Parameter("crt") {},
    new ReadOnlyParameter("csi") {},
    new Parameter("csy") {},
    new ReadOnlyParameter("cxy") {},
    new ReadOnlyParameter("din") {},
    new Parameter("err") {},
    new Parameter("family") {},
    new Parameter("fg") {

      @Override
      public SEXP query(GraphicsDevice dd) {
         return new StringVector(dd.getParameters().getForeground().toString());
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
    new NonInlineParameter("fig") {},
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
        return new DoubleVector(dd.getParameters().getLineWidth());
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
    new NonInlineParameter("mar") {},
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
    new NonInlineParameter("pin") {},
    new NonInlineParameter("plt") {},
    new NonInlineParameter("ps") {},
    new NonInlineParameter("pty") {},
    new Parameter("smo") {},
    new Parameter("srt") {},
    new Parameter("tck") {},
    new Parameter("tcl") {},
    new NonInlineParameter("usr") {},
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
        return dd.getParameters().getyAxisIntervalCalculationStyle().toExp();
      }

      @Override
      public void specify(Context context, GraphicsDevice dd, SEXP exp) {
        dd.getParameters().setyAxisIntervalCalculationStyle(
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
