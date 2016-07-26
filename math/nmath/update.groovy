
def pull = evaluate(new File("../../tools/build/Upstream.groovy"))
def config = [
        "upstreamPrefix": "src/nmath",
        "localPrefix": "src/main/c",
       // "exclude": "math/nmath/src/main/c/Makefile.in" <-- Works!
        "exclude": "**/Makefile*"
]
pull(config)