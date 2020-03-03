
# Primitives that have been moved to pure R functions need to be
# removed from the tables defined by GNU R

# The methods package for one expects them to be primitives, and will fail
# if they are closures

local({

    defrocked <- c("anyNA", "as.character", "seq.int")

    rm(list = defrocked[defrocked %in% ls(.GenericArgsEnv)], envir = .GenericArgsEnv)
    rm(list = defrocked[defrocked %in% ls(.ArgsEnv)], envir = .ArgsEnv)

    .S3PrimitiveGenerics <<- .S3PrimitiveGenerics[ !( .S3PrimitiveGenerics %in% defrocked ) ]

})