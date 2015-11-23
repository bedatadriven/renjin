
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
      
      
*
*     Test complex comparisons
*
      INTEGER FUNCTION CEQ(X, Y) 
      DOUBLE COMPLEX X, Y
      IF (X.EQ.Y) THEN
          CEQ = 1
      ELSE
          CEQ = 0
      END IF
      RETURN
      END

*
*     Test complex comparisons
*
      INTEGER FUNCTION CNE(X, Y) 
      DOUBLE COMPLEX X, Y
      IF (X.NE.Y) THEN
          CNE = 1
      ELSE
          CNE = 0
      END IF
      RETURN
      END
      
*
*     Multiply two complex numbers together
*  

      DOUBLE COMPLEX FUNCTION CMUL(X, Y) 
      DOUBLE COMPLEX X, Y
      CMUL = X * Y
      RETURN
      END
     
     
*
*     Add two complex numbers together
*

      DOUBLE COMPLEX FUNCTION CADD(X, Y)
      DOUBLE COMPLEX X, Y
      CADD = X + Y
      RETURN
      END
      
*
*     Subtract two complex numbers
*

      DOUBLE COMPLEX FUNCTION CSUB(X, Y)
      DOUBLE COMPLEX X, Y
      CSUB = X - Y
      RETURN
      END

      
*
*     Divide two complex numbers
*

*      DOUBLE COMPLEX FUNCTION CDIV(X, Y)
*      DOUBLE COMPLEX X, Y
*      CSUB = X / Y
*      RETURN
*      END
