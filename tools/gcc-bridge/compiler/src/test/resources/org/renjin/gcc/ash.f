c ash.f----------------------------------------------------------------------
c       April 8, 1986
c
c       Find bin counts of data array "x(n)" for ASH estimator
c
c       "nbin" bins are formed over the interval [a,b)
c
c       bin counts returned in array "nc"  -  # pts outside [a,b) = "nskip"

c ##### Copyright 1986-2009 David W. Scott
c #####
c ##### This program is free software; you can redistribute it and/or
c ##### modify it under the terms of the GNU General Public License as 
c ##### published by the Free Software Foundation; either version 2 of 
c ##### the License, or (at your option) any later version.
c #####
c ##### This program is distributed in the hope that it will be useful,
c ##### but WITHOUT ANY WARRANTY; without even the implied warranty of
c ##### MERCHANTABILITY or FITNESS FOR A PARTICULAR PURSE.
c ##### See the GNU General Public License for more details.
c #####
c ##### You should have received a copy of the GNU General Public
c ##### License along with this program; if not, write to the Free 
c ##### Software Foundation, Inc.,
c ##### 51 Franklin St, Fifth Floor, 
c ##### Boston, MA  02110-1301 USA
c #####
c ##### On Debian GNU/Linux systems, the complete text of the GNU
c ##### General Public License can be found in
c ##### /usr/share/common-licenses/GPL-2.


        subroutine bin1 ( x , n , ab , nbin , nc , nskip )
        double precision x(n) , ab(2), d, a, b
        integer nc(nbin)

        nskip = 0
        a = ab(1)
        b = ab(2)

        do 5 i = 1,nbin
                nc(i) = 0
5       continue

        d = (b-a) / nbin

        do 10 i = 1,n
                k = (x(i)-a) / d + 1.0
                if (k.ge.1 .and. k.le.nbin) then
                        nc(k) = nc(k) + 1
                else
                        nskip = nskip + 1
                end if
10      continue

        return
        end


c       April 8, 1986
c
c       Computer ASH density estimate;  Quartic (biweight) kernel
c
c       Average of "m" shifted histograms
c       
c       Bin counts in array "nc(nbin)"  -  from routine "bin1"
c
c       "nbin" bins are formed over the interval [a,b)
c
c       ASH estimates returned in array "f(nbin)"
c
c       FP-ASH plotted at  a+d/2 ... b-d/2   where d = (b-a)/nbin
c
c       Note:  If "nskip" was nonzero, ASH estimates incorrect near boundary
c       Note:  Should leave "m" empty bins on each end of array "nc" so f OK

c ##### Copyright 1986 David W. Scott

        subroutine ash1 ( m, nc, nbin, ab, kopt, t, f, w, ier )
        double precision ab(2), t(nbin), f(nbin), w(m), delta, h
        integer m, nc(nbin), kopt(2)

        ier = 0
        a = ab(1)
        b = ab(2)
        n = 0

c-compute weights    cons * ( 1-abs((i/m))^kopt1)^kopt2
c             --  should sum to "m"   5-8-91
c                       w-array shifted by 1

        mm1 = m-1
        xm = m
c                     cons = sum of weights from -(m-1) to (m-1) = 1 + 2 (sum from 1 to m-1)

        w(1) = 1.0
        cons = 1.0
        do 5 i = 1,mm1
                w(i+1) = ( 1.0 - abs(i/xm)**kopt(1) )**kopt(2)
                cons = cons + 2*w(i+1)
5       continue
        cons = float(m)/cons
        do 6 i=1,m
           w(i) = cons*w(i)
6      continue

c-check if estimate extends beyond mesh

        do 7 i = 1,mm1
                if( nc(i)+nc(nbin+1-i) .gt. 0) ier = 1
7       continue

c-compute ash(m) estimate

        delta = (b-a) / nbin
        h = m*delta

        do 10 i = 1,nbin
                t(i) = a + (i-0.5)*delta
                f(i) = 0.0
                n = n + nc(i)
10      continue

        do 20 i = 1,nbin
                if (nc(i).eq.0) goto 20

                c = nc(i) / (n*h)
                do 15 k = max0(1,i-mm1) , min0(nbin,i+mm1)
                        f(k) = f(k) + c * w( iabs(k-i)+1 )
15              continue
20      continue

        return
        end
      
c       April 12, 1986          bin2.f
c
c       Find bin counts of data array "x(n,2)" for ASH estimator
c
c       "nbin1" by "nbin2" bins are formed
c
c       x:axis  [ ab(1,1) , ab(1,2) )
c       y:axis  [ ab(2,1) , ab(2,2) )   half-open
c
c       bin counts returned in array "nc"  -  # pts outside [a,b) = "nskip"

