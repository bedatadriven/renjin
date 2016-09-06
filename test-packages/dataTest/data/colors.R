
colors <- local({ 

    color.table <- read.table("colors.csv", skip = 4, col.names = "name", stringsAsFactors = FALSE)
    color.table[,1]
})
