

      REAL FUNCTION DOSUM(M)
        INTEGER M
        INTEGER :: x(5) = (/ 1, 2, 3, 4 ,5 /)
        
        x(1) = x(1) * M
        
        DOSUM = SUM(x)
        RETURN 
      END