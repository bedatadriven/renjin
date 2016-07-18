library(hamcrest)

 
 
expected <- structure(list("gl(1, length(types))" = 1L), .Names = "gl(1, length(types))") 


assertThat(base:::mapply(FUN="[",SIMPLIFY=FALSE,structure(list("gl(1, length(types))" = 1L), .Names = "gl(1, length(types))"),list(1L))
,  equalTo( expected ) ) 
 

