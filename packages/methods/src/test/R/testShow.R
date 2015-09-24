library(hamcrest)

test.Show = function(){
    a = new("numeric")
    show(a)
}

test.ShowS4ClassObject = function(){
    setClass("test_class",slots=c(a="numeric"))
    a = new("test_class")
    show(a)
}