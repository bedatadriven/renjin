library(s4test)
library(hamcrest)

test.seqType.ClassExport.ToNamespace = function(){

    d = new("Gene", sequence = "ACCATCG", name = "Kozak", type = "")
    r = new("Gene", sequence = "AUG", name = "Startcodon", type = "")
    p = new("Gene", sequence = "VNAGNVQELHIG", name = "ProtTest", type = "")

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

test.ExtendClassFromMethodAndExportToNamespace = function(){

a = new("mle")
b = new("mle_ext")

assertThat( class(b@input_file), identicalTo("character") )

assertThat( {class(a@input_file)}, ThrowsError )

assertThat( {class(a@coef)}, identicalTo("numeric") )

}