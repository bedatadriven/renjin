#include "javaGD.h"
#include "jGDtalk.h"
#include <Rdefines.h>

/* the internal representation of a color in this API is RGBa with a=0 meaning transparent and a=255 meaning opaque
 (hence a means 'opacity'). previous implementation was different (inverse meaning and 0x80 as NA), so watch out.
  */
#if R_VERSION < 0x20000
#define CONVERT_COLOR(C) ((((C)==0x80000000) || ((C)==-1))?0:(((C)&0xFFFFFF)|((0xFF000000-((C)&0xFF000000)))))
#else
#define CONVERT_COLOR(C) (C)
#endif


// The following are declarations for functions written in Java and
// implemented in org.renjin.grDevices.Devices.

// GCC-Bridge links them using the metadata in
// grDevices/src/main/resources/META-INF/org.renjin.gcc.symbols

void *  GraphicsDevices_newDevice();
void    GraphicsDevices_open(void *p, int width, int height);
void    GraphicsDevices_close(void *p);
void    GraphicsDevices_activate(void *p);
void    GraphicsDevices_circle(void *p, double x, double y, double r);
void    GraphicsDevices_clip(void *p, double x0, double x1, double y0, double y1);
void    GraphicsDevices_deactivate(void *p);
void    GraphicsDevices_hold(void *p);
void    GraphicsDevices_flush(void *p, int hold);
double* GraphicsDevices_locator(void *p);
void    GraphicsDevices_line(void *p, double x1, double y1, double x2, double y2);
double* GraphicsDevices_metricInfo(void *p, int c);
void    GraphicsDevices_mode(void *p, int mode);
void    GraphicsDevices_newPage(void *p, int devNumber);
void    GraphicsDevices_path(void *p, int npoly, int * nper, double *x, double *y, Rboolean winding);
void    GraphicsDevices_polygon(void *p, int n, double *x, double *y);
void    GraphicsDevices_polyline(void *p, int n, double *x, double *y);
void    GraphicsDevices_rect(void *p, double x0, double y0, double x1, double y1);
double* GraphicsDevices_size(void *p);
double  GraphicsDevices_strWidth(void *p, const char *str);
void    GraphicsDevices_text(void *p, double x, double y, const char *str, double rot, double hadj);
void    GraphicsDevices_raster(void *p, unsigned int *raster, int w, int h, double x, double y, double width, double height, double rot, double interpolate);

void    GraphicsDevices_setColor(void *p, int col);
void    GraphicsDevices_setFill(void *p, int col);
void    GraphicsDevices_setLine(void *p, double a, int b);
void    GraphicsDevices_setFont(void *p, double cex, double ps, double lineheight, int fontface, const char *fontfamily);



// The rest of these functions are in C and adapted from Simon's
// JNI glue code.

int initJavaGD(newJavaGDDesc* xd) {

    xd->talk = GraphicsDevices_newDevice();
    return 0;
}

/** last graphics context. the API send changes, not the entire context, so we cache it for comparison here */
static R_GE_gcontext lastGC;

#define checkGC(xd,gc) sendGC(xd,gc,0)

/** check changes in GC and issue corresponding commands if necessary */
static void sendGC(newJavaGDDesc *xd, R_GE_gcontext *gc, int sendAll) {

    if (sendAll || gc->col != lastGC.col) {
        GraphicsDevices_setColor(xd->talk, CONVERT_COLOR(gc->col));
    }

    if (sendAll || gc->fill != lastGC.fill)  {
        GraphicsDevices_setFill(xd->talk, CONVERT_COLOR(gc->fill));
    }

    if (sendAll || gc->lwd != lastGC.lwd || gc->lty != lastGC.lty) {
        GraphicsDevices_setLine(xd->talk, gc->lwd, gc->lty);
    }

    if (sendAll || gc->cex!=lastGC.cex || gc->ps!=lastGC.ps || gc->lineheight!=lastGC.lineheight || gc->fontface!=lastGC.fontface || strcmp(gc->fontfamily, lastGC.fontfamily)) {
        GraphicsDevices_setFont(xd->talk, gc->cex, gc->ps, gc->lineheight, gc->fontface, gc->fontfamily);
    }
    memcpy(&lastGC, gc, sizeof(lastGC));
}

