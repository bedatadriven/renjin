
      subroutine runtest()

      logical cb,cqy,cqty,cr,cxb
      integer job

      job = 1110

      cqy = job/10000 .ne. 0
      cqty = mod(job,10000) .ne. 0
      cb = mod(job,1000)/100 .ne. 0
      cr = mod(job,100)/10 .ne. 0
      cxb = mod(job,10) .ne. 0

      call assertFalse(cqy)
      call assertTrue(cqty)
      call assertTrue(cb)
      call assertTrue(cr)
      call assertFalse(cxb)

      return
      end

      subroutine iftest(z, x)
      integer z, x
      logical q

      q = z > 42

      x = 1
      if (.not.q) go to 100
        x = 2
  100 continue

      return
      end


