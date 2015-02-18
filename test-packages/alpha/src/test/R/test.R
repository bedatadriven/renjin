
library(org.renjin.test.alpha)
library(hamcrest)

test.invokeMethodsInJvmDependency <- function() {
    node <- parseAlpha('{ "foo": 42 }')
    assertThat(node$nodeType, identicalTo('OBJECT'))
    assertThat(node$get('foo')$asInt(), equalTo(42))
}

test.jacksonVersion <- function() {
    assertThat(alphaVersion(), identicalTo("2.5.1"))
}

test.invokeMethodsInJavaClasses <- function() {
    assertThat(alphaName(), identicalTo("Alpha"))
}

test.dependenciesLoadedOnClasspath <- function() {

    # This is currently an unfortunate side affect of the way were are mixing
    # R's concept of namespaces and java libraries.
    
    # We are still waiting for a standard for modules for the JVM,
    # which would give you the ability to keep dependencies truly private.
    
    # In the meantime, when an R Package depends on a JVM library, this library
    # will be on or added to the JVM's classpath, and so available to any R 
    # code that happens to run. 
    
    import(com.fasterxml.jackson.databind.ObjectMapper)
    om <- ObjectMapper$new()
    assertThat(om$version()$toString(), identicalTo("2.5.1"))
}