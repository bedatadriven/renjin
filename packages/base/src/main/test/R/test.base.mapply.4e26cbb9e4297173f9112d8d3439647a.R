library(hamcrest)

 
 
expected <- structure(c("z", "z", "z", "z", "z", "z", "z", "z", "z", "z"), .Dim = c(5L, 
2L)) 


assertThat(base:::mapply(FUN=function (x, times) 
.Internal(rep.int(x, times)),USE.NAMES=FALSE,c("z", "z"),c(5L, 5L))
,  equalTo( expected ) ) 
 

