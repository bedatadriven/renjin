
      program testprogram
      double precision x(4)
      call test(x, 4)


      stop
      end


      subroutine test(x, n)
      double precision x(*)
      integer n

      double precision p
      call magicnumber("xred", p)

      do 45 i=1,n
        x(i) = i * p
   45 continue
      return
      end

      subroutine magicnumber(name, p)
      character*4 name
      double precision p

      p = 4
      if(name(1:1) == 'x') p = 3

      return
      end



