library(hamcrest)

 
 
expected <- list() 


assertThat(base:::mapply(FUN=function (x, times) 
.Internal(rep.int(x, times)),USE.NAMES=FALSE,character(0),integer(0))
,  equalTo( expected ) ) 
 

