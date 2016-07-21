library(hamcrest)

 
 
expected <- "Anthony C. Davison [aut], David V. Hinkley [aut]" 


assertThat(base:::mapply(FUN=function (u, so, ml) 
{
    if ((n <- length(so)) == 1L) {
        if (is.na(so)) 
            return(NA_character_)
        else if (so == -1L) 
            return(u)
    }
    beg <- if (n > 1L) {
        eo <- so + ml - 1L
        if (any(eo[-n] >= so[-1L])) 
            stop(gettextf("need non-overlapping matches for %s", 
                sQuote("invert = TRUE")), domain = NA)
        c(1L, eo + 1L)
    }
    else {
        c(1L, so + ml)
    }
    end <- c(so - 1L, nchar(u))
    substring(u, beg, end)
},USE.NAMES=FALSE,"Anthony C. Davison [aut], David V. Hinkley [aut]",list(structure(-1L, match.length = -1L, useBytes = TRUE)),list(-1L))
,  equalTo( expected ) ) 
 

