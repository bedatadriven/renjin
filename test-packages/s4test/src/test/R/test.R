library(stats4)
library(s4test)
library(hamcrest)


test.seqType.ClassExport.ToNamespace = function(){
  
  d = new("seq", sequence = "ACCATCG", name = "Kozak", type = "")
  r = new("seq", sequence = "AUG", name = "Startcodon", type = "")
  p = new("seq", sequence = "VNAGNVQELHIG", name = "ProtTest", type = "")
  
  d = findType(d)
  r = findType(r)
  p = findType(p)
  
  assertThat(
    d2@type,
    identicalTo("DNA")
  )
  
  assertThat(
    r2@type,
    identicalTo("RNA")
  )
  
  assertThat(
    p2@type,
    identicalTo("protein")
    )
}

test.ExtendClassFromMethodAndExportToNamespace = function() {
  
  a = new("mle")
  b = new("mle_ext")
  
  assertThat( class(b@input_file), identicalTo("character") )
  
  assertThat( class(a@input_file), throwsError() )
  
  assertThat( class(a@coef), identicalTo("numeric") )
  
  assertThat( class(a@coef), identicalTo(class(b@coef)) )
  
  assertThat( show(a), throwsError() )
  
  
  setClass("new_ext_mle",
           prototype = c(B = 100, c= "character"),
           contains = "mle_ext"
           )
  
  e = new("new_ext_mle")
  
  assertThat( e@names, identicalTo(c("B","c")))
  
}
