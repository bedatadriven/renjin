

new.circle <- function(radius) structure(radius, class = "circle")
new.square <- function(width) structure(width, class = "square")

as.character.circle <- function(radius) sprintf("circle of radius %d", radius)
as.character.square <- function(width) sprintf("%dx%d square", width, width)


area <- function(animal) UseMethod("area")
area.circle <- function(radius) radius*radius*pi
area.square <- function(width) width*width


