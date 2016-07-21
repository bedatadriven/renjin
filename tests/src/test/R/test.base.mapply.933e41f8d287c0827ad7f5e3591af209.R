library(hamcrest)

 
 
expected <- structure(c("zzzzz", "zzzzz"), .Dim = c(2L, 1L)) 


assertThat(base:::mapply(FUN=function (n, c = "z") 
{
    vapply(Map(rep.int, rep.int(c, length(n)), n, USE.NAMES = FALSE), 
        paste, "", collapse = "")
},list(c(5L, 5L)))
,  equalTo( expected ) ) 
 