/* re-set the GC - i.e. send commands for all monitored GC entries */
static void sendAllGC(newJavaGDDesc *xd, R_GE_gcontext *gc) {
    /*
    printf("Basic GC:\n col=%08x\n fill=%08x\n gamma=%f\n lwd=%f\n lty=%08x\n cex=%f\n ps=%f\n lineheight=%f\n fontface=%d\n fantfamily=\"%s\"\n\n",
	 gc->col, gc->fill, gc->gamma, gc->lwd, gc->lty,
	 gc->cex, gc->ps, gc->lineheight, gc->fontface, gc->fontfamily);
     */
    sendGC(xd, gc, 1);
}



/*------- the R callbacks begin here ... ------------------------*/

Rboolean newJavaGD_Open(NewDevDesc *dd, newJavaGDDesc *xd, const char *dsp, double w, double h)
{
    if (initJavaGD(xd)) return FALSE;

    xd->fill = 0xffffffff; /* transparent, was R_RGB(255, 255, 255); */
    xd->col = R_RGB(0, 0, 0);
    xd->canvas = R_RGB(255, 255, 255);
    xd->windowWidth = w;
    xd->windowHeight = h;
    xd->holdlevel = 0;

    GraphicsDevices_open(xd->talk, w, h);

    return TRUE;
}

static void newJavaGD_Activate(NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    GraphicsDevices_activate(xd->talk);
}


static void newJavaGD_Close(NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    GraphicsDevices_close(xd->talk);
}


static void newJavaGD_Circle(double x, double y, double r,  R_GE_gcontext *gc,  NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    GraphicsDevices_circle(xd->talk, x, y, r);
}


static void newJavaGD_Clip(double x0, double x1, double y0, double y1,  NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    GraphicsDevices_clip(xd->talk, x0, x1, y0, y1);
}

static void newJavaGD_Deactivate(NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    GraphicsDevices_deactivate(xd->talk);
}

static void newJavaGD_Hold(NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    GraphicsDevices_hold(xd->talk);
}


static int  newJavaGD_HoldFlush(NewDevDesc *dd, int level)
{
    int ol;
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    if (!xd) {
        return 0;
    }

    ol = xd->holdlevel;
    xd->holdlevel += level;

    if (xd->holdlevel < 0) {
	    xd->holdlevel = 0;
    }

    if(!xd->talk) {
	    return xd->holdlevel;
    }
	if (xd->holdlevel == 0) {
	    /* flush */
	    GraphicsDevices_flush(xd->talk, 1);
    } else if (ol == 0) {
        /* first hold */
	    GraphicsDevices_flush(xd->talk, 0);
    }

    return xd->holdlevel;
}


static Rboolean newJavaGD_Locator(double *x, double *y, NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    if(!xd || !xd->talk) {
        return FALSE;
    }

    double *coords = GraphicsDevices_locator(xd->talk);
    *x = coords[0];
    *y = coords[1];

    return TRUE;
}



static void newJavaGD_Line(double x1, double y1, double x2, double y2,  R_GE_gcontext *gc,  NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    if(!xd || !xd->talk) {
        return;
    }

    checkGC(xd, gc);

    GraphicsDevices_line(xd->talk, x1, y1, x2, y2);
}


static void newJavaGD_MetricInfo(int c,  R_GE_gcontext *gc,  double* ascent, double* descent,  double* width, NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    if(!xd || !xd->talk) {
        return;
    }

    checkGC(xd, gc);

    if(c <0) {
        c = -c;
    }
    double *ac = GraphicsDevices_metricInfo(xd->talk, c);
    *ascent =  ac[0];
    *descent = ac[1];
    *width =   ac[2];
}


static void newJavaGD_Mode(int mode, NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    if(!xd || !xd->talk) {
        return;
    }

    GraphicsDevices_mode(xd->talk, mode);
}


static void newJavaGD_NewPage(R_GE_gcontext *gc, NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    int devNr;

    if(!xd || !xd->talk) {
        return;
    }

    devNr = ndevNumber(dd);

    GraphicsDevices_newPage(xd->talk, devNr);

    /* this is an exception - we send all GC attributes just after the NewPage command */
    sendAllGC(xd, gc);
}


static void newJavaGD_Path(double *x, double *y, int npoly, int *nper, Rboolean winding,
        R_GE_gcontext *gc, NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    int n;

    if (!xd || !xd->talk) {
        return;
    }

    checkGC(xd, gc);

    GraphicsDevices_path(xd->talk, npoly, nper, x, y, winding);
}


static void newJavaGD_Polygon(int n, double *x, double *y,  R_GE_gcontext *gc,  NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    if(!xd || !xd->talk) return;

    checkGC(xd, gc);

    GraphicsDevices_polygon(xd->talk, n, x, y);
}


