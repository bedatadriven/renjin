
#ifndef R_RS_H
#define R_RS_H


#ifdef HAVE_F77_UNDERSCORE
# define F77_CALL(x)    x ## _
#else
# define F77_CALL(x)    x
#endif
#define F77_NAME(x)    F77_CALL(x)
#define F77_SUB(x)     F77_CALL(x)
#define F77_COM(x)     F77_CALL(x)
#define F77_COMDECL(x) F77_CALL(x)


#endif