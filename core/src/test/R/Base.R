
testBquote <- function() {
	model <-  bquote(~0 + .(quote(births)))
	
	assertThat(model, identicalTo( ~0 + births ) )
}