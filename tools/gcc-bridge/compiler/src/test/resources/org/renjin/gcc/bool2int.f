

      subroutine test(x, y)
      integer x, y
      integer n
      logical booleanfn

      n = booleanfn(x)
      y = 2*n

      return
      end


      logical function booleanfn(x)
      integer x
        booleanfn = (x .gt. 42)
      return
      end
