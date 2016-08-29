

new.circle <- function(radius) structure(radius, class = "circle")
new.square <- function(width) structure(width, class = "square")

as.character.circle <- function(radius) sprintf("circle of radius %d", radius)
as.character.square <- function(width) sprintf("%dx%d square", width, width)


area <- function(animal) UseMethod("area")
area.circle <- function(radius) radius*radius*pi
area.square <- function(width) width*width

`%/%.circle` <- function(e1, e2) {
    # Long method to test serialization to external file
    if(e1 == 1) {
        return(e1+e2)
    }
    if(e1 == 2) {
        return(42)
    }
    if(e1 == 3) {
        return(43)
    }
    if(e1 == 4) {
        return(44)
    }
    if(e1 == 3) {
        return(43)
    }
    if(e1 == 4) {
        return(44)
    }
    if(e1 == 3) {
        return(43)
    }
    if(e1 == 4) {
        return(44)
    }
}