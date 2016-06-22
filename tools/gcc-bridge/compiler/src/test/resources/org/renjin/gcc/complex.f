
*
*     Return the absolute value of a complex number
*     Tests element access
*


      REAL FUNCTION DCABS1(Z)

      COMPLEX Z
      INTRINSIC ABS,REAL,IMAG

      DCABS1 = ABS(REAL(Z)) + ABS(IMAG(Z))
      RETURN
      END
      
      
*
*     Return the conjugate of a constant complex value
*
            
      COMPLEX FUNCTION DCONST1(Z)
      COMPLEX Z
      COMPLEX TEMP
      INTRINSIC CONJG
            
      TEMP = (1.0D+0,2.0D+0)
      DCONST1 = CONJG(TEMP)
      RETURN
      END

*
*     Return the last the element of a complex array
*     Test complex array pointers
*

      COMPLEX FUNCTION CLAST(X, N)
      INTEGER N
      COMPLEX X(N)
      
      CLAST = X(N)
      RETURN
      END
      
      
      
