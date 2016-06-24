library(hamcrest)

failing.test.Show = function(){
    a = new("numeric")
    show(a)
}

failing.test.ShowS4ClassObject = function(){
    setClass("test_class",slots=c(a="numeric"))
    a = new("test_class")
    show(a)
}