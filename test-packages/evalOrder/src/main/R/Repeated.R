
# During package build, GNU R orders files using LC_COLLATE=C, ensuring that
# ordering is done on a byte-by-byte basis, which means that upper case letters
# come _before_ lower case letters, among other differences.

defined.first <- 42