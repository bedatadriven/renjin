
*
*     Return the absolute value of a complex number
*     Tests element access
*


      DOUBLE PRECISION FUNCTION DCABS1(Z)

      DOUBLE COMPLEX Z
      INTRINSIC ABS,DBLE,DIMAG

      DCABS1 = ABS(DBLE(Z)) + ABS(DIMAG(Z))
      RETURN
      END
      
      
*
*     Return the conjugate of a constant complex value
*
            
      DOUBLE COMPLEX FUNCTION DCONST1(Z)
      DOUBLE COMPLEX Z
      DOUBLE COMPLEX TEMP
      INTRINSIC DCONJG
            
      TEMP = (1.0D+0,2.0D+0)
      DCONST1 = DCONJG(TEMP)
      RETURN
      END

*
*     Return the last the element of a complex array
*     Test complex array pointers
*

      DOUBLE COMPLEX FUNCTION CLAST(X, N)
      INTEGER N
      DOUBLE COMPLEX X(N)
      
      CLAST = X(N)
      RETURN
      END
      
      
