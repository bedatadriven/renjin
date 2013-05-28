

      subroutine test(x, n)
      double precision x(*)
      double precision magicnumber
      integer n

      double precision p
      p = magicnumber("x")

      do 45 i=1,n
        x(i) = i
   45 continue
      return
      end
