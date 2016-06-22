
      SUBROUTINE HCLUST(R, DMIN)
c Args
      DOUBLE PRECISION R, DMIN
      
c Vars
      DOUBLE PRECISION INF
      DATA INF/1.D+300/
      
      DMIN=INF
      IF (DMIN .GT. R) THEN
            DMIN=R
      end if
      
      RETURN
      END
