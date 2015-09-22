setClass("seq",
    slots = c(
        name = "character",
        sequence = "character",
        type = "character"
        ),
    prototype = c(
        name = "SequenceName",
        sequence = "A",
        type = ""
    )
)

setGeneric(name = "setType",
    def = function(object,type){
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
    def = function(object,type){
        standardGeneric("getType")
    }
)
setMethod(f = "getType",
    definition = function(object,type){
        return(object@type)
    }
)


setGeneric(name = "setName",
    def = function(object,name){
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
    def = function(object,name){
        standardGeneric("getName")
    }
)
setMethod(f = "getName",
    definition = function(object,name){
        return(object@name)
    }
)


setGeneric(name = "setSequence",
    def = function(object,sequence){
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
    def = function(object,sequence){
        standardGeneric("getSequence")
    }
)
setMethod(f = "getSequence",
    definition = function(object,sequence){
        return(object@sequence)
    }
)

setGeneric(name = "seqType",
    def = function(seq){
        standardGeneric("seqType")
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

        if(notDNA == FALSE & notRNA == TRUE & notPROTEIN == FALSE){
            #cat("notDNA: ",notDNA,"\n")
            #cat("notRNA: ",notRNA,"\n")
            #cat("notPROTEIN: ",notPROTEIN,"\n")
            #cat("Set as: DNA\n")
            return(setType(seq,"DNA"))
        } else if(notDNA == TRUE & notRNA == FALSE & notPROTEIN == TRUE){
            #cat("notDNA: ",notDNA,"\n")
            #cat("notRNA: ",notRNA,"\n")
            #cat("notPROTEIN: ",notPROTEIN,"\n")
            #cat("Set as: RNA\n")
            return(setType(seq,"RNA"))
        } else if(notDNA == TRUE & notRNA == TRUE & notPROTEIN == FALSE){
            #cat("notDNA: ",notDNA,"\n")
            #cat("notRNA: ",notRNA,"\n")
            #cat("notPROTEIN: ",notPROTEIN,"\n")
            #cat("Set as: protein\n")
            return(setType(seq,"protein"))
        } else if(notDNA == FALSE & notRNA == FALSE & notPROTEIN == TRUE){
            #cat("Could not distinquish between DNA or RNA, no U/T present in sequence. Type set as DNA\n")
            #cat("notDNA: ",notDNA,"\n")
            #cat("notRNA: ",notRNA,"\n")
            #cat("notPROTEIN: ",notPROTEIN,"\n")
            #cat("Set as: DNA\n")
            return(setType(seq,"DNA"))
        } else {
            #cat("The type of input sequence could not be determined and is defined as unknown\n")
            #cat("notDNA: ",notDNA,"\n")
            #cat("notRNA: ",notRNA,"\n")
            #cat("notPROTEIN: ",notPROTEIN,"\n")
            #cat("Set as: unknown\n")
            return(setType(seq,"unknown"))
        }
    }
)

mle_ext = setClass("mle_ext",
    slots = c(
        input_file = "character"
    ),
    prototype = c(
        input_file = "INPUT_OBJECT"
    ),
    contains = "mle"
)