static void newJavaGD_Polyline(int n, double *x, double *y,  R_GE_gcontext *gc,  NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    if(!xd || !xd->talk) {
        return;
    }

    checkGC(xd, gc);

    GraphicsDevices_polyline(xd->talk, n, x, y);
}



static void newJavaGD_Rect(double x0, double y0, double x1, double y1,  R_GE_gcontext *gc,  NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    if(!xd || !xd->talk) return;

    checkGC(xd, gc);

    GraphicsDevices_rect(xd->talk, x0, y0, x1, y1);
}


static void newJavaGD_Size(double *left, double *right,  double *bottom, double *top,  NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    if(!xd || !xd->talk) {
        return;
    }

    double *ac = GraphicsDevices_size(xd->talk);
    *left = ac[0];
    *right = ac[1];
    *bottom = ac[2];
    *top = ac[3];
}

static const char *convertToUTF8(const char *str, R_GE_gcontext *gc)
{
    if (gc->fontface == 5) /* symbol font needs re-coding to UTF-8 */
	str = symbol2utf8(str);
#ifdef translateCharUTF8
    else { /* first check whether we are dealing with non-ASCII at all */
	int ascii = 1;
	const unsigned char *c = (const unsigned char*) str;
	while (*c) { if (*c > 127) { ascii = 0; break; } c++; }
	if (!ascii) /* non-ASCII, we need to convert it to UTF8 */
	    str = translateCharUTF8(mkCharCE(str, CE_NATIVE));
    }
#endif
    return str;
}

static double newJavaGD_StrWidthUTF8(const char *str,  R_GE_gcontext *gc,  NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;
    double res = 0.0;

    if(!xd || !xd->talk) {
        return 0.0;
    }

    checkGC(xd, gc);

    return GraphicsDevices_strWidth(xd->talk, str);
}

static double newJavaGD_StrWidth(const char *str,  R_GE_gcontext *gc,  NewDevDesc *dd)
{
    return newJavaGD_strWidthUTF8(convertToUTF8(str, gc), gc, dd);
}


static void newJavaGD_TextUTF8(double x, double y, const char *str,  double rot, double hadj,  R_GE_gcontext *gc,  NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    if(!xd || !xd->talk) {
        return;
    }

    checkGC(xd, gc);

    GraphicsDevices_text(xd->talk, x, y, str, rot, hadj);
}

static void newJavaGD_Text(double x, double y, const char *str,  double rot, double hadj,  R_GE_gcontext *gc,  NewDevDesc *dd)
{
    newJavaGD_TextUTF8(x, y, convertToUTF8(str, gc), rot, hadj, gc, dd);
}

static void newJavaGD_Raster(unsigned int *raster, int w, int h,
			   double x, double y, double width, double height,
			   double rot, Rboolean interpolate,
			   R_GE_gcontext *gc, NewDevDesc *dd)
{
    newJavaGDDesc *xd = (newJavaGDDesc *) dd->deviceSpecific;

    if(!xd || !xd->talk) {
        return;
    }

    checkGC(xd, gc);

    GraphicsDevices_raster(xd->talk, raster, w, h, x, y, width, height, rot, interpolate);
}


/** fill the R device structure with callback functions */
void setupJavaGDfunctions(NewDevDesc *dd) {
    dd->close = newJavaGD_Close;
    dd->activate = newJavaGD_Activate;
    dd->deactivate = newJavaGD_Deactivate;
    dd->size = newJavaGD_Size;
    dd->newPage = newJavaGD_NewPage;
    dd->clip = newJavaGD_Clip;
    dd->strWidth = newJavaGD_StrWidth;
    dd->text = newJavaGD_Text;
    dd->rect = newJavaGD_Rect;
    dd->circle = newJavaGD_Circle;
    dd->line = newJavaGD_Line;
    dd->polyline = newJavaGD_Polyline;
    dd->polygon = newJavaGD_Polygon;
    dd->locator = newJavaGD_Locator;
    dd->mode = newJavaGD_Mode;
    dd->metricInfo = newJavaGD_MetricInfo;
#if R_GE_version >= 4
    dd->hasTextUTF8 = TRUE;
    dd->strWidthUTF8 = newJavaGD_StrWidthUTF8;
    dd->textUTF8 = newJavaGD_TextUTF8;
#if R_GE_version >= 6
    dd->raster = newJavaGD_Raster;
#if R_GE_version >= 8
    dd->path = newJavaGD_Path;
#if R_GE_version >= 9
    dd->holdflush = newJavaGD_HoldFlush;
#endif
#endif
#endif
#else
    dd->hold = newJavaGD_Hold;
#endif
}