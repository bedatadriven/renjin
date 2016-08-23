
      REAL FUNCTION SUB1 ()
      INTEGER A, B, C
      DOUBLE PRECISION F
      COMMON /ZVOD01/ A, B, C, F
      
      CALL SUB2()
      
      SUB1 = C
      RETURN
      END
      
      
      SUBROUTINE SUB2 () 
      INTEGER D
      COMMON /ZVOD01/ D(3)
      
      D(1) = 41
      D(2) = 42;
      D(3) = 43;
      
      RETURN
      END
      
      