
      subroutine stlest(nleft,nright,h)

c     implicit none
c Arg
      integer nleft, nright
      double precision h
      
c GCC 4.6 will compile the following using 
c a TRUTH_OR_EXPR, which is what we want to test
      
      h = max(dble(nleft), dble(nright))
    
      return
      end
