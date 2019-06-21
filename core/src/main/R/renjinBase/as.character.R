
# This needs to be evaluated BEFORE the normal

# Replace as.character() builtin with regular R function
# GNU R implements as.character() with very weird S3 dispatch
# that is better implemented here. That keeps the compiler simpler and requires
# hard-coding fewer exceptions


as.character.default <- function(x = NULL)
    as.vector(x, mode = "character")


as.character <- function(x, ...)
    UseMethod("as.character")
