library(hamcrest)

 
 
expected <- "Anthony C. Davison zzzzz, David V. Hinkley zzzzz" 


assertThat(base:::mapply(FUN=function (u, v) 
{
    nu <- length(u)
    nv <- length(v)
    if (nv != (nu - 1L)) {
        if (!nv) 
            stop("must have replacements for matches")
        v <- rep_len(v, nu - 1L)
    }
    paste0(u, c(v, ""), collapse = "")
},USE.NAMES=FALSE,list("Anthony C. Davison zzzzz, David V. Hinkley zzzzz"),list(character(0)))
,  equalTo( expected ) ) 
 

