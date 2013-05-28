
      subroutine test(x,n)
      integer n
      double precision x(n,n)

      integer i, j
      double precision q

      do 100 i=1,n
          call square(x(i,i), i)
  100 continue

      return
      end

      subroutine square(x, i)
      double precision x
      integer i

      x = i*i
      return
      end
