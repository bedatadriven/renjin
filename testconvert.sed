# remove eval() calls
s/eval[(]\"\([^\"]*\)\"[)]/\1/g

# no semicolons needed!
s/;$//

# no need to distinguish between c() and c_i(), R's == function 
# will convert as necessary
s/c_i/c/g

# s/c[(]\([^,]*\)[)]/\1/g

s/public void \([A-Za-z0-9_]\+\)()/test.\1 <- function()/ 

s/@Test//

# change java to R comments
s@//@#@


s/false/FALSE/g
s/true/TRUE/g

s/symbol[(]/as.symbol(/g

#other java junk we don't need
s/assumingBasePackagesLoad()//
s/throws[A-Za-z0-9 ,]*//