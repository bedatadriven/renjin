      INTEGER FUNCTION IOFFST(N,I,J)
C  Map row I and column J of upper half diagonal symmetric matrix
C  onto vector.
      INTEGER N,I,J
      IOFFST=J+(I-1)*N-(I*(I+1))/2
      RETURN
      END
