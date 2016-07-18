library(hamcrest)

 
 
expected <- structure(c("bibentry(bibtype = \"Manual\",", "         title = \"R: A Language and Environment for Statistical Computing\",", 
"         author = person(given = \"R Core Team\"),", "         organization = \"R Foundation for Statistical Computing\",", 
"         address = \"Vienna, Austria\",", "         year = \"2014\",", 
"         url = \"https://www.R-project.org/\")"), .Dim = c(7L, 
1L), .Dimnames = list(NULL, "bibentry")) 


assertThat(base:::mapply(FUN=function (cname, cargs) 
{
    cargs <- as.list(cargs)
    n <- length(cargs)
    lens <- lengths(cargs)
    sums <- cumsum(lens)
    starters <- c(sprintf("%s(", cname), rep.int(strrep(" ", 
        nchar(cname) + 1L), sums[n] - 1L))
    trailers <- c(rep.int("", sums[n] - 1L), ")")
    trailers[sums[-n]] <- ","
    sprintf("%s%s%s", starters, unlist(cargs), trailers)
},"bibentry",list(structure(list(bibtype = "bibtype = \"Manual\"", title = "title = \"R: A Language and Environment for Statistical Computing\"", 
    author = "author = person(given = \"R Core Team\")", organization = "organization = \"R Foundation for Statistical Computing\"", 
    address = "address = \"Vienna, Austria\"", year = "year = \"2014\"", 
    url = "url = \"https://www.R-project.org/\""), .Names = c("bibtype", 
"title", "author", "organization", "address", "year", "url"))))
,  equalTo( expected ) ) 
 

