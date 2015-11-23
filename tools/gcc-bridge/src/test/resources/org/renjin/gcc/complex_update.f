
      
*
*     Update pointer to an array of complex numbers
* 

      SUBROUTINE UPDATE2(X, Y)
      DOUBLE COMPLEX X(*), Y
      X(2) = Y
      RETURN
      END
      
      
      
*
*     Update pointer to an array of with a real number
* 

      SUBROUTINE UPDATER2(X, Y)
      DOUBLE COMPLEX X(*)
      DOUBLE PRECISION Y
      X(2) = Y
      RETURN
      END
  
