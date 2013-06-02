

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

      subroutine localarray(y)
      double precision y
      double precision x(10)
      double precision sumarray
      integer i

      do 200 i=1,10
        x(i) = i*2
  200 continue

      y = sumarray(x, 10)

      return
      end


      double precision function sumarray(x, n)
      integer n
      double precision x(n)
      double precision y
      integer i

      y = 0

      do 210 i=1,n
        y = y + x(i)
  210 continue

      sumarray = y

      return
      end

