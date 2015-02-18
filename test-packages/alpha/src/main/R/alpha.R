

parseAlpha <- function(json) {
    om <- ObjectMapper$new()
    om$readTree(json)
}


alphaVersion <- function() {
    om <- ObjectMapper$new()
    om$version()$toString()
}

alphaName <- function() {
    Alpha$execute()
}