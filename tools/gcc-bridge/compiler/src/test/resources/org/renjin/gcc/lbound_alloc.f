
      subroutine testprogram
      call sumseries(4)
      call sumseries(20)
      return
      end

      subroutine sumseries(ni)
      real, dimension (:), allocatable :: xs
      real sum
      allocate(xs(1:ni),stat=ierr)
      xs(ni) = 200
      return
      end



