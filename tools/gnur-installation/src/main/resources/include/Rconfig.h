/* Rconfig.h.  Generated automatically */
#ifndef R_RCONFIG_H
#define R_RCONFIG_H

#ifndef R_CONFIG_H

#define HAVE_F77_UNDERSCORE 1
/* all R platforms have this */
#define IEEE_754 1
/* #undef WORDS_BIGENDIAN */
#define R_INLINE inline
#define HAVE_VISIBILITY_ATTRIBUTE 1
/* all R platforms have the next two */
#define SUPPORT_UTF8 1
#define SUPPORT_MBCS 1
#define ENABLE_NLS 1
/* #undef HAVE_AQUA */
#define SUPPORT_OPENMP 1
#define SIZEOF_SIZE_T 8
#define HAVE_ALLOCA_H 1

// We are not supporting localization in Renjin at this time.
#define NO_NLS

#endif /* not R_CONFIG_H */

#endif /* not R_RCONFIG_H */