c ##### Copyright 1986 David W. Scott

        subroutine bin2 ( x , n , ab , nbin1, nbin2 , nc , nskip )
        double precision x(n,2) , ab(2,2), dx, dy, ax , by
        integer nc(nbin1,nbin2), nskip, n
        
        nskip = 0
        ax = ab(1,1)
        bx = ab(1,2)
        ay = ab(2,1)
        by = ab(2,2)

        do 5 j = 1,nbin2
                do 4 i = 1,nbin1
                        nc(i,j) = 0
4               continue
5       continue

        dx = (bx-ax) / nbin1
        dy = (by-ay) / nbin2

        do 10 i = 1,n
                kx = (x(i,1)-ax) / dx + 1.0
                ky = (x(i,2)-ay) / dy + 1.0
                if (kx.ge.1 .and. kx.le.nbin1 .and.
     *              ky.ge.1 .and. ky.le.nbin2) then
                                nc(kx,ky) = nc(kx,ky) + 1
                else
                        nskip = nskip + 1
                end if
10      continue
        return
        end


c       April 12, 1986          ash2.f
c
c       Computer ASH density estimate;  Product Quartic (biweight) kernel
c
c       Average of "m[1] by m[2]" shifted histograms
c       
c       Bin counts in matrix "nc"  -  from routine "nbin2"
c
c       ASH estimates returned in matrix "f"
c
c       FP-ASH plotted at  a+d/2 ... b-d/2   where d = (b-a)/nbin
c
c       Note:  If "nskip" was nonzero, ASH estimates incorrect near boundary
c       Note:  Should leave "m" empty bins on each end of array "nc" so f OK

c #### Copyright 1986 David W. Scott
c #### added kernel option kopt 5-8-91

        subroutine ash2 ( m1, m2, nc, nbinx, nbiny, ab, kopt, f, w, ier)
        double precision ab(2,2) , f(nbinx,nbiny), w( m1 , m2 )
        integer m1, m2, nbinx, nbiny, nc(nbinx,nbiny), kopt(2)

        ier = 0
        ax = ab(1,1)
        bx = ab(1,2)
        ay = ab(2,1)
        by = ab(2,2)

c-compute weights    cons * ( 1-abs(i/m)^kopt1)^kopt2
c           --  should sum to "m"   5-8-91
c                       w-array shifted by 1

        mx = m1
        my = m2
        mxm1 = mx-1
        mym1 = my-1
        xm = mx
        ym = my

c-----ASSUMES f dimensioned larger than m1 or m2

c-put marginal weights in f array as work array

        f(1,1) = 1.0
        f(2,1) = 1.0
c       consx = sum of weights from -(m-1) to (m-1) = 1 + 2 (sum from 1 to m-1)
        consx = 1.0
        consy = 1.0

        do 5 i = 1,mxm1
                f(1,i+1) = ( 1.0 - abs(i/xm)**kopt(1) )**kopt(2)
                consx = consx + 2*f(1,i+1)
5       continue
        consx = float(mx)/consx
        do 6 i = 1,mym1
                f(2,i+1) = ( 1.0 - abs(i/ym)**kopt(1) )**kopt(2)
                consy = consy + 2*f(2,i+1)
6       continue
        consy = float(my)/consy

c-computer product weight array (avoids later multiplications)

        do 3 j = 1,my
                do 2 i = 1,mx
                        w(i,j) = (consx*f(1,i)) * (consy*f(2,j))
2               continue
3       continue

c-compute ash(m) estimate

        n = 0
        do 10 j = 1,nbiny
                do 9 i = 1,nbinx
                        f(i,j) = 0.0
                        n = n + nc(i,j)
9               continue
10      continue
c-check if estimate extends beyond mesh

        ncheck = 0
        do 12 j = my , nbiny+1-my
                do 11 i = mx , nbinx+1-mx
                        ncheck = ncheck + nc(i,j)
11              continue
12      continue
        if (ncheck .ne. n) ier = 1

        dx = (bx-ax) / nbinx
        dy = (by-ay) / nbiny

        hx = mx*dx
        hy = my*dy

        do 20 j = 1,nbiny
            do 19 i = 1,nbinx

                if (nc(i,j).eq.0) goto 19

                c = nc(i,j) / (n*hx*hy)

                do 18 ky = max0(1,j-mym1) , min0(nbiny,j+mym1)
                    do 17 kx = max0(1,i-mxm1) , min0(nbinx,i+mxm1)
                        f(kx,ky) = f(kx,ky) + c *
     *                          w( iabs(kx-i)+1 , iabs(ky-j)+1 )
17                  continue
18              continue

19              continue
20      continue

        return
        end

