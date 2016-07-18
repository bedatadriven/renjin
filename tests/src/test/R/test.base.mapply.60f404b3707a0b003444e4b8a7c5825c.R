library(hamcrest)

 
 
expected <- structure(list(bibentry = c("bibentry(bibtype = \"Manual\",", 
"         key = \"boot-package\",", "         title = \"boot: Bootstrap R (S-PLUS) Functions\",", 
"         author = c(person(given = \"Angelo\",", "                           family = \"Canty\",", 
"                           role = \"aut\",", "                           comment = \"S original\"),", 
"                    person(given = c(\"Brian\", \"D.\"),", "                           family = \"Ripley\",", 
"                           role = c(\"aut\", \"trl\", \"cre\"),", 
"                           email = \"ripley@stats.ox.ac.uk\",", 
"                           comment = \"R port, author of parallel support\")),", 
"         year = \"2012\",", "         note = \"R package version 1.3-4\",", 
"         url = \"https://CRAN.R-project.org/package=boot\")"
), "NA" = c("bibentry(bibtype = \"Book\",", "         key = \"boot-book\",", 
"         title = \"Bootstrap Methods and Their Applications\",", 
"         author = c(person(given = c(\"Anthony\", \"C.\"),", 
"                           family = \"Davison\",", "                           role = \"aut\"),", 
"                    person(given = c(\"David\", \"V.\"),", "                           family = \"Hinkley\",", 
"                           role = \"aut\")),", "         year = \"1997\",", 
"         publisher = \"Cambridge University Press\",", "         address = \"Cambridge\",", 
"         isbn = \"0-521-57391-2\",", "         url = \"http://statwww.epfl.ch/davison/BMA/\")"
)), .Names = c("bibentry", NA)) 


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
},"bibentry",list(structure(list(bibtype = "bibtype = \"Manual\"", key = "key = \"boot-package\"", 
    title = "title = \"boot: Bootstrap R (S-PLUS) Functions\"", 
    author = c("author = c(person(given = \"Angelo\",", "                  family = \"Canty\",", 
    "                  role = \"aut\",", "                  comment = \"S original\"),", 
    "           person(given = c(\"Brian\", \"D.\"),", "                  family = \"Ripley\",", 
    "                  role = c(\"aut\", \"trl\", \"cre\"),", 
    "                  email = \"ripley@stats.ox.ac.uk\",", "                  comment = \"R port, author of parallel support\"))"
    ), year = "year = \"2012\"", note = "note = \"R package version 1.3-4\"", 
    url = "url = \"https://CRAN.R-project.org/package=boot\""), .Names = c("bibtype", 
"key", "title", "author", "year", "note", "url")), structure(list(
    bibtype = "bibtype = \"Book\"", key = "key = \"boot-book\"", 
    title = "title = \"Bootstrap Methods and Their Applications\"", 
    author = c("author = c(person(given = c(\"Anthony\", \"C.\"),", 
    "                  family = \"Davison\",", "                  role = \"aut\"),", 
    "           person(given = c(\"David\", \"V.\"),", "                  family = \"Hinkley\",", 
    "                  role = \"aut\"))"), year = "year = \"1997\"", 
    publisher = "publisher = \"Cambridge University Press\"", 
    address = "address = \"Cambridge\"", isbn = "isbn = \"0-521-57391-2\"", 
    url = "url = \"http://statwww.epfl.ch/davison/BMA/\""), .Names = c("bibtype", 
"key", "title", "author", "year", "publisher", "address", "isbn", 
"url"))))
,  equalTo( expected ) ) 
 

