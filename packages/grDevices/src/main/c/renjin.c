
#include <wchar.h>
#include <stdlib.h>
#include <string.h>

#define _(String) (String)

#define size_t int

void Rf_error(char * message);

/* These return the result in wchar_t, but does assume
   wchar_t is UCS-2/4 and so are for internal use only */
size_t
Rf_utf8toucs(wchar_t *wc, const char *s)
{
    unsigned int byte;
    wchar_t local, *w;
    byte = *((unsigned char *)s);
    w = wc ? wc: &local;

    if (byte == 0) {
	*w = (wchar_t) 0;
	return 0;
    } else if (byte < 0xC0) {
	*w = (wchar_t) byte;
	return 1;
    } else if (byte < 0xE0) {
	if(strlen(s) < 2) return (size_t)-2;
	if ((s[1] & 0xC0) == 0x80) {
	    *w = (wchar_t) (((byte & 0x1F) << 6) | (s[1] & 0x3F));
	    return 2;
	} else return (size_t)-1;
    } else if (byte < 0xF0) {
	if(strlen(s) < 3) return (size_t)-2;
	if (((s[1] & 0xC0) == 0x80) && ((s[2] & 0xC0) == 0x80)) {
	    *w = (wchar_t) (((byte & 0x0F) << 12)
			    | (unsigned int) ((s[1] & 0x3F) << 6)
			    | (s[2] & 0x3F));
	    byte = (unsigned int) *w;
	    /* Surrogates range */
	    if(byte >= 0xD800 && byte <= 0xDFFF) return (size_t)-1;
	    if(byte == 0xFFFE || byte == 0xFFFF) return (size_t)-1;
	    return 3;
	} else return (size_t)-1;
    }
    if(sizeof(wchar_t) < 4) return (size_t)-2;
    /* So now handle 4,5.6 byte sequences with no testing */
    if (byte < 0xf8) {
	if(strlen(s) < 4) return (size_t)-2;
	*w = (wchar_t) (((byte & 0x0F) << 18)
			| (unsigned int) ((s[1] & 0x3F) << 12)
			| (unsigned int) ((s[2] & 0x3F) << 6)
			| (s[3] & 0x3F));
	return 4;
    } else if (byte < 0xFC) {
	if(strlen(s) < 5) return (size_t)-2;
	*w = (wchar_t) (((byte & 0x0F) << 24)
			| (unsigned int) ((s[1] & 0x3F) << 12)
			| (unsigned int) ((s[2] & 0x3F) << 12)
			| (unsigned int) ((s[3] & 0x3F) << 6)
			| (s[4] & 0x3F));
	return 5;
    } else {
	if(strlen(s) < 6) return (size_t)-2;
	*w = (wchar_t) (((byte & 0x0F) << 30)
			| (unsigned int) ((s[1] & 0x3F) << 24)
			| (unsigned int) ((s[2] & 0x3F) << 18)
			| (unsigned int) ((s[3] & 0x3F) << 12)
			| (unsigned int) ((s[4] & 0x3F) << 6)
			| (s[5] & 0x3F));
	return 6;
    }
}


/* used in plot.c for non-UTF-8 MBCS */
size_t
Rf_mbtoucs(unsigned int *wc, const char *s, size_t n)
{
    Rf_error("mbtoucs not supported.");
}

double R_GE_VStrHeight (const char *s, int enc, void * gc, void *dd) {
    Rf_error("R_GE_VStrHeight is not supported.");
}

double R_GE_VStrWidth(const char *s, int enc, void * gc, void * dd) {
    Rf_error("R_GE_VStrWidth is not supported.");
}

size_t Rf_ucstoutf8(char *s, const unsigned int c) {
    Rf_error("Rf_ucstoutf8 is not supported.");
}

void R_GE_VText(double x, double y, const char * const s, int enc,
		double x_justify, double y_justify, double rotation,
		void * gc,  void * dd) {

    Rf_error("R_GE_VText is not supported.");
}
