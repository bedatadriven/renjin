library("com.mycompany:extensionsdemo")
library("hamcrest")

assertThat(meantrim(1:10), identicalTo(5.5))
