library(hamcrest)

 
 
expected <- structure(list(), .Names = character(0)) 


assertThat(base:::mapply(FUN=function (f, p) 
{
    attr(f, "package") <- p
    f
},character(0),character(0))
,  equalTo( expected ) ) 
 

