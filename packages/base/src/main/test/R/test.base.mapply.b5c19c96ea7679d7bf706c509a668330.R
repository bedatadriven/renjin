library(hamcrest)

 
 
expected <- structure(c("bibtype = \"Manual\"", "key = \"boot-package\""), .Names = c("bibtype", 
"key")) 


assertThat(base:::mapply(FUN=function (u, v) 
{
    prefix <- sprintf("%s = ", u)
    n <- length(v)
    if (n > 1L) 
        prefix <- c(prefix, rep.int(strrep(" ", nchar(prefix)), 
            n - 1L))
    sprintf("%s%s", prefix, v)
},c("bibtype", "key"),structure(c("\"Manual\"", "\"boot-package\""), .Names = c("bibtype", 
"key")))
,  equalTo( expected ) ) 
 

