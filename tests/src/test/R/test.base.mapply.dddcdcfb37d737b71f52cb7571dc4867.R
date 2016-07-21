library(hamcrest)

 
 
expected <- structure("bibtype = \"Manual\"", .Names = "bibtype") 


assertThat(base:::mapply(FUN=function (u, v) 
{
    prefix <- sprintf("%s = ", u)
    n <- length(v)
    if (n > 1L) 
        prefix <- c(prefix, rep.int(strrep(" ", nchar(prefix)), 
            n - 1L))
    sprintf("%s%s", prefix, v)
},"bibtype",structure("\"Manual\"", .Names = "bibtype"))
,  equalTo( expected ) ) 
 

