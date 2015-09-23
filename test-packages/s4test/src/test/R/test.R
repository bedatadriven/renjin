library(stats4)
library(s4test)
library(hamcrest)


test.seqType.ClassExport.ToNamespace = function(){

    d = new("seq", sequence = "ACCATCG", name = "Kozak", type = "")
    r = new("seq", sequence = "AUG", name = "Startcodon", type = "")
    p = new("seq", sequence = "VNAGNVQELHIG", name = "ProtTest", type = "")

    d = setType(d)
    r = setType(r)
    p = setType(p)

    assertThat(
        d@type,
        identicalTo("DNA")
        )

    assertThat(
        r@type,
        identicalTo("RNA")
        )

    assertThat(
        p@type,
        identicalTo("protein"))
}

test.ExtendClassFromMethodAndExportToNamespace = function() {

    a = new("mle")
    b = new("mle_ext")
    
    assertThat( class(b@input_file), identicalTo("character") )
    
    assertThat( class(a@input_file), throwsError() )
    
    assertThat( class(a@coef), identicalTo("numeric") )

}