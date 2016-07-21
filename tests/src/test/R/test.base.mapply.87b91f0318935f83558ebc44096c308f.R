library(hamcrest)

 
 
expected <- structure(list(title = "title = \"Bootstrap Methods and Their Applications\"", 
    author = c("author = c(person(given = c(\"Anthony\", \"C.\"),", 
    "                  family = \"Davison\",", "                  role = \"aut\"),", 
    "           person(given = c(\"David\", \"V.\"),", "                  family = \"Hinkley\",", 
    "                  role = \"aut\"))"), year = "year = \"1997\"", 
    publisher = "publisher = \"Cambridge University Press\"", 
    address = "address = \"Cambridge\"", isbn = "isbn = \"0-521-57391-2\"", 
    url = "url = \"http://statwww.epfl.ch/davison/BMA/\""), .Names = c("title", 
"author", "year", "publisher", "address", "isbn", "url")) 


assertThat(base:::mapply(FUN=function (u, v) 
{
    prefix <- sprintf("%s = ", u)
    n <- length(v)
    if (n > 1L) 
        prefix <- c(prefix, rep.int(strrep(" ", nchar(prefix)), 
            n - 1L))
    sprintf("%s%s", prefix, v)
},c("title", "author", "year", "publisher", "address", "isbn", 
"url"),structure(list(title = "\"Bootstrap Methods and Their Applications\"", 
    author = c("c(person(given = c(\"Anthony\", \"C.\"),", "         family = \"Davison\",", 
    "         role = \"aut\"),", "  person(given = c(\"David\", \"V.\"),", 
    "         family = \"Hinkley\",", "         role = \"aut\"))"
    ), year = "\"1997\"", publisher = "\"Cambridge University Press\"", 
    address = "\"Cambridge\"", isbn = "\"0-521-57391-2\"", url = "\"http://statwww.epfl.ch/davison/BMA/\""), .Names = c("title", 
"author", "year", "publisher", "address", "isbn", "url")))
,  equalTo( expected ) ) 
 

