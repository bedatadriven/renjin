


lm.model.0 <- lm(weight ~ Time, data = ChickWeight)
lm.model.1 <- lm(weight ~ 1, data = ChickWeight)
glm.model.0 <- glm(weight ~ Time, data = ChickWeight, family = poisson)
glm.model.1 <- glm(weight ~ 1, data = ChickWeight, family = poisson)

anova.list <- list(
    lm = anova(lm.model.0, lm.model.1),
    glm = anova(glm.model.0, glm.model.1),
    glm.test = anova(glm.model.0, glm.model.1, test = "Chisq")
)

x <- anova.list[[1]]
y <- readRDS("/tmp/anova.rds")

str(unclass(y))

stopifnot(identical(attributes(x), attributes(y)))

for(i in 1:6) {
    cat(i, "\n")
    stopifnot(identical(x[[i]], y[[i]]))

}
