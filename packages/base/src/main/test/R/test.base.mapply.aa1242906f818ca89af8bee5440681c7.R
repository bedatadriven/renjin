library(hamcrest)

 
 
expected <- list(c("lazy", "la", "zy"), character(0), character(0)) 


assertThat(base:::mapply(FUN=function (u, so, ml) 
{
    if (length(so) == 1L) {
        if (is.na(so) || (so == -1L)) 
            return(character())
    }
    substring(u, so, so + ml - 1L)
},USE.NAMES=FALSE,c("1 lazy", "1", "1 LAZY"),list(structure(c(3L, 3L, 5L), match.length = c(4L, 2L, 2L)), 
    structure(-1L, match.length = -1L), structure(-1L, match.length = -1L)),list(c(4L, 2L, 2L), -1L, -1L))
,  equalTo( expected ) ) 
 

