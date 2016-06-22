
library(hamcrest)

test.typeConvert <- function() {

    
    import(org.renjin.utils.Tables)
    type.convert <- function(x, naStrings, asIs, dec = '.', numerals = 'allow.loss')
        Tables$typeconvert(x, naStrings, asIs, dec, numerals)

    assertThat(type.convert(c('1','2','3'), 'NA', FALSE), identicalTo(c(1L,2L,3L)))
    assertThat(type.convert(c('T','NA','F'), 'NA', FALSE), identicalTo(c(TRUE, NA, FALSE)))
    assertThat(type.convert(c('T','NA',''), 'NA', FALSE), equalTo(c(TRUE, NA, NA)))
    assertThat(type.convert(c('T','FALSE','BOB'), 'BOB', FALSE), equalTo(c(TRUE, FALSE, NA)))
    assertThat(type.convert(c('3.5','3.6','FOO'), 'FOO', FALSE), equalTo(c(3.5,3.6,NA)))
    assertThat(type.convert(c('bing', 'bop'), 'FOO', TRUE), equalTo(c("bing","bop")))
    assertThat(type.convert(c('bing', 'bop'), 'FOO', FALSE), 
        identicalTo(structure(1:2, class = "factor", .Label = c("bing", "bop"))))

}