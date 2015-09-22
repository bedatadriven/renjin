
setClass("seq",
    slots = c(
        name = "character",
        sequence = "character"
        type = "character"
        )
)

setGeneric(name = "setType",
    def = function(object){
        standardGeneric("setType")
    }
)
setMethod(f = "setType",
    definition = function(object,type){
        object@type <- type
        return(object)
    }
)

setGeneric(name = "getType",
    def = function(object){
        standardGeneric("getType")
    }
)
setMethod(f = "getType",
    definition = function(object,type){
        return(object@type)
    }
)


setGeneric(name = "setName",
    def = function(object){
        standardGeneric("setName")
    }
)
setMethod(f = "setName",
    definition = function(object,name){
        object@name <- name
        return(object)
    }
)

setGeneric(name = "getName",
    def = function(object){
        standardGeneric("getName")
    }
)
setMethod(f = "getName",
    definition = function(object,name){
        return(object@name)
    }
)


setGeneric(name = "setSequence",
    def = function(object){
        standardGeneric("setSequence")
    }
)
setMethod(f = "setSequence",
    definition = function(object,sequence){
        object@sequence <- sequence
        return(object)
    }
)

setGeneric(name = "getSequence",
    def = function(object){
        standardGeneric("getSequence")
    }
)
setMethod(f = "getSequence",
    definition = function(object,sequence){
        return(object@sequence)
    }
)


setMethod(f = "seqType",
    signature = "seq",
    definition = function(seq){
        DNA = c("A","T","C","G")
        RNA = c("A","U","C","G")
        PROTEIN = c("A","R","N","D","C",
                    "E","Q","G","H","I",
                    "L","K","M","F","P",
                    "S","T","W","Y","V")

        notDNA = is.element("FALSE",is.element(strsplit(getSequence(seq),split="")[[1]],DNA))
        notRNA = is.element("FALSE",is.element(strsplit(getSequence(seq),split="")[[1]],RNA))
        notPROTEIN = is.element("FALSE",is.element(strsplit(getSequence(seq),split="")[[1]],PROTEIN))

        if(notDNA == TRUE & notRNA == TRUE & notPROTEIN == FALSE){
            return(setType(seq,"protein"))
        } else if(notDNA == TRUE & notRNA == FALSE & notPROTEIN == TRUE){
            return(setType(seq,"RNA"))
        } else if(notDNA == FALSE & notRNA == TRUE & notPROTEIN == TRUE){
            return(setType(seq,"DNA"))
        } else if(notDNA == FALSE & notRNA == FALSE & notPROTEIN == TRUE){
            cat("Could not distinquish between DNA or RNA, no U/T present in sequence. Type set as DNA")
            return(setType(seq,"DNA"))
        } else {
            cat("The type of input sequence could not be determined and is defined as unknown")
            return(setType(seq,"unknown"))
        }
    }
)