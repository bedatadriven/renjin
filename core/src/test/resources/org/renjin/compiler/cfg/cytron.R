I <- 1
J <- 1
K <- 1
L <- 1
repeat {
	if(P) {
		J <- I
		if(Q) {
			L <- 2
		} else {
			L <- 3
		}
		K <- K + 1
	} else {
		K <- K + 2
	}
  list(I,J,K,L)
	repeat {
		if(R) {
			L <- L + 4
		}
		if(S) break;
	}
	I <- I + 6
	if(T) break;
}
