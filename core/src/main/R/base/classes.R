
# Implementation of class(x) <- y
# Moved from C/Java to R


oldClass <- function(x) attr(x, 'class')

`oldClass<-` <- function(x, value) {
    attr(x, 'class') <- value
    x
}


`class<-` <- function(x, value) {


    if(is.null(value) ||
        (is.character(value) && length(value) == 0)) {

        attr(x, 'class') <- NULL
        return(x)
    }

    # Ensure class attribute is a character vector
    # (Use mode<- to avoid invoking S3 as.character()) methods
    classes <- value
    mode(classes) <- "character"

    if(length(classes) == 0) {
        stop("attempt to set invalid 'class' attribute")
    }

    # Note that we "unbless" S4 objects as they cannot
    # have multiple classes.
    if(length(classes) > 1) {
        x <- .Internal(setS4Object(x, FALSE, FALSE))
        attr(x, 'class') <- classes
        return(x)
    }


    # The basic class names are subject to special
    # validation and have the effect of coercing x and clearing any explicit class attribute
    className <- classes

    if(is.na(className)) {
        attr(x, 'class') <- NA_character_
        x
    } else if(className == "matrix") {
        if(length(dim(x)) != 2) {
            stop("invalid to set the class to matrix unless the dimension attribute is of length 2 (was %d)", dim(x));
        }
        unclass(x)

    } else if(className == "array") {
        if(length(dim(x)) == 0) {
            stop("cannot set class to \"array\" unless the dimension attribute has length > 0")
        }
        unclass(x)

    } else if(className == "numeric") {
        # Do not promote integers to double
        if(is.integer(x)) {
            unclass(x)
        } else {
            mode(x) <- "numeric"
            unclass(x)
        }

    } else if(className %in% c("raw", "logical", "integer", "double", "complex", "character", "list")) {
        mode(x) <- className
        unclass(x)

    } else if(className == "list") {
        mode(x) <- "list"
        unclass(x)

    } else if(className %in% c("environment", "name")) {
        if(typeof(x) != "environment") {
            stop('"', className, '" can only be set as the class if the object has this type; found "', typeof(x), "'")
        }
        unclass(x)

    } else {
        attr(x, 'class') <- className
        x
    }
}

comment <- function(x) {
    attr(x, 'comment')
}

`comment<-` <- function(x, value) {
    attr(x, 'comment') <- value
}
