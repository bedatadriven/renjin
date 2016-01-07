library(hamcrest)

test.getParseData <- function() {
    p <- parse(text="x+1")
    print(attributes(p))
    pd <- getParseData(p)
    
    # The result is a parse tree that looks like this:
    #      line1 col1 line2 col2 id parent     token terminal text
    #    7     1    1     1    3  7      0      expr    FALSE     
    #    1     1    1     1    1  1      3    SYMBOL     TRUE    x
    #    3     1    1     1    1  3      7      expr    FALSE     
    #    2     1    2     1    2  2      7       '+'     TRUE    +
    #    4     1    3     1    3  4      5 NUM_CONST     TRUE    1
    #    5     1    3     1    3  5      7      expr    FALSE     

    
    assertThat(pd$terminal, identicalTo(c(F,  T,   F,  T,   T,   F)))
    assertThat(pd$text,     identicalTo(c("", "x", "", "+", "1", "")))
    assertThat(pd$token,    identicalTo(c("expr", "SYMBOL", "expr", "'+'", "NUM_CONST", "expr")))
    
}