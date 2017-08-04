# 2015 Parham Solaimani, BeDataDriven.com
# S4 class tests
library(hamcrest)
library(methods)


test.S4.class1 = function() {

    # create S4 class "molecule"
    molecule <- setClass("Molecule",
	    slots = c(
		    name = "character",
		    content = "character",
		    size = "numeric"
		    ),
	    prototype = list(
		    name = "Molecule_Name",
		    content = "Molecule_Formula",
		    size = 0.0
		    ),
	    validity = function(object)
	    {
		    if((object@name == "") | (object@content == "") | (object@size == "")) {
			    return("INVALID OBJECT: Name, content, or the size of your object is empty.")
		    }
		    return(TRUE)
	    }
    )

    a = new("Molecule", name = "Water", content = "H2O", size = 100)
    b = molecule(name = "Salt", content = "NaCl", size = 10.1)

    assertTrue(
        isS4(a)
        )

    assertTrue(
        isS4(b)
        )

    assertTrue(
        is(a@size, class(b@size))
        )

    slot(a,"name") <- "test"

    assertThat(
        a@name,
        identicalTo("test")
        )

    slot(a,"name") <- "Water"

	assertTrue(
		is(tryCatch(slot(a,"name") <- 100, error = function(e)e), "error")
		)

	assertFalse(
		is(tryCatch(slot(a,"name") <- "", error = function(e)e), "error")
		)
}

test.S4.class.methods.1 = function() {
    # create S4 class "molecule"
    molecule <- setClass("Molecule",
	    slots = c(
		    name = "character",
		    content = "character",
		    size = "numeric"
		    ),
	    prototype = list(
		    name = "Molecule_Name",
		    content = "Molecule_Formula",
		    size = 0.0
		    ),
	    validity = function(object)
	    {
		    if((object@name == "") | (object@content == "") | (object@size == "")) {
			    return("INVALID OBJECT: Name, content, or the size of your object is empty.")
		    }
		    return(TRUE)
	    }
    )

    setGeneric(name="setName",
        def=function(object,name){
            standardGeneric("setName")
        }
    )

    # Generate Get and Set Methods for Name, Content, and Size slots
    setMethod(f="setName",
        signature="Molecule",
        definition=function(object,name){
            object@name <- name
            validObject(object)
            return(object)
        }
    )

    setGeneric(name="setContent",
        def=function(object,content){
            standardGeneric("setContent")
        }
    )

    setMethod(f="setContent",
        signature="Molecule",
        definition=function(object,content){
            object@content <- content
            validObject(object)
            return(object)
        }
    )

    setGeneric(name="setSize",
        def=function(object,size){
            standardGeneric("setSize")
        }
    )

    setMethod(f="setSize",
        signature="Molecule",
        definition=function(object,size){
            object@size <- size
            validObject(object)
            return(object)
        }
    )

    setGeneric(name="getName",
        def=function(object){
            standardGeneric("getName")
        }
    )

    setMethod(f="getName",
        signature="Molecule",
        definition=function(object){
            return(object@name)
        }
    )

    setGeneric(name="getContent",
        def=function(object){
            standardGeneric("getContent")
        }
    )

    setMethod(f="getContent",
        signature="Molecule",
        definition=function(object){
            return(object@content)
        }
    )

    setGeneric(name="getSize",
        def=function(object){
            standardGeneric("getSize")
        }
    )

    setMethod(f="getSize",
        signature="Molecule",
        definition=function(object){
            return(object@size)
        }
    )

    a = new("Molecule", name = "Water", content = "H2O", size = 100)
    b = molecule(name = "Salt", content = "NaCl", size = 10.1)

    #Test methods
    assertTrue(
        is(getContent(a), class(getContent(b)))
        )

    assertTrue(
        is(getSize(a), class(getSize(b)))
        )

    assertThat(
        a@name,
        identicalTo(getName(a))
        )

    assertThat(
        a@size,
        identicalTo(getSize(a))
        )

    a = setSize(a, 200)
    a = setContent(a, "O2")
    a = setName(a, "Oxygen")

    assertThat(
        getSize(a),
        identicalTo(200)
        )

    assertThat(
        getName(a),
        identicalTo("Oxygen")
        )

    # Test signature specific methods
    setGeneric(name="resetMolecule",
        def=function(object,value){
            standardGeneric("resetMolecule")
        }
    )

    setMethod(f="resetMolecule",
        signature=c("Molecule","character"),
        definition=function(object,value){
            object <- setName(object,value)
            object <- setContent(object,value)
            return(object)
        }
    )

    setMethod(f="resetMolecule",
        signature=c("Molecule","numeric"),
        definition=function(object,value){
            object <- setSize(object,0.0)
            return(object)
        }
    )

    c = a
    c = resetMolecule(c, "empty")

    assertThat(
        c@name,
        identicalTo(c@content)
        )

    assertThat(
        c@name,
        identicalTo("empty")
        )

    assertThat(
        c@size,
        identicalTo(200)
        )

    c = resetMolecule(c, 0.0)

    assertThat(
        c@size,
        identicalTo(0.0)
        )

    assertThat(
        c@name,
        identicalTo("empty")
        )

    # Inherit s4 class properties
    Gene <- setClass("Gene",
            slots = c(
                sequence = "character"
                ),
            prototype=list(
                name = "GeneSymbol",
                content = "SequenceType",
                sequence = "ATCG",
                size = 0
                ),
            validity=function(object){
                if((object@name == "") | (object@content == "") | (object@size == "")) {
                                return("INVALID OBJECT: Name, content, or the size of your object is empty.")
                }
                if(is.element("FALSE",is.element(strsplit(object@sequence,split="")[[1]],c("A","T","C","G")))){
                    return("Your gene sequence contains nucleotides other than A/T/C/G.")
                }
                if(object@size == 0){
                    return("Gene size of 0 is not allowed.")
                }
                return(TRUE)
            },
        contains = "Molecule"
        )

    e = Gene(name="Gpr56",content="mRNA",sequence="ATCGAGT",size=7)

    assertThat(
        getName(e),
        identicalTo("Gpr56")
        )

    assertThat(
        getSize(e),
        identicalTo(7)
        )

    assertThat(
        getContent(e),
        identicalTo("mRNA")
        )

	assertTrue(
		is(tryCatch(Gene(name="Gpr56",content="mRNA",sequence="ATCGAGT3",size=7), error = function(e)e), "error")
		)

	assertTrue(
		is(tryCatch(Gene(name="Gpr56",content=3L,sequence="ATCGAGT",size=7), error = function(e)e), "error")
		)

	assertTrue(
		is(tryCatch(Gene(name=10,content="mRNA",sequence="ATCGAGT3",size=7), error = function(e)e), "error")
		)



    setGeneric(name="setSeq",
        def=function(object,seq){
            standardGeneric("setSeq")
        }
    )

    setMethod(f="setSeq",
        signature="Gene",
        definition=function(object,seq){
            object@sequence <- seq
            validObject(object)
            return(object)
        }
    )

    setGeneric(name="getSeq",
        def=function(object){
            standardGeneric("getSeq")
        }
    )

    setMethod(f="getSeq",
        signature="Gene",
        definition=function(object){
            return(object@sequence)
        }
    )

    # Test signature specific methods
    setGeneric(name="setEmpty",
        def=function(object){
            standardGeneric("setEmpty")
        }
    )

    setMethod(f="setEmpty",
        signature="Molecule",
        definition=function(object){
            object <- setName(object,"empty")
            object <- setContent(object,"empty")
            object <- setSize(object,1)
            return(object)
        }
    )

    setMethod(f="setEmpty",
        signature="Gene",
        definition=function(object){
            object <- setSeq(object,"A")
            object <- callNextMethod(object)
            return(object)
        }
    )

    g = new("Gene", sequence = "ACCATCG", name = "Kozak", content = "regulatory", size = 7)

    assertThat(
        g@name,
        identicalTo("Kozak")
        )

    g = setEmpty(g)

    assertThat(
        g@name,
        identicalTo("empty")
        )

    assertThat(
        g@content,
        identicalTo("empty")
        )

    assertThat(
        g@sequence,
        identicalTo("A")
        )

    assertThat(
        g@size,
        identicalTo(1)
        )


}