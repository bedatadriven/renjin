library(hamcrest)

 
 
expected <- structure(c("title = \"R: A Language and Environment for Statistical Computing\"", 
"author = person(given = \"R Core Team\")", "organization = \"R Foundation for Statistical Computing\"", 
"address = \"Vienna, Austria\"", "year = \"2014\"", "url = \"https://www.R-project.org/\""
), .Names = c("title", "author", "organization", "address", "year", 
"url")) 


assertThat(base:::mapply(FUN=function (u, v) 
{
    prefix <- sprintf("%s = ", u)
    n <- length(v)
    if (n > 1L) 
        prefix <- c(prefix, rep.int(strrep(" ", nchar(prefix)), 
            n - 1L))
    sprintf("%s%s", prefix, v)
},c("title", "author", "organization", "address", "year", "url"
),structure(c("\"R: A Language and Environment for Statistical Computing\"", 
"person(given = \"R Core Team\")", "\"R Foundation for Statistical Computing\"", 
"\"Vienna, Austria\"", "\"2014\"", "\"https://www.R-project.org/\""
), .Names = c("title", "author", "organization", "address", "year", 
"url")))
,  equalTo( expected ) ) 
 

