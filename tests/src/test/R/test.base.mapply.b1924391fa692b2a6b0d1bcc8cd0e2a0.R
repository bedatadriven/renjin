library(hamcrest)

 
 
expected <- list(character(0)) 


assertThat(base:::mapply(FUN=function (n, c = "z") 
{
    vapply(Map(rep.int, rep.int(c, length(n)), n, USE.NAMES = FALSE), 
        paste, "", collapse = "")
},list(integer(0)))
,  equalTo( expected ) ) 
 

