
library(hamcrest)
library("org.renjin.test:native")

assertThat(.Call("call_asChar", letters), identicalTo("a"))
assertThat(.Call("call_asChar", 1L), identicalTo("1"))
assertThat(.Call("call_asChar", character(0)), identicalTo(NA_character_))
assertThat(.Call("call_asChar", NULL), identicalTo(NA_character_))

assertThat(.Call("test_CHAR_NULL"), identicalTo(-1L))
