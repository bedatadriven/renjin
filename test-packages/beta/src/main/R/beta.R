

parseBeta <- function(json) {
    om <- ObjectMapper$new()
    om$readTree(json)
}


betaVersion <- function() {
    om <- ObjectMapper$new()
    om$version()$toString()
}


betaName <- function() {
    Beta$execute()
}

