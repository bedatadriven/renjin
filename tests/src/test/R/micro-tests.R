test.micro2 <- function() { cat('2\n'); stopifnot(identical({ (0+2i)^0 }, 1+0i)) }
test.micro3 <- function() { cat('3\n'); stopifnot(identical({ x<-c(1,2,3);x }, c(1, 2, 3))) }
test.micro4 <- function() { cat('4\n'); stopifnot(identical({ x<-c(1,2,3);x*2 }, c(2, 4, 6))) }
test.micro5 <- function() { cat('5\n'); stopifnot(identical({ x<-c(1,2,3);x+2 }, c(3, 4, 5))) }
test.micro6 <- function() { cat('6\n'); stopifnot(identical({ x<-c(1,2,3);x+FALSE }, c(1, 2, 3))) }
test.micro7 <- function() { cat('7\n'); stopifnot(identical({ x<-c(1,2,3);x+TRUE }, c(2, 3, 4))) }
test.micro8 <- function() { cat('8\n'); stopifnot(identical({ x<-c(1,2,3);x*x+x }, c(2, 6, 12))) }
test.micro9 <- function() { cat('9\n'); stopifnot(identical({ x<-c(1,2);y<-c(3,4,5,6);x+y }, c(4, 6, 6, 8))) }
test.micro10 <- function() { cat('10\n'); stopifnot(identical({ x<-c(1,2);y<-c(3,4,5,6);x*y }, c(3, 8, 5, 12))) }
test.micro11 <- function() { cat('11\n'); stopifnot(identical({ x<-c(1,2);z<-c();x==z }, logical(0))) }
test.micro12 <- function() { cat('12\n'); stopifnot(identical({ x<-1+NA; c(1,2,3,4)+c(x,10) }, c(NA, 12, NA, 14))) }
test.micro13 <- function() { cat('13\n'); stopifnot(identical({ c(1L,2L,3L)+TRUE }, 2:4)) }
test.micro14 <- function() { cat('14\n'); stopifnot(identical({ c(1L,2L,3L)*c(10L) }, c(10L, 20L, 30L))) }
test.micro15 <- function() { cat('15\n'); stopifnot(identical({ c(1L,2L,3L)*c(10,11,12) }, c(10, 22, 36))) }
test.micro16 <- function() { cat('16\n'); stopifnot(identical({ c(1L,2L,3L,4L)-c(TRUE,FALSE) }, c(0L, 2L, 2L, 4L))) }
test.micro17 <- function() { cat('17\n'); stopifnot(identical({ ia<-c(1L,2L);ib<-c(3L,4L);d<-c(5,6);ia+ib+d }, c(9, 12))) }
test.micro18 <- function() { cat('18\n'); stopifnot(identical({ z <- c(-1.5-1i,10) ; (z * z)[1] }, 1.25+3i)) }
test.micro19 <- function() { cat('19\n'); stopifnot(identical({ c(1,2,3+1i)^3 }, c(1+0i, 8+0i, 18+26i))) }
test.micro20 <- function() { cat('20\n'); stopifnot(identical({ round( 3^c(1,2,3+1i), digits=5 ) }, c(3+0i, 9+0i, 12.28048+24.04558i))) }
test.micro21 <- function() { cat('21\n'); stopifnot(identical({ 1L + 1:2 }, 2:3)) }
test.micro22 <- function() { cat('22\n'); stopifnot(identical({ 4:3 + 2L }, c(6L, 5L))) }
test.micro23 <- function() { cat('23\n'); stopifnot(identical({ 1:2 + 3:4 }, c(4L, 6L))) }
test.micro24 <- function() { cat('24\n'); stopifnot(identical({ 1:2 + c(1L, 2L) }, c(2L, 4L))) }
test.micro25 <- function() { cat('25\n'); stopifnot(identical({ c(1L, 2L) + 1:4 }, c(2L, 4L, 4L, 6L))) }
test.micro26 <- function() { cat('26\n'); stopifnot(identical({ 1:4 + c(1L, 2L) }, c(2L, 4L, 4L, 6L))) }
test.micro27 <- function() { cat('27\n'); stopifnot(identical({ 2L + 1:2 }, 3:4)) }
test.micro28 <- function() { cat('28\n'); stopifnot(identical({ 1:2 + 2L }, 3:4)) }
test.micro29 <- function() { cat('29\n'); stopifnot(identical({ c(1L, 2L) + 2L }, 3:4)) }
test.micro30 <- function() { cat('30\n'); stopifnot(identical({ 2L + c(1L, 2L) }, 3:4)) }
test.micro31 <- function() { cat('31\n'); stopifnot(identical({ 1 + 1:2 }, c(2, 3))) }
test.micro32 <- function() { cat('32\n'); stopifnot(identical({ c(1,2) + 1:2 }, c(2, 4))) }
test.micro33 <- function() { cat('33\n'); stopifnot(identical({ c(1,2,3,4) + 1:2 }, c(2, 4, 4, 6))) }
test.micro34 <- function() { cat('34\n'); stopifnot(identical({ c(1,2,3,4) + c(1L,2L) }, c(2, 4, 4, 6))) }
test.micro35 <- function() { cat('35\n'); stopifnot(identical({ 1:2 + 1 }, c(2, 3))) }
test.micro36 <- function() { cat('36\n'); stopifnot(identical({ 1:2 + c(1,2) }, c(2, 4))) }
test.micro37 <- function() { cat('37\n'); stopifnot(identical({ 1:2 + c(1,2,3,4) }, c(2, 4, 4, 6))) }
test.micro38 <- function() { cat('38\n'); stopifnot(identical({ c(1L,2L) + c(1,2,3,4) }, c(2, 4, 4, 6))) }
test.micro39 <- function() { cat('39\n'); stopifnot(identical({ 1L + c(1,2) }, c(2, 3))) }
test.micro40 <- function() { cat('40\n'); stopifnot(identical({ a <- c(1,3) ; b <- c(2,4) ; a ^ b }, c(1, 81))) }
test.micro41 <- function() { cat('41\n'); stopifnot(identical({ a <- c(1,3) ; a ^ 3 }, c(1, 27))) }
test.micro42 <- function() { cat('42\n'); stopifnot(identical({ a <- c(1+1i,3+2i) ; a - (4+3i) }, c(-3-2i, -1-1i))) }
test.micro43 <- function() { cat('43\n'); stopifnot(identical({ c(1,3) - 4 }, c(-3, -1))) }
test.micro44 <- function() { cat('44\n'); stopifnot(identical({ c(1+1i,3+2i) * c(1,2) }, c(1+1i, 6+4i))) }
test.micro45 <- function() { cat('45\n'); stopifnot(identical({ z <- c(1+1i,3+2i) ; z * c(1,2) }, c(1+1i, 6+4i))) }
test.micro46 <- function() { cat('46\n'); stopifnot(identical({ round(c(1+1i,2+3i)^c(1+1i,3+4i), digits = 5) }, c(0.27396+0.5837i, -0.20455+0.89662i))) }
test.micro47 <- function() { cat('47\n'); stopifnot(identical({ c(1+1i,3+2i) / 2 }, c(0.5+0.5i, 1.5+1i))) }
test.micro48 <- function() { cat('48\n'); stopifnot(identical({ c(1,3) / c(2,4) }, c(0.5, 0.75))) }
test.micro49 <- function() { cat('49\n'); stopifnot(identical({ c(1,3) %/% c(2,4) }, c(0, 0))) }
test.micro50 <- function() { cat('50\n'); stopifnot(identical({ integer()+1 }, numeric(0))) }
test.micro51 <- function() { cat('51\n'); stopifnot(identical({ 1+integer() }, numeric(0))) }
test.micro52 <- function() { cat('52\n'); stopifnot(identical({ 1:2+1:3 }, c(2L, 4L, 4L))) }
test.micro53 <- function() { cat('53\n'); stopifnot(identical({ 1:3*1:2 }, c(1L, 4L, 3L))) }
test.micro54 <- function() { cat('54\n'); stopifnot(identical({ 1:3+c(1,2+2i) }, c(2+0i, 4+2i, 4+0i))) }
test.micro55 <- function() { cat('55\n'); stopifnot(identical({ c(1,2+2i)+1:3 }, c(2+0i, 4+2i, 4+0i))) }
test.micro56 <- function() { cat('56\n'); stopifnot(identical({ NA+1:3 }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro57 <- function() { cat('57\n'); stopifnot(identical({ 1:3+NA }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro58 <- function() { cat('58\n'); stopifnot(identical({ NA+c(1L, 2L, 3L) }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro59 <- function() { cat('59\n'); stopifnot(identical({ c(1L, 2L, 3L)+NA }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro60 <- function() { cat('60\n'); stopifnot(identical({ c(NA,NA,NA)+1:3 }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro61 <- function() { cat('61\n'); stopifnot(identical({ 1:3+c(NA, NA, NA) }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro62 <- function() { cat('62\n'); stopifnot(identical({ c(NA,NA,NA)+c(1L,2L,3L) }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro63 <- function() { cat('63\n'); stopifnot(identical({ c(1L,2L,3L)+c(NA, NA, NA) }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro64 <- function() { cat('64\n'); stopifnot(identical({ c(NA,NA)+1:4 }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro65 <- function() { cat('65\n'); stopifnot(identical({ 1:4+c(NA, NA) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro66 <- function() { cat('66\n'); stopifnot(identical({ c(NA,NA,NA,NA)+1:2 }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro67 <- function() { cat('67\n'); stopifnot(identical({ 1:2+c(NA,NA,NA,NA) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro68 <- function() { cat('68\n'); stopifnot(identical({ c(NA,NA)+c(1L,2L,3L,4L) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro69 <- function() { cat('69\n'); stopifnot(identical({ c(1L,2L,3L,4L)+c(NA, NA) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro70 <- function() { cat('70\n'); stopifnot(identical({ c(NA,NA,NA,NA)+c(1L,2L) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro71 <- function() { cat('71\n'); stopifnot(identical({ c(1L,2L)+c(NA,NA,NA,NA) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro72 <- function() { cat('72\n'); stopifnot(identical({ c(1L,NA)+1 }, c(2, NA))) }
test.micro73 <- function() { cat('73\n'); stopifnot(identical({ c(1L,NA) + c(2,3) }, c(3, NA))) }
test.micro74 <- function() { cat('74\n'); stopifnot(identical({ c(2,3) + c(1L,NA) }, c(3, NA))) }
test.micro75 <- function() { cat('75\n'); stopifnot(identical({ 1:4+c(1,2) }, c(2, 4, 4, 6))) }
test.micro76 <- function() { cat('76\n'); stopifnot(identical({ c(1,2)+1:4 }, c(2, 4, 4, 6))) }
test.micro77 <- function() { cat('77\n'); stopifnot(identical({ 1:4+c(1,2+2i) }, c(2+0i, 4+2i, 4+0i, 6+2i))) }
test.micro78 <- function() { cat('78\n'); stopifnot(identical({ c(1,2+2i)+1:4 }, c(2+0i, 4+2i, 4+0i, 6+2i))) }
test.micro79 <- function() { cat('79\n'); stopifnot(identical({ c(3,4) %% 2 }, c(1, 0))) }
test.micro80 <- function() { cat('80\n'); stopifnot(identical({ c(3,4) %% c(2,5) }, c(1, 4))) }
test.micro81 <- function() { cat('81\n'); stopifnot(identical({ c(3,4) %/% 2 }, c(1, 2))) }
test.micro82 <- function() { cat('82\n'); stopifnot(identical({ 3L %/% 2L }, 1L)) }
test.micro83 <- function() { cat('83\n'); stopifnot(identical({ 3L %/% 0L }, NA_integer_)) }
test.micro84 <- function() { cat('84\n'); stopifnot(identical({ ((1+0i)/(0+0i)) ^ (-3) }, 0+0i)) }
test.micro85 <- function() { cat('85\n'); stopifnot(identical({ ((1+1i)/(0+0i)) }, complex(real=Inf, i=Inf))) }
test.micro86 <- function() { cat('86\n'); stopifnot(identical({ ((1+1i)/(0+0i)) ^ (-3) }, 0+0i)) }
test.micro87 <- function() { cat('87\n'); stopifnot(identical({ round( ((1+1i)/(0+1i)) ^ (-3.54), digits=5) }, -0.27428+0.10364i)) }
test.micro88 <- function() { cat('88\n'); stopifnot(identical({ 0/0 - 4i }, NA_complex_)) }
test.micro89 <- function() { cat('89\n'); stopifnot(identical({ 4i + 0/0 }, NA_complex_)) }
test.micro90 <- function() { cat('90\n'); stopifnot(identical({ a <- 1 + 2i; b <- 0/0 - 4i; a + b }, NA_complex_)) }
test.micro91 <- function() { cat('91\n'); stopifnot(identical({ 1L+1 }, 2)) }
test.micro92 <- function() { cat('92\n'); stopifnot(identical({ 1L+1L }, 2L)) }
test.micro93 <- function() { cat('93\n'); stopifnot(identical({ (1+1)*(3+2) }, 10)) }
test.micro94 <- function() { cat('94\n'); stopifnot(identical({ 1000000000*100000000000 }, 1e+20)) }
test.micro95 <- function() { cat('95\n'); stopifnot(identical({ 1000000000L*1000000000L }, NA_integer_)) }
test.micro96 <- function() { cat('96\n'); stopifnot(identical({ 1000000000L*1000000000 }, 1e+18)) }
test.micro97 <- function() { cat('97\n'); stopifnot(identical({ 1+TRUE }, 2)) }
test.micro98 <- function() { cat('98\n'); stopifnot(identical({ 1L+TRUE }, 2L)) }
test.micro99 <- function() { cat('99\n'); stopifnot(identical({ 1+FALSE<=0 }, FALSE)) }
test.micro100 <- function() { cat('100\n'); stopifnot(identical({ 1L+FALSE<=0 }, FALSE)) }
test.micro101 <- function() { cat('101\n'); stopifnot(identical({ TRUE+TRUE+TRUE*TRUE+FALSE+4 }, 7)) }
test.micro102 <- function() { cat('102\n'); stopifnot(identical({ 1L*NA }, NA_integer_)) }
test.micro103 <- function() { cat('103\n'); stopifnot(identical({ 1+NA }, NA_real_)) }
test.micro104 <- function() { cat('104\n'); stopifnot(identical({ 2L^10L }, 1024)) }
test.micro105 <- function() { cat('105\n'); stopifnot(identical({ 3 %/% 2 }, 1)) }
test.micro106 <- function() { cat('106\n'); stopifnot(identical({ 3L %/% 2L }, 1L)) }
test.micro107 <- function() { cat('107\n'); stopifnot(identical({ 3L %/% -2L }, -2L)) }
test.micro108 <- function() { cat('108\n'); stopifnot(identical({ 3 %/% -2 }, -2)) }
test.micro109 <- function() { cat('109\n'); stopifnot(identical({ 3 %/% 0 }, Inf)) }
test.micro110 <- function() { cat('110\n'); stopifnot(identical({ 3L %/% 0L }, NA_integer_)) }
test.micro111 <- function() { cat('111\n'); stopifnot(identical({ 3 %% 2 }, 1)) }
test.micro112 <- function() { cat('112\n'); stopifnot(identical({ 3L %% 2L }, 1L)) }
test.micro113 <- function() { cat('113\n'); stopifnot(identical({ 3L %% -2L }, -1L)) }
test.micro114 <- function() { cat('114\n'); stopifnot(identical({ 3 %% -2 }, -1)) }
test.micro115 <- function() { cat('115\n'); stopifnot(identical({ is.nan(3 %% 0) }, TRUE)) }
test.micro116 <- function() { cat('116\n'); stopifnot(identical({ 3L %% 0L }, NA_integer_)) }
test.micro117 <- function() { cat('117\n'); stopifnot(identical({ 0x10 + 0x10L + 1.28 }, 33.28)) }
test.micro118 <- function() { cat('118\n'); stopifnot(identical({ 1/0 }, Inf)) }
test.micro119 <- function() { cat('119\n'); stopifnot(identical({ (1+2i)*(3+4i) }, -5+10i)) }
test.micro120 <- function() { cat('120\n'); stopifnot(identical({ x <- 1+2i; y <- 3+4i; x*y }, -5+10i)) }
test.micro121 <- function() { cat('121\n'); stopifnot(identical({ x <- 1+2i; y <- 3+4i; x/y }, 0.44+0.08i)) }
test.micro122 <- function() { cat('122\n'); stopifnot(identical({ x <- 1+2i; y <- 3+4i; x-y }, -2-2i)) }
test.micro123 <- function() { cat('123\n'); stopifnot(identical({ x <- 1+2i; y <- 3+4i; identical(round(x*x*y/(x+y), digits=5), -1.92308+2.88462i) }, TRUE)) }
test.micro124 <- function() { cat('124\n'); stopifnot(identical({ x <- c(-1.5-1i,-1.3-1i) ; y <- c(0+0i, 0+0i) ; y*y+x }, c(-1.5-1i, -1.3-1i))) }
test.micro125 <- function() { cat('125\n'); stopifnot(identical({ x <- c(-1.5-1i,-1.3-1i) ; y <- c(0+0i, 0+0i) ; y-x }, c(1.5+1i, 1.3+1i))) }
test.micro126 <- function() { cat('126\n'); stopifnot(identical({ x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; y-x }, c(4+3i, -7-5i))) }
test.micro127 <- function() { cat('127\n'); stopifnot(identical({ x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; y+x }, c(2-1i, -1+15i))) }
test.micro128 <- function() { cat('128\n'); stopifnot(identical({ x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; y*x }, c(-1-7i, -62-25i))) }
test.micro129 <- function() { cat('129\n'); stopifnot(identical({ x <- c(-1-2i,3+10i) ; y <- c(3+1i, -4+5i) ; round(y/x, digits=5) }, c(-1+1i, 0.34862+0.50459i))) }
test.micro130 <- function() { cat('130\n'); stopifnot(identical({ round( (1+2i)^(3+4i), digits=5 ) }, 0.12901+0.03392i)) }
test.micro131 <- function() { cat('131\n'); stopifnot(identical({ (1+2i)^2 }, -3+4i)) }
test.micro132 <- function() { cat('132\n'); stopifnot(identical({ (1+2i)^(-2) }, -0.12-0.16i)) }
test.micro133 <- function() { cat('133\n'); stopifnot(identical({ (1+2i)^0 }, 1+0i)) }
test.micro134 <- function() { cat('134\n'); stopifnot(identical({ 0^(-1+1i) }, complex(real=NaN, i=NaN))) }
test.micro135 <- function() { cat('135\n'); stopifnot(identical({ (0+0i)/(0+0i) }, complex(real=NaN, i=NaN))) }
test.micro136 <- function() { cat('136\n'); stopifnot(identical({ (1+0i)/(0+0i) }, complex(real=Inf, i=NaN))) }
test.micro137 <- function() { cat('137\n'); stopifnot(identical({ (0+1i)/(0+0i) }, complex(real=NaN, i=Inf))) }
test.micro138 <- function() { cat('138\n'); stopifnot(identical({ (1+1i)/(0+0i) }, complex(real=Inf, i=Inf))) }
test.micro139 <- function() { cat('139\n'); stopifnot(identical({ (-1+0i)/(0+0i) }, complex(real=-Inf, i=NaN))) }
test.micro140 <- function() { cat('140\n'); stopifnot(identical({ (-1-1i)/(0+0i) }, complex(real=-Inf, i=-Inf))) }
test.micro141 <- function() { cat('141\n'); stopifnot(identical({ ((0+1i)/0) * ((0+1i)/0) }, complex(real=-Inf, i=NaN))) }
test.micro142 <- function() { cat('142\n'); stopifnot(identical({ ((0-1i)/0) * ((0+1i)/0) }, complex(real=Inf, i=NaN))) }
test.micro143 <- function() { cat('143\n'); stopifnot(identical({ ((0-1i)/0) * ((0-1i)/0) }, complex(real=-Inf, i=NaN))) }
test.micro144 <- function() { cat('144\n'); stopifnot(identical({ ((0-1i)/0) * ((1-1i)/0) }, complex(real=-Inf, i=-Inf))) }
test.micro145 <- function() { cat('145\n'); stopifnot(identical({ ((0-1i)/0) * ((-1-1i)/0) }, complex(real=-Inf, i=Inf))) }
test.micro146 <- function() { cat('146\n'); stopifnot(identical({ 1/((1+0i)/(0+0i)) }, 0+0i)) }
test.micro147 <- function() { cat('147\n'); stopifnot(identical({ (1+2i) / ((0-0i)/(0+0i)) }, complex(real=NaN, i=NaN))) }
test.micro148 <- function() { cat('148\n'); stopifnot(identical({ 1^(1/0) }, 1)) }
test.micro149 <- function() { cat('149\n'); stopifnot(identical({ (-2)^(1/0) }, NaN)) }
test.micro150 <- function() { cat('150\n'); stopifnot(identical({ (-2)^(-1/0) }, NaN)) }
test.micro151 <- function() { cat('151\n'); stopifnot(identical({ (1)^(-1/0) }, 1)) }
test.micro152 <- function() { cat('152\n'); stopifnot(identical({ 0^(-1/0) }, Inf)) }
test.micro153 <- function() { cat('153\n'); stopifnot(identical({ 0^(1/0) }, 0)) }
test.micro154 <- function() { cat('154\n'); stopifnot(identical({ 0^(0/0) }, NaN)) }
test.micro155 <- function() { cat('155\n'); stopifnot(identical({ 1^(0/0) }, 1)) }
test.micro156 <- function() { cat('156\n'); stopifnot(identical({ (-1)^(0/0) }, NaN)) }
test.micro157 <- function() { cat('157\n'); stopifnot(identical({ (-1/0)^(0/0) }, NaN)) }
test.micro158 <- function() { cat('158\n'); stopifnot(identical({ (1/0)^(0/0) }, NaN)) }
test.micro159 <- function() { cat('159\n'); stopifnot(identical({ (0/0)^(1/0) }, NaN)) }
test.micro160 <- function() { cat('160\n'); stopifnot(identical({ (-1/0)^3 }, -Inf)) }
test.micro161 <- function() { cat('161\n'); stopifnot(identical({ (1/0)^(-4) }, 0)) }
test.micro162 <- function() { cat('162\n'); stopifnot(identical({ (-1/0)^(-4) }, 0)) }
test.micro163 <- function() { cat('163\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1+2i, 3+4i) ; f(1, 2) }, 3)) }
test.micro164 <- function() { cat('164\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(2, 3+4i) ; f(1, 2) }, 3)) }
test.micro165 <- function() { cat('165\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1+2i, 3) ; f(1, 2) }, 3)) }
test.micro166 <- function() { cat('166\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(2, 3+4i) ; f(1, 2) }, 3)) }
test.micro167 <- function() { cat('167\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1+2i, 3) ; f(1, 2) }, 3)) }
test.micro168 <- function() { cat('168\n'); stopifnot(identical({ 1L / 2L }, 0.5)) }
test.micro169 <- function() { cat('169\n'); stopifnot(identical({ f <- function(a, b) { a / b } ; f(1L, 2L) ; f(1, 2) }, 0.5)) }
test.micro170 <- function() { cat('170\n'); stopifnot(identical({ (1:2)[3] / 2L }, NA_real_)) }
test.micro171 <- function() { cat('171\n'); stopifnot(identical({ 2L / (1:2)[3] }, NA_real_)) }
test.micro172 <- function() { cat('172\n'); stopifnot(identical({ a <- (1:2)[3] ; b <- 2L ; a / b }, NA_real_)) }
test.micro173 <- function() { cat('173\n'); stopifnot(identical({ a <- 2L ; b <- (1:2)[3] ; a / b }, NA_real_)) }
test.micro174 <- function() { cat('174\n'); stopifnot(identical({ (1:2)[3] + 2L }, NA_integer_)) }
test.micro175 <- function() { cat('175\n'); stopifnot(identical({ 2L + (1:2)[3] }, NA_integer_)) }
test.micro176 <- function() { cat('176\n'); stopifnot(identical({ a <- (1:2)[3] ; b <- 2L ; a + b }, NA_integer_)) }
test.micro177 <- function() { cat('177\n'); stopifnot(identical({ a <- 2L ; b <- (1:2)[3] ; a + b }, NA_integer_)) }
test.micro178 <- function() { cat('178\n'); stopifnot(identical({ a <- (1:2)[3] ; b <- 2 ; a + b }, NA_real_)) }
test.micro179 <- function() { cat('179\n'); stopifnot(identical({ a <- 2 ; b <- (1:2)[3] ; a + b }, NA_real_)) }
test.micro180 <- function() { cat('180\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(c(1,2), c(3,4)) ; f(c(1,2), 3:4) }, c(4, 6))) }
test.micro181 <- function() { cat('181\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1:2, c(3,4)) ; f(c(1,2), 3:4) }, c(4, 6))) }
test.micro182 <- function() { cat('182\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1:2, 3:4) ; f(c(1,2), 3:4) }, c(4, 6))) }
test.micro183 <- function() { cat('183\n'); stopifnot(identical({ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2L,4) }, 0.5)) }
test.micro184 <- function() { cat('184\n'); stopifnot(identical({ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2L,4L) }, 0.5)) }
test.micro185 <- function() { cat('185\n'); stopifnot(identical({ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2L,(1:2)[3]) }, NA_real_)) }
test.micro186 <- function() { cat('186\n'); stopifnot(identical({ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f((1:2)[3], 2L) }, NA_real_)) }
test.micro187 <- function() { cat('187\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f(2L,4) }, 6)) }
test.micro188 <- function() { cat('188\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f(2L,4L) }, 6L)) }
test.micro189 <- function() { cat('189\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f(2L,(1:2)[3]) }, NA_integer_)) }
test.micro190 <- function() { cat('190\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1L) ; f((1:2)[3], 2L) }, NA_integer_)) }
test.micro191 <- function() { cat('191\n'); stopifnot(identical({ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2,(1:2)[3]) }, NA_real_)) }
test.micro192 <- function() { cat('192\n'); stopifnot(identical({ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f((1:2)[3],2) }, NA_real_)) }
test.micro193 <- function() { cat('193\n'); stopifnot(identical({ f <- function(a, b) { a / b } ; f(1,1) ; f(1,1L) ; f(2+1i,(1:2)[3]) }, NA_complex_)) }
test.micro194 <- function() { cat('194\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; f(1,1) ; f(1,1+2i) ; f(TRUE, 2) }, 3)) }
test.micro195 <- function() { cat('195\n'); stopifnot(identical({ f <- function(b) { 1 / b } ; f(1) ; f(1L) ; f(4) }, 0.25)) }
test.micro196 <- function() { cat('196\n'); stopifnot(identical({ f <- function(b) { 1 / b } ; f(1+1i) ; f(1L) }, 1)) }
test.micro197 <- function() { cat('197\n'); stopifnot(identical({ f <- function(b) { 1 / b } ; f(1) ; f(1L) }, 1)) }
test.micro198 <- function() { cat('198\n'); stopifnot(identical({ f <- function(b) { 1 / b } ; f(1L) ; f(1) }, 1)) }
test.micro199 <- function() { cat('199\n'); stopifnot(identical({ f <- function(b) { 1 / b } ; f(TRUE) ; f(1L) }, 1)) }
test.micro200 <- function() { cat('200\n'); stopifnot(identical({ f <- function(b) { 1i / b } ; f(1) ; f(1L) ; f(4) }, 0+0.25i)) }
test.micro201 <- function() { cat('201\n'); stopifnot(identical({ f <- function(b) { 1i / b } ; f(1+1i) ; f(1L) }, 0+1i)) }
test.micro202 <- function() { cat('202\n'); stopifnot(identical({ f <- function(b) { 1i / b } ; f(1) ; f(1L) }, 0+1i)) }
test.micro203 <- function() { cat('203\n'); stopifnot(identical({ f <- function(b) { 1i / b } ; f(TRUE) ; f(1L) }, 0+1i)) }
test.micro204 <- function() { cat('204\n'); stopifnot(identical({ f <- function(b) { b / 1 } ; f(1) ; f(1L) ; f(4) }, 4)) }
test.micro205 <- function() { cat('205\n'); stopifnot(identical({ f <- function(b) { b / 2 } ; f(1+1i) ; f(1L) }, 0.5)) }
test.micro206 <- function() { cat('206\n'); stopifnot(identical({ f <- function(b) { b / 2 } ; f(1) ; f(1L) }, 0.5)) }
test.micro207 <- function() { cat('207\n'); stopifnot(identical({ f <- function(b) { b / 4 } ; f(1L) ; f(1) }, 0.25)) }
test.micro208 <- function() { cat('208\n'); stopifnot(identical({ f <- function(b) { b / 4i } ; f(1) ; f(1L) }, 0-0.25i)) }
test.micro209 <- function() { cat('209\n'); stopifnot(identical({ f <- function(b) { 4L / b } ; f(1L) ; f(2) }, 2)) }
test.micro210 <- function() { cat('210\n'); stopifnot(identical({ f <- function(b) { 4L + b } ; f(1L) ; f(2) }, 6)) }
test.micro211 <- function() { cat('211\n'); stopifnot(identical({ f <- function(b) { b / 2L } ; f(1L) ; f(2) }, 1)) }
test.micro212 <- function() { cat('212\n'); stopifnot(identical({ f <- function(b) { 4L / b } ; f(1L) ; f(2) ; f(TRUE) }, 4)) }
test.micro213 <- function() { cat('213\n'); stopifnot(identical({ f <- function(b) { 4L + b } ; f(1L) ; f(2) ; f(TRUE) }, 5L)) }
test.micro214 <- function() { cat('214\n'); stopifnot(identical({ f <- function(b) { 4L + b } ; f(1L) ; f(2) ; f((1:2)[3]) }, NA_integer_)) }
test.micro215 <- function() { cat('215\n'); stopifnot(identical({ f <- function(b) { 4L / b } ; f(1L) ; f(2) ; f((1:2)[3]) }, NA_real_)) }
test.micro216 <- function() { cat('216\n'); stopifnot(identical({ f <- function(b) { (1:2)[3] + b } ; f(1L) ; f(2) }, NA_real_)) }
test.micro217 <- function() { cat('217\n'); stopifnot(identical({ f <- function(b) { (1:2)[3] + b } ; f(1) ; f(2L) }, NA_integer_)) }
test.micro218 <- function() { cat('218\n'); stopifnot(identical({ f <- function(b) { b + 4L } ; f(1L) ; f(2) ; f(TRUE) }, 5L)) }
test.micro219 <- function() { cat('219\n'); stopifnot(identical({ f <- function(b) { b + 4L } ; f(1L) ; f(2) ; f((1:2)[3]) }, NA_integer_)) }
test.micro220 <- function() { cat('220\n'); stopifnot(identical({ f <- function(b) { b / 4L } ; f(1L) ; f(2) ; f(TRUE) }, 0.25)) }
test.micro221 <- function() { cat('221\n'); stopifnot(identical({ f <- function(b) { b / 4L } ; f(1L) ; f(2) ; f((1:2)[3]) }, NA_real_)) }
test.micro222 <- function() { cat('222\n'); stopifnot(identical({ f <- function(b) { 1 + b } ; f(1L) ; f(TRUE) }, 2)) }
test.micro223 <- function() { cat('223\n'); stopifnot(identical({ f <- function(b) { FALSE + b } ; f(1L) ; f(2) }, 2)) }
test.micro224 <- function() { cat('224\n'); stopifnot(identical({ f <- function(b) { b + 1 } ; f(1L) ; f(TRUE) }, 2)) }
test.micro225 <- function() { cat('225\n'); stopifnot(identical({ f <- function(b) { b + FALSE } ; f(1L) ; f(2) }, 2)) }
test.micro226 <- function() { cat('226\n'); stopifnot(identical({ !TRUE }, FALSE)) }
test.micro227 <- function() { cat('227\n'); stopifnot(identical({ !FALSE }, TRUE)) }
test.micro228 <- function() { cat('228\n'); stopifnot(identical({ !NA }, NA)) }
test.micro229 <- function() { cat('229\n'); stopifnot(identical({ !c(TRUE,TRUE,FALSE,NA) }, c(FALSE, FALSE, TRUE, NA))) }
test.micro230 <- function() { cat('230\n'); stopifnot(identical({ !c(1,2,3,4,0,0,NA) }, c(FALSE, FALSE, FALSE, FALSE, TRUE, TRUE, NA))) }
test.micro231 <- function() { cat('231\n'); stopifnot(identical({ !((0-3):3) }, c(FALSE, FALSE, FALSE, TRUE, FALSE, FALSE, FALSE))) }
test.micro232 <- function() { cat('232\n'); stopifnot(identical({ f <- function(arg) { !arg } ; f(as.raw(10)) ; f(as.raw(1:3)) }, as.raw(c(0xfe, 0xfd, 0xfc)))) }
test.micro233 <- function() { cat('233\n'); stopifnot(identical({ a <- as.raw(201) ; !a }, as.raw(0x36))) }
test.micro234 <- function() { cat('234\n'); stopifnot(identical({ a <- as.raw(12) ; !a }, as.raw(0xf3))) }
test.micro235 <- function() { cat('235\n'); stopifnot(identical({ l <- list(); !l }, logical(0))) }
test.micro236 <- function() { cat('236\n'); stopifnot(identical({ f <- function(arg) { !arg } ; f(as.raw(10)) ; f(as.raw(c(a=1,b=2))) }, as.raw(c(0xfe, 0xfd)))) }
test.micro239 <- function() { cat('239\n'); stopifnot(identical({ -(0/0) }, NaN)) }
test.micro240 <- function() { cat('240\n'); stopifnot(identical({ -(1/0) }, -Inf)) }
test.micro241 <- function() { cat('241\n'); stopifnot(identical({ -(1[2]) }, NA_real_)) }
test.micro242 <- function() { cat('242\n'); stopifnot(identical({ -(2+1i) }, -2-1i)) }
test.micro243 <- function() { cat('243\n'); stopifnot(identical({ -((0+1i)/0) }, complex(real=NaN, i=-Inf))) }
test.micro244 <- function() { cat('244\n'); stopifnot(identical({ -((1+0i)/0) }, complex(real=-Inf, i=NaN))) }
test.micro245 <- function() { cat('245\n'); stopifnot(identical({ -c((1+0i)/0,2) }, c(complex(real=-Inf, i=NaN), -2+0i))) }
test.micro246 <- function() { cat('246\n'); stopifnot(identical({ f <- function(z) { -z } ; f(1+1i) ; f(1L) }, -1L)) }
test.micro247 <- function() { cat('247\n'); stopifnot(identical({ f <- function(z) { -z } ; f(TRUE) ; f(1L) }, -1L)) }
test.micro248 <- function() { cat('248\n'); stopifnot(identical({ f <- function(z) { -z } ; f(1L) ; f(1) }, -1)) }
test.micro249 <- function() { cat('249\n'); stopifnot(identical({ f <- function(z) { -z } ; f(1) ; f(1L) }, -1L)) }
test.micro250 <- function() { cat('250\n'); stopifnot(identical({ f <- function(z) { -z } ; f(1L) ; f(1+1i) }, -1-1i)) }
test.micro251 <- function() { cat('251\n'); stopifnot(identical({ f <- function(z) { -z } ; f(1L) ; f(TRUE) }, -1L)) }
test.micro252 <- function() { cat('252\n'); stopifnot(identical({ f <- function(z) { -z } ; f(1:3) ; f(1L) }, -1L)) }
test.micro253 <- function() { cat('253\n'); stopifnot(identical({ f <- function(z) { -z } ; f(1:3) ; f(TRUE) }, -1L)) }
test.micro254 <- function() { cat('254\n'); stopifnot(identical({ f <- function(z) { -z } ; f(1:3) ; f(c((0+0i)/0,1+1i)) }, c(complex(real=NaN, i=NaN), -1-1i))) }
test.micro259 <- function() { cat('259\n'); stopifnot(identical({ x <- 1:3 %*% 9:11 ; x[1] }, 62)) }
test.micro278 <- function() { cat('278\n'); stopifnot(identical({ 1.1 || 3.15 }, TRUE)) }
test.micro279 <- function() { cat('279\n'); stopifnot(identical({ 0 || 0 }, FALSE)) }
test.micro280 <- function() { cat('280\n'); stopifnot(identical({ 1 || 0 }, TRUE)) }
test.micro281 <- function() { cat('281\n'); stopifnot(identical({ NA || 1 }, TRUE)) }
test.micro282 <- function() { cat('282\n'); stopifnot(identical({ NA || 0 }, NA)) }
test.micro283 <- function() { cat('283\n'); stopifnot(identical({ 0 || NA }, NA)) }
test.micro284 <- function() { cat('284\n'); stopifnot(identical({ x <- 1 ; f <- function(r) { x <<- 2; r } ; NA || f(NA) ; x }, 2)) }
test.micro286 <- function() { cat('286\n'); stopifnot(identical({ TRUE && FALSE }, FALSE)) }
test.micro287 <- function() { cat('287\n'); stopifnot(identical({ FALSE && FALSE }, FALSE)) }
test.micro288 <- function() { cat('288\n'); stopifnot(identical({ FALSE && TRUE }, FALSE)) }
test.micro289 <- function() { cat('289\n'); stopifnot(identical({ TRUE && TRUE }, TRUE)) }
test.micro290 <- function() { cat('290\n'); stopifnot(identical({ TRUE && NA }, NA)) }
test.micro291 <- function() { cat('291\n'); stopifnot(identical({ FALSE && NA }, FALSE)) }
test.micro292 <- function() { cat('292\n'); stopifnot(identical({ NA && TRUE }, NA)) }
test.micro293 <- function() { cat('293\n'); stopifnot(identical({ NA && FALSE }, FALSE)) }
test.micro294 <- function() { cat('294\n'); stopifnot(identical({ NA && NA }, NA)) }
test.micro297 <- function() { cat('297\n'); stopifnot(identical({ f <- function(a,b) { a || b } ; f(1,2) ; f(1,2) ; f(1L,2L) }, TRUE)) }
test.micro298 <- function() { cat('298\n'); stopifnot(identical({ f <- function(a,b) { a || b } ; f(1L,2L) ; f(1L,2L) ; f(0,FALSE) }, FALSE)) }
test.micro299 <- function() { cat('299\n'); stopifnot(identical({ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), TRUE) }, TRUE)) }
test.micro300 <- function() { cat('300\n'); stopifnot(identical({ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) }, NA)) }
test.micro301 <- function() { cat('301\n'); stopifnot(identical({ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(1,2) }, TRUE)) }
test.micro302 <- function() { cat('302\n'); stopifnot(identical({ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(double(),2) }, NA)) }
test.micro303 <- function() { cat('303\n'); stopifnot(identical({ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(integer(),2) }, NA)) }
test.micro304 <- function() { cat('304\n'); stopifnot(identical({ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(2+3i,1/0) }, TRUE)) }
test.micro305 <- function() { cat('305\n'); stopifnot(identical({ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(2+3i,logical()) }, NA)) }
test.micro306 <- function() { cat('306\n'); stopifnot(identical({ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(1:3,4:10) ; f(1,2) ; f(logical(),4) }, NA)) }
test.micro307 <- function() { cat('307\n'); stopifnot(identical({ f <- function(a,b) { a && b } ;  f(c(TRUE, FALSE), logical()) ; f(TRUE, c(TRUE,TRUE,FALSE)) ; f(1,2) }, TRUE)) }
test.micro308 <- function() { cat('308\n'); stopifnot(identical({ FALSE && "hello" }, FALSE)) }
test.micro309 <- function() { cat('309\n'); stopifnot(identical({ TRUE || "hello" }, TRUE)) }
test.micro310 <- function() { cat('310\n'); stopifnot(identical({ c(TRUE,FALSE) | logical() }, logical(0))) }
test.micro311 <- function() { cat('311\n'); stopifnot(identical({ logical() | c(TRUE,FALSE) }, logical(0))) }
test.micro312 <- function() { cat('312\n'); stopifnot(identical({ as.raw(c(1,4)) | raw() }, raw(0))) }
test.micro314 <- function() { cat('314\n'); stopifnot(identical({ as.raw(c(1,4)) | as.raw(c(1,5,4)) }, as.raw(c(0x01, 0x05, 0x05)))) }
test.micro315 <- function() { cat('315\n'); stopifnot(identical({ as.raw(c(1,5,4)) | as.raw(c(1,4)) }, as.raw(c(0x01, 0x05, 0x05)))) }
test.micro316 <- function() { cat('316\n'); stopifnot(identical({ c(TRUE, FALSE, FALSE) & c(TRUE,TRUE) }, c(TRUE, FALSE, FALSE))) }
test.micro317 <- function() { cat('317\n'); stopifnot(identical({ c(TRUE, TRUE) & c(TRUE, FALSE, FALSE) }, c(TRUE, FALSE, FALSE))) }
test.micro318 <- function() { cat('318\n'); stopifnot(identical({ c(a=TRUE, TRUE) | c(TRUE, b=FALSE, FALSE) }, structure(c(TRUE, TRUE, TRUE), .Names = c("", "b", "")))) }
test.micro319 <- function() { cat('319\n'); stopifnot(identical({ 1.1 | 3.15 }, TRUE)) }
test.micro320 <- function() { cat('320\n'); stopifnot(identical({ 0 | 0 }, FALSE)) }
test.micro321 <- function() { cat('321\n'); stopifnot(identical({ 1 | 0 }, TRUE)) }
test.micro322 <- function() { cat('322\n'); stopifnot(identical({ NA | 1 }, TRUE)) }
test.micro323 <- function() { cat('323\n'); stopifnot(identical({ NA | 0 }, NA)) }
test.micro324 <- function() { cat('324\n'); stopifnot(identical({ 0 | NA }, NA)) }
test.micro325 <- function() { cat('325\n'); stopifnot(identical({ x <- 1 ; f <- function(r) { x <<- 2; r } ; NA | f(NA) ; x }, 2)) }
test.micro326 <- function() { cat('326\n'); stopifnot(identical({ x <- 1 ; f <- function(r) { x <<- 2; r } ; TRUE | f(FALSE) ; x }, 2)) }
test.micro327 <- function() { cat('327\n'); stopifnot(identical({ TRUE & FALSE }, FALSE)) }
test.micro328 <- function() { cat('328\n'); stopifnot(identical({ FALSE & FALSE }, FALSE)) }
test.micro329 <- function() { cat('329\n'); stopifnot(identical({ FALSE & TRUE }, FALSE)) }
test.micro330 <- function() { cat('330\n'); stopifnot(identical({ TRUE & TRUE }, TRUE)) }
test.micro331 <- function() { cat('331\n'); stopifnot(identical({ TRUE & NA }, NA)) }
test.micro332 <- function() { cat('332\n'); stopifnot(identical({ FALSE & NA }, FALSE)) }
test.micro333 <- function() { cat('333\n'); stopifnot(identical({ NA & TRUE }, NA)) }
test.micro334 <- function() { cat('334\n'); stopifnot(identical({ NA & FALSE }, FALSE)) }
test.micro335 <- function() { cat('335\n'); stopifnot(identical({ NA & NA }, NA)) }
test.micro336 <- function() { cat('336\n'); stopifnot(identical({ x <- 1 ; f <- function(r) { x <<- 2; r } ; NA & f(NA) ; x }, 2)) }
test.micro337 <- function() { cat('337\n'); stopifnot(identical({ x <- 1 ; f <- function(r) { x <<- 2; r } ; FALSE & f(FALSE) ; x }, 2)) }
test.micro338 <- function() { cat('338\n'); stopifnot(identical({ 1:4 & c(FALSE,TRUE) }, c(FALSE, TRUE, FALSE, TRUE))) }
test.micro339 <- function() { cat('339\n'); stopifnot(identical({ 1+2i | 0 }, TRUE)) }
test.micro340 <- function() { cat('340\n'); stopifnot(identical({ 1+2i & 0 }, FALSE)) }
test.micro341 <- function() { cat('341\n'); stopifnot(identical({ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(FALSE, FALSE) }, FALSE)) }
test.micro342 <- function() { cat('342\n'); stopifnot(identical({ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(as.raw(10), as.raw(11)) }, as.raw(0x0a))) }
test.micro343 <- function() { cat('343\n'); stopifnot(identical({ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, 0L) }, FALSE)) }
test.micro344 <- function() { cat('344\n'); stopifnot(identical({ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, 0) }, FALSE)) }
test.micro345 <- function() { cat('345\n'); stopifnot(identical({ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, TRUE) }, TRUE)) }
test.micro346 <- function() { cat('346\n'); stopifnot(identical({ f <- function(a,b) { a & b } ; f(TRUE, 1L) ; f(1L, 3+4i) }, TRUE)) }
test.micro347 <- function() { cat('347\n'); stopifnot(identical({ f <- function(a,b) { a & b } ; f(TRUE, FALSE) ; f(1L, 3+4i) }, TRUE)) }
test.micro348 <- function() { cat('348\n'); stopifnot(identical({ f <- function(a,b) { a & b } ; f(TRUE, FALSE) ; f(TRUE, 3+4i) }, TRUE)) }
test.micro349 <- function() { cat('349\n'); stopifnot(identical({ f <- function(a,b) { a | b } ; f(c(TRUE, FALSE), FALSE) ; f(1L, 3+4i) }, TRUE)) }
test.micro350 <- function() { cat('350\n'); stopifnot(identical({ f <- function(a,b) { a | b } ; f(c(TRUE, FALSE), FALSE) ; f(c(FALSE,FALSE), 3+4i) }, c(TRUE, TRUE))) }
test.micro351 <- function() { cat('351\n'); stopifnot(identical({ f <- function(a,b) { a | b } ; f(as.raw(c(1,4)), as.raw(3)) ; f(4, FALSE) }, TRUE)) }
test.micro352 <- function() { cat('352\n'); stopifnot(identical({ a <- as.raw(200) ; b <- as.raw(255) ; a | b }, as.raw(0xff))) }
test.micro353 <- function() { cat('353\n'); stopifnot(identical({ a <- as.raw(200) ; b <- as.raw(1) ; a | b }, as.raw(0xc9))) }
test.micro354 <- function() { cat('354\n'); stopifnot(identical({ a <- as.raw(201) ; b <- as.raw(1) ; a & b }, as.raw(0x01))) }
test.micro355 <- function() { cat('355\n'); stopifnot(identical({ x <- 2147483647L ; x + 1L }, NA_integer_)) }
test.micro356 <- function() { cat('356\n'); stopifnot(identical({ x <- 2147483647L ; x * x }, NA_integer_)) }
test.micro357 <- function() { cat('357\n'); stopifnot(identical({ x <- -2147483647L ; x - 2L }, NA_integer_)) }
test.micro358 <- function() { cat('358\n'); stopifnot(identical({ x <- -2147483647L ; x - 1L }, NA_integer_)) }
test.micro359 <- function() { cat('359\n'); stopifnot(identical({ 3L %/% 0L }, NA_integer_)) }
test.micro360 <- function() { cat('360\n'); stopifnot(identical({ 3L %% 0L }, NA_integer_)) }
test.micro361 <- function() { cat('361\n'); stopifnot(identical({ c(3L,3L) %/% 0L }, c(NA_integer_, NA_integer_))) }
test.micro362 <- function() { cat('362\n'); stopifnot(identical({ c(3L,3L) %% 0L }, c(NA_integer_, NA_integer_))) }
test.micro363 <- function() { cat('363\n'); stopifnot(identical({ 2147483647L + 1:3 }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro364 <- function() { cat('364\n'); stopifnot(identical({ 2147483647L + c(1L,2L,3L) }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro365 <- function() { cat('365\n'); stopifnot(identical({ 1:3 + 2147483647L }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro366 <- function() { cat('366\n'); stopifnot(identical({ c(1L,2L,3L) + 2147483647L }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro367 <- function() { cat('367\n'); stopifnot(identical({ 1:3 + c(2147483647L,2147483647L,2147483647L) }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro368 <- function() { cat('368\n'); stopifnot(identical({ c(2147483647L,2147483647L,2147483647L) + 1:3 }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro369 <- function() { cat('369\n'); stopifnot(identical({ c(1L,2L,3L) + c(2147483647L,2147483647L,2147483647L) }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro370 <- function() { cat('370\n'); stopifnot(identical({ c(2147483647L,2147483647L,2147483647L) + c(1L,2L,3L) }, c(NA_integer_, NA_integer_, NA_integer_))) }
test.micro371 <- function() { cat('371\n'); stopifnot(identical({ 1:4 + c(2147483647L,2147483647L) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro372 <- function() { cat('372\n'); stopifnot(identical({ c(2147483647L,2147483647L) + 1:4 }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro373 <- function() { cat('373\n'); stopifnot(identical({ c(1L,2L,3L,4L) + c(2147483647L,2147483647L) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro374 <- function() { cat('374\n'); stopifnot(identical({ c(2147483647L,2147483647L) + c(1L,2L,3L,4L) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro375 <- function() { cat('375\n'); stopifnot(identical({ x <- 3 ; f <- function(z) { if (z) { x <- 1 } ; x <- x + 1L ; x } ; f(FALSE) }, 4)) }
test.micro376 <- function() { cat('376\n'); stopifnot(identical({ x <- 3 ; f <- function(z) { if (z) { x <- 1 } ; x <- 1L + x ; x } ; f(FALSE) }, 4)) }
test.micro377 <- function() { cat('377\n'); stopifnot(identical({ x <- 3 ; f <- function(z) { if (z) { x <- 1 } ; x <- x - 1L ; x } ; f(FALSE) }, 2)) }
test.micro378 <- function() { cat('378\n'); stopifnot(identical({ a = array(); length(a) == 1; }, TRUE)) }
test.micro379 <- function() { cat('379\n'); stopifnot(identical({ a = array(); is.na(a[1]); }, TRUE)) }
test.micro380 <- function() { cat('380\n'); stopifnot(identical({ a <- array(); dim(a) == 1; }, TRUE)) }
test.micro381 <- function() { cat('381\n'); stopifnot(identical({ a = array(1:10, dim = c(2,6)); length(a) == 12; }, TRUE)) }
test.micro382 <- function() { cat('382\n'); stopifnot(identical({ length(array(dim=c(1,0,2,3))) == 0; }, TRUE)) }
test.micro383 <- function() { cat('383\n'); stopifnot(identical({ a = dim(array(dim=c(2.1,2.9,3.1,4.7))); a[1] == 2 && a[2] == 2 && a[3] == 3 && a[4] == 4; }, TRUE)) }
test.micro384 <- function() { cat('384\n'); stopifnot(identical({ length(matrix()) == 1; }, TRUE)) }
test.micro385 <- function() { cat('385\n'); stopifnot(identical({ a = array(1:27,c(3,3,3)); a[1,1,1] == 1 && a[3,3,3] == 27 && a[1,2,3] == 22 && a[3,2,1] == 6; }, TRUE)) }
test.micro386 <- function() { cat('386\n'); stopifnot(identical({ a = array(1:27, c(3,3,3)); b = a[,,]; d = dim(b); d[1] == 3 && d[2] == 3 && d[3] == 3; }, TRUE)) }
test.micro387 <- function() { cat('387\n'); stopifnot(identical({ a = array(1,c(3,3,3)); a = dim(a[,1,]); length(a) == 2 && a[1] == 3 && a[2] == 3; }, TRUE)) }
test.micro388 <- function() { cat('388\n'); stopifnot(identical({ a = array(1,c(3,3,3)); is.null(dim(a[1,1,1])); }, TRUE)) }
test.micro389 <- function() { cat('389\n'); stopifnot(identical({ a = array(1,c(3,3,3)); is.null(dim(a[1,1,])); }, TRUE)) }
test.micro390 <- function() { cat('390\n'); stopifnot(identical({ a = array(1,c(3,3,3)); a = dim(a[1,1,1, drop = FALSE]); length(a) == 3 && a[1] == 1 && a[2] == 1 && a[3] == 1; }, TRUE)) }
test.micro391 <- function() { cat('391\n'); stopifnot(identical({ m <- array(1:4, dim=c(4,1,1)) ; x <- m[[2,1,1,drop=FALSE]] ; is.null(dim(x)) }, TRUE)) }
test.micro392 <- function() { cat('392\n'); stopifnot(identical({ a = array(1:27, c(3,3,3)); a[1] == 1 && a[27] == 27 && a[22] == 22 && a[6] == 6; }, TRUE)) }
test.micro393 <- function() { cat('393\n'); stopifnot(identical({ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,1] ; x[1] == 1 && x[2] == 2 }, TRUE)) }
test.micro394 <- function() { cat('394\n'); stopifnot(identical({ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- dim(m[1:2,1,1]) ; is.null(x) }, TRUE)) }
test.micro395 <- function() { cat('395\n'); stopifnot(identical({ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- dim(m[1:2,1,1,drop=FALSE]) ; x[1] == 2 && x[2] == 1 && x[3] == 1 }, TRUE)) }
test.micro396 <- function() { cat('396\n'); stopifnot(identical({ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,integer()] ; d <- dim(x) ; length(x) == 0 }, TRUE)) }
test.micro397 <- function() { cat('397\n'); stopifnot(identical({ m <- array(c(1,2,3), dim=c(3,1,1)) ; x <- m[1:2,1,integer()] ; d <- dim(x) ; d[1] == 2 && d[2] == 0 }, TRUE)) }
test.micro398 <- function() { cat('398\n'); stopifnot(identical({ array(1,c(3,3,3))[1,1,1] == 1; }, TRUE)) }
test.micro399 <- function() { cat('399\n'); stopifnot(identical({ array(1,c(3,3,3))[[1,1,1]] == 1; }, TRUE)) }
test.micro402 <- function() { cat('402\n'); stopifnot(identical({ m <- array(1:24, dim=c(2,3,4)) ; f <- function(i) { m[,,i] } ; f(1) ; f(2) ; dim(f(1:2)) }, c(2L, 3L, 2L))) }
test.micro404 <- function() { cat('404\n'); stopifnot(identical({ matrix(1,3,3)[1,1] == 1; }, TRUE)) }
test.micro405 <- function() { cat('405\n'); stopifnot(identical({ matrix(1,3,3)[[1,1]] == 1; }, TRUE)) }
test.micro406 <- function() { cat('406\n'); stopifnot(identical({  m <- matrix(1:6, nrow=2) ;  m[1,NULL] }, integer(0))) }
test.micro407 <- function() { cat('407\n'); stopifnot(identical({ a = matrix(1,2,2); a[1,2] = 3; a[1,2] == 3; }, TRUE)) }
test.micro408 <- function() { cat('408\n'); stopifnot(identical({ a = array(1,c(3,3,3)); a[1,2,3] = 3; a[1,2,3] == 3; }, TRUE)) }
test.micro409 <- function() { cat('409\n'); stopifnot(identical({ a = array(1,c(3,3,3)); (a[1,2,3] = 3) == 3; }, TRUE)) }
test.micro410 <- function() { cat('410\n'); stopifnot(identical({ a = array(1,c(3,3,3)); b = a; b[1,2,3] = 3; a[1,2,3] == 1 && b[1,2,3] == 3; }, TRUE)) }
test.micro411 <- function() { cat('411\n'); stopifnot(identical({ x <- array(c(1,2,3), dim=c(3,1,1)) ; x[1:2,1,1] <- sqrt(x[2:1]) ; x[1] == sqrt(2) && x[2] == 1 && x[3] == 3 }, TRUE)) }
test.micro412 <- function() { cat('412\n'); stopifnot(identical({ a = array(TRUE,c(3,3,3)); a[1,2,3] = 8L; a[1,2,3] == 8L; }, TRUE)) }
test.micro413 <- function() { cat('413\n'); stopifnot(identical({ a = array(TRUE,c(3,3,3)); a[1,2,3] = 8.1; a[1,2,3] == 8.1; }, TRUE)) }
test.micro414 <- function() { cat('414\n'); stopifnot(identical({ a = array(1L,c(3,3,3)); a[1,2,3] = 8.1; a[1,2,3] == 8.1; }, TRUE)) }
test.micro415 <- function() { cat('415\n'); stopifnot(identical({ a = array(TRUE,c(3,3,3)); a[1,2,3] = 2+3i; a[1,2,3] == 2+3i; }, TRUE)) }
test.micro416 <- function() { cat('416\n'); stopifnot(identical({ a = array(1L,c(3,3,3)); a[1,2,3] = 2+3i; a[1,2,3] == 2+3i; }, TRUE)) }
test.micro417 <- function() { cat('417\n'); stopifnot(identical({ a = array(1.3,c(3,3,3)); a[1,2,3] = 2+3i; a[1,2,3] == 2+3i; }, TRUE)) }
test.micro418 <- function() { cat('418\n'); stopifnot(identical({ a = array(TRUE,c(3,3,3)); a[1,2,3] = "2+3i"; a[1,2,3] == "2+3i" && a[1,1,1] == "TRUE"; }, TRUE)) }
test.micro419 <- function() { cat('419\n'); stopifnot(identical({ a = array(1L,c(3,3,3)); a[1,2,3] = "2+3i"; a[1,2,3] == "2+3i" && a[1,1,1] == "1L"; }, FALSE)) }
test.micro420 <- function() { cat('420\n'); stopifnot(identical({ a = array(1.5,c(3,3,3)); a[1,2,3] = "2+3i"; a[1,2,3] == "2+3i" && a[1,1,1] == "1.5"; }, TRUE)) }
test.micro421 <- function() { cat('421\n'); stopifnot(identical({ a = array(7L,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == 1L && a[1,1,1] == 7L; }, TRUE)) }
test.micro422 <- function() { cat('422\n'); stopifnot(identical({ a = array(1.7,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == 1 && a[1,1,1] == 1.7; }, TRUE)) }
test.micro423 <- function() { cat('423\n'); stopifnot(identical({ a = array(3+2i,c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == 1 && a[1,1,1] == 3+2i; }, TRUE)) }
test.micro424 <- function() { cat('424\n'); stopifnot(identical({ a = array("3+2i",c(3,3,3)); b = TRUE; a[1,2,3] = b; a[1,2,3] == "TRUE" && a[1,1,1] == "3+2i"; }, TRUE)) }
test.micro425 <- function() { cat('425\n'); stopifnot(identical({ a = array(1.7,c(3,3,3)); b = 3L; a[1,2,3] = b; a[1,2,3] == 3 && a[1,1,1] == 1.7; }, TRUE)) }
test.micro426 <- function() { cat('426\n'); stopifnot(identical({ a = array(3+2i,c(3,3,3)); b = 4L; a[1,2,3] = b; a[1,2,3] == 4 && a[1,1,1] == 3+2i; }, TRUE)) }
test.micro427 <- function() { cat('427\n'); stopifnot(identical({ m <- array(c(1+1i,2+2i,3+3i), dim=c(3,1,1)) ; m[1:2,1,1] <- c(100L,101L) ; m ; m[1,1,1] == 100 && m[2,1,1] == 101 }, TRUE)) }
test.micro428 <- function() { cat('428\n'); stopifnot(identical({ a = array("3+2i",c(3,3,3)); b = 7L; a[1,2,3] = b; a[1,2,3] == "7L" && a[1,1,1] == "3+2i"; }, FALSE)) }
test.micro429 <- function() { cat('429\n'); stopifnot(identical({ a = array(3+2i,c(3,3,3)); b = 4.2; a[1,2,3] = b; a[1,2,3] == 4.2 && a[1,1,1] == 3+2i; }, TRUE)) }
test.micro430 <- function() { cat('430\n'); stopifnot(identical({ a = array("3+2i",c(3,3,3)); b = 2+3i; a[1,2,3] = b; a[1,2,3] == "2.0+3.0i" && a[1,1,1] == "3+2i"; }, FALSE)) }
test.micro431 <- function() { cat('431\n'); stopifnot(identical({ a = matrix(1,3,3); a[1,] = c(3,4,5); a[1,1] == 3 && a[1,2] == 4 && a[1,3] == 5; }, TRUE)) }
test.micro432 <- function() { cat('432\n'); stopifnot(identical({ a = matrix(1,3,3); a[,1] = c(3,4,5); a[1,1] == 3 && a[2,1] == 4 && a[3,1] == 5; }, TRUE)) }
test.micro433 <- function() { cat('433\n'); stopifnot(identical({ a = array(1,c(3,3,3)); a[1,1,] = c(3,4,5); a[1,1,1] == 3 && a[1,1,2] == 4 && a[1,1,3] == 5; }, TRUE)) }
test.micro434 <- function() { cat('434\n'); stopifnot(identical({ a = array(1,c(3,3,3)); a[1,,1] = c(3,4,5); a[1,1,1] == 3 && a[1,2,1] == 4 && a[1,3,1] == 5; }, TRUE)) }
test.micro435 <- function() { cat('435\n'); stopifnot(identical({ a = array(1,c(3,3,3)); a[,1,1] = c(3,4,5); a[1,1,1] == 3 && a[2,1,1] == 4 && a[3,1,1] == 5; }, TRUE)) }
test.micro436 <- function() { cat('436\n'); stopifnot(identical({ a = array(1,c(3,3,3)); a[1,,] = matrix(1:9,3,3); a[1,1,1] == 1 && a[1,3,1] == 3 && a[1,3,3] == 9; }, TRUE)) }
test.micro437 <- function() { cat('437\n'); stopifnot(identical({ m <- array(1:3, dim=c(3,1,1)) ; f <- function(x,v) { x[1:2,1,1] <- v ; x } ; f(m,10L) ; f(m,10) ; f(m,c(11L,12L)); m[1,1,1] == 1L && m[2,1,1] == 2L && m[3,1,1] == 3L }, TRUE)) }
test.micro438 <- function() { cat('438\n'); stopifnot(identical({ a = matrix(1,3,3); is.null(dim(a[1,])); }, TRUE)) }
test.micro450 <- function() { cat('450\n'); stopifnot(identical({ m <- matrix(1:4,nrow=2) ; m[2,2,drop=TRUE] }, 4L)) }
test.micro453 <- function() { cat('453\n'); stopifnot(identical({ m <- matrix(1:4,nrow=2) ; m[,2,drop=TRUE] }, 3:4)) }
test.micro459 <- function() { cat('459\n'); stopifnot(identical({ f <- function(b,x,y) { b[1:2,2:2,drop=TRUE] } ; f(matrix(1:4,nrow=2)) }, 3:4)) }
test.micro460 <- function() { cat('460\n'); stopifnot(identical({ f <- function(b,x,y) { b[1:1,2:1,drop=TRUE] } ; f(matrix(1:4,nrow=2)) }, c(3L, 1L))) }
test.micro463 <- function() { cat('463\n'); stopifnot(identical({ f <- function(b,x,y) { b[1e100:1e100,2:2] } ; f(matrix(1:4,nrow=2)) }, NA_integer_)) }
test.micro464 <- function() { cat('464\n'); stopifnot(identical({ f <- function(b,x,y) { b[-2L:-2L,2:2] } ; f(matrix(1:4,nrow=2)) }, 3L)) }
test.micro465 <- function() { cat('465\n'); stopifnot(identical({ f <- function(b,x,y) { b[TRUE:FALSE,2:2] } ; f(matrix(1:4,nrow=2)) }, 3L)) }
test.micro466 <- function() { cat('466\n'); stopifnot(identical({ f <- function(b,x,y) { b[[2,1]] } ; f(matrix(1:4,nrow=2)) }, 2L)) }
test.micro467 <- function() { cat('467\n'); stopifnot(identical({ f <- function(b,x,y) { b[[2,1]] } ; f(matrix(as.list(1:4),nrow=2)) }, 2L)) }
test.micro469 <- function() { cat('469\n'); stopifnot(identical({ f <- function(d) { b <- matrix(1:4,nrow=2,ncol=2) ; b[,drop=d,2] } ; f(0) ; f(1L) }, 3:4)) }
test.micro470 <- function() { cat('470\n'); stopifnot(identical({ z <- 1 ; f <- function(d) { b <- matrix(1:4,nrow=2,ncol=2) ; b[{z<<-z+1;1},drop=z<<-z*10,{z<<-z*2;2}] } ; f(0) ; f(1L) ; z }, 820)) }
test.micro471 <- function() { cat('471\n'); stopifnot(identical({ b <- 1:4 ; dim(b) <- c(1,1,4,1); x <- b[,,,1] ; x }, 1:4)) }
test.micro477 <- function() { cat('477\n'); stopifnot(identical({ x <- list(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,1L) ; f(x,2) }, 2)) }
test.micro478 <- function() { cat('478\n'); stopifnot(identical({ x <- c(1,2,3) ; dim(x) <- c(3,1,1) ; f <- function(b,i) { b[[i,1,1]] } ; f(x,TRUE) }, 1)) }
test.micro479 <- function() { cat('479\n'); stopifnot(identical({ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4,-6), 1) }, c(2, 3))) }
test.micro480 <- function() { cat('480\n'); stopifnot(identical({ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4,-6), 1) ; f(x,c(-2,-4,-4,-6), 1) }, c(1, 3))) }
test.micro481 <- function() { cat('481\n'); stopifnot(identical({ x <- c(1,2,3) ; dim(x) <- c(1,3,1) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-4,-4,-6), 1) ; y <- 1:8 ; dim(y) <- c(1,8,1) ; f(y,c(-2,-4,-4,-6,-8), 1) }, c(1L, 3L, 5L, 7L))) }
test.micro483 <- function() { cat('483\n'); stopifnot(identical({ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(-1,-1),c(-3,-3,-3,-4)) }, c(2, 4))) }
test.micro484 <- function() { cat('484\n'); stopifnot(identical({ x <- c(1,2,3,4) ; dim(x) <- c(1,2,2) ; f <- function(b,i,j) { b[1,i,j] } ; f(x,TRUE,1) ; f(x,c(TRUE,FALSE),c(NA)) }, c(NA_real_, NA_real_))) }
test.micro500 <- function() { cat('500\n'); stopifnot(identical({  m <- array(1:3, dim=c(3,1,1)) ; f <- function(x,v) { x[[2,1,1]] <- v ; x } ; f(m,10L) ; f(m,10) ; x <- f(m,11L) ; x[1] == 1 && x[2] == 11 && x[3] == 3 }, TRUE)) }
test.micro501 <- function() { cat('501\n'); stopifnot(identical({ m <- matrix(1:100, nrow=10) ; z <- 1; s <- 0 ; for(i in 1:3) { m[z <- z + 1,z <- z + 1] <- z * z * 1000 } ; sum(m) }, 39918)) }
test.micro502 <- function() { cat('502\n'); stopifnot(identical({ m <- matrix(as.double(1:6), nrow=2) ; mi <- matrix(1:6, nrow=2) ; f <- function(v,i,j) { v[i,j] <- 100 ; v[i,j] * i * j } ; f(m, 1L, 2L) ; f(m,1L,TRUE)  }, c(100, 100, 100))) }
test.micro503 <- function() { cat('503\n'); stopifnot(identical({ m <- matrix(as.double(1:6), nrow=2) ; mi <- matrix(1:6, nrow=2) ; f <- function(v,i,j) { v[i,j] <- 100 ; v[i,j] * i * j } ; f(m, 1L, 2L) ; f(m,1L,-1)  }, c(-100, -100))) }
test.micro519 <- function() { cat('519\n'); stopifnot(identical({ x <- list(1+2i,3+4i,5+6i,4+5i) ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- as.raw(c(1,2)) ; unlist(x) }, c(1+2i, 1+0i, 5+6i, 2+0i))) }
test.micro539 <- function() { cat('539\n'); stopifnot(identical({ r <- 0 ; for (i in 1:5 ) { x <- c(11:14) ; if (i==2 || i==3) { x <- c(1,2,10+1i,100) } ; dim(x) <- c(2,1,2) ; x[2,1,1:2] <- c(15L*i,-12L+i) ; r <- r + sum(x) } ; r }, 274+2i)) }
test.micro561 <- function() { cat('561\n'); stopifnot(identical({ x <- c(1L,3L,4L,NA) ; dim(x) <- c(2,1,2); x[[2,1,1]] <- list(10+1i); x[2] }, list(list(10+1i)))) }
test.micro564 <- function() { cat('564\n'); stopifnot(identical({ x <- c("a","b","c","d") ; dim(x) <- c(2,1,2); x[2,1,1] <- list(1); dim(x) <- NULL; x }, list("a", 1, "c", "d"))) }
test.micro572 <- function() { cat('572\n'); stopifnot(identical({ x <- list(1,10,-1/0,0/0) ; dim(x) <- c(2,1,2); f <- function(v) { x[[2,1,1]] <- v ; x } ; f(list(TRUE)) ; z <- f(NA) ; unlist(z) }, c(1, NA, -Inf, NaN))) }
test.micro578 <- function() { cat('578\n'); stopifnot(identical({ for(i in 1:2) { if (i==1) { b <- as.list(11:14) } else { b <- c(1/0,-3/0,0/0,4) }; dim(b) <- c(2,1,2); b[[2,1,1]] <- list(111) } ; dim(b) <- NULL ; b }, list(Inf, list(111), NaN, 4))) }
test.micro580 <- function() { cat('580\n'); stopifnot(identical({ for(i in 1:2) { x <- 1:4 ; dim(x) <- c(1,1,4); if (i==2) { z <- x } ; x[,,1] <- 12L } ; as.integer(z) }, 1:4)) }
test.micro581 <- function() { cat('581\n'); stopifnot(identical({ x <- 1:4 ; dim(x) <- c(1,1,4); x[,,NA] <- 12L ; as.integer(x) }, 1:4)) }
test.micro582 <- function() { cat('582\n'); stopifnot(identical({ for (i in 1:3) { if (i==1) { z <- 1 } ; if (i==2) { z <- c(-1,-1) } ; x <- 1:4 ; dim(x) <- c(1,1,4)  ; x[,,z] <- 12L } ; as.integer(x) }, c(1L, 12L, 12L, 12L))) }
test.micro592 <- function() { cat('592\n'); stopifnot(identical({ for (i in c(1,-1)) { x <- 1:4 ; dim(x) <- c(2,2) ; x[i,2] <- list(12) } ; unlist(x) }, c(1, 2, 3, 12))) }
test.micro625 <- function() { cat('625\n'); stopifnot(identical({ for(b in list(as.raw(11:14),list(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- as.raw(3:4)[2:1] } ; dim(b) <- NULL ; b }, list(as.raw(0x03), as.raw(0x04), NA, NA))) }
test.micro626 <- function() { cat('626\n'); stopifnot(identical({ for(b in list(as.list(11:14),c(TRUE,FALSE,NA,NA))) { dim(b) <- c(2,2,1) ; b[2:1,1,1] <- list(1,2) } ; dim(b) <- NULL ; b }, list(2, 1, NA, NA))) }
test.micro632 <- function() { cat('632\n'); stopifnot(identical({ typeof({ for(v in list(as.list(13:14),c(TRUE,NA))) { x <- list(1,1L,TRUE,NA) ; dim(x) <- c(2,2,1) ; x[2,1:2,1] <- v } ; x }) }, "list")) }
test.micro699 <- function() { cat('699\n'); stopifnot(identical({ typeof({ for(b in list(1:4, list(TRUE,1, 3+4i, 0/0))) { dim(b) <- c(2,2,1); b[2:1,1,1] <- list(1L,TRUE) };  b }) }, "list")) }
test.micro701 <- function() { cat('701\n'); stopifnot(identical({ for(b in list(list(TRUE,1, 3+4i, 0/0), 1:4)) { dim(b) <- c(2,2,1); s <- b ; b[2:1,1,1] <- list(1L,TRUE) };  dim(b) <- NULL ; b }, list(TRUE, 1L, 3L, 4L))) }
test.micro711 <- function() { cat('711\n'); stopifnot(identical({ l <- quote(x <- 1) ; f <- function() { eval(l) } ; x <- 10 ; f() ; x }, 10)) }
test.micro712 <- function() { cat('712\n'); stopifnot(identical({ l <- quote(x <- 1) ; f <- function() { eval(l) ; x <<- 10 ; get("x") } ; f() }, 1)) }
test.micro713 <- function() { cat('713\n'); stopifnot(identical({ a<-1 }, 1)) }
test.micro714 <- function() { cat('714\n'); stopifnot(identical({ a<-FALSE ; b<-a }, FALSE)) }
test.micro716 <- function() { cat('716\n'); stopifnot(identical({ x <<- 1 }, 1)) }
test.micro717 <- function() { cat('717\n'); stopifnot(identical({ x <<- 1 ; x }, 1)) }
test.micro718 <- function() { cat('718\n'); stopifnot(identical({ f <- function() { x <<- 2 } ; f() ; x }, 2)) }
test.micro719 <- function() { cat('719\n'); stopifnot(identical({ x <- 10 ; f <- function() { x <<- 2 } ; f() ; x }, 2)) }
test.micro720 <- function() { cat('720\n'); stopifnot(identical({ x <- 10 ; f <- function() { x <<- 2 ; x } ; c(f(), f()) }, c(2, 2))) }
test.micro721 <- function() { cat('721\n'); stopifnot(identical({ x <- 10 ; f <- function() { x <- x ; x <<- 2 ; x } ; c(f(), f()) }, c(10, 2))) }
test.micro722 <- function() { cat('722\n'); stopifnot(identical({ x <- 10 ; g <- function() { f <- function() { x <- x ; x <<- 2 ; x } ; c(f(), f()) } ; g() }, c(10, 2))) }
test.micro723 <- function() { cat('723\n'); stopifnot(identical({ x <- 10 ; g <- function() { x ; f <- function() { x <- x ; x <<- 2 ; x } ; c(f(), f()) } ; g() }, c(10, 2))) }
test.micro724 <- function() { cat('724\n'); stopifnot(identical({ x <- 10 ; g <- function() { x <- 100 ; f <- function() { x <- x ; x <<- 2 ; x } ; c(f(), f()) } ; g() }, c(100, 2))) }
test.micro725 <- function() { cat('725\n'); stopifnot(identical({ h <- function() { x <- 10 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { x <<- 3 ; x } ; f() } ; g() } ; h() }, 3)) }
test.micro726 <- function() { cat('726\n'); stopifnot(identical({ x <- 3 ; f <- function() { assign("x", 4) ; h <- function() { assign("z", 5) ; g <- function() { x <<- 10 ; x } ; g() } ; h() } ; f() ; x }, 3)) }
test.micro727 <- function() { cat('727\n'); stopifnot(identical({ f <- function(i) { if (i==1) { c <- 1 } ; c } ; f(1) ; typeof(f(2)) }, "builtin")) }
test.micro728 <- function() { cat('728\n'); stopifnot(identical({ f <- function(i) { if (i==1) { c <- 1 ; x <- 1 } ; if (i!=2) { x } else { c }} ; f(1) ; f(1) ; typeof(f(2)) }, "builtin")) }
test.micro729 <- function() { cat('729\n'); stopifnot(identical({ x <- 3 ; f <- function() { assign("x", 4) ; g <- function() { assign("y", 3) ; hh <- function() { assign("z", 6) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x } ; h() } ; hh() } ; g()  } ; f() }, 4)) }
test.micro730 <- function() { cat('730\n'); stopifnot(identical({ f <- function() { if (FALSE) { c <- 1 } ; g <- function() { c } ; g() } ; typeof(f()) }, "builtin")) }
test.micro742 <- function() { cat('742\n'); stopifnot(identical({ x <- 1:2;  attr(x, "hi") <- 2 ;  x+1:4 }, c(2L, 4L, 4L, 6L))) }
test.micro746 <- function() { cat('746\n'); stopifnot(identical({ x <- 1+1i;  attr(x, "hi") <- 1+2 ; y <- 2:3 ;  x+y }, c(3+1i, 4+1i))) }
test.micro748 <- function() { cat('748\n'); stopifnot(identical({ x <- c(a=1) ; y <- c(b=2,c=3) ; x + y }, structure(c(3, 4), .Names = c("b", "c")))) }
test.micro749 <- function() { cat('749\n'); stopifnot(identical({ x <- c(a=1) ; y <- c(b=2,c=3) ; y + x }, structure(c(3, 4), .Names = c("b", "c")))) }
test.micro755 <- function() { cat('755\n'); stopifnot(identical({ x <- 1:2;  attr(x, "hi") <- 2 ;  x & x }, c(TRUE, TRUE))) }
test.micro756 <- function() { cat('756\n'); stopifnot(identical({ x <- as.raw(1:2);  attr(x, "hi") <- 2 ;  x & x }, as.raw(c(0x01, 0x02)))) }
test.micro757 <- function() { cat('757\n'); stopifnot(identical({ x <- 1:2 ;  attr(x, "hi") <- 2 ;  !x  }, c(FALSE, FALSE))) }
test.micro759 <- function() { cat('759\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, "myatt") <- 1 ; as.character(x) }, c("1", "2"))) }
test.micro760 <- function() { cat('760\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, "myatt") <- 1 ; as.double(x) }, c(1, 2))) }
test.micro761 <- function() { cat('761\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, "myatt") <- 1 ; as.integer(x) }, 1:2)) }
test.micro762 <- function() { cat('762\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, "myatt") <- 1; x[c(1,1)] }, structure(c(1, 1), .Names = c("a", "a")))) }
test.micro772 <- function() { cat('772\n'); stopifnot(identical({ x <- c(a=1) ; attr(x, "myatt") <- 1 ; lapply(1:2, function(z) {x}) }, list(structure(1, .Names = "a", myatt = 1), structure(1, .Names = "a", myatt = 1)))) }
test.micro776 <- function() { cat('776\n'); stopifnot(identical({ x <- 1 ; attr(x, "myatt") <- 1; x:x }, 1L)) }
test.micro777 <- function() { cat('777\n'); stopifnot(identical({ x <- 1 ; attr(x, "myatt") <- 1; c(x, x, x) }, c(1, 1, 1))) }
test.micro778 <- function() { cat('778\n'); stopifnot(identical({ x <- 1 ; attr(x, "myatt") <- 1; cumsum(c(x, x, x)) }, c(1, 2, 3))) }
test.micro780 <- function() { cat('780\n'); stopifnot(identical({ m <- matrix(c(1,1,1,1), nrow=2) ; attr(m,"a") <- 1 ;  r <- eigen(m) ; r$vectors <- round(r$vectors, digits=5) ; r  }, structure(list(values = c(2, 0), vectors = structure(c(0.70711, 0.70711, -0.70711, 0.70711), .Dim = c(2L, 2L))), .Names = c("values", "vectors")))) }
test.micro782 <- function() { cat('782\n'); stopifnot(identical({ x <- 1 ; attr(x, "myatt") <- 1; min(x) }, 1)) }
test.micro784 <- function() { cat('784\n'); stopifnot(identical({ x <- c(a=1) ; attr(x, "myatt") <- 1; nchar(x) }, structure(1L, .Names = "a"))) }
test.micro786 <- function() { cat('786\n'); stopifnot(identical({ x <- 1 ; attr(x, "myatt") <- 1; rep(x,2) }, c(1, 1))) }
test.micro787 <- function() { cat('787\n'); stopifnot(identical({ x <- c(a=TRUE) ; attr(x, "myatt") <- 1; rep(x,2) }, structure(c(TRUE, TRUE), .Names = c("a", "a")))) }
test.micro788 <- function() { cat('788\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, "myatt") <- 1; rev(x) }, structure(c(2, 1), .Names = c("b", "a")))) }
test.micro789 <- function() { cat('789\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, "myatt") <- 1; seq(x) }, 1:2)) }
test.micro790 <- function() { cat('790\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, "myatt") <- 1; order(x) }, 1:2)) }
test.micro792 <- function() { cat('792\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, "myatt") <- 1; sum(x) }, 3)) }
test.micro797 <- function() { cat('797\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, "myatt") <- 1; unlist(list(x,x)) }, structure(c(1, 2, 1, 2), .Names = c("a", "b", "a", "b")))) }
test.micro798 <- function() { cat('798\n'); stopifnot(identical({ x <- 1:2;  attr(x, "hi") <- 2 ;  x == x }, c(TRUE, TRUE))) }
test.micro799 <- function() { cat('799\n'); stopifnot(identical({ as.integer(c(1,2,3)) }, 1:3)) }
test.micro800 <- function() { cat('800\n'); stopifnot(identical({ as.integer(list(c(1),2,3)) }, 1:3)) }
test.micro801 <- function() { cat('801\n'); stopifnot(identical({ as.integer(list(integer(),2,3)) }, c(NA, 2L, 3L))) }
test.micro802 <- function() { cat('802\n'); stopifnot(identical({ as.integer(list(list(1),2,3)) }, c(NA, 2L, 3L))) }
test.micro803 <- function() { cat('803\n'); stopifnot(identical({ as.integer(list(1,2,3,list())) }, c(1L, 2L, 3L, NA))) }
test.micro804 <- function() { cat('804\n'); stopifnot(identical({ m<-matrix(1:6, nrow=3) ; as.integer(m) }, 1:6)) }
test.micro805 <- function() { cat('805\n'); stopifnot(identical({ m<-matrix(1:6, nrow=3) ; as.vector(m, 'any') }, 1:6)) }
test.micro806 <- function() { cat('806\n'); stopifnot(identical({ m<-matrix(1:6, nrow=3) ; as.vector(mode = 'integer', x=m) }, 1:6)) }
test.micro807 <- function() { cat('807\n'); stopifnot(identical({ as.vector(list(1,2,3), mode='integer') }, 1:3)) }
test.micro808 <- function() { cat('808\n'); stopifnot(identical({ as.double('1.27') }, 1.27)) }
test.micro809 <- function() { cat('809\n'); stopifnot(identical({ as.double(1L) }, 1)) }
test.micro810 <- function() { cat('810\n'); stopifnot(identical({ as.double('TRUE') }, NA_real_)) }
test.micro811 <- function() { cat('811\n'); stopifnot(identical({ as.double(c('1','hello')) }, c(1, NA))) }
test.micro812 <- function() { cat('812\n'); stopifnot(identical({ as.character(1L) }, "1")) }
test.micro813 <- function() { cat('813\n'); stopifnot(identical({ as.character(TRUE) }, "TRUE")) }
test.micro814 <- function() { cat('814\n'); stopifnot(identical({ as.character(1:3) }, c("1", "2", "3"))) }
test.micro815 <- function() { cat('815\n'); stopifnot(identical({ as.character(NULL) }, character(0))) }
test.micro816 <- function() { cat('816\n'); stopifnot(identical({ as.character(list(c('hello', 'hi'))) }, "c(\"hello\", \"hi\")")) }
test.micro817 <- function() { cat('817\n'); stopifnot(identical({ as.character(list(list(c('hello', 'hi')))) }, "list(c(\"hello\", \"hi\"))")) }
test.micro818 <- function() { cat('818\n'); stopifnot(identical({ as.character(list(1,2,3)) }, c("1", "2", "3"))) }
test.micro819 <- function() { cat('819\n'); stopifnot(identical({ as.character(list(c(2L, 3L))) }, "2:3")) }
test.micro820 <- function() { cat('820\n'); stopifnot(identical({ as.character(list(c(2L, 3L, 5L))) }, "c(2, 3, 5)")) }
test.micro821 <- function() { cat('821\n'); stopifnot(identical({ as.raw(list(1,2,3)) }, as.raw(c(0x01, 0x02, 0x03)))) }
test.micro822 <- function() { cat('822\n'); stopifnot(identical({ as.raw(list('1', 2L, 3.4)) }, as.raw(c(0x01, 0x02, 0x03)))) }
test.micro823 <- function() { cat('823\n'); stopifnot(identical({ as.raw(c(1,1000,NA)) }, as.raw(c(0x01, 0x00, 0x00)))) }
test.micro824 <- function() { cat('824\n'); stopifnot(identical({ as.logical(1) }, TRUE)) }
test.micro825 <- function() { cat('825\n'); stopifnot(identical({ as.logical('false') }, FALSE)) }
test.micro826 <- function() { cat('826\n'); stopifnot(identical({ as.logical('dummy') }, NA)) }
test.micro827 <- function() { cat('827\n'); stopifnot(identical({ as.complex(0) }, 0+0i)) }
test.micro828 <- function() { cat('828\n'); stopifnot(identical({ as.complex(TRUE) }, 1+0i)) }
test.micro829 <- function() { cat('829\n'); stopifnot(identical({ as.complex('1+5i') }, 1+5i)) }
test.micro830 <- function() { cat('830\n'); stopifnot(identical({ as.complex('1e10+5i') }, 1e+10+5e+00i)) }
test.micro831 <- function() { cat('831\n'); stopifnot(identical({ as.complex('-1+5i') }, -1+5i)) }
test.micro832 <- function() { cat('832\n'); stopifnot(identical({ as.complex('-1-5i') }, -1-5i)) }
test.micro833 <- function() { cat('833\n'); stopifnot(identical({ as.complex('-.1e10+5i') }, -1e+09+5e+00i)) }
test.micro834 <- function() { cat('834\n'); stopifnot(identical({ as.complex('1e-2+3i') }, 0.01+3i)) }
test.micro835 <- function() { cat('835\n'); stopifnot(identical({ as.complex('+.1e+2-3i') }, 10-3i)) }
test.micro836 <- function() { cat('836\n'); stopifnot(identical({ as.complex(0/0) }, NA_complex_)) }
test.micro837 <- function() { cat('837\n'); stopifnot(identical({ as.complex(c(0/0, 0/0)) }, c(NA_complex_, NA_complex_))) }
test.micro839 <- function() { cat('839\n'); stopifnot(identical({ l <- 1 ; attr(l, 'my') <- 1; as.list(l) }, list(1))) }
test.micro840 <- function() { cat('840\n'); stopifnot(identical({ l <- c(x=1) ; as.list(l) }, structure(list(x = 1), .Names = "x"))) }
test.micro841 <- function() { cat('841\n'); stopifnot(identical({ as.complex(as.character(c(1+1i,1+1i))) }, c(1+1i, 1+1i))) }
test.micro842 <- function() { cat('842\n'); stopifnot(identical({ as.complex(as.double(c(1+1i,1+1i))) }, c(1+0i, 1+0i))) }
test.micro843 <- function() { cat('843\n'); stopifnot(identical({ as.complex(as.integer(c(1+1i,1+1i))) }, c(1+0i, 1+0i))) }
test.micro844 <- function() { cat('844\n'); stopifnot(identical({ as.complex(as.logical(c(1+1i,1+1i))) }, c(1+0i, 1+0i))) }
test.micro845 <- function() { cat('845\n'); stopifnot(identical({ as.complex(as.raw(c(1+1i,1+1i))) }, c(1+0i, 1+0i))) }
test.micro846 <- function() { cat('846\n'); stopifnot(identical({ as.double(as.logical(c(10,10))) }, c(1, 1))) }
test.micro847 <- function() { cat('847\n'); stopifnot(identical({ as.integer(as.logical(-1:1)) }, c(1L, 0L, 1L))) }
test.micro848 <- function() { cat('848\n'); stopifnot(identical({ as.raw(as.logical(as.raw(c(1,2)))) }, as.raw(c(0x01, 0x01)))) }
test.micro849 <- function() { cat('849\n'); stopifnot(identical({ as.character(as.double(1:5)) }, c("1", "2", "3", "4", "5"))) }
test.micro850 <- function() { cat('850\n'); stopifnot(identical({ as.character(as.complex(1:2)) }, c("1+0i", "2+0i"))) }
test.micro851 <- function() { cat('851\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; as.double(m) }, c(1, 2, 3, 4, 5, 6))) }
test.micro852 <- function() { cat('852\n'); stopifnot(identical({ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; as.integer(m) }, 1:6)) }
test.micro853 <- function() { cat('853\n'); stopifnot(identical({ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; as.logical(m) }, c(TRUE, TRUE, TRUE, TRUE, TRUE, TRUE))) }
test.micro854 <- function() { cat('854\n'); stopifnot(identical({ x <- 1:2; names(x) <- c('hello','hi') ; as.double(x) }, c(1, 2))) }
test.micro855 <- function() { cat('855\n'); stopifnot(identical({ x <- c(1,2); names(x) <- c('hello','hi') ; as.integer(x) }, 1:2)) }
test.micro856 <- function() { cat('856\n'); stopifnot(identical({ x <- c(0,2); names(x) <- c('hello','hi') ; as.logical(x) }, c(FALSE, TRUE))) }
test.micro859 <- function() { cat('859\n'); stopifnot(identical({ x <- 1:3; z <- as.matrix(x); x }, 1:3)) }
test.micro860 <- function() { cat('860\n'); stopifnot(identical({ x <- 1:3 ; attr(x,'my') <- 10 ; attributes(as.matrix(x)) }, structure(list(dim = c(3L, 1L)), .Names = "dim"))) }
test.micro861 <- function() { cat('861\n'); stopifnot(identical({ as.raw('09') }, as.raw(0x09))) }
test.micro862 <- function() { cat('862\n'); stopifnot(identical({ as.raw('077') }, as.raw(0x4d))) }
test.micro863 <- function() { cat('863\n'); stopifnot(identical({ as.raw('0004') }, as.raw(0x04))) }
test.micro864 <- function() { cat('864\n'); stopifnot(identical({ 5L:10L }, 5:10)) }
test.micro865 <- function() { cat('865\n'); stopifnot(identical({ 5L:(0L-5L) }, c(5L, 4L, 3L, 2L, 1L, 0L, -1L, -2L, -3L, -4L, -5L))) }
test.micro866 <- function() { cat('866\n'); stopifnot(identical({ 1:10 }, 1:10)) }
test.micro867 <- function() { cat('867\n'); stopifnot(identical({ 1:(0-10) }, c(1L, 0L, -1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L))) }
test.micro868 <- function() { cat('868\n'); stopifnot(identical({ 1L:(0-10) }, c(1L, 0L, -1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L))) }
test.micro869 <- function() { cat('869\n'); stopifnot(identical({ 1:(0L-10L) }, c(1L, 0L, -1L, -2L, -3L, -4L, -5L, -6L, -7L, -8L, -9L, -10L))) }
test.micro870 <- function() { cat('870\n'); stopifnot(identical({ (0-12):1.5 }, -12:1)) }
test.micro871 <- function() { cat('871\n'); stopifnot(identical({ 1.5:(0-12) }, c(1.5, 0.5, -0.5, -1.5, -2.5, -3.5, -4.5, -5.5, -6.5, -7.5, -8.5, -9.5, -10.5, -11.5))) }
test.micro872 <- function() { cat('872\n'); stopifnot(identical({ (0-1.5):(0-12) }, c(-1.5, -2.5, -3.5, -4.5, -5.5, -6.5, -7.5, -8.5, -9.5, -10.5, -11.5))) }
test.micro873 <- function() { cat('873\n'); stopifnot(identical({ 10:1 }, c(10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L))) }
test.micro874 <- function() { cat('874\n'); stopifnot(identical({ (0-5):(0-9) }, c(-5L, -6L, -7L, -8L, -9L))) }
test.micro875 <- function() { cat('875\n'); stopifnot(identical({ seq(1,10) }, 1:10)) }
test.micro876 <- function() { cat('876\n'); stopifnot(identical({ seq(10,1) }, c(10L, 9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L))) }
test.micro877 <- function() { cat('877\n'); stopifnot(identical({ seq(from=1,to=3) }, 1:3)) }
test.micro878 <- function() { cat('878\n'); stopifnot(identical({ seq(to=-1,from=-10) }, -10:-1)) }
test.micro879 <- function() { cat('879\n'); stopifnot(identical({ seq(length.out=13.4) }, 1:14)) }
test.micro880 <- function() { cat('880\n'); stopifnot(identical({ seq(length.out=0) }, integer(0))) }
test.micro881 <- function() { cat('881\n'); stopifnot(identical({ seq(length.out=1) }, 1L)) }
test.micro882 <- function() { cat('882\n'); stopifnot(identical({ seq(along.with=10) }, 1L)) }
test.micro883 <- function() { cat('883\n'); stopifnot(identical({ seq(along.with=NA) }, 1L)) }
test.micro884 <- function() { cat('884\n'); stopifnot(identical({ seq(along.with=1:10) }, 1:10)) }
test.micro885 <- function() { cat('885\n'); stopifnot(identical({ seq(along.with=-3:-5) }, 1:3)) }
test.micro886 <- function() { cat('886\n'); stopifnot(identical({ seq(from=1.4) }, 1L)) }
test.micro887 <- function() { cat('887\n'); stopifnot(identical({ seq(from=1.7) }, 1L)) }
test.micro888 <- function() { cat('888\n'); stopifnot(identical({ seq(from=10:12) }, 1:3)) }
test.micro889 <- function() { cat('889\n'); stopifnot(identical({ seq(from=c(TRUE, FALSE)) }, 1:2)) }
test.micro890 <- function() { cat('890\n'); stopifnot(identical({ seq(from=TRUE, to=TRUE, length.out=0) }, integer(0))) }
test.micro891 <- function() { cat('891\n'); stopifnot(identical({ round(seq(from=10.5, to=15.4, length.out=4), digits=5) }, c(10.5, 12.13333, 13.76667, 15.4))) }
test.micro892 <- function() { cat('892\n'); stopifnot(identical({ seq(from=11, to=12, length.out=2) }, c(11, 12))) }
test.micro893 <- function() { cat('893\n'); stopifnot(identical({ seq(from=1,to=3,by=1) }, c(1, 2, 3))) }
test.micro894 <- function() { cat('894\n'); stopifnot(identical({ seq(from=-10,to=-5,by=2) }, c(-10, -8, -6))) }
test.micro895 <- function() { cat('895\n'); stopifnot(identical({ seq(from=-10.4,to=-5.8,by=2.1) }, c(-10.4, -8.3, -6.2))) }
test.micro896 <- function() { cat('896\n'); stopifnot(identical({ round(seq(from=3L,to=-2L,by=-4.2), digits=5) }, c(3, -1.2))) }
test.micro897 <- function() { cat('897\n'); stopifnot(identical({ seq(along=c(10,11,12)) }, 1:3)) }
test.micro898 <- function() { cat('898\n'); stopifnot(identical({ seq(1L,4L,2L) }, c(1L, 3L))) }
test.micro899 <- function() { cat('899\n'); stopifnot(identical({ seq(1,-4,-2) }, c(1, -1, -3))) }
test.micro900 <- function() { cat('900\n'); stopifnot(identical({ integer() }, integer(0))) }
test.micro901 <- function() { cat('901\n'); stopifnot(identical({ double() }, numeric(0))) }
test.micro902 <- function() { cat('902\n'); stopifnot(identical({ logical() }, logical(0))) }
test.micro903 <- function() { cat('903\n'); stopifnot(identical({ double(3) }, c(0, 0, 0))) }
test.micro904 <- function() { cat('904\n'); stopifnot(identical({ logical(3L) }, c(FALSE, FALSE, FALSE))) }
test.micro905 <- function() { cat('905\n'); stopifnot(identical({ character(1L) }, "")) }
test.micro906 <- function() { cat('906\n'); stopifnot(identical({ max(1:10, 100:200, c(4.0, 5.0)) }, 200)) }
test.micro907 <- function() { cat('907\n'); stopifnot(identical({ max((-1):100) }, 100L)) }
test.micro908 <- function() { cat('908\n'); stopifnot(identical({ max(1:10, 100:200, c(4.0, 5.0), c(TRUE,FALSE,NA)) }, NA_real_)) }
test.micro909 <- function() { cat('909\n'); stopifnot(identical({ max(2L, 4L) }, 4L)) }
test.micro910 <- function() { cat('910\n'); stopifnot(identical({ max() }, -Inf)) }
test.micro911 <- function() { cat('911\n'); stopifnot(identical({ max(c('hi','abbey','hello')) }, "hi")) }
test.micro912 <- function() { cat('912\n'); stopifnot(identical({ max('hi','abbey','hello') }, "hi")) }
test.micro913 <- function() { cat('913\n'); stopifnot(identical({ min((-1):100) }, -1L)) }
test.micro914 <- function() { cat('914\n'); stopifnot(identical({ min(1:10, 100:200, c(4.0, -5.0)) }, -5)) }
test.micro915 <- function() { cat('915\n'); stopifnot(identical({ min(1:10, 100:200, c(4.0, 5.0), c(TRUE,FALSE,NA)) }, NA_real_)) }
test.micro916 <- function() { cat('916\n'); stopifnot(identical({ min(2L, 4L) }, 2L)) }
test.micro917 <- function() { cat('917\n'); stopifnot(identical({ min() }, Inf)) }
test.micro918 <- function() { cat('918\n'); stopifnot(identical({ min(c('hi','abbey','hello')) }, "abbey")) }
test.micro919 <- function() { cat('919\n'); stopifnot(identical({ min('hi','abbey','hello') }, "abbey")) }
test.micro920 <- function() { cat('920\n'); stopifnot(identical({ min('hi',100) }, "100")) }
test.micro921 <- function() { cat('921\n'); stopifnot(identical({ min(c(1,2,0/0)) }, NaN)) }
test.micro922 <- function() { cat('922\n'); stopifnot(identical({ max(c(1,2,0/0)) }, NaN)) }
test.micro923 <- function() { cat('923\n'); stopifnot(identical({ rep(1,3) }, c(1, 1, 1))) }
test.micro924 <- function() { cat('924\n'); stopifnot(identical({ rep(1:3,2) }, c(1L, 2L, 3L, 1L, 2L, 3L))) }
test.micro925 <- function() { cat('925\n'); stopifnot(identical({ rep(c(1,2),0) }, numeric(0))) }
test.micro926 <- function() { cat('926\n'); stopifnot(identical({ rep(1:3, length.out=4) }, c(1L, 2L, 3L, 1L))) }
test.micro927 <- function() { cat('927\n'); stopifnot(identical({ rep(1:3, length.out=NA) }, 1:3)) }
test.micro928 <- function() { cat('928\n'); stopifnot(identical({ rep(as.raw(14), 4) }, as.raw(c(0x0e, 0x0e, 0x0e, 0x0e)))) }
test.micro929 <- function() { cat('929\n'); stopifnot(identical({ x <- as.raw(11) ; names(x) <- c('X') ; rep(x, 3) }, structure(as.raw(c(0x0b, 0x0b, 0x0b)), .Names = c("X", "X", "X")))) }
test.micro930 <- function() { cat('930\n'); stopifnot(identical({ x <- as.raw(c(11,12)) ; names(x) <- c('X','Y') ; rep(x, 2) }, structure(as.raw(c(0x0b, 0x0c, 0x0b, 0x0c)), .Names = c("X", "Y", "X", "Y")))) }
test.micro931 <- function() { cat('931\n'); stopifnot(identical({ x <- c(TRUE,NA) ; names(x) <- c('X',NA) ; rep(x, length.out=3) }, structure(c(TRUE, NA, TRUE), .Names = c("X", NA, "X")))) }
test.micro933 <- function() { cat('933\n'); stopifnot(identical({ x <- 1 ; names(x) <- c('X') ; rep(x, times=0) }, structure(numeric(0), .Names = character(0)))) }
test.micro934 <- function() { cat('934\n'); stopifnot(identical({ x <- 1+1i ; names(x) <- c('X') ; rep(x, times=2) }, structure(c(1+1i, 1+1i), .Names = c("X", "X")))) }
test.micro935 <- function() { cat('935\n'); stopifnot(identical({ x <- c(1+1i,1+2i) ; names(x) <- c('X') ; rep(x, times=2) }, structure(c(1+1i, 1+2i, 1+1i, 1+2i), .Names = c("X", NA, "X", NA)))) }
test.micro936 <- function() { cat('936\n'); stopifnot(identical({ x <- c('A','B') ; names(x) <- c('X') ; rep(x, length.out=3) }, structure(c("A", "B", "A"), .Names = c("X", NA, "X")))) }
test.micro937 <- function() { cat('937\n'); stopifnot(identical({ c(1.0,1L) }, c(1, 1))) }
test.micro938 <- function() { cat('938\n'); stopifnot(identical({ c(1L,1.0) }, c(1, 1))) }
test.micro939 <- function() { cat('939\n'); stopifnot(identical({ c(TRUE,1L,1.0,list(3,4)) }, list(TRUE, 1L, 1, 3, 4))) }
test.micro940 <- function() { cat('940\n'); stopifnot(identical({ c(TRUE,1L,1.0,list(3,list(4,5))) }, list(TRUE, 1L, 1, 3, list(4, 5)))) }
test.micro943 <- function() { cat('943\n'); stopifnot(identical({ c(NULL,1,2,3) }, c(1, 2, 3))) }
test.micro944 <- function() { cat('944\n'); stopifnot(identical({ f <- function(x,y) { c(x,y) } ; f(1,1) ; f(1, TRUE) }, c(1, 1))) }
test.micro946 <- function() { cat('946\n'); stopifnot(identical({ c('hello', 'hi') }, c("hello", "hi"))) }
test.micro947 <- function() { cat('947\n'); stopifnot(identical({ c(1+1i, as.raw(10)) }, c(1+1i, 10+0i))) }
test.micro948 <- function() { cat('948\n'); stopifnot(identical({ c(as.raw(10), as.raw(20)) }, as.raw(c(0x0a, 0x14)))) }
test.micro949 <- function() { cat('949\n'); stopifnot(identical({ c(x=1,y=2) }, structure(c(1, 2), .Names = c("x", "y")))) }
test.micro950 <- function() { cat('950\n'); stopifnot(identical({ c(x=1,2) }, structure(c(1, 2), .Names = c("x", "")))) }
test.micro951 <- function() { cat('951\n'); stopifnot(identical({ x <- 1:2 ; names(x) <- c('A',NA) ; c(x,test=x) }, structure(c(1L, 2L, 1L, 2L), .Names = c("A", NA, "test.A", "test.NA")))) }
test.micro952 <- function() { cat('952\n'); stopifnot(identical({ c(a=1,b=2:3,list(x=FALSE)) }, structure(list(a = 1, b1 = 2L, b2 = 3L, x = FALSE), .Names = c("a", "b1", "b2", "x")))) }
test.micro953 <- function() { cat('953\n'); stopifnot(identical({ c(1,z=list(1,b=22,3)) }, structure(list(1, z1 = 1, z.b = 22, z3 = 3), .Names = c("", "z1", "z.b", "z3")))) }
test.micro954 <- function() { cat('954\n'); stopifnot(identical({ c(1i,0/0) }, c(0+1i, complex(real=NaN, i=0)))) }
test.micro955 <- function() { cat('955\n'); stopifnot(identical({ is.na(c(1,2,3,4)) }, c(FALSE, FALSE, FALSE, FALSE))) }
test.micro956 <- function() { cat('956\n'); stopifnot(identical({ is.na(1[10]) }, TRUE)) }
test.micro957 <- function() { cat('957\n'); stopifnot(identical({ is.na(c(1[10],2[10],3)) }, c(TRUE, TRUE, FALSE))) }
test.micro958 <- function() { cat('958\n'); stopifnot(identical({ is.na(list(1[10],1L[10],list(),integer())) }, c(TRUE, TRUE, FALSE, FALSE))) }
test.micro959 <- function() { cat('959\n'); stopifnot(identical({ sum(1:6, 3, 4) }, 28)) }
test.micro960 <- function() { cat('960\n'); stopifnot(identical({ sum(1:6, 3L, TRUE) }, 25L)) }
test.micro961 <- function() { cat('961\n'); stopifnot(identical({ sum() }, 0L)) }
test.micro962 <- function() { cat('962\n'); stopifnot(identical({ sum(0, 1[3]) }, NA_real_)) }
test.micro963 <- function() { cat('963\n'); stopifnot(identical({ sum(na.rm=FALSE, 0, 1[3]) }, NA_real_)) }
test.micro964 <- function() { cat('964\n'); stopifnot(identical({ sum(0, na.rm=FALSE, 1[3]) }, NA_real_)) }
test.micro965 <- function() { cat('965\n'); stopifnot(identical({ sum(0, 1[3], na.rm=FALSE) }, NA_real_)) }
test.micro966 <- function() { cat('966\n'); stopifnot(identical({ sum(0, 1[3], na.rm=TRUE) }, 0)) }
test.micro967 <- function() { cat('967\n'); stopifnot(identical({ `sum`(1:10) }, 55L)) }
test.micro968 <- function() { cat('968\n'); stopifnot(identical({ sum(1+1i,2,NA, na.rm=TRUE) }, 3+1i)) }
test.micro969 <- function() { cat('969\n'); stopifnot(identical({ lapply(1:3, function(x) { 2*x }) }, list(2, 4, 6))) }
test.micro970 <- function() { cat('970\n'); stopifnot(identical({ lapply(1:3, function(x,y) { x*y }, 2) }, list(2, 4, 6))) }
test.micro971 <- function() { cat('971\n'); stopifnot(identical({ sapply(1:3,function(x){x*2}) }, c(2, 4, 6))) }
test.micro972 <- function() { cat('972\n'); stopifnot(identical({ sapply(c(1,2,3),function(x){x*2}) }, c(2, 4, 6))) }
test.micro973 <- function() { cat('973\n'); stopifnot(identical({ sapply(list(1,2,3),function(x){x*2}) }, c(2, 4, 6))) }
test.micro974 <- function() { cat('974\n'); stopifnot(identical({ sapply(1:3, function(x) { if (x==1) { 1 } else if (x==2) { integer() } else { TRUE } }) }, list(1, integer(0), TRUE))) }
test.micro975 <- function() { cat('975\n'); stopifnot(identical({ f<-function(g) { sapply(1:3, g) } ; f(function(x) { x*2 }) }, c(2, 4, 6))) }
test.micro976 <- function() { cat('976\n'); stopifnot(identical({ f<-function(g) { sapply(1:3, g) } ; f(function(x) { x*2 }) ; f(function(x) { TRUE }) }, c(TRUE, TRUE, TRUE))) }
test.micro977 <- function() { cat('977\n'); stopifnot(identical({ sapply(1:3, function(x) { if (x==1) { list(1) } else if (x==2) { list(NULL) } else { list(2) } }) }, list(1, NULL, 2))) }
test.micro978 <- function() { cat('978\n'); stopifnot(identical({ sapply(1:3, function(x) { if (x==1) { list(1) } else if (x==2) { list(NULL) } else { list() } }) }, list(list(1), list(NULL), list()))) }
test.micro979 <- function() { cat('979\n'); stopifnot(identical({ f<-function() { x<-2 ; sapply(1, function(i) { x }) } ; f() }, 2)) }
test.micro980 <- function() { cat('980\n'); stopifnot(identical({ sapply(1:3, length) }, c(1L, 1L, 1L))) }
test.micro981 <- function() { cat('981\n'); stopifnot(identical({ f<-length; sapply(1:3, f) }, c(1L, 1L, 1L))) }
test.micro982 <- function() { cat('982\n'); stopifnot(identical({ sapply(1:3, `-`, 2) }, c(-1, 0, 1))) }
test.micro983 <- function() { cat('983\n'); stopifnot(identical({ sapply(1:3, '-', 2) }, c(-1, 0, 1))) }
test.micro984 <- function() { cat('984\n'); stopifnot(identical({ sapply(1:2, function(i) { if (i==1) { as.raw(0) } else { as.raw(10) } }) }, as.raw(c(0x00, 0x0a)))) }
test.micro986 <- function() { cat('986\n'); stopifnot(identical({ sapply(1:2, function(i) { if (i==1) { as.raw(0) } else { 5+10i } }) }, c(0+0i, 5+10i))) }
test.micro990 <- function() { cat('990\n'); stopifnot(identical({ ( sapply(1:3, function(i) { if (i < 3) { list(xxx=1) } else {list(zzz=2)} })) }, structure(list(xxx = 1, xxx = 1, zzz = 2), .Names = c("xxx", "xxx", "zzz")))) }
test.micro991 <- function() { cat('991\n'); stopifnot(identical({ ( sapply(1:3, function(i) { list(xxx=1:i) } )) }, structure(list(xxx = 1L, xxx = 1:2, xxx = 1:3), .Names = c("xxx", "xxx", "xxx")))) }
test.micro992 <- function() { cat('992\n'); stopifnot(identical({ sapply(1:3, function(i) { if (i < 3) { list(xxx=1) } else {list(2)} }) }, structure(list(xxx = 1, xxx = 1, 2), .Names = c("xxx", "xxx", "")))) }
test.micro993 <- function() { cat('993\n'); stopifnot(identical({ ( sapply(1:3, function(i) { if (i < 3) { c(xxx=1) } else {c(2)} })) }, structure(c(1, 1, 2), .Names = c("xxx", "xxx", "")))) }
test.micro994 <- function() { cat('994\n'); stopifnot(identical({ f <- function() { lapply(c(X='a',Y='b'), function(x) { c(a=x) })  } ; f() }, structure(list(X = structure("a", .Names = "a"), Y = structure("b", .Names = "a")), .Names = c("X", "Y")))) }
test.micro995 <- function() { cat('995\n'); stopifnot(identical({ f <- function() { sapply(c(1,2), function(x) { c(a=x) })  } ; f() }, structure(c(1, 2), .Names = c("a", "a")))) }
test.micro996 <- function() { cat('996\n'); stopifnot(identical({ f <- function() { sapply(c(X=1,Y=2), function(x) { c(a=x) })  } ; f() }, structure(c(1, 2), .Names = c("X.a", "Y.a")))) }
test.micro997 <- function() { cat('997\n'); stopifnot(identical({ f <- function() { sapply(c('a','b'), function(x) { c(a=x) })  } ; f() }, structure(c("a", "b"), .Names = c("a.a", "b.a")))) }
test.micro998 <- function() { cat('998\n'); stopifnot(identical({ f <- function() { sapply(c(X='a',Y='b'), function(x) { c(a=x) })  } ; f() }, structure(c("a", "b"), .Names = c("X.a", "Y.a")))) }
test.micro999 <- function() { cat('999\n'); stopifnot(identical({ sapply(c('a','b','c'), function(x) { x }) }, structure(c("a", "b", "c"), .Names = c("a", "b", "c")))) }
test.micro1000 <- function() { cat('1000\n'); stopifnot(identical({ sapply(c(a=1,b=2,`c+`=3), function(x) { c(x=x*x) }) }, structure(c(1, 4, 9), .Names = c("a.x", "b.x", "c+.x")))) }
test.micro1001 <- function() { cat('1001\n'); stopifnot(identical({ sapply(c(a=1,2,3,`c+`=3), function(x) { c(x=x*x) }) }, structure(c(1, 4, 9, 9), .Names = c("a.x", "x", "x", "c+.x")))) }
test.micro1002 <- function() { cat('1002\n'); stopifnot(identical({ sapply(c(a=1,2,3,`c+`=3), function(x) { c(x*x) }) }, structure(c(1, 4, 9, 9), .Names = c("a", "", "", "c+")))) }
test.micro1003 <- function() { cat('1003\n'); stopifnot(identical({ sapply(c(a=1,2,3,`c+`=3), function(x) { rep(x,x) }) }, structure(list(a = 1, c(2, 2), c(3, 3, 3), "c+" = c(3, 3, 3)), .Names = c("a", "", "", "c+")))) }
test.micro1004 <- function() { cat('1004\n'); stopifnot(identical({ l <- (sapply(c(a=1,2,3,`c+`=4), function(i) { if (i==1) { c(x=5) } else if (i==2) {c(z=5) } else if (i==3) { c(1) } else { list(`c+`=3) } })) ; names(l) }, c("a.x", "z", "", "c+.c+"))) }
test.micro1005 <- function() { cat('1005\n'); stopifnot(identical({ l <- (sapply(c(a=1,2,3,`c+`=4), function(i) { if (i==1) { c(x=5) } else if (i==2) {c(z=5) } else if (i==3) { c(1) } else { list(`c+`=3,d=5) } })) ; l }, structure(list(a = structure(5, .Names = "x"), structure(5, .Names = "z"),     1, "c+" = structure(list("c+" = 3, d = 5), .Names = c("c+",     "d"))), .Names = c("a", "", "", "c+")))) }
test.micro1006 <- function() { cat('1006\n'); stopifnot(identical({ l <- (sapply(c(a=1,2,3,`c+`=4), function(i) { if (i==1) { list(x=5) } else if (i==2) {list(z=5) } else if (i==3) { list(1) } else { list(`c+`=3) } })) ; l }, structure(list(a.x = 5, z = 5, 1, "c+.c+" = 3), .Names = c("a.x", "z", "", "c+.c+")))) }
test.micro1007 <- function() { cat('1007\n'); stopifnot(identical({ sapply(c(a=1,2,3,`c+`=3), function(x) { as.raw(x) }) }, structure(as.raw(c(0x01, 0x02, 0x03, 0x03)), .Names = c("a", "", "", "c+")))) }
test.micro1008 <- function() { cat('1008\n'); stopifnot(identical({ sapply(c(a=1,2,3,`c+`=3), function(x) { list(z=NULL) }) }, structure(list(a.z = NULL, z = NULL, z = NULL, "c+.z" = NULL), .Names = c("a.z", "z", "z", "c+.z")))) }
test.micro1009 <- function() { cat('1009\n'); stopifnot(identical({ sapply(c(a=1,2,3,`c+`=3), function(x) { NULL }) }, structure(list(a = NULL, NULL, NULL, "c+" = NULL), .Names = c("a", "", "", "c+")))) }
test.micro1014 <- function() { cat('1014\n'); stopifnot(identical({ l <- list(as.raw(11), TRUE, 2L, 3, 4+1i, 'a') ; sapply(1:6, function(i) { l[[i]] } ) }, c("0b", "TRUE", "2", "3", "4+1i", "a"))) }
test.micro1015 <- function() { cat('1015\n'); stopifnot(identical({ l <- list(as.raw(11), TRUE, 2L, 3, 4+1i) ; sapply(1:5, function(i) { l[[i]] } ) }, c(11+0i, 1+0i, 2+0i, 3+0i, 4+1i))) }
test.micro1016 <- function() { cat('1016\n'); stopifnot(identical({ l <- list(as.raw(11), TRUE, 2L, 4) ; sapply(1:4, function(i) { l[[i]] } ) }, c(11, 1, 2, 4))) }
test.micro1017 <- function() { cat('1017\n'); stopifnot(identical({ l <- list(as.raw(11), TRUE, 2L) ; sapply(1:3, function(i) { l[[i]] } ) }, c(11L, 1L, 2L))) }
test.micro1018 <- function() { cat('1018\n'); stopifnot(identical({ l <- list(as.raw(11), TRUE) ; sapply(1:2, function(i) { l[[i]] } ) }, c(TRUE, TRUE))) }
test.micro1019 <- function() { cat('1019\n'); stopifnot(identical({ sapply(1:3, function(i) { rep(i, i+1) }) }, list(c(1L, 1L), c(2L, 2L, 2L), c(3L, 3L, 3L, 3L)))) }
test.micro1020 <- function() { cat('1020\n'); stopifnot(identical({ for (z in list(1:3,list(1L,5,10))) { x <- sapply(1:3, function(i,z) { i+z[[i]] }, z) } ; x }, c(2, 7, 13))) }
test.micro1021 <- function() { cat('1021\n'); stopifnot(identical({ for (z in list(list(list(10,11),list(11,12)),1:2)) { x <- sapply(1:2, function(i,z) { z[[i]] }, z) } ; x }, 1:2)) }
test.micro1022 <- function() { cat('1022\n'); stopifnot(identical({ for (z in list(list(1:2,1:3),list(list(10,11),list(11,12)),1:2)) { x <- sapply(1:2, function(i,z) { z[[i]] }, z) } ; x }, 1:2)) }
test.micro1023 <- function() { cat('1023\n'); stopifnot(identical({ for(i in 1:2) { x <- sapply(1:2, function(i) { l <- list(1:2,NULL) ; l[[i]] }) } ; x }, list(1:2, NULL))) }
test.micro1024 <- function() { cat('1024\n'); stopifnot(identical({ typeof({ for(i in 1:2) { x <- sapply(1:2, function(i) { l <- list(1:2,list(1,2)) ; l[[i]] }) } ; x }) }, "list")) }
test.micro1026 <- function() { cat('1026\n'); stopifnot(identical({ for (z in list(c(TRUE,FALSE,NA),c(NA,FALSE,FALSE),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) } ; x }, c(1, 2, 3))) }
test.micro1027 <- function() { cat('1027\n'); stopifnot(identical({ for (z in list(c('a','b','x'),c('z','z','y'),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) } ; x }, c(1, 2, 3))) }
test.micro1028 <- function() { cat('1028\n'); stopifnot(identical({ for (z in list(c(1+2i,3+4i,5+6i),c(2+2i,3,4),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) } ; x }, c(1, 2, 3))) }
test.micro1029 <- function() { cat('1029\n'); stopifnot(identical({ y <- NULL ; for (z in list(c(1+2i,3+4i,5+6i),c(2+2i,3,4),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) ; if (is.null(y)) { y <- x } } ; y }, c(1+2i, 3+4i, 5+6i))) }
test.micro1030 <- function() { cat('1030\n'); stopifnot(identical({ y <- NULL ; for (z in list(as.raw(11:13),as.raw(21:23),c(1,2,3))) { x <- sapply(1:3, function(i,z) { z[[i]] }, z) ; if (is.null(y)) { y <- x } } ; y }, as.raw(c(0x0b, 0x0c, 0x0d)))) }
test.micro1031 <- function() { cat('1031\n'); stopifnot(identical({ for (idxs in list(1:3, c(1,2,3), c('a','x','z'))) { x <- sapply(idxs, function(i) { i }) } ; x }, structure(c("a", "x", "z"), .Names = c("a", "x", "z")))) }
test.micro1032 <- function() { cat('1032\n'); stopifnot(identical({ for (idxs in list(1:3, c(1,2,3), c(Z='a','x','z'))) { x <- lapply(idxs, function(i) { i }) } ; x }, structure(list(Z = "a", "x", "z"), .Names = c("Z", "", "")))) }
test.micro1033 <- function() { cat('1033\n'); stopifnot(identical({ for (idxs in list(as.list(1:3), c(1,2,3), c(Z='a','x','z'))) { x <- lapply(idxs, function(i) { i }) } ; x }, structure(list(Z = "a", "x", "z"), .Names = c("Z", "", "")))) }
test.micro1034 <- function() { cat('1034\n'); stopifnot(identical({ for (idxs in list(as.list(1:3), c(1,2,3), list(Z='a','x','z'))) { x <- lapply(idxs, function(i) { i }) } ; x }, structure(list(Z = "a", "x", "z"), .Names = c("Z", "", "")))) }
test.micro1047 <- function() { cat('1047\n'); stopifnot(identical({ `+`(1,2) }, 3)) }
test.micro1048 <- function() { cat('1048\n'); stopifnot(identical({ `-`(1,2) }, -1)) }
test.micro1049 <- function() { cat('1049\n'); stopifnot(identical({ `*`(1,2) }, 2)) }
test.micro1050 <- function() { cat('1050\n'); stopifnot(identical({ `/`(1,2) }, 0.5)) }
test.micro1051 <- function() { cat('1051\n'); stopifnot(identical({ `%/%`(1,2) }, 0)) }
test.micro1052 <- function() { cat('1052\n'); stopifnot(identical({ `%%`(1,2) }, 1)) }
test.micro1053 <- function() { cat('1053\n'); stopifnot(identical({ `^`(1,2) }, 1)) }
test.micro1054 <- function() { cat('1054\n'); stopifnot(identical({ `!`(TRUE) }, FALSE)) }
test.micro1055 <- function() { cat('1055\n'); stopifnot(identical({ `||`(TRUE, FALSE) }, TRUE)) }
test.micro1056 <- function() { cat('1056\n'); stopifnot(identical({ `&&`(TRUE, FALSE) }, FALSE)) }
test.micro1057 <- function() { cat('1057\n'); stopifnot(identical({ `|`(TRUE, FALSE) }, TRUE)) }
test.micro1058 <- function() { cat('1058\n'); stopifnot(identical({ `&`(TRUE, FALSE) }, FALSE)) }
test.micro1061 <- function() { cat('1061\n'); stopifnot(identical({ x <- `+` ; x(2,3) }, 5)) }
test.micro1062 <- function() { cat('1062\n'); stopifnot(identical({ x <- `+` ; f <- function() { x <- 1 ; x(2,3) } ; f() }, 5)) }
test.micro1079 <- function() { cat('1079\n'); stopifnot(identical({ m <- matrix(1:6, nrow=3) ; dim(m) }, c(3L, 2L))) }
test.micro1082 <- function() { cat('1082\n'); stopifnot(identical({ m <- matrix(1:6, nrow=3) ; nrow(m) }, 3L)) }
test.micro1085 <- function() { cat('1085\n'); stopifnot(identical({ m <- matrix(1:6, nrow=3) ; ncol(m) }, 2L)) }
test.micro1087 <- function() { cat('1087\n'); stopifnot(identical({ z <- 1 ; dim(z) <- c(1,1) ; dim(z) <- NULL ; z }, 1)) }
test.micro1090 <- function() { cat('1090\n'); stopifnot(identical({ cumsum(1:10) }, c(1L, 3L, 6L, 10L, 15L, 21L, 28L, 36L, 45L, 55L))) }
test.micro1091 <- function() { cat('1091\n'); stopifnot(identical({ cumsum(c(1,2,3)) }, c(1, 3, 6))) }
test.micro1092 <- function() { cat('1092\n'); stopifnot(identical({ cumsum(rep(1e308, 3) ) }, c(1e+308, Inf, Inf))) }
test.micro1093 <- function() { cat('1093\n'); stopifnot(identical({ cumsum(NA) }, NA_integer_)) }
test.micro1094 <- function() { cat('1094\n'); stopifnot(identical({ cumsum(c(1e308, 1e308, NA, 1, 2)) }, c(1e+308, Inf, NA, NA, NA))) }
test.micro1095 <- function() { cat('1095\n'); stopifnot(identical({ cumsum(c(2000000000L, 2000000000L)) }, c(2000000000L, NA))) }
test.micro1096 <- function() { cat('1096\n'); stopifnot(identical({ cumsum(c(2000000000L, NA, 2000000000L)) }, c(2000000000L, NA, NA))) }
test.micro1097 <- function() { cat('1097\n'); stopifnot(identical({ cumsum(as.logical(-2:2)) }, c(1L, 2L, 2L, 3L, 4L))) }
test.micro1098 <- function() { cat('1098\n'); stopifnot(identical({ cumsum((1:6)*(1+1i)) }, c(1+1i, 3+3i, 6+6i, 10+10i, 15+15i, 21+21i))) }
test.micro1099 <- function() { cat('1099\n'); stopifnot(identical({ cumsum(as.raw(1:6)) }, c(1, 3, 6, 10, 15, 21))) }
test.micro1100 <- function() { cat('1100\n'); stopifnot(identical({ cumsum(c(1,2,3,0/0,5)) }, c(1, 3, 6, NA, NA))) }
test.micro1101 <- function() { cat('1101\n'); stopifnot(identical({ cumsum(c(1,0/0,5+1i)) }, c(1+0i, complex(real=NaN, i=0), complex(real=NaN, i=1)))) }
test.micro1102 <- function() { cat('1102\n'); stopifnot(identical({ which(c(TRUE, FALSE, NA, TRUE)) }, c(1L, 4L))) }
test.micro1103 <- function() { cat('1103\n'); stopifnot(identical({ which(logical()) }, integer(0))) }
test.micro1104 <- function() { cat('1104\n'); stopifnot(identical({ which(c(a=TRUE,b=FALSE,c=TRUE)) }, structure(c(1L, 3L), .Names = c("a", "c")))) }
test.micro1105 <- function() { cat('1105\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; colMeans(m) }, c(1.5, 3.5, 5.5))) }
test.micro1106 <- function() { cat('1106\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; colSums(na.rm = FALSE, x = m) }, c(3, 7, 11))) }
test.micro1107 <- function() { cat('1107\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; rowMeans(x = m, na.rm = TRUE) }, c(3, 4))) }
test.micro1108 <- function() { cat('1108\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; rowSums(x = m) }, c(9, 12))) }
test.micro1109 <- function() { cat('1109\n'); stopifnot(identical({ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; colMeans(m) }, c(1.5, 3.5, 5.5))) }
test.micro1110 <- function() { cat('1110\n'); stopifnot(identical({ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; colSums(m) }, c(3, 7, 11))) }
test.micro1111 <- function() { cat('1111\n'); stopifnot(identical({ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; rowMeans(m) }, c(3, 4))) }
test.micro1112 <- function() { cat('1112\n'); stopifnot(identical({ m <- matrix(c(1,2,3,4,5,6), nrow=2) ; rowSums(m) }, c(9, 12))) }
test.micro1113 <- function() { cat('1113\n'); stopifnot(identical({ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; rowSums(m) }, c(NA, 12))) }
test.micro1114 <- function() { cat('1114\n'); stopifnot(identical({ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; rowSums(m, na.rm = TRUE) }, c(3, 12))) }
test.micro1115 <- function() { cat('1115\n'); stopifnot(identical({ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; rowMeans(m, na.rm = TRUE) }, c(3, 4))) }
test.micro1116 <- function() { cat('1116\n'); stopifnot(identical({ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colSums(m) }, c(NA, 7, NA))) }
test.micro1117 <- function() { cat('1117\n'); stopifnot(identical({ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colSums(na.rm = TRUE, m) }, c(2, 7, 6))) }
test.micro1118 <- function() { cat('1118\n'); stopifnot(identical({ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colMeans(m) }, c(NA, 3.5, NA))) }
test.micro1119 <- function() { cat('1119\n'); stopifnot(identical({ m <- matrix(c(NA,2,3,4,NA,6), nrow=2) ; colMeans(m, na.rm = TRUE) }, c(2, 3.5, 6))) }
test.micro1120 <- function() { cat('1120\n'); stopifnot(identical({ colSums(matrix(as.complex(1:6), nrow=2)) }, c(3+0i, 7+0i, 11+0i))) }
test.micro1121 <- function() { cat('1121\n'); stopifnot(identical({ colSums(matrix((1:6)*(1+1i), nrow=2)) }, c(3+3i, 7+7i, 11+11i))) }
test.micro1122 <- function() { cat('1122\n'); stopifnot(identical({ colMeans(matrix(as.complex(1:6), nrow=2)) }, c(1.5+0i, 3.5+0i, 5.5+0i))) }
test.micro1123 <- function() { cat('1123\n'); stopifnot(identical({ colMeans(matrix((1:6)*(1+1i), nrow=2)) }, c(1.5+1.5i, 3.5+3.5i, 5.5+5.5i))) }
test.micro1124 <- function() { cat('1124\n'); stopifnot(identical({ rowSums(matrix(as.complex(1:6), nrow=2)) }, c(9+0i, 12+0i))) }
test.micro1125 <- function() { cat('1125\n'); stopifnot(identical({ rowSums(matrix((1:6)*(1+1i), nrow=2)) }, c(9+9i, 12+12i))) }
test.micro1126 <- function() { cat('1126\n'); stopifnot(identical({ rowMeans(matrix(as.complex(1:6), nrow=2)) }, c(3+0i, 4+0i))) }
test.micro1127 <- function() { cat('1127\n'); stopifnot(identical({ rowMeans(matrix((1:6)*(1+1i), nrow=2)) }, c(3+3i, 4+4i))) }
test.micro1128 <- function() { cat('1128\n'); stopifnot(identical({ o <- outer(1:3, 1:4, '<') ; colSums(o) }, c(0, 1, 2, 3))) }
test.micro1129 <- function() { cat('1129\n'); stopifnot(identical({ nchar(c('hello', 'hi')) }, c(5L, 2L))) }
test.micro1130 <- function() { cat('1130\n'); stopifnot(identical({ nchar(c('hello', 'hi', 10, 130)) }, c(5L, 2L, 2L, 3L))) }
test.micro1131 <- function() { cat('1131\n'); stopifnot(identical({ nchar(c(10,130)) }, 2:3)) }
test.micro1132 <- function() { cat('1132\n'); stopifnot(identical({ strsplit('helloh', 'h', fixed=TRUE) }, list(c("", "ello")))) }
test.micro1133 <- function() { cat('1133\n'); stopifnot(identical({ strsplit( c('helloh', 'hi'), c('h',''), fixed=TRUE) }, list(c("", "ello"), c("h", "i")))) }
test.micro1134 <- function() { cat('1134\n'); stopifnot(identical({ strsplit('helloh', '', fixed=TRUE) }, list(c("h", "e", "l", "l", "o", "h")))) }
test.micro1135 <- function() { cat('1135\n'); stopifnot(identical({ strsplit('helloh', 'h') }, list(c("", "ello")))) }
test.micro1136 <- function() { cat('1136\n'); stopifnot(identical({ strsplit( c('helloh', 'hi'), c('h','')) }, list(c("", "ello"), c("h", "i")))) }
test.micro1137 <- function() { cat('1137\n'); stopifnot(identical({ strsplit('ahoj', split='') [[c(1,2)]] }, "h")) }
test.micro1138 <- function() { cat('1138\n'); stopifnot(identical({ paste(1:2, 1:3, FALSE, collapse=NULL) }, c("1 1 FALSE", "2 2 FALSE", "1 3 FALSE"))) }
test.micro1139 <- function() { cat('1139\n'); stopifnot(identical({ paste(1:2, 1:3, FALSE, collapse='-', sep='+') }, "1+1+FALSE-2+2+FALSE-1+3+FALSE")) }
test.micro1140 <- function() { cat('1140\n'); stopifnot(identical({ paste() }, character(0))) }
test.micro1141 <- function() { cat('1141\n'); stopifnot(identical({ paste(sep='') }, character(0))) }
test.micro1142 <- function() { cat('1142\n'); stopifnot(identical({ a <- as.raw(200) ; b <- as.raw(255) ; paste(a, b) }, "c8 ff")) }
test.micro1143 <- function() { cat('1143\n'); stopifnot(identical({ file.path('a', 'b', c('d','e','f')) }, c("a/b/d", "a/b/e", "a/b/f"))) }
test.micro1144 <- function() { cat('1144\n'); stopifnot(identical({ file.path() }, character(0))) }
test.micro1145 <- function() { cat('1145\n'); stopifnot(identical({ substr('123456', start=2, stop=4) }, "234")) }
test.micro1146 <- function() { cat('1146\n'); stopifnot(identical({ substr('123456', start=2L, stop=4L) }, "234")) }
test.micro1147 <- function() { cat('1147\n'); stopifnot(identical({ substr('123456', start=2.8, stop=4) }, "234")) }
test.micro1148 <- function() { cat('1148\n'); stopifnot(identical({ substr(c('hello', 'bye'), start=c(1,2,3), stop=4) }, c("hell", "ye"))) }
test.micro1149 <- function() { cat('1149\n'); stopifnot(identical({ substr('fastr', start=NA, stop=2) }, NA_character_)) }
test.micro1150 <- function() { cat('1150\n'); stopifnot(identical({ substring('123456', first=2, last=4) }, "234")) }
test.micro1151 <- function() { cat('1151\n'); stopifnot(identical({ substring('123456', first=2.8, last=4) }, "234")) }
test.micro1152 <- function() { cat('1152\n'); stopifnot(identical({ substring(c('hello', 'bye'), first=c(1,2,3), last=4) }, c("hell", "ye", "ll"))) }
test.micro1153 <- function() { cat('1153\n'); stopifnot(identical({ substring('fastr', first=NA, last=2) }, NA_character_)) }
test.micro1154 <- function() { cat('1154\n'); stopifnot(identical({ order(1:3) }, 1:3)) }
test.micro1155 <- function() { cat('1155\n'); stopifnot(identical({ order(3:1) }, c(3L, 2L, 1L))) }
test.micro1156 <- function() { cat('1156\n'); stopifnot(identical({ order(c(1,1,1), 3:1) }, c(3L, 2L, 1L))) }
test.micro1157 <- function() { cat('1157\n'); stopifnot(identical({ order(c(1,1,1), 3:1, decreasing=FALSE) }, c(3L, 2L, 1L))) }
test.micro1158 <- function() { cat('1158\n'); stopifnot(identical({ order(c(1,1,1), 3:1, decreasing=TRUE, na.last=TRUE) }, 1:3)) }
test.micro1159 <- function() { cat('1159\n'); stopifnot(identical({ order(c(1,1,1), 3:1, decreasing=TRUE, na.last=NA) }, 1:3)) }
test.micro1160 <- function() { cat('1160\n'); stopifnot(identical({ order(c(1,1,1), 3:1, decreasing=TRUE, na.last=FALSE) }, 1:3)) }
test.micro1162 <- function() { cat('1162\n'); stopifnot(identical({ order(c(NA,NA,1), c(2,1,3)) }, c(3L, 2L, 1L))) }
test.micro1163 <- function() { cat('1163\n'); stopifnot(identical({ order(c(NA,NA,1), c(1,2,3)) }, c(3L, 1L, 2L))) }
test.micro1164 <- function() { cat('1164\n'); stopifnot(identical({ order(c(1,2,3,NA)) }, 1:4)) }
test.micro1165 <- function() { cat('1165\n'); stopifnot(identical({ order(c(1,2,3,NA), na.last=FALSE) }, c(4L, 1L, 2L, 3L))) }
test.micro1166 <- function() { cat('1166\n'); stopifnot(identical({ order(c(1,2,3,NA), na.last=FALSE, decreasing=TRUE) }, c(4L, 3L, 2L, 1L))) }
test.micro1167 <- function() { cat('1167\n'); stopifnot(identical({ order(c(0/0, -1/0, 2)) }, c(2L, 3L, 1L))) }
test.micro1168 <- function() { cat('1168\n'); stopifnot(identical({ order(c(0/0, -1/0, 2), na.last=NA) }, 2:3)) }
test.micro1169 <- function() { cat('1169\n'); stopifnot(identical({ order(c('a','z','Z','xxxz','zza','b')) }, c(1L, 6L, 4L, 2L, 3L, 5L))) }
test.micro1171 <- function() { cat('1171\n'); stopifnot(identical({ round( log(10,), digits = 5 ) }, 2.30259)) }
test.micro1172 <- function() { cat('1172\n'); stopifnot(identical({ round( log(10,2), digits = 5 ) }, 3.32193)) }
test.micro1173 <- function() { cat('1173\n'); stopifnot(identical({ round( log(10,10), digits = 5 ) }, 1)) }
test.micro1175 <- function() { cat('1175\n'); stopifnot(identical({ x <- c(a=1, b=10) ; round( c(log(x), log10(x), log2(x)), digits=5 ) }, structure(c(0, 2.30259, 0, 1, 0, 3.32193), .Names = c("a", "b", "a", "b", "a", "b")))) }
test.micro1176 <- function() { cat('1176\n'); stopifnot(identical({ sqrt(c(a=9,b=81)) }, structure(c(3, 9), .Names = c("a", "b")))) }
test.micro1177 <- function() { cat('1177\n'); stopifnot(identical({ round( exp(c(1+1i,-2-3i)), digits=5 ) }, c(1.46869+2.28736i, -0.13398-0.0191i))) }
test.micro1178 <- function() { cat('1178\n'); stopifnot(identical({ round( exp(1+2i), digits=5 ) }, -1.1312+2.47173i)) }
test.micro1179 <- function() { cat('1179\n'); stopifnot(identical({ abs((-1-0i)/(0+0i)) }, Inf)) }
test.micro1180 <- function() { cat('1180\n'); stopifnot(identical({ abs((-0-1i)/(0+0i)) }, Inf)) }
test.micro1181 <- function() { cat('1181\n'); stopifnot(identical({ abs(NA+0.1) }, NA_real_)) }
test.micro1182 <- function() { cat('1182\n'); stopifnot(identical({ abs(0/0) }, NaN)) }
test.micro1184 <- function() { cat('1184\n'); stopifnot(identical({ abs(c(0/0,1i)) }, c(NaN, 1))) }
test.micro1185 <- function() { cat('1185\n'); stopifnot(identical({ abs((0+0i)/0) }, NaN)) }
test.micro1186 <- function() { cat('1186\n'); stopifnot(identical({ exp(-abs((0+1i)/(0+0i))) }, 0)) }
test.micro1187 <- function() { cat('1187\n'); stopifnot(identical({ floor(c(0.2,-3.4)) }, c(0, -4))) }
test.micro1188 <- function() { cat('1188\n'); stopifnot(identical({ ceiling(c(0.2,-3.4,NA,0/0,1/0)) }, c(1, -3, NA, NaN, Inf))) }
test.micro1189 <- function() { cat('1189\n'); stopifnot(identical({ toupper(c('hello','bye')) }, c("HELLO", "BYE"))) }
test.micro1190 <- function() { cat('1190\n'); stopifnot(identical({ tolower(c('Hello','ByE')) }, c("hello", "bye"))) }
test.micro1191 <- function() { cat('1191\n'); stopifnot(identical({ tolower(1E100) }, "1e+100")) }
test.micro1192 <- function() { cat('1192\n'); stopifnot(identical({ toupper(1E100) }, "1E+100")) }
test.micro1193 <- function() { cat('1193\n'); stopifnot(identical({ tolower(c()) }, character(0))) }
test.micro1194 <- function() { cat('1194\n'); stopifnot(identical({ tolower(NA) }, NA_character_)) }
test.micro1196 <- function() { cat('1196\n'); stopifnot(identical({ toupper(c(a='hi', 'hello')) }, structure(c("HI", "HELLO"), .Names = c("a", "")))) }
test.micro1197 <- function() { cat('1197\n'); stopifnot(identical({ tolower(c(a='HI', 'HELlo')) }, structure(c("hi", "hello"), .Names = c("a", "")))) }
test.micro1198 <- function() { cat('1198\n'); stopifnot(identical({ typeof(1) }, "double")) }
test.micro1199 <- function() { cat('1199\n'); stopifnot(identical({ typeof(1L) }, "integer")) }
test.micro1200 <- function() { cat('1200\n'); stopifnot(identical({ typeof(sum) }, "builtin")) }
test.micro1201 <- function() { cat('1201\n'); stopifnot(identical({ typeof(function(){}) }, "closure")) }
test.micro1202 <- function() { cat('1202\n'); stopifnot(identical({ typeof('hi') }, "character")) }
test.micro1203 <- function() { cat('1203\n'); stopifnot(identical({ gsub('a','aa', 'prague alley', fixed=TRUE) }, "praague aalley")) }
test.micro1204 <- function() { cat('1204\n'); stopifnot(identical({ sub('a','aa', 'prague alley', fixed=TRUE) }, "praague alley")) }
test.micro1205 <- function() { cat('1205\n'); stopifnot(identical({ gsub('a','aa', 'prAgue alley', fixed=TRUE) }, "prAgue aalley")) }
test.micro1206 <- function() { cat('1206\n'); stopifnot(identical({ gsub('a','aa', 'prAgue alley', fixed=TRUE, ignore.case=TRUE) }, "prAgue aalley")) }
test.micro1207 <- function() { cat('1207\n'); stopifnot(identical({ gsub('h','', c('hello', 'hi', 'bye'), fixed=TRUE) }, c("ello", "i", "bye"))) }
test.micro1208 <- function() { cat('1208\n'); stopifnot(identical({ gsub('a','aa', 'prague alley') }, "praague aalley")) }
test.micro1209 <- function() { cat('1209\n'); stopifnot(identical({ sub('a','aa', 'prague alley') }, "praague alley")) }
test.micro1210 <- function() { cat('1210\n'); stopifnot(identical({ gsub('a','aa', 'prAgue alley') }, "prAgue aalley")) }
test.micro1211 <- function() { cat('1211\n'); stopifnot(identical({ gsub('a','aa', 'prAgue alley', ignore.case=TRUE) }, "praague aalley")) }
test.micro1213 <- function() { cat('1213\n'); stopifnot(identical({ gsub('([a-e])','\\1\\1', 'prague alley') }, "praaguee aalleey")) }
test.micro1214 <- function() { cat('1214\n'); stopifnot(identical({ gregexpr('(a)[^a]\\1', c('andrea apart', 'amadeus', NA)) }, list(structure(6L, match.length = 3L), structure(1L, match.length = 3L),     structure(NA_integer_, match.length = NA_integer_)))) }
test.micro1216 <- function() { cat('1216\n'); stopifnot(identical({ x <- 1:4 ; length(x) <- 2 ; x }, 1:2)) }
test.micro1217 <- function() { cat('1217\n'); stopifnot(identical({ x <- 1:2 ; length(x) <- 4 ; x }, c(1L, 2L, NA, NA))) }
test.micro1218 <- function() { cat('1218\n'); stopifnot(identical({ x <- 1:2 ; z <- (length(x) <- 4) ; z }, 4)) }
test.micro1219 <- function() { cat('1219\n'); stopifnot(identical({ length(c(z=1:4)) }, 4L)) }
test.micro1220 <- function() { cat('1220\n'); stopifnot(identical({ x <- 1 ; f <- function() { length(x) <<- 2 } ; f() ; x }, c(1, NA))) }
test.micro1222 <- function() { cat('1222\n'); stopifnot(identical({ x <- 1:2 ; names(x) <- c('hello'); names(x) }, c("hello", NA))) }
test.micro1223 <- function() { cat('1223\n'); stopifnot(identical({ x <- 1:2; names(x) <- c('hello', 'hi') ; x }, structure(1:2, .Names = c("hello", "hi")))) }
test.micro1224 <- function() { cat('1224\n'); stopifnot(identical({ x <- c(1,9); names(x) <- c('hello','hi') ; sqrt(x) }, structure(c(1, 3), .Names = c("hello", "hi")))) }
test.micro1225 <- function() { cat('1225\n'); stopifnot(identical({ x <- c(1,9); names(x) <- c('hello','hi') ; is.na(x) }, structure(c(FALSE, FALSE), .Names = c("hello", "hi")))) }
test.micro1226 <- function() { cat('1226\n'); stopifnot(identical({ x <- c(1,NA); names(x) <- c('hello','hi') ; cumsum(x) }, structure(c(1, NA), .Names = c("hello", "hi")))) }
test.micro1227 <- function() { cat('1227\n'); stopifnot(identical({ x <- c(1,NA); names(x) <- c(NA,'hi') ; cumsum(x) }, structure(c(1, NA), .Names = c(NA, "hi")))) }
test.micro1228 <- function() { cat('1228\n'); stopifnot(identical({ x <- c(1,2); names(x) <- c('A', 'B') ; x + 1 }, structure(c(2, 3), .Names = c("A", "B")))) }
test.micro1229 <- function() { cat('1229\n'); stopifnot(identical({ x <- 1:2; names(x) <- c('A', 'B') ; y <- c(1,2,3,4) ; names(y) <- c('X', 'Y', 'Z') ; x + y }, structure(c(2, 4, 4, 6), .Names = c("X", "Y", "Z", NA)))) }
test.micro1230 <- function() { cat('1230\n'); stopifnot(identical({ x <- 1:2; names(x) <- c('A', 'B') ; abs(x) }, structure(1:2, .Names = c("A", "B")))) }
test.micro1231 <- function() { cat('1231\n'); stopifnot(identical({ z <- c(a=1, b=2) ; names(z) <- NULL ; z }, c(1, 2))) }
test.micro1232 <- function() { cat('1232\n'); stopifnot(identical({ rev(c(1+1i, 2+2i)) }, c(2+2i, 1+1i))) }
test.micro1233 <- function() { cat('1233\n'); stopifnot(identical({ rev(1:3) }, c(3L, 2L, 1L))) }
test.micro1234 <- function() { cat('1234\n'); stopifnot(identical({ f <- function() { assign('x', 1) ; x } ; f() }, 1)) }
test.micro1235 <- function() { cat('1235\n'); stopifnot(identical({ f <- function() { x <- 2 ; g <- function() { x <- 3 ; assign('x', 1, inherits=FALSE) ; x } ; g() } ; f() }, 1)) }
test.micro1236 <- function() { cat('1236\n'); stopifnot(identical({ f <- function() { x <- 2 ; g <- function() { assign('x', 1, inherits=FALSE) } ; g() ; x } ; f() }, 2)) }
test.micro1237 <- function() { cat('1237\n'); stopifnot(identical({ f <- function() { x <- 2 ; g <- function() { assign('x', 1, inherits=TRUE) } ; g() ; x } ; f() }, 1)) }
test.micro1238 <- function() { cat('1238\n'); stopifnot(identical({ f <- function() {  g <- function() { assign('x', 1, inherits=TRUE) } ; g() } ; f() ; x }, 1)) }
test.micro1239 <- function() { cat('1239\n'); stopifnot(identical({ x <- 3 ; g <- function() { x } ; f <- function() { assign('x', 2) ; g() } ; f() }, 3)) }
test.micro1240 <- function() { cat('1240\n'); stopifnot(identical({ x <- 3 ; f <- function() { assign('x', 2) ; g <- function() { x } ; g() } ; f() }, 2)) }
test.micro1241 <- function() { cat('1241\n'); stopifnot(identical({ h <- function() { x <- 3 ; g <- function() { x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; h() }, 3)) }
test.micro1242 <- function() { cat('1242\n'); stopifnot(identical({ h <- function() { x <- 3  ; f <- function() { assign('x', 2) ; g <- function() { x } ; g() } ; f() }  ; h() }, 2)) }
test.micro1243 <- function() { cat('1243\n'); stopifnot(identical({ x <- 3 ; h <- function() { g <- function() { x } ; f <- function() { assign('x', 2, inherits=TRUE) } ; f() ; g() }  ; h() }, 2)) }
test.micro1244 <- function() { cat('1244\n'); stopifnot(identical({ x <- 3 ; h <- function(s) { if (s == 2) { assign('x', 2) } ; x }  ; h(1) ; h(2) }, 2)) }
test.micro1245 <- function() { cat('1245\n'); stopifnot(identical({ x <- 3 ; h <- function(s) { y <- x ; if (s == 2) { assign('x', 2) } ; c(y,x) }  ; c(h(1),h(2)) }, c(3, 3, 3, 2))) }
test.micro1246 <- function() { cat('1246\n'); stopifnot(identical({ g <- function() { x <- 2 ; f <- function() { x ; exists('x') }  ; f() } ; g() }, TRUE)) }
test.micro1247 <- function() { cat('1247\n'); stopifnot(identical({ g <- function() { f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() }, FALSE)) }
test.micro1248 <- function() { cat('1248\n'); stopifnot(identical({ g <- function() { f <- function() { if (FALSE) { x } ; assign('x', 1) ; exists('x') }  ; f() } ; g() }, TRUE)) }
test.micro1249 <- function() { cat('1249\n'); stopifnot(identical({ g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() }, FALSE)) }
test.micro1250 <- function() { cat('1250\n'); stopifnot(identical({ g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; assign('x', 2) ; exists('x') }  ; f() } ; g() }, TRUE)) }
test.micro1251 <- function() { cat('1251\n'); stopifnot(identical({ h <- function() { g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }, FALSE)) }
test.micro1252 <- function() { cat('1252\n'); stopifnot(identical({ h <- function() { x <- 3 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }, TRUE)) }
test.micro1253 <- function() { cat('1253\n'); stopifnot(identical({ f <- function(z) { exists('z') } ; f() }, TRUE)) }
test.micro1254 <- function() { cat('1254\n'); stopifnot(identical({ f <- function(z) { exists('z') } ; f(a) }, TRUE)) }
test.micro1255 <- function() { cat('1255\n'); stopifnot(identical({ f <- function() { x <- 3 ; exists('x', inherits=FALSE) } ; f() }, TRUE)) }
test.micro1256 <- function() { cat('1256\n'); stopifnot(identical({ f <- function() { z <- 3 ; exists('x', inherits=FALSE) } ; f() }, FALSE)) }
test.micro1257 <- function() { cat('1257\n'); stopifnot(identical({ f <- function() { if (FALSE) { x <- 3 } ; exists('x', inherits=FALSE) } ; f() }, FALSE)) }
test.micro1258 <- function() { cat('1258\n'); stopifnot(identical({ f <- function() { assign('x', 2) ; exists('x', inherits=FALSE) } ; f() }, TRUE)) }
test.micro1259 <- function() { cat('1259\n'); stopifnot(identical({ g <- function() { x <- 2 ; f <- function() { if (FALSE) { x <- 3 } ; exists('x') }  ; f() } ; g() }, TRUE)) }
test.micro1260 <- function() { cat('1260\n'); stopifnot(identical({ g <- function() { x <- 2 ; f <- function() { x <- 5 ; exists('x') }  ; f() } ; g() }, TRUE)) }
test.micro1261 <- function() { cat('1261\n'); stopifnot(identical({ g <- function() { f <- function() { assign('x', 3) ; if (FALSE) { x } ; exists('x') }  ; f() } ; g() }, TRUE)) }
test.micro1262 <- function() { cat('1262\n'); stopifnot(identical({ g <- function() { f <- function() { assign('z', 3) ; if (FALSE) { x } ; exists('x') }  ; f() } ; g() }, FALSE)) }
test.micro1263 <- function() { cat('1263\n'); stopifnot(identical({ h <- function() { assign('x', 1) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }, TRUE)) }
test.micro1264 <- function() { cat('1264\n'); stopifnot(identical({ h <- function() { assign('z', 1) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }, FALSE)) }
test.micro1265 <- function() { cat('1265\n'); stopifnot(identical({ h <- function() { x <- 3 ; g <- function() { f <- function() { if (FALSE) { x } ; exists('x') }  ; f() } ; g() } ; h() }, TRUE)) }
test.micro1268 <- function() { cat('1268\n'); stopifnot(identical({ x <- 3 ; f <- function() { exists('x') } ; f() }, TRUE)) }
test.micro1269 <- function() { cat('1269\n'); stopifnot(identical({ x <- 3 ; f <- function() { exists('x', inherits=FALSE) } ; f() }, FALSE)) }
test.micro1270 <- function() { cat('1270\n'); stopifnot(identical({ h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; assign('y', 2, h) ; ls(h) }, c("x", "y"))) }
test.micro1271 <- function() { cat('1271\n'); stopifnot(identical({ f <- function() { assign('x', 1) ; y <- 2 ; ls() } ; f() }, c("x", "y"))) }
test.micro1272 <- function() { cat('1272\n'); stopifnot(identical({ f <- function() { x <- 1 ; y <- 2 ; ls() } ; f() }, c("x", "y"))) }
test.micro1273 <- function() { cat('1273\n'); stopifnot(identical({ f <- function() { assign('x', 1) ; y <- 2 ; if (FALSE) { z <- 3 } ; ls() } ; f() }, c("x", "y"))) }
test.micro1274 <- function() { cat('1274\n'); stopifnot(identical({ f <- function() { if (FALSE) { x <- 1 } ; y <- 2 ; ls() } ; f() }, "y")) }
test.micro1275 <- function() { cat('1275\n'); stopifnot(identical({ f <- function() { for (i in rev(1:10)) { assign(as.character(i), i) } ; ls() } ; length(f()) }, 11L)) }
test.micro1276 <- function() { cat('1276\n'); stopifnot(identical({ f <- function() { x <- 2 ; get('x') } ; f() }, 2)) }
test.micro1277 <- function() { cat('1277\n'); stopifnot(identical({ x <- 3 ; f <- function() { get('x') } ; f() }, 3)) }
test.micro1278 <- function() { cat('1278\n'); stopifnot(identical({ x <- 3 ; f <- function() { x <- 2 ; get('x') } ; f() }, 2)) }
test.micro1279 <- function() { cat('1279\n'); stopifnot(identical({ x <- 3 ; f <- function() { x <- 2; h <- function() {  get('x') }  ; h() } ; f() }, 2)) }
test.micro1280 <- function() { cat('1280\n'); stopifnot(identical({ x <- 3 ; f <- function() { assign('x', 4) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x } ; h() } ; f() }, 4)) }
test.micro1281 <- function() { cat('1281\n'); stopifnot(identical({ x <- 3 ; f <- function() { assign('x', 4) ; g <- function() { assign('y', 3) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x } ; h() } ; g()  } ; f() }, 4)) }
test.micro1282 <- function() { cat('1282\n'); stopifnot(identical({ f <- function() { assign('x', 2, inherits=TRUE) ; assign('x', 1) ; h <- function() { x } ; h() } ; f() }, 1)) }
test.micro1283 <- function() { cat('1283\n'); stopifnot(identical({ x <- 3 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x } ; h() } ; f() } ; g() }, 3)) }
test.micro1284 <- function() { cat('1284\n'); stopifnot(identical({ x <- 3 ; gg <- function() {  g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x } ; h() } ; f() } ; g() } ; gg() }, 3)) }
test.micro1285 <- function() { cat('1285\n'); stopifnot(identical({ h <- function() { x <- 2 ; f <- function() { if (FALSE) { x <- 1 } ; g <- function() { x } ; g() } ; f() } ; h() }, 2)) }
test.micro1286 <- function() { cat('1286\n'); stopifnot(identical({ f <- function() { g <- function() { get('x', inherits=TRUE) } ; g() } ; x <- 3 ; f() }, 3)) }
test.micro1287 <- function() { cat('1287\n'); stopifnot(identical({ f <- function() { assign('z', 2) ; g <- function() { get('x', inherits=TRUE) } ; g() } ; x <- 3 ; f() }, 3)) }
test.micro1288 <- function() { cat('1288\n'); stopifnot(identical({ f <- function() { assign('x', 3) ; g <- function() { x } ; g() } ; x <- 10 ; f() }, 3)) }
test.micro1289 <- function() { cat('1289\n'); stopifnot(identical({ f <- function() { assign('x', 3) ; h <- function() { assign('z', 4) ; g <- function() { x } ; g() } ; h() } ; x <- 10 ; f() }, 3)) }
test.micro1290 <- function() { cat('1290\n'); stopifnot(identical({ f <- function() { assign('x', 3) ; h <- function() { g <- function() { x } ; g() } ; h() } ; x <- 10 ; f() }, 3)) }
test.micro1291 <- function() { cat('1291\n'); stopifnot(identical({ f <- function() { assign('x', 1) ; g <- function() { assign('z', 2) ; x } ; g() } ; f() }, 1)) }
test.micro1292 <- function() { cat('1292\n'); stopifnot(identical({ h <- function() { x <- 3 ; g <- function() { assign('z', 2) ; x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; h() }, 3)) }
test.micro1293 <- function() { cat('1293\n'); stopifnot(identical({ h <- function() { x <- 3 ; g <- function() { assign('x', 5) ; x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; h() }, 5)) }
test.micro1294 <- function() { cat('1294\n'); stopifnot(identical({ x <- 10 ; g <- function() { x <- 100 ; z <- 2 ; f <- function() { assign('z', 1); x <- x ; x } ; f() } ; g() }, 100)) }
test.micro1295 <- function() { cat('1295\n'); stopifnot(identical({ f <- function() { x <- 22 ; get('x', inherits=FALSE) } ; f() }, 22)) }
test.micro1296 <- function() { cat('1296\n'); stopifnot(identical({ x <- 33 ; f <- function() { assign('x', 44) ; get('x', inherits=FALSE) } ; f() }, 44)) }
test.micro1297 <- function() { cat('1297\n'); stopifnot(identical({ hh <- new.env() ; assign('z', 3, hh) ; h <- new.env(parent=hh) ; assign('y', 2, h) ; get('z', h) }, 3)) }
test.micro1298 <- function() { cat('1298\n'); stopifnot(identical({ g <- function() { if (FALSE) { x <- 2 ; y <- 3} ; f <- function() { if (FALSE) { x } ; assign('y', 2) ; exists('x') }  ; f() } ; g() }, FALSE)) }
test.micro1299 <- function() { cat('1299\n'); stopifnot(identical({ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; exists('x') }  ; f() } ; g() }, TRUE)) }
test.micro1300 <- function() { cat('1300\n'); stopifnot(identical({ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; h <- function() { exists('x') } ; h() }  ; f() } ; g() }, TRUE)) }
test.micro1301 <- function() { cat('1301\n'); stopifnot(identical({ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('y', 2) ; h <- function() { exists('x') } ; h() }  ; f() } ; g() }, FALSE)) }
test.micro1302 <- function() { cat('1302\n'); stopifnot(identical({ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; gg <- function() { h <- function() { get('x') } ; h() } ; gg() } ; f() } ; g() }, 2)) }
test.micro1303 <- function() { cat('1303\n'); stopifnot(identical({ g <- function() { if (FALSE) {y <- 3; x <- 2} ; f <- function() { assign('x', 2) ; gg <- function() { h <- function() { exists('x') } ; h() } ; gg() } ; f() } ; g() }, TRUE)) }
test.micro1304 <- function() { cat('1304\n'); stopifnot(identical({ x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function() { x } } ; f1 <- f(1) ; f2 <- f(2) ; f1() }, 4)) }
test.micro1305 <- function() { cat('1305\n'); stopifnot(identical({ x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function() { x } } ; f1 <- f(1) ; f2 <- f(2) ; f2() ; f1() }, 4)) }
test.micro1306 <- function() { cat('1306\n'); stopifnot(identical({ f <- function() { x <- 2 ; g <- function() { if (FALSE) { x <- 2 } ; assign('x', 1, inherits=TRUE) } ; g() ; x } ; f() }, 1)) }
test.micro1307 <- function() { cat('1307\n'); stopifnot(identical({ h <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; g <- function() { assign('z', 3) ; if (FALSE) { x <- 4 } ;  f <- function() { exists('x') } ; f() } ; g() } ; h() }, FALSE)) }
test.micro1308 <- function() { cat('1308\n'); stopifnot(identical({ x <- function(){3} ; f <- function() { assign('x', function(){4}) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x() } ; h() } ; f() }, 4)) }
test.micro1309 <- function() { cat('1309\n'); stopifnot(identical({ f <- function() { assign('x', function(){2}, inherits=TRUE) ; assign('x', function(){1}) ; h <- function() { x() } ; h() } ; f() }, 1)) }
test.micro1310 <- function() { cat('1310\n'); stopifnot(identical({ x <- function(){3} ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() }, 3)) }
test.micro1311 <- function() { cat('1311\n'); stopifnot(identical({ x <- function(){3} ; gg <- function() {  g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() } ; gg() }, 3)) }
test.micro1312 <- function() { cat('1312\n'); stopifnot(identical({ h <- function() { x <- function(){2} ; f <- function() { if (FALSE) { x <- 1 } ; g <- function() { x } ; g() } ; f() } ; z <- h() ; z() }, 2)) }
test.micro1313 <- function() { cat('1313\n'); stopifnot(identical({ h <- function() { g <- function() {4} ; f <- function() { if (FALSE) { g <- 4 } ; g() } ; f() } ; h() }, 4)) }
test.micro1314 <- function() { cat('1314\n'); stopifnot(identical({ h <- function() { assign('f', function() {4}) ; f() } ; h() }, 4)) }
test.micro1315 <- function() { cat('1315\n'); stopifnot(identical({ f <- function() { 4 } ; h <- function() { assign('f', 5) ; f() } ; h() }, 4)) }
test.micro1316 <- function() { cat('1316\n'); stopifnot(identical({ f <- function() { 4 } ; h <- function() { assign('z', 5) ; f() } ; h() }, 4)) }
test.micro1317 <- function() { cat('1317\n'); stopifnot(identical({ gg <- function() {  assign('x', function(){11}) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() } ; gg() }, 11)) }
test.micro1318 <- function() { cat('1318\n'); stopifnot(identical({ x <- function(){3} ; gg <- function() { assign('x', 4) ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x() } ; h() } ; f() } ; g() } ; gg() }, 3)) }
test.micro1319 <- function() { cat('1319\n'); stopifnot(identical({ h <- function() { x <- function() {3} ; g <- function() { assign('z', 2) ; x } ; f <- function() { assign('x', 2) ; g() } ; f() }  ; z <- h() ; z() }, 3)) }
test.micro1320 <- function() { cat('1320\n'); stopifnot(identical({ h <- function() { x <- function() {3} ; g <- function() { assign('x', function() {5} ) ; x() } ; g() } ; h() }, 5)) }
test.micro1321 <- function() { cat('1321\n'); stopifnot(identical({ h <- function() { z <- 3 ; x <- function() {3} ; g <- function() { x <- 1 ; assign('z', 5) ; x() } ; g() } ; h() }, 3)) }
test.micro1322 <- function() { cat('1322\n'); stopifnot(identical({ h <- function() { x <- function() {3} ; gg <- function() { assign('x', 5) ; g <- function() { x() } ; g() } ; gg() } ; h() }, 3)) }
test.micro1323 <- function() { cat('1323\n'); stopifnot(identical({ h <- function() { z <- 2 ; x <- function() {3} ; gg <- function() { assign('z', 5) ; g <- function() { x() } ; g() } ; gg() } ; h() }, 3)) }
test.micro1324 <- function() { cat('1324\n'); stopifnot(identical({ h <- function() { x <- function() {3} ; g <- function() { assign('x', function() {4}) ; x() } ; g() } ; h() }, 4)) }
test.micro1325 <- function() { cat('1325\n'); stopifnot(identical({ h <- function() { z <- 2 ; x <- function() {3} ; g <- function() { assign('z', 1) ; x() } ; g() } ; h() }, 3)) }
test.micro1326 <- function() { cat('1326\n'); stopifnot(identical({ x <- function() { 3 } ; h <- function() { if (FALSE) { x <- 2 } ;  z <- 2  ; g <- function() { assign('z', 1) ; x() } ; g() } ; h() }, 3)) }
test.micro1327 <- function() { cat('1327\n'); stopifnot(identical({ x <- function() { 3 } ; h <- function() { g <- function() { f <- function() { x <- 1 ; x() } ; f() } ; g() } ; h() }, 3)) }
test.micro1328 <- function() { cat('1328\n'); stopifnot(identical({ g <- function() { assign('myfunc', function(i) { sum(i) });  f <- function() { lapply(2, 'myfunc') } ; f() } ; g() }, list(2))) }
test.micro1329 <- function() { cat('1329\n'); stopifnot(identical({ myfunc <- function(i) { sum(i) } ; g <- function() { assign('z', 1);  f <- function() { lapply(2, 'myfunc') } ; f() } ; g() }, list(2))) }
test.micro1330 <- function() { cat('1330\n'); stopifnot(identical({ g <- function() { f <- function() { assign('myfunc', function(i) { sum(i) }); lapply(2, 'myfunc') } ; f() } ; g() }, list(2))) }
test.micro1331 <- function() { cat('1331\n'); stopifnot(identical({ h <- function() { myfunc <- function(i) { sum(i) } ; g <- function() { myfunc <- 2 ; f <- function() { myfunc(2) } ; f() } ; g() } ; h() }, 2)) }
test.micro1332 <- function() { cat('1332\n'); stopifnot(identical({ x <- function() {11} ; g <- function() { f <- function() { assign('x', 2) ; x() } ; f() } ; g() }, 11)) }
test.micro1333 <- function() { cat('1333\n'); stopifnot(identical({ g <- function() { myfunc <- function(i) { i+i } ; f <- function() { lapply(2, 'myfunc') } ; f() } ; g() }, list(4))) }
test.micro1334 <- function() { cat('1334\n'); stopifnot(identical({ x <- function() {3} ; f <- function(i) { if (i == 1) { assign('x', function() {4}) } ; function() { x() } } ; f1 <- f(1) ; f2 <- f(2) ; f1() }, 4)) }
test.micro1335 <- function() { cat('1335\n'); stopifnot(identical({ x <- function() {3} ; f <- function(i) { if (i == 1) { assign('x', function() {4}) } ; function() { x() } } ; f1 <- f(1) ; f2 <- f(2) ; f2() ; f1() }, 4)) }
test.micro1336 <- function() { cat('1336\n'); stopifnot(identical({ x <- function() {3} ; f <- function(i) { if (i == 1) { assign('x', function() {4}) } ; function() { x() } } ; f1 <- f(1) ; f2 <- f(2) ; f1() ; f2() }, 3)) }
test.micro1337 <- function() { cat('1337\n'); stopifnot(identical({ x <- 3 ; f <- function() { assign('x', 4) ; h <- function(s=1) { if (s==2) { x <- 5 } ; x <<- 6 } ; h() ; get('x') } ; f() }, 6)) }
test.micro1338 <- function() { cat('1338\n'); stopifnot(identical({ x <- 3 ; f <- function() { assign('x', 4) ; hh <- function() { if (FALSE) { x <- 100 } ; h <- function() { x <<- 6 } ; h() } ; hh() ; get('x') } ; f() }, 6)) }
test.micro1339 <- function() { cat('1339\n'); stopifnot(identical({ x <- 3 ; g <- function() { if (FALSE) { x <- 2 } ; f <- function() { h <- function() { x ; hh <- function() { x <<- 4 } ; hh() } ; h() } ; f() } ; g() ; x }, 4)) }
test.micro1340 <- function() { cat('1340\n'); stopifnot(identical({ f <- function() { x <- 1 ; g <- function() { h <- function() { x <<- 2 } ; h() } ; g() ; x } ; f() }, 2)) }
test.micro1341 <- function() { cat('1341\n'); stopifnot(identical({ g <- function() { if (FALSE) { x <- 2 } ; f <- function() { assign('x', 4) ; x <<- 3 } ; f() } ; g() ; x }, 3)) }
test.micro1342 <- function() { cat('1342\n'); stopifnot(identical({ g <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; h <- function() { if (FALSE) { x <- 1 } ; assign('z', 10) ; f <- function() { assign('x', 4) ; x <<- 3 } ; f() } ; h() } ; g() ; x }, 3)) }
test.micro1343 <- function() { cat('1343\n'); stopifnot(identical({ gg <- function() { assign('x', 100) ; g <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; h <- function() { if (FALSE) { x <- 1 } ; assign('z', 10) ; f <- function() { assign('x', 4) ; x <<- 3 } ; f() } ; h() } ; g() } ; x <- 10 ; gg() ; x }, 10)) }
test.micro1344 <- function() { cat('1344\n'); stopifnot(identical({ gg <- function() { if (FALSE) { x <- 100 } ; g <- function() { if (FALSE) { x <- 100 } ; h <- function() { f <- function() { x <<- 3 } ; f() } ; h() } ; g() } ; x <- 10 ; gg() ; x }, 3)) }
test.micro1345 <- function() { cat('1345\n'); stopifnot(identical({ g <- function() { if (FALSE) { x <- 2 ; z <- 3 } ; h <- function() { assign('z', 10) ; f <- function() { x <<- 3 } ; f() } ; h() } ; g() ; x }, 3)) }
test.micro1346 <- function() { cat('1346\n'); stopifnot(identical({ g <- function() { x <- 2 ; z <- 3 ; hh <- function() { assign('z', 2) ; h <- function() { f <- function() { x <<- 3 } ; f() } ; h() } ; hh() } ; x <- 10 ; g() ; x }, 10)) }
test.micro1347 <- function() { cat('1347\n'); stopifnot(identical({ g <- function() { x <- 2 ; z <- 3 ; hh <- function() { assign('z', 2) ; h <- function() { assign('x', 1); f <- function() { x <<- 3 } ; f() } ; h() } ; hh() ; x } ; x <- 10 ; g() }, 2)) }
test.micro1348 <- function() { cat('1348\n'); stopifnot(identical({ x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function(v) { x <<- v} } ; f1 <- f(1) ; f2 <- f(2) ; f1(10) ; f2(11) ; x }, 11)) }
test.micro1349 <- function() { cat('1349\n'); stopifnot(identical({ x <- 3 ; f <- function(i) { if (i == 1) { assign('x', 4) } ; function(v) { x <<- v} } ; f1 <- f(1) ; f2 <- f(2) ; f2(10) ; f1(11) ; x }, 10)) }
test.micro1350 <- function() { cat('1350\n'); stopifnot(identical({ h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; exists('x', h) }, TRUE)) }
test.micro1351 <- function() { cat('1351\n'); stopifnot(identical({ h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; exists('xx', h) }, FALSE)) }
test.micro1352 <- function() { cat('1352\n'); stopifnot(identical({ hh <- new.env() ; assign('z', 3, hh) ; h <- new.env(parent=hh) ; assign('y', 2, h) ; exists('z', h) }, TRUE)) }
test.micro1353 <- function() { cat('1353\n'); stopifnot(identical({ ph <- new.env() ; h <- new.env(parent=ph) ; assign('x', 2, ph) ; assign('x', 10, h, inherits=TRUE) ; get('x', ph) }, 10)) }
test.micro1354 <- function() { cat('1354\n'); stopifnot(identical({ ph <- new.env() ; h <- new.env(parent=ph) ; assign('x', 10, h, inherits=TRUE) ; x }, 10)) }
test.micro1355 <- function() { cat('1355\n'); stopifnot(identical({ assign('z', 10, inherits=TRUE) ; z }, 10)) }
test.micro1356 <- function() { cat('1356\n'); stopifnot(identical({ h <- new.env(parent=globalenv()) ; assign('x', 10, h, inherits=TRUE) ; x }, 10)) }
test.micro1357 <- function() { cat('1357\n'); stopifnot(identical({ h <- new.env() ; assign('x', 1, h) ; assign('x', 1, h) ; get('x', h) }, 1)) }
test.micro1358 <- function() { cat('1358\n'); stopifnot(identical({ h <- new.env() ; assign('x', 1, h) ; assign('x', 2, h) ; get('x', h) }, 2)) }
test.micro1359 <- function() { cat('1359\n'); stopifnot(identical({ h <- new.env() ; u <- 1 ; assign('x', u, h) ; assign('x', u, h) ; get('x', h) }, 1)) }
test.micro1360 <- function() { cat('1360\n'); stopifnot(identical({ exists('sum') }, TRUE)) }
test.micro1361 <- function() { cat('1361\n'); stopifnot(identical({ exists('sum', inherits = FALSE) }, FALSE)) }
test.micro1362 <- function() { cat('1362\n'); stopifnot(identical({ x <- 1; exists('x', inherits = FALSE) }, TRUE)) }
test.micro1363 <- function() { cat('1363\n'); stopifnot(identical({ ls() }, character(0))) }
test.micro1364 <- function() { cat('1364\n'); stopifnot(identical({ x <- 1 ; ls(globalenv()) }, "x")) }
test.micro1365 <- function() { cat('1365\n'); stopifnot(identical({ ls(.GlobalEnv) }, character(0))) }
test.micro1366 <- function() { cat('1366\n'); stopifnot(identical({ x <- 1 ; ls(.GlobalEnv) }, "x")) }
test.micro1374 <- function() { cat('1374\n'); stopifnot(identical({ m <- matrix(1:49, nrow=7) ; sum(m * t(m)) }, 33369L)) }
test.micro1375 <- function() { cat('1375\n'); stopifnot(identical({ m <- matrix(1:81, nrow=9) ; sum(m * t(m)) }, 145881L)) }
test.micro1376 <- function() { cat('1376\n'); stopifnot(identical({ m <- matrix(-5000:4999, nrow=100) ; sum(m * t(m)) }, 1666502500L)) }
test.micro1377 <- function() { cat('1377\n'); stopifnot(identical({ m <- matrix(c(rep(1:10,100200),100L), nrow=1001) ; sum(m * t(m)) }, 38587000L)) }
test.micro1388 <- function() { cat('1388\n'); stopifnot(identical({ is.double(10L) }, FALSE)) }
test.micro1389 <- function() { cat('1389\n'); stopifnot(identical({ is.double(10) }, TRUE)) }
test.micro1390 <- function() { cat('1390\n'); stopifnot(identical({ is.double('10') }, FALSE)) }
test.micro1391 <- function() { cat('1391\n'); stopifnot(identical({ is.numeric(10L) }, TRUE)) }
test.micro1392 <- function() { cat('1392\n'); stopifnot(identical({ is.numeric(10) }, TRUE)) }
test.micro1393 <- function() { cat('1393\n'); stopifnot(identical({ is.numeric(TRUE) }, FALSE)) }
test.micro1394 <- function() { cat('1394\n'); stopifnot(identical({ is.character('hi') }, TRUE)) }
test.micro1395 <- function() { cat('1395\n'); stopifnot(identical({ is.list(NULL) }, FALSE)) }
test.micro1396 <- function() { cat('1396\n'); stopifnot(identical({ is.logical(NA) }, TRUE)) }
test.micro1397 <- function() { cat('1397\n'); stopifnot(identical({ is.logical(1L) }, FALSE)) }
test.micro1398 <- function() { cat('1398\n'); stopifnot(identical({ is.integer(1) }, FALSE)) }
test.micro1399 <- function() { cat('1399\n'); stopifnot(identical({ is.integer(1L) }, TRUE)) }
test.micro1400 <- function() { cat('1400\n'); stopifnot(identical({ is.complex(1i) }, TRUE)) }
test.micro1401 <- function() { cat('1401\n'); stopifnot(identical({ is.complex(1) }, FALSE)) }
test.micro1402 <- function() { cat('1402\n'); stopifnot(identical({ is.raw(raw()) }, TRUE)) }
test.micro1403 <- function() { cat('1403\n'); stopifnot(identical({ is.matrix(1) }, FALSE)) }
test.micro1404 <- function() { cat('1404\n'); stopifnot(identical({ is.matrix(matrix(1:6, nrow=2)) }, TRUE)) }
test.micro1405 <- function() { cat('1405\n'); stopifnot(identical({ is.matrix(NULL) }, FALSE)) }
test.micro1406 <- function() { cat('1406\n'); stopifnot(identical({ sub <- function(x,y) { x - y }; sub(10,5) }, 5)) }
test.micro1407 <- function() { cat('1407\n'); stopifnot(identical({ sub('a','aa', 'prague alley', fixed=TRUE) }, "praague alley")) }
test.micro1409 <- function() { cat('1409\n'); stopifnot(identical({ r <- eigen(matrix(rep(1,4), nrow=2), only.values=FALSE) ; round( r$values, digits=5 ) }, c(2, 0))) }
test.micro1410 <- function() { cat('1410\n'); stopifnot(identical({ eigen(10, only.values=FALSE) }, structure(list(values = 10, vectors = structure(1, .Dim = c(1L, 1L))), .Names = c("values", "vectors")))) }
test.micro1412 <- function() { cat('1412\n'); stopifnot(identical({ r <- eigen(matrix(c(1,2,2,3), nrow=2), only.values=FALSE); round( r$values, digits=5 ) }, c(4.23607, -0.23607))) }
test.micro1414 <- function() { cat('1414\n'); stopifnot(identical({ r <- eigen(matrix(c(1,2,3,4), nrow=2), only.values=FALSE); round( r$values, digits=5 ) }, c(5.37228, -0.37228))) }
test.micro1416 <- function() { cat('1416\n'); stopifnot(identical({ r <- eigen(matrix(c(3,-2,4,-1), nrow=2), only.values=FALSE); round( r$values, digits=5 ) }, c(1+2i, 1-2i))) }
test.micro1418 <- function() { cat('1418\n'); stopifnot(identical({ x <- 1; names(x) <- 'hello' ; attributes(x) }, structure(list(names = "hello"), .Names = "names"))) }
test.micro1419 <- function() { cat('1419\n'); stopifnot(identical({ x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; attributes(x) }, structure(list(myatt = 2:4), .Names = "myatt"))) }
test.micro1420 <- function() { cat('1420\n'); stopifnot(identical({ x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; attr(x, 'myatt1') <- 'hello' ; attributes(x) }, structure(list(myatt = 2:4, myatt1 = "hello"), .Names = c("myatt", "myatt1")))) }
test.micro1421 <- function() { cat('1421\n'); stopifnot(identical({ x <- 1:3 ; attr(x, 'myatt') <- 2:4 ; y <- x; attr(x, 'myatt1') <- 'hello' ; attributes(y) }, structure(list(myatt = 2:4), .Names = "myatt"))) }
test.micro1422 <- function() { cat('1422\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, 'myatt') <- 2:4 ; y <- x; attr(x, 'myatt1') <- 'hello' ; attributes(y) }, structure(list(names = c("a", "b"), myatt = 2:4), .Names = c("names", "myatt")))) }
test.micro1423 <- function() { cat('1423\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, 'names') }, c("a", "b"))) }
test.micro1424 <- function() { cat('1424\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, 'na') }, c("a", "b"))) }
test.micro1425 <- function() { cat('1425\n'); stopifnot(identical({ x <- c(a=1, b=2) ; attr(x, 'mya') <- 1; attr(x, 'b') <- 2; attr(x, 'm') }, 1)) }
test.micro1426 <- function() { cat('1426\n'); stopifnot(identical({ x <- 1:2; attr(x, 'aa') <- 1 ; attr(x, 'ab') <- 2; attr(x, 'bb') <- 3; attr(x, 'b') }, 3)) }
test.micro1430 <- function() { cat('1430\n'); stopifnot(identical({ x <- c(hello=1) ; attributes(x) <- list(names=NULL) ; x }, 1)) }
test.micro1432 <- function() { cat('1432\n'); stopifnot(identical({ x <- c(hello=1) ; attributes(x) <- list(hi=1) ;  attributes(x) <- NULL ; x }, 1)) }
test.micro1434 <- function() { cat('1434\n'); stopifnot(identical({ unlist(list('hello', 'hi')) }, c("hello", "hi"))) }
test.micro1435 <- function() { cat('1435\n'); stopifnot(identical({ unlist(list(a='hello', b='hi')) }, structure(c("hello", "hi"), .Names = c("a", "b")))) }
test.micro1436 <- function() { cat('1436\n'); stopifnot(identical({ x <- list(a=1,b=2:3,list(x=FALSE)) ; unlist(x, recursive=FALSE) }, structure(list(a = 1, b1 = 2L, b2 = 3L, x = FALSE), .Names = c("a", "b1", "b2", "x")))) }
test.micro1437 <- function() { cat('1437\n'); stopifnot(identical({ x <- list(1,z=list(1,b=22,3)) ; unlist(x, recursive=FALSE) }, structure(list(1, z1 = 1, z.b = 22, z3 = 3), .Names = c("", "z1", "z.b", "z3")))) }
test.micro1438 <- function() { cat('1438\n'); stopifnot(identical({ x <- list(1,z=list(1,b=22,3)) ; unlist(x, recursive=FALSE, use.names=FALSE) }, list(1, 1, 22, 3))) }
test.micro1439 <- function() { cat('1439\n'); stopifnot(identical({ x <- list('a', c('b', 'c'), list('d', list('e'))) ; unlist(x) }, c("a", "b", "c", "d", "e"))) }
test.micro1440 <- function() { cat('1440\n'); stopifnot(identical({ x <- list(NULL, list('d', list(), character())) ; unlist(x) }, "d")) }
test.micro1441 <- function() { cat('1441\n'); stopifnot(identical({ x <- list(a=list('1','2',b='3','4')) ; unlist(x) }, structure(c("1", "2", "3", "4"), .Names = c("a1", "a2", "a.b", "a4")))) }
test.micro1442 <- function() { cat('1442\n'); stopifnot(identical({ x <- list(a=list('1','2',b=list('3'))) ; unlist(x) }, structure(c("1", "2", "3"), .Names = c("a1", "a2", "a.b")))) }
test.micro1443 <- function() { cat('1443\n'); stopifnot(identical({ x <- list(a=list(1,FALSE,b=list(2:4))) ; unlist(x) }, structure(c(1, 0, 2, 3, 4), .Names = c("a1", "a2", "a.b1", "a.b2", "a.b3")))) }
test.micro1444 <- function() { cat('1444\n'); stopifnot(identical({ rev.mine <- function(x) { if (length(x)) x[length(x):1L] else x } ; rev.mine(1:3) }, c(3L, 2L, 1L))) }
test.micro1445 <- function() { cat('1445\n'); stopifnot(identical({ a = array(1:4,c(2,2)); b = aperm(a); (a[1,1] == b[1,1]) && (a[1,2] == b[2,1]) && (a[2,1] == b[1,2]) && (a[2,2] == b[2,2]); }, TRUE)) }
test.micro1446 <- function() { cat('1446\n'); stopifnot(identical({ a = array(1:24,c(2,3,4)); b = aperm(a); dim(b)[1] == 4 && dim(b)[2] == 3 && dim(b)[3] == 2; }, TRUE)) }
test.micro1447 <- function() { cat('1447\n'); stopifnot(identical({ a = array(1:24,c(2,3,4)); b = aperm(a, resize=FALSE); dim(b)[1] == 2 && dim(b)[2] == 3 && dim(b)[3] == 4; }, TRUE)) }
test.micro1448 <- function() { cat('1448\n'); stopifnot(identical({ a = array(1:24,c(2,3,4)); b = aperm(a, c(2,3,1)); a[1,2,3] == b[2,3,1]; }, TRUE)) }
test.micro1449 <- function() { cat('1449\n'); stopifnot(identical({ a = array(1:24,c(3,3,3)); b = aperm(a, c(2,3,1)); a[1,2,3] == b[2,3,1] && a[2,3,1] == b[3,1,2] && a[3,1,2] == b[1,2,3]; }, TRUE)) }
test.micro1450 <- function() { cat('1450\n'); stopifnot(identical({ a = array(1:24,c(3,3,3)); b = aperm(a, c(2,3,1), resize = FALSE); a[1,2,3] == b[2,3,1] && a[2,3,1] == b[3,1,2] && a[3,1,2] == b[1,2,3]; }, TRUE)) }
test.micro1451 <- function() { cat('1451\n'); stopifnot(identical({ a = array(1:24,c(2,3,4)); b = aperm(a, c(2,3,1), resize = FALSE); a[1,2,3] == b[2,1,2]; }, TRUE)) }
test.micro1452 <- function() { cat('1452\n'); stopifnot(identical({ aperm(array(1:27,c(3,3,3)), c(1+1i,3+3i,2+2i))[1,2,3] == array(1:27,c(3,3,3))[1,3,2]; }, TRUE)) }
test.micro1453 <- function() { cat('1453\n'); stopifnot(identical({ a = colSums(matrix(1:12,3,4)); is.null(dim(a)); }, TRUE)) }
test.micro1454 <- function() { cat('1454\n'); stopifnot(identical({ a = colSums(matrix(1:12,3,4)); length(a) == 4; }, TRUE)) }
test.micro1455 <- function() { cat('1455\n'); stopifnot(identical({ a = colSums(matrix(1:12,3,4)); a[1] == 6 && a[2] == 15 && a[3] == 24 && a[4] == 33; }, TRUE)) }
test.micro1456 <- function() { cat('1456\n'); stopifnot(identical({ a = colSums(array(1:24,c(2,3,4))); d = dim(a); d[1] == 3 && d[2] == 4; }, TRUE)) }
test.micro1457 <- function() { cat('1457\n'); stopifnot(identical({ a = colSums(array(1:24,c(2,3,4))); length(a) == 12; }, TRUE)) }
test.micro1458 <- function() { cat('1458\n'); stopifnot(identical({ a = colSums(array(1:24,c(2,3,4))); a[1,1] == 3 && a[2,2] == 19 && a[3,3] == 35 && a[3,4] == 47; }, TRUE)) }
test.micro1459 <- function() { cat('1459\n'); stopifnot(identical({ a = rowSums(matrix(1:12,3,4)); is.null(dim(a)); }, TRUE)) }
test.micro1460 <- function() { cat('1460\n'); stopifnot(identical({ a = rowSums(matrix(1:12,3,4)); length(a) == 3; }, TRUE)) }
test.micro1461 <- function() { cat('1461\n'); stopifnot(identical({ a = rowSums(matrix(1:12,3,4)); a[1] == 22 && a[2] == 26 && a[3] == 30; }, TRUE)) }
test.micro1462 <- function() { cat('1462\n'); stopifnot(identical({ a = rowSums(array(1:24,c(2,3,4))); is.null(dim(a)); }, TRUE)) }
test.micro1463 <- function() { cat('1463\n'); stopifnot(identical({ a = rowSums(array(1:24,c(2,3,4))); length(a) == 2; }, TRUE)) }
test.micro1464 <- function() { cat('1464\n'); stopifnot(identical({ a = rowSums(array(1:24,c(2,3,4))); a[1] == 144 && a[2] == 156; }, TRUE)) }
test.micro1465 <- function() { cat('1465\n'); stopifnot(identical({ f<-function(i) { if(i<=1) 1 else i*Recall(i-1) } ; f(10) }, 3628800)) }
test.micro1466 <- function() { cat('1466\n'); stopifnot(identical({ f<-function(i) { if(i<=1) 1 else i*Recall(i-1) } ; g <- f ; f <- sum ; g(10) }, 3628800)) }
test.micro1467 <- function() { cat('1467\n'); stopifnot(identical({ f<-function(i) { if (i==1) { 1 } else if (i==2) { 1 } else { Recall(i-1) + Recall(i-2) } } ; f(10) }, 55)) }
test.micro1473 <- function() { cat('1473\n'); stopifnot(identical({ sort(c(1L,10L,2L)) }, c(1L, 2L, 10L))) }
test.micro1474 <- function() { cat('1474\n'); stopifnot(identical({ sort(c(3,10,2)) }, c(2, 3, 10))) }
test.micro1475 <- function() { cat('1475\n'); stopifnot(identical({ sort(c(1,2,0/0,NA)) }, c(1, 2))) }
test.micro1476 <- function() { cat('1476\n'); stopifnot(identical({ sort(c(2,1,0/0,NA), na.last=NA) }, c(1, 2))) }
test.micro1477 <- function() { cat('1477\n'); stopifnot(identical({ sort(c(3,0/0,2,NA), na.last=TRUE) }, c(2, 3, NaN, NA))) }
test.micro1478 <- function() { cat('1478\n'); stopifnot(identical({ sort(c(3,NA,0/0,2), na.last=FALSE) }, c(NA, NaN, 2, 3))) }
test.micro1479 <- function() { cat('1479\n'); stopifnot(identical({ sort(c(3L,NA,2L)) }, 2:3)) }
test.micro1480 <- function() { cat('1480\n'); stopifnot(identical({ sort(c(3L,NA,-2L), na.last=TRUE) }, c(-2L, 3L, NA))) }
test.micro1481 <- function() { cat('1481\n'); stopifnot(identical({ sort(c(3L,NA,-2L), na.last=FALSE) }, c(NA, -2L, 3L))) }
test.micro1482 <- function() { cat('1482\n'); stopifnot(identical({ sort(c(a=NA,b=NA,c=3,d=1),na.last=TRUE, decreasing=TRUE) }, structure(c(3, 1, NA, NA), .Names = c("c", "d", "a", "b")))) }
test.micro1483 <- function() { cat('1483\n'); stopifnot(identical({ sort(c(a=NA,b=NA,c=3,d=1),na.last=FALSE, decreasing=FALSE) }, structure(c(NA, NA, 1, 3), .Names = c("a", "b", "d", "c")))) }
test.micro1484 <- function() { cat('1484\n'); stopifnot(identical({ sort(c(a=0/0,b=1/0,c=3,d=NA),na.last=TRUE, decreasing=FALSE) }, structure(c(3, Inf, NaN, NA), .Names = c("c", "b", "a", "d")))) }
test.micro1485 <- function() { cat('1485\n'); stopifnot(identical({ sort(double()) }, numeric(0))) }
test.micro1486 <- function() { cat('1486\n'); stopifnot(identical({ sort(c(a=NA,b=NA,c=3L,d=-1L),na.last=TRUE, decreasing=FALSE) }, structure(c(-1L, 3L, NA, NA), .Names = c("d", "c", "a", "b")))) }
test.micro1487 <- function() { cat('1487\n'); stopifnot(identical({ sort(c(3,NA,1,d=10), decreasing=FALSE, index.return=TRUE) }, structure(list(x = structure(c(1, 3, 10), .Names = c("", "", "d")), ix = c(2L, 1L, 3L)), .Names = c("x", "ix")))) }
test.micro1488 <- function() { cat('1488\n'); stopifnot(identical({ sort(3:1, index.return=TRUE) }, structure(list(x = 1:3, ix = c(3L, 2L, 1L)), .Names = c("x", "ix")))) }
test.micro1489 <- function() { cat('1489\n'); stopifnot(identical({ sort(c(TRUE,FALSE,FALSE,NA,FALSE), index.return=TRUE)$ix }, c(2L, 3L, 4L, 1L))) }
test.micro1490 <- function() { cat('1490\n'); stopifnot(identical({ sort(c('a','z','Z','xxxz','zza','b'), index.return=TRUE)$ix }, c(1L, 6L, 4L, 2L, 3L, 5L))) }
test.micro1491 <- function() { cat('1491\n'); stopifnot(identical({ sort(c(a=NA,1,b=NA,0/0,2,3), na.last=TRUE, decreasing=FALSE) }, structure(c(1, 2, 3, NA, NA, NaN), .Names = c("", "", "", "a", "b", "")))) }
test.micro1492 <- function() { cat('1492\n'); stopifnot(identical({ sort(c(a=NA,1L,b=NA,0L,2L,-3L), na.last=TRUE, decreasing=TRUE) }, structure(c(2L, 1L, 0L, -3L, NA, NA), .Names = c("", "", "", "", "a", "b")))) }
test.micro1493 <- function() { cat('1493\n'); stopifnot(identical({ sort(c(a=NA,1L,b=NA,0L,2L,-3L), na.last=FALSE, decreasing=TRUE) }, structure(c(NA, NA, 2L, 1L, 0L, -3L), .Names = c("a", "b", "", "", "", "")))) }
test.micro1494 <- function() { cat('1494\n'); stopifnot(identical({ sort(c(a=NA,1L,b=NA,0L,2L,-3L), na.last=NA, decreasing=TRUE) }, structure(c(2L, 1L, 0L, -3L), .Names = c("", "", "", "")))) }
test.micro1495 <- function() { cat('1495\n'); stopifnot(identical({ sort(c('A','a'), decreasing=TRUE) }, c("A", "a"))) }
test.micro1496 <- function() { cat('1496\n'); stopifnot(identical({ sort(c('a','A'), decreasing=FALSE) }, c("a", "A"))) }
test.micro1497 <- function() { cat('1497\n'); stopifnot(identical({ sort(c('a','A','z','Z','   01','01',NA), na.last=NA, decreasing=TRUE, index.return=TRUE)$ix }, c(4L, 3L, 2L, 1L, 5L, 6L))) }
test.micro1498 <- function() { cat('1498\n'); stopifnot(identical({ sort(c('a','A','z','Z','   01','01',NA), na.last=TRUE, decreasing=FALSE) }, c("01", "   01", "a", "A", "z", "Z", NA))) }
test.micro1499 <- function() { cat('1499\n'); stopifnot(identical({ sort(c(TRUE,NA,TRUE,NA,FALSE,TRUE,NA), na.last=FALSE, decreasing=FALSE) }, c(NA, NA, NA, FALSE, TRUE, TRUE, TRUE))) }
test.micro1500 <- function() { cat('1500\n'); stopifnot(identical({ sort(c(TRUE,NA,TRUE,NA,FALSE,TRUE,NA), na.last=NA, decreasing=TRUE) }, c(TRUE, TRUE, TRUE, FALSE))) }
test.micro1507 <- function() { cat('1507\n'); stopifnot(identical({ rank(c(10,100,100,1000)) }, c(1, 2.5, 2.5, 4))) }
test.micro1508 <- function() { cat('1508\n'); stopifnot(identical({ rank(c(1000,100,100,100, 10)) }, c(5, 3, 3, 3, 1))) }
test.micro1509 <- function() { cat('1509\n'); stopifnot(identical({ rank(c(a=2,b=1,c=3,40)) }, structure(c(2, 1, 3, 4), .Names = c("a", "b", "c", "")))) }
test.micro1510 <- function() { cat('1510\n'); stopifnot(identical({ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=NA) }, structure(c(2, 1, 3, 4), .Names = c("a", "b", "c", "e")))) }
test.micro1511 <- function() { cat('1511\n'); stopifnot(identical({ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last='keep') }, structure(c(2, 1, 3, NA, 4), .Names = c("a", "b", "c", "d", "e")))) }
test.micro1512 <- function() { cat('1512\n'); stopifnot(identical({ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=TRUE) }, structure(c(2, 1, 3, 5, 4), .Names = c("a", "b", "c", "d", "e")))) }
test.micro1513 <- function() { cat('1513\n'); stopifnot(identical({ rank(c(a=2,b=1,c=3,d=NA,e=40), na.last=FALSE) }, structure(c(3, 2, 4, 1, 5), .Names = c("a", "b", "c", "d", "e")))) }
test.micro1514 <- function() { cat('1514\n'); stopifnot(identical({ rank(c(a=1,b=1,c=3,d=NA,e=3), na.last=FALSE, ties.method='max') }, structure(c(3L, 3L, 5L, 1L, 5L), .Names = c("a", "b", "c", "d", "e")))) }
test.micro1515 <- function() { cat('1515\n'); stopifnot(identical({ rank(c(a=1,b=1,c=3,d=NA,e=3), na.last=NA, ties.method='min') }, structure(c(1L, 1L, 3L, 3L), .Names = c("a", "b", "c", "e")))) }
test.micro1516 <- function() { cat('1516\n'); stopifnot(identical({ rank(c(1000, 100, 100, NA, 1, 20), ties.method='first') }, c(5L, 3L, 4L, 6L, 1L, 2L))) }
test.micro1521 <- function() { cat('1521\n'); stopifnot(identical({ round(det(matrix(c(1,2,4,5),nrow=2))) }, -3)) }
test.micro1522 <- function() { cat('1522\n'); stopifnot(identical({ round(det(matrix(c(1,-3,4,-5),nrow=2))) }, 7)) }
test.micro1523 <- function() { cat('1523\n'); stopifnot(identical({ round(det(matrix(c(1,0,4,NA),nrow=2))) }, NA_real_)) }
test.micro1524 <- function() { cat('1524\n'); stopifnot(identical({ fft(1:4) }, c(10+0i, -2+2i, -2+0i, -2-2i))) }
test.micro1525 <- function() { cat('1525\n'); stopifnot(identical({ fft(1:4, inverse=TRUE) }, c(10+0i, -2-2i, -2+0i, -2+2i))) }
test.micro1526 <- function() { cat('1526\n'); stopifnot(identical({ fft(10) }, 10+0i)) }
test.micro1533 <- function() { cat('1533\n'); stopifnot(identical({ qr(matrix(1:6,nrow=2), LAPACK=FALSE)$pivot }, 1:3)) }
test.micro1534 <- function() { cat('1534\n'); stopifnot(identical({ qr(matrix(1:6,nrow=2), LAPACK=FALSE)$rank }, 2L)) }
test.micro1535 <- function() { cat('1535\n'); stopifnot(identical({ round( qr(matrix(1:6,nrow=2), LAPACK=FALSE)$qraux, digits=5 ) }, c(1.44721, 0.89443, 1.78885))) }
test.micro1537 <- function() { cat('1537\n'); stopifnot(identical({ x <- qr(t(cbind(1:10,2:11)), LAPACK=TRUE) ; qr.coef(x, 1:2) }, c(1, NA, NA, NA, NA, NA, NA, NA, NA, 0))) }
test.micro1539 <- function() { cat('1539\n'); stopifnot(identical({ x <- qr(c(3,1,2), LAPACK=TRUE) ; round( qr.coef(x, c(1,3,2)), digits=5 ) }, 0.71429)) }
test.micro1540 <- function() { cat('1540\n'); stopifnot(identical({ x <- qr(t(cbind(1:10,2:11)), LAPACK=FALSE) ; qr.coef(x, 1:2) }, c(1, 0, NA, NA, NA, NA, NA, NA, NA, NA))) }
test.micro1541 <- function() { cat('1541\n'); stopifnot(identical({ x <- qr(c(3,1,2), LAPACK=FALSE) ; round( qr.coef(x, c(1,3,2)), digits=5 ) }, 0.71429)) }
test.micro1542 <- function() { cat('1542\n'); stopifnot(identical({ m <- matrix(c(1,0,0,0,1,0,0,0,1),nrow=3) ; x <- qr(m, LAPACK=FALSE) ; qr.coef(x, 1:3) }, c(1, 2, 3))) }
test.micro1543 <- function() { cat('1543\n'); stopifnot(identical({ x <- qr(cbind(1:3,2:4), LAPACK=FALSE) ; round( qr.coef(x, 1:3), digits=5 ) }, c(1, 0))) }
test.micro1544 <- function() { cat('1544\n'); stopifnot(identical({ round( qr.solve(qr(c(1,3,4,2)), c(1,2,3,4)), digits=5 ) }, 0.9)) }
test.micro1545 <- function() { cat('1545\n'); stopifnot(identical({ round( qr.solve(c(1,3,4,2), c(1,2,3,4)), digits=5) }, 0.9)) }
test.micro1550 <- function() { cat('1550\n'); stopifnot(identical({ round(0.4) }, 0)) }
test.micro1551 <- function() { cat('1551\n'); stopifnot(identical({ round(0.5) }, 0)) }
test.micro1552 <- function() { cat('1552\n'); stopifnot(identical({ round(0.6) }, 1)) }
test.micro1553 <- function() { cat('1553\n'); stopifnot(identical({ round(1.5) }, 2)) }
test.micro1554 <- function() { cat('1554\n'); stopifnot(identical({ round(1L) }, 1)) }
test.micro1555 <- function() { cat('1555\n'); stopifnot(identical({ round(1.123456,digit=2.8) }, 1.123)) }
test.micro1556 <- function() { cat('1556\n'); stopifnot(identical({ round(1/0) }, Inf)) }
test.micro1557 <- function() { cat('1557\n'); stopifnot(identical({ delayedAssign('x', y); y <- 10; x }, 10)) }
test.micro1558 <- function() { cat('1558\n'); stopifnot(identical({ delayedAssign('x', a+b); a <- 1 ; b <- 3 ; x }, 4)) }
test.micro1559 <- function() { cat('1559\n'); stopifnot(identical({ f <- function() { delayedAssign('x', y); y <- 10; x  } ; f() }, 10)) }
test.micro1560 <- function() { cat('1560\n'); stopifnot(identical({ h <- new.env(parent=emptyenv()) ; delayedAssign('x', y, h, h) ; assign('y', 2, h) ; get('x', h) }, 2)) }
test.micro1561 <- function() { cat('1561\n'); stopifnot(identical({ h <- new.env(parent=emptyenv()) ; assign('x', 1, h) ; delayedAssign('x', y, h, h) ; assign('y', 2, h) ; get('x', h) }, 2)) }
test.micro1562 <- function() { cat('1562\n'); stopifnot(identical({ f <- function(...) { delayedAssign('x', ..1) ; y <<- x } ; f(10) ; y }, 10)) }
test.micro1563 <- function() { cat('1563\n'); stopifnot(identical({ f <- function() { delayedAssign('x', 3); delayedAssign('x', 2); x } ; f() }, 2)) }
test.micro1564 <- function() { cat('1564\n'); stopifnot(identical({ f <- function() { x <- 4 ; delayedAssign('x', y); y <- 10; x  } ; f() }, 10)) }
test.micro1565 <- function() { cat('1565\n'); stopifnot(identical({ f <- function(a = 2 + 3) { missing(a) } ; f() }, TRUE)) }
test.micro1566 <- function() { cat('1566\n'); stopifnot(identical({ f <- function(a = z) { missing(a) } ; f() }, TRUE)) }
test.micro1567 <- function() { cat('1567\n'); stopifnot(identical({ f <- function(a = 2 + 3) { a;  missing(a) } ; f() }, TRUE)) }
test.micro1568 <- function() { cat('1568\n'); stopifnot(identical({ f <- function(a) { g(a) } ;  g <- function(b) { missing(b) } ; f() }, TRUE)) }
test.micro1569 <- function() { cat('1569\n'); stopifnot(identical({ f <- function(a = 2) { g(a) } ; g <- function(b) { missing(b) } ; f() }, FALSE)) }
test.micro1570 <- function() { cat('1570\n'); stopifnot(identical({ f <- function(a = z) {  g(a) } ; g <- function(b) { missing(b) } ; f() }, FALSE)) }
test.micro1571 <- function() { cat('1571\n'); stopifnot(identical({ f <- function(a = z, z) {  g(a) } ; g <- function(b) { missing(b) } ; f() }, TRUE)) }
test.micro1572 <- function() { cat('1572\n'); stopifnot(identical({ f <- function(a) { g(a) } ; g <- function(b=2) { missing(b) } ; f() }, TRUE)) }
test.micro1573 <- function() { cat('1573\n'); stopifnot(identical({ f <- function(x = y, y = x) { g(x, y) } ; g <- function(x, y) { missing(x) } ; f() }, TRUE)) }
test.micro1574 <- function() { cat('1574\n'); stopifnot(identical({ f <- function(a,b,c) { missing(b) } ; f(1,,2) }, TRUE)) }
test.micro1575 <- function() { cat('1575\n'); stopifnot(identical({ g <- function(a, b, c) { b } ; f <- function(a,b,c) { g(a,b=2,c) } ; f(1,,2) }, 2)) }
test.micro1576 <- function() { cat('1576\n'); stopifnot(identical({ f <- function(x) { missing(x) } ; f(a) }, FALSE)) }
test.micro1577 <- function() { cat('1577\n'); stopifnot(identical({ f <- function(a) { g <- function(b) { before <- missing(b) ; a <<- 2 ; after <- missing(b) ; c(before, after) } ; g(a) } ; f() }, c(TRUE, FALSE))) }
test.micro1578 <- function() { cat('1578\n'); stopifnot(identical({ f <- function(...) { g(...) } ;  g <- function(b=2) { missing(b) } ; f() }, TRUE)) }
test.micro1579 <- function() { cat('1579\n'); stopifnot(identical({ f <- function(...) { missing(..2) } ; f(x + z, a * b) }, FALSE)) }
test.micro1582 <- function() { cat('1582\n'); stopifnot(identical({ typeof(quote(1)) }, "double")) }
test.micro1583 <- function() { cat('1583\n'); stopifnot(identical({ typeof(quote(x + y)) }, "language")) }
test.micro1585 <- function() { cat('1585\n'); stopifnot(identical({ typeof(quote(x)) }, "symbol")) }
test.micro1603 <- function() { cat('1603\n'); stopifnot(identical({ f <- function(y) { substitute(y) } ; typeof(f()) }, "symbol")) }
test.micro1621 <- function() { cat('1621\n'); stopifnot(identical({ f <- function(...) { g <- function() { list(...)$a } ; g() } ; f(a=1) }, 1)) }
test.micro1622 <- function() { cat('1622\n'); stopifnot(identical({ f <- function(...) { l <- list(...) ; l[[1]] <- 10; ..1 } ; f(11,12,13) }, 11)) }
test.micro1623 <- function() { cat('1623\n'); stopifnot(identical({ g <- function(...) { length(list(...)) } ; f <- function(...) { g(..., ...) } ; f(z = 1, g = 31) }, 4L)) }
test.micro1624 <- function() { cat('1624\n'); stopifnot(identical({ g <- function(...) { max(...) } ; g(1,2) }, 2)) }
test.micro1625 <- function() { cat('1625\n'); stopifnot(identical({ g <- function(...) { `-`(...) } ; g(1,2) }, -1)) }
test.micro1626 <- function() { cat('1626\n'); stopifnot(identical({ f <- function(...) { list(a=1,...) } ; f(b=2,3) }, structure(list(a = 1, b = 2, 3), .Names = c("a", "b", "")))) }
test.micro1628 <- function() { cat('1628\n'); stopifnot(identical({ f <- function(a, ...) { list(...) } ; f(1) }, list())) }
test.micro1629 <- function() { cat('1629\n'); stopifnot(identical({ f <- function(...) { args <- list(...) ; args$name } ; f(name = 42) }, 42)) }
test.micro1631 <- function() { cat('1631\n'); stopifnot(identical({ eval(quote(x+x), list(x=1)) }, 2)) }
test.micro1632 <- function() { cat('1632\n'); stopifnot(identical({ y <- 2; eval(quote(x+y), list(x=1)) }, 3)) }
test.micro1633 <- function() { cat('1633\n'); stopifnot(identical({ y <- 2; x <- 4; eval(x + y, list(x=1)) }, 6)) }
test.micro1634 <- function() { cat('1634\n'); stopifnot(identical({ y <- 2; x <- 2 ; eval(quote(x+y), -1) }, 4)) }
test.micro1635 <- function() { cat('1635\n'); stopifnot(identical({ f <- function(x) { deparse(substitute(x)) } ; f(a + b * (c - d)) }, "a + b * (c - d)")) }
test.micro1636 <- function() { cat('1636\n'); stopifnot(identical({ sprintf('%d', 10) }, "10")) }
test.micro1637 <- function() { cat('1637\n'); stopifnot(identical({ sprintf('%7.3f', 10.1) }, " 10.100")) }
test.micro1638 <- function() { cat('1638\n'); stopifnot(identical({ sprintf('%03d', 1:3) }, c("001", "002", "003"))) }
test.micro1639 <- function() { cat('1639\n'); stopifnot(identical({ sprintf('%3d', 1:3) }, c("  1", "  2", "  3"))) }
test.micro1640 <- function() { cat('1640\n'); stopifnot(identical({ sprintf('Hello %*d', 3, 2) }, "Hello   2")) }
test.micro1641 <- function() { cat('1641\n'); stopifnot(identical({ sprintf('Hello %*2$d', 3, 2) }, "Hello  3")) }
test.micro1642 <- function() { cat('1642\n'); stopifnot(identical({ sprintf('Hello %2$*2$d', 3, 2) }, "Hello  2")) }
test.micro1643 <- function() { cat('1643\n'); stopifnot(identical({ sprintf('%4X', 26) }, "  1A")) }
test.micro1644 <- function() { cat('1644\n'); stopifnot(identical({ sprintf('%04X', 26) }, "001A")) }
test.micro1645 <- function() { cat('1645\n'); stopifnot(identical({ sprintf('%s',NULL) }, character(0))) }
test.micro1646 <- function() { cat('1646\n'); stopifnot(identical({ sprintf(c('%f','%e'),1) }, c("1.000000", "1.000000e+00"))) }
test.micro1647 <- function() { cat('1647\n'); stopifnot(identical({ sprintf(c('%f','%% %e'),1) }, c("1.000000", "% 1.000000e+00"))) }
test.micro1648 <- function() { cat('1648\n'); stopifnot(identical({ sprintf(c('%f','%e %%'),1) }, c("1.000000", "1.000000e+00 %"))) }
test.micro1649 <- function() { cat('1649\n'); stopifnot(identical({ sprintf('second %2$1.0f, first %1$5.2f, third %3$1.0f', 3.141592, 2, 3) }, "second 2, first  3.14, third 3")) }
test.micro1650 <- function() { cat('1650\n'); stopifnot(identical({ sprintf('res %4$6d',1,2,3,4,5,6,7,8,9,10,11) }, "res      4")) }
test.micro1651 <- function() { cat('1651\n'); stopifnot(identical({ sprintf('res %11$06d',1,2,3,4,5,6,7,8,9,10,11) }, "res 000011")) }
test.micro1652 <- function() { cat('1652\n'); stopifnot(identical({ sprintf('Hello %1$*11$d', 3, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12) }, "Hello            3")) }
test.micro1653 <- function() { cat('1653\n'); stopifnot(identical({ sprintf('Hello %1$*3$d', 3, 2, 4L) }, "Hello    3")) }
test.micro1654 <- function() { cat('1654\n'); stopifnot(identical({ sprintf('Hello %*i', 2, 3) }, "Hello  3")) }
test.micro1655 <- function() { cat('1655\n'); stopifnot(identical({ sprintf('Hello %d == %s', TRUE, TRUE) }, "Hello 1 == TRUE")) }
test.micro1656 <- function() { cat('1656\n'); stopifnot(identical({ sprintf('Hello %d == %s', 1L, 1L) }, "Hello 1 == 1")) }
test.micro1657 <- function() { cat('1657\n'); stopifnot(identical({ sprintf('Hello %s', 'World!') }, "Hello World!")) }
test.micro1658 <- function() { cat('1658\n'); stopifnot(identical({ sprintf('Hello %d', 100) }, "Hello 100")) }
test.micro1659 <- function() { cat('1659\n'); stopifnot(identical({ sprintf('Hello %f %f %f %f', 0/0, -1/0, 1/0, 1[2]) }, "Hello NaN -Inf Inf NA")) }
test.micro1660 <- function() { cat('1660\n'); stopifnot(identical({ sprintf('Hello %5.f %5.f %5.f %5.f', 0/0, -1/0, 1/0, 1[2]) }, "Hello   NaN  -Inf   Inf    NA")) }
test.micro1661 <- function() { cat('1661\n'); stopifnot(identical({ sprintf('% f',1.234556) }, " 1.234556")) }
test.micro1662 <- function() { cat('1662\n'); stopifnot(identical({ sprintf('Hello %s', 0/0) }, "Hello NaN")) }
test.micro1663 <- function() { cat('1663\n'); stopifnot(identical({ sprintf('Hello %x', 1L[2]) }, "Hello NA")) }
test.micro1664 <- function() { cat('1664\n'); stopifnot(identical({ sprintf('Hello %g', NA) }, "Hello NA")) }
test.micro1665 <- function() { cat('1665\n'); stopifnot(identical({ sprintf('Hello %g', 1L[2]) }, "Hello NA")) }
test.micro1666 <- function() { cat('1666\n'); stopifnot(identical({ sprintf('Hello %i', NA) }, "Hello NA")) }
test.micro1667 <- function() { cat('1667\n'); stopifnot(identical({ identical(1,1) }, TRUE)) }
test.micro1668 <- function() { cat('1668\n'); stopifnot(identical({ identical(1L,1) }, FALSE)) }
test.micro1669 <- function() { cat('1669\n'); stopifnot(identical({ identical(1:3, c(1L,2L,3L)) }, TRUE)) }
test.micro1670 <- function() { cat('1670\n'); stopifnot(identical({ identical(0/0,1[2]) }, FALSE)) }
test.micro1671 <- function() { cat('1671\n'); stopifnot(identical({ identical(list(1, list(2)), list(list(1), 1)) }, FALSE)) }
test.micro1672 <- function() { cat('1672\n'); stopifnot(identical({ identical(list(1, list(2)), list(1, list(2))) }, TRUE)) }
test.micro1673 <- function() { cat('1673\n'); stopifnot(identical({ x <- 1 ; attr(x, 'my') <- 10; identical(x, 1) }, FALSE)) }
test.micro1674 <- function() { cat('1674\n'); stopifnot(identical({ x <- 1 ; attr(x, 'my') <- 10; y <- 1 ; attr(y, 'my') <- 10 ; identical(x,y) }, TRUE)) }
test.micro1675 <- function() { cat('1675\n'); stopifnot(identical({ x <- 1 ; attr(x, 'my') <- 10; y <- 1 ; attr(y, 'my') <- 11 ; identical(x,y) }, FALSE)) }
test.micro1676 <- function() { cat('1676\n'); stopifnot(identical({ x <- 1 ; attr(x, 'hello') <- 2 ; attr(x, 'my') <- 10;  attr(x, 'hello') <- NULL ; y <- 1 ; attr(y, 'my') <- 10 ; identical(x,y) }, TRUE)) }
test.micro1677 <- function() { cat('1677\n'); stopifnot(identical({ identical(1,c) }, FALSE)) }
test.micro1678 <- function() { cat('1678\n'); stopifnot(identical({ identical(c,1) }, FALSE)) }
test.micro1679 <- function() { cat('1679\n'); stopifnot(identical({ identical(1:4, matrix(1:4,nrow=2)) }, FALSE)) }
test.micro1680 <- function() { cat('1680\n'); stopifnot(identical({ identical(1:4, c(a=1L,b=2L,3L,4L)) }, FALSE)) }
test.micro1681 <- function() { cat('1681\n'); stopifnot(identical({ identical(as.list(1:4), c(a=1L,b=2L,3L,4L)) }, FALSE)) }
test.micro1682 <- function() { cat('1682\n'); stopifnot(identical({ identical(as.list(1:4),1:4) }, FALSE)) }
test.micro1683 <- function() { cat('1683\n'); stopifnot(identical({ identical(c,c) }, TRUE)) }
test.micro1684 <- function() { cat('1684\n'); stopifnot(identical({ identical('1+2i',1+2i) }, FALSE)) }
test.micro1685 <- function() { cat('1685\n'); stopifnot(identical({ identical(1L, 1:1) }, TRUE)) }
test.micro1686 <- function() { cat('1686\n'); stopifnot(identical({ identical('hello', 'hello') }, TRUE)) }
test.micro1687 <- function() { cat('1687\n'); stopifnot(identical({ identical(1+2i, 0+1.0+2.0i-0) }, TRUE)) }
test.micro1688 <- function() { cat('1688\n'); stopifnot(identical({ identical(1+2i, 0+1.0+2.0i-0.001) }, FALSE)) }
test.micro1689 <- function() { cat('1689\n'); stopifnot(identical({ identical(0+0i,0) }, FALSE)) }
test.micro1690 <- function() { cat('1690\n'); stopifnot(identical({ identical(TRUE,as.logical(10)) }, TRUE)) }
test.micro1691 <- function() { cat('1691\n'); stopifnot(identical({ identical(TRUE,1L) }, FALSE)) }
test.micro1692 <- function() { cat('1692\n'); stopifnot(identical({ identical(as.raw(11), as.raw(10+1)) }, TRUE)) }
test.micro1693 <- function() { cat('1693\n'); stopifnot(identical({ identical(as.raw(11), 11) }, FALSE)) }
test.micro1694 <- function() { cat('1694\n'); stopifnot(identical({ identical(11, as.raw(11)) }, FALSE)) }
test.micro1695 <- function() { cat('1695\n'); stopifnot(identical({ identical(NULL,0) }, FALSE)) }
test.micro1696 <- function() { cat('1696\n'); stopifnot(identical({ identical(list(list(1,2),list(3)), list(list(1,2),list(3+0))) }, TRUE)) }
test.micro1697 <- function() { cat('1697\n'); stopifnot(identical({ identical(list(list(1,2),list(3)), list(list(1,2),list(3+0), list(4))) }, FALSE)) }
test.micro1698 <- function() { cat('1698\n'); stopifnot(identical({ identical(c('hello','hi'),c('hello','hI')) }, FALSE)) }
test.micro1699 <- function() { cat('1699\n'); stopifnot(identical({ identical(c('hello','hi'),c('hello',NA)) }, FALSE)) }
test.micro1700 <- function() { cat('1700\n'); stopifnot(identical({ identical(c('hello',NA),c('hello',NA)) }, TRUE)) }
test.micro1701 <- function() { cat('1701\n'); stopifnot(identical({ x <- 'hi' ; identical(c('hello',x),c('hello',x)) }, TRUE)) }
test.micro1702 <- function() { cat('1702\n'); stopifnot(identical({ identical(c('hello',NA),c('hello','x')) }, FALSE)) }
test.micro1703 <- function() { cat('1703\n'); stopifnot(identical({ identical(c('hello',NA),c('hello')) }, FALSE)) }
test.micro1704 <- function() { cat('1704\n'); stopifnot(identical({ identical(c(0/0,NA),c(NA,0/0)) }, FALSE)) }
test.micro1705 <- function() { cat('1705\n'); stopifnot(identical({ identical(c(1/0,-3/0),c(2/0,-1e100/0)) }, TRUE)) }
test.micro1706 <- function() { cat('1706\n'); stopifnot(identical({ identical(c(1/0,-3/0),c(0/0,NA)) }, FALSE)) }
test.micro1707 <- function() { cat('1707\n'); stopifnot(identical({ identical(c(0/0,NA),c(1/0,-3/0)) }, FALSE)) }
test.micro1708 <- function() { cat('1708\n'); stopifnot(identical({ identical(c(1+1,NA),c(2,NA)) }, TRUE)) }
test.micro1709 <- function() { cat('1709\n'); stopifnot(identical({ identical(c(1+2i,3+4i), c(1+2i,3+2i)) }, FALSE)) }
test.micro1710 <- function() { cat('1710\n'); stopifnot(identical({ identical(c(1+2i,3+4i), c(1+2i,2+4i)) }, FALSE)) }
test.micro1711 <- function() { cat('1711\n'); stopifnot(identical({ identical(c(1+2i,3+4i), c(1+2i)) }, FALSE)) }
test.micro1712 <- function() { cat('1712\n'); stopifnot(identical({ identical(c(1,2),c(1)) }, FALSE)) }
test.micro1713 <- function() { cat('1713\n'); stopifnot(identical({ identical(1:2,1:1) }, FALSE)) }
test.micro1714 <- function() { cat('1714\n'); stopifnot(identical({ identical(1:2,c(1L,3L)) }, FALSE)) }
test.micro1715 <- function() { cat('1715\n'); stopifnot(identical({ identical(c(TRUE,FALSE), c(TRUE,NA)) }, FALSE)) }
test.micro1716 <- function() { cat('1716\n'); stopifnot(identical({ identical(c(TRUE,FALSE), c(TRUE)) }, FALSE)) }
test.micro1717 <- function() { cat('1717\n'); stopifnot(identical({ identical(as.raw(11:12), as.raw(11)) }, FALSE)) }
test.micro1718 <- function() { cat('1718\n'); stopifnot(identical({ identical(as.raw(11:12), as.raw(c(11,13))) }, FALSE)) }
test.micro1719 <- function() { cat('1719\n'); stopifnot(identical({ x <- 1 ; attr(x,'my') <- 1 ; identical(1, x) }, FALSE)) }
test.micro1720 <- function() { cat('1720\n'); stopifnot(identical({ x <- 1 ; attr(x,'my') <- 1 ; attr(x,'my') <- NULL ; identical(1, x) }, TRUE)) }
test.micro1721 <- function() { cat('1721\n'); stopifnot(identical({ x <- 1 ; attr(x,'my') <- 1 ; attr(x,'my') <- NULL ; identical(x, 1) }, TRUE)) }
test.micro1722 <- function() { cat('1722\n'); stopifnot(identical({ x <- 1 ; attr(x,'my') <- 1 ; attr(x,'my') <- NULL ; y <- 1 ; attr(y,'hi') <- 2 ; identical(x, y) }, FALSE)) }
test.micro1723 <- function() { cat('1723\n'); stopifnot(identical({ x <- 1 ; attr(x,'my') <- 1 ; y <- 1 ; attr(y,'my') <- 2 ; identical(x, y) }, FALSE)) }
test.micro1724 <- function() { cat('1724\n'); stopifnot(identical({ l <- list(1,2,3); l[[2]] <- NULL; identical(l, list(1,3)) }, TRUE)) }
test.micro1725 <- function() { cat('1725\n'); stopifnot(identical({ x <- 1 ; attr(x,'my') <- 1 ; y <- 1 ; attr(y,'hi') <- 1 ; identical(x, y) }, FALSE)) }
test.micro1726 <- function() { cat('1726\n'); stopifnot(identical({ identical(c(a=1,b=2,c=3),c(a=1,aa=2,c=3)) }, FALSE)) }
test.micro1727 <- function() { cat('1727\n'); stopifnot(identical({ identical(c(a=1,b=2,c=3),c(a=1,b=2)) }, FALSE)) }
test.micro1728 <- function() { cat('1728\n'); stopifnot(identical({ identical(c(a=1,b=2),c(a=1,b=2)) }, TRUE)) }
test.micro1729 <- function() { cat('1729\n'); stopifnot(identical({ identical(c(a=1,b=2),c(1,2)) }, FALSE)) }
test.micro1730 <- function() { cat('1730\n'); stopifnot(identical({ identical(c(1,2), c(a=1,b=2)) }, FALSE)) }
test.micro1731 <- function() { cat('1731\n'); stopifnot(identical({ x <- list(1,b=2,3) ; x[[2]] <- NULL ; identical(x,list(1,3)) }, FALSE)) }
test.micro1732 <- function() { cat('1732\n'); stopifnot(identical({ cur <- getwd(); cur1 <- setwd(getwd()) ; cur2 <- getwd() ; cur == cur1 && cur == cur2 }, TRUE)) }
test.micro1733 <- function() { cat('1733\n'); stopifnot(identical({ cur <- getwd(); cur1 <- setwd(c(cur, 'dummy')) ; cur2 <- getwd() ; cur == cur1 }, TRUE)) }
test.micro1734 <- function() { cat('1734\n'); stopifnot(identical({ list.files('test/r/simple/data/tree1') }, character(0))) }
test.micro1735 <- function() { cat('1735\n'); stopifnot(identical({ list.files('test/r/simple/data/tree1', recursive=TRUE) }, character(0))) }
test.micro1736 <- function() { cat('1736\n'); stopifnot(identical({ list.files('test/r/simple/data/tree1', recursive=TRUE, pattern='.*dummy.*') }, character(0))) }
test.micro1737 <- function() { cat('1737\n'); stopifnot(identical({ list.files('test/r/simple/data/tree1', recursive=TRUE, pattern='dummy') }, character(0))) }
test.micro1738 <- function() { cat('1738\n'); stopifnot(identical({ list.files('test/r/simple/data/tree1', pattern='*.tx') }, character(0))) }
test.micro1739 <- function() { cat('1739\n'); stopifnot(identical({ all(TRUE, FALSE, NA,  na.rm=FALSE) }, FALSE)) }
test.micro1740 <- function() { cat('1740\n'); stopifnot(identical({ all(TRUE, FALSE, NA,  na.rm=TRUE) }, FALSE)) }
test.micro1741 <- function() { cat('1741\n'); stopifnot(identical({ all(TRUE, TRUE, NA,  na.rm=TRUE) }, TRUE)) }
test.micro1742 <- function() { cat('1742\n'); stopifnot(identical({ all(TRUE, TRUE, NA,  na.rm=FALSE) }, NA)) }
test.micro1743 <- function() { cat('1743\n'); stopifnot(identical({ all() }, TRUE)) }
test.micro1744 <- function() { cat('1744\n'); stopifnot(identical({ any() }, FALSE)) }
test.micro1745 <- function() { cat('1745\n'); stopifnot(identical({ any(TRUE, TRUE, NA,  na.rm=TRUE) }, TRUE)) }
test.micro1746 <- function() { cat('1746\n'); stopifnot(identical({ any(TRUE, FALSE, NA,  na.rm=TRUE) }, TRUE)) }
test.micro1747 <- function() { cat('1747\n'); stopifnot(identical({ any(FALSE, NA,  na.rm=TRUE) }, FALSE)) }
test.micro1748 <- function() { cat('1748\n'); stopifnot(identical({ any(FALSE, NA,  na.rm=FALSE) }, NA)) }
test.micro1755 <- function() { cat('1755\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; l <- call('f', 2, 3) ; eval(l) }, 5)) }
test.micro1756 <- function() { cat('1756\n'); stopifnot(identical({ f <- function(a, b) { a + b } ; x <- 1 ; y <- 2 ; l <- call('f', x, y) ; x <- 10 ; eval(l) }, 3)) }
test.micro1757 <- function() { cat('1757\n'); stopifnot(identical({ s <- proc.time()[3] ; e <- proc.time()[3] ; e >= s }, structure(TRUE, .Names = "elapsed"))) }
test.micro1765 <- function() { cat('1765\n'); stopifnot(identical({ x<-c(1,2,3,4);y<-c(10,2); x<=y }, c(TRUE, TRUE, TRUE, FALSE))) }
test.micro1766 <- function() { cat('1766\n'); stopifnot(identical({ x<-c(1,2,3,4);y<-2.5; x<=y }, c(TRUE, TRUE, FALSE, FALSE))) }
test.micro1767 <- function() { cat('1767\n'); stopifnot(identical({ x<-c(1,2,3,4);y<-c(2.5+NA,2.5); x<=y }, c(NA, TRUE, NA, FALSE))) }
test.micro1768 <- function() { cat('1768\n'); stopifnot(identical({ x<-c(1L,2L,3L,4L);y<-c(2.5+NA,2.5); x<=y }, c(NA, TRUE, NA, FALSE))) }
test.micro1769 <- function() { cat('1769\n'); stopifnot(identical({ x<-c(1L,2L,3L,4L);y<-c(TRUE,FALSE); x<=y }, c(TRUE, FALSE, FALSE, FALSE))) }
test.micro1770 <- function() { cat('1770\n'); stopifnot(identical({ x<-c(1L,2L,3L,4L);y<-1.5; x<=y }, c(TRUE, FALSE, FALSE, FALSE))) }
test.micro1771 <- function() { cat('1771\n'); stopifnot(identical({ c(1:3,4,5)==1:5 }, c(TRUE, TRUE, TRUE, TRUE, TRUE))) }
test.micro1772 <- function() { cat('1772\n'); stopifnot(identical({ 3 != 1:2 }, c(TRUE, TRUE))) }
test.micro1773 <- function() { cat('1773\n'); stopifnot(identical({ b <- 1:3 ; z <- FALSE ; b[2==2] }, 1:3)) }
test.micro1774 <- function() { cat('1774\n'); stopifnot(identical({ 1:3 == TRUE }, c(TRUE, FALSE, FALSE))) }
test.micro1775 <- function() { cat('1775\n'); stopifnot(identical({ TRUE == 1:3 }, c(TRUE, FALSE, FALSE))) }
test.micro1776 <- function() { cat('1776\n'); stopifnot(identical({ c(1,2) < c(2,1,4) }, c(TRUE, FALSE, TRUE))) }
test.micro1777 <- function() { cat('1777\n'); stopifnot(identical({ c(2,1,4) < c(1,2) }, c(FALSE, TRUE, FALSE))) }
test.micro1778 <- function() { cat('1778\n'); stopifnot(identical({ c(1L,2L) < c(2L,1L,4L) }, c(TRUE, FALSE, TRUE))) }
test.micro1779 <- function() { cat('1779\n'); stopifnot(identical({ c(2L,1L,4L) < c(1L,2L) }, c(FALSE, TRUE, FALSE))) }
test.micro1780 <- function() { cat('1780\n'); stopifnot(identical({ c(TRUE,FALSE,FALSE) < c(TRUE,TRUE) }, c(FALSE, TRUE, TRUE))) }
test.micro1781 <- function() { cat('1781\n'); stopifnot(identical({ c(TRUE,TRUE) == c(TRUE,FALSE,FALSE) }, c(TRUE, FALSE, FALSE))) }
test.micro1782 <- function() { cat('1782\n'); stopifnot(identical({ as.raw(c(1,2)) < as.raw(c(2,1,4)) }, c(TRUE, FALSE, TRUE))) }
test.micro1783 <- function() { cat('1783\n'); stopifnot(identical({ as.raw(c(2,1,4)) < as.raw(c(1,2)) }, c(FALSE, TRUE, FALSE))) }
test.micro1784 <- function() { cat('1784\n'); stopifnot(identical({ c('hi','hello','bye') > c('cau', 'ahoj') }, c(TRUE, TRUE, FALSE))) }
test.micro1785 <- function() { cat('1785\n'); stopifnot(identical({ c('cau', 'ahoj') != c('hi','hello','bye') }, c(TRUE, TRUE, TRUE))) }
test.micro1786 <- function() { cat('1786\n'); stopifnot(identical({ c(1+1i,2+2i) == c(2+1i,1+2i,1+1i) }, c(FALSE, FALSE, TRUE))) }
test.micro1787 <- function() { cat('1787\n'); stopifnot(identical({ c(2+1i,1+2i,1+1i) == c(1+1i, 2+2i) }, c(FALSE, FALSE, TRUE))) }
test.micro1788 <- function() { cat('1788\n'); stopifnot(identical({ as.raw(c(2,1,4)) < raw() }, logical(0))) }
test.micro1789 <- function() { cat('1789\n'); stopifnot(identical({ raw() < as.raw(c(2,1,4)) }, logical(0))) }
test.micro1790 <- function() { cat('1790\n'); stopifnot(identical({ 1:3 < integer() }, logical(0))) }
test.micro1791 <- function() { cat('1791\n'); stopifnot(identical({ integer() < 1:3 }, logical(0))) }
test.micro1792 <- function() { cat('1792\n'); stopifnot(identical({ c(1,2,3) < double() }, logical(0))) }
test.micro1793 <- function() { cat('1793\n'); stopifnot(identical({ double() == c(1,2,3) }, logical(0))) }
test.micro1794 <- function() { cat('1794\n'); stopifnot(identical({ c(TRUE,FALSE) < logical() }, logical(0))) }
test.micro1795 <- function() { cat('1795\n'); stopifnot(identical({ logical() == c(FALSE, FALSE) }, logical(0))) }
test.micro1796 <- function() { cat('1796\n'); stopifnot(identical({ c(1+2i, 3+4i) == (1+2i)[0] }, logical(0))) }
test.micro1797 <- function() { cat('1797\n'); stopifnot(identical({ (1+2i)[0] == c(2+3i, 4+1i) }, logical(0))) }
test.micro1798 <- function() { cat('1798\n'); stopifnot(identical({ c('hello', 'hi') == character() }, logical(0))) }
test.micro1799 <- function() { cat('1799\n'); stopifnot(identical({ character() > c('hello', 'hi') }, logical(0))) }
test.micro1800 <- function() { cat('1800\n'); stopifnot(identical({ c(1,2,3,4) != c(1,NA) }, c(FALSE, NA, TRUE, NA))) }
test.micro1801 <- function() { cat('1801\n'); stopifnot(identical({ c(1,2,NA,4) != 2 }, c(TRUE, FALSE, NA, TRUE))) }
test.micro1802 <- function() { cat('1802\n'); stopifnot(identical({ 2 != c(1,2,NA,4) }, c(TRUE, FALSE, NA, TRUE))) }
test.micro1803 <- function() { cat('1803\n'); stopifnot(identical({ c(1,2,NA,4) == 2 }, c(FALSE, TRUE, NA, FALSE))) }
test.micro1804 <- function() { cat('1804\n'); stopifnot(identical({ 2 == c(1,2,NA,4) }, c(FALSE, TRUE, NA, FALSE))) }
test.micro1805 <- function() { cat('1805\n'); stopifnot(identical({ c('hello', NA) < c('hi', NA) }, c(TRUE, NA))) }
test.micro1806 <- function() { cat('1806\n'); stopifnot(identical({ c('hello', NA) >= 'hi' }, c(FALSE, NA))) }
test.micro1807 <- function() { cat('1807\n'); stopifnot(identical({ 'hi' > c('hello', NA)  }, c(TRUE, NA))) }
test.micro1808 <- function() { cat('1808\n'); stopifnot(identical({ c('hello', NA) > c(NA, 'hi') }, c(NA, NA))) }
test.micro1809 <- function() { cat('1809\n'); stopifnot(identical({ c(1L, NA) > c(NA, 2L) }, c(NA, NA))) }
test.micro1810 <- function() { cat('1810\n'); stopifnot(identical({ c(TRUE, NA) > c(NA, FALSE) }, c(NA, NA))) }
test.micro1811 <- function() { cat('1811\n'); stopifnot(identical({ 'hi' > c('hello', 'hi')  }, c(TRUE, FALSE))) }
test.micro1812 <- function() { cat('1812\n'); stopifnot(identical({ NA > c('hello', 'hi') }, c(NA, NA))) }
test.micro1813 <- function() { cat('1813\n'); stopifnot(identical({ c('hello', 'hi') < NA }, c(NA, NA))) }
test.micro1814 <- function() { cat('1814\n'); stopifnot(identical({ 1:3 < NA }, c(NA, NA, NA))) }
test.micro1815 <- function() { cat('1815\n'); stopifnot(identical({ NA > 1:3 }, c(NA, NA, NA))) }
test.micro1816 <- function() { cat('1816\n'); stopifnot(identical({ 2L > c(1L,NA,2L) }, c(TRUE, NA, FALSE))) }
test.micro1817 <- function() { cat('1817\n'); stopifnot(identical({ c(1L,NA,2L) < 2L }, c(TRUE, NA, FALSE))) }
test.micro1818 <- function() { cat('1818\n'); stopifnot(identical({ c(0/0+1i,2+1i) == c(1+1i,2+1i) }, c(NA, TRUE))) }
test.micro1819 <- function() { cat('1819\n'); stopifnot(identical({ c(1+1i,2+1i) == c(0/0+1i,2+1i) }, c(NA, TRUE))) }
test.micro1820 <- function() { cat('1820\n'); stopifnot(identical({ integer() == 2L }, logical(0))) }
test.micro1821 <- function() { cat('1821\n'); stopifnot(identical({ 1==1 }, TRUE)) }
test.micro1822 <- function() { cat('1822\n'); stopifnot(identical({ 2==1 }, FALSE)) }
test.micro1823 <- function() { cat('1823\n'); stopifnot(identical({ 1L<=1 }, TRUE)) }
test.micro1824 <- function() { cat('1824\n'); stopifnot(identical({ 1<=0L }, FALSE)) }
test.micro1825 <- function() { cat('1825\n'); stopifnot(identical({ x<-2; f<-function(z=x) { if (z<=x) {z} else {x} } ; f(1.4)}, 1.4)) }
test.micro1826 <- function() { cat('1826\n'); stopifnot(identical({ 1==NULL }, logical(0))) }
test.micro1827 <- function() { cat('1827\n'); stopifnot(identical({ 1L==1 }, TRUE)) }
test.micro1828 <- function() { cat('1828\n'); stopifnot(identical({ TRUE==1 }, TRUE)) }
test.micro1829 <- function() { cat('1829\n'); stopifnot(identical({ TRUE==1L }, TRUE)) }
test.micro1830 <- function() { cat('1830\n'); stopifnot(identical({ 2L==TRUE }, FALSE)) }
test.micro1831 <- function() { cat('1831\n'); stopifnot(identical({ TRUE==FALSE }, FALSE)) }
test.micro1832 <- function() { cat('1832\n'); stopifnot(identical({ FALSE<=TRUE }, TRUE)) }
test.micro1833 <- function() { cat('1833\n'); stopifnot(identical({ FALSE<TRUE }, TRUE)) }
test.micro1834 <- function() { cat('1834\n'); stopifnot(identical({ TRUE>FALSE }, TRUE)) }
test.micro1835 <- function() { cat('1835\n'); stopifnot(identical({ TRUE>=FALSE }, TRUE)) }
test.micro1836 <- function() { cat('1836\n'); stopifnot(identical({ TRUE!=FALSE }, TRUE)) }
test.micro1837 <- function() { cat('1837\n'); stopifnot(identical({ 2L==NA }, NA)) }
test.micro1838 <- function() { cat('1838\n'); stopifnot(identical({ NA==2L }, NA)) }
test.micro1839 <- function() { cat('1839\n'); stopifnot(identical({ 2L==as.double(NA) }, NA)) }
test.micro1840 <- function() { cat('1840\n'); stopifnot(identical({ as.double(NA)==2L }, NA)) }
test.micro1841 <- function() { cat('1841\n'); stopifnot(identical({ 1+1i == 1-1i }, FALSE)) }
test.micro1842 <- function() { cat('1842\n'); stopifnot(identical({ 1+1i == 1+1i }, TRUE)) }
test.micro1843 <- function() { cat('1843\n'); stopifnot(identical({ 1+1i == 2+1i }, FALSE)) }
test.micro1844 <- function() { cat('1844\n'); stopifnot(identical({ 1+1i != 1+1i }, FALSE)) }
test.micro1845 <- function() { cat('1845\n'); stopifnot(identical({ 1+1i != 1-1i }, TRUE)) }
test.micro1846 <- function() { cat('1846\n'); stopifnot(identical({ 1+1i != 2+1i }, TRUE)) }
test.micro1847 <- function() { cat('1847\n'); stopifnot(identical({ 'hello' < 'hi' }, TRUE)) }
test.micro1848 <- function() { cat('1848\n'); stopifnot(identical({ 'hello' > 'hi' }, FALSE)) }
test.micro1849 <- function() { cat('1849\n'); stopifnot(identical({ 'hi' <= 'hello' }, FALSE)) }
test.micro1850 <- function() { cat('1850\n'); stopifnot(identical({ 'hi' >= 'hello' }, TRUE)) }
test.micro1851 <- function() { cat('1851\n'); stopifnot(identical({ 'hi' < 'hello' }, FALSE)) }
test.micro1852 <- function() { cat('1852\n'); stopifnot(identical({ 'hi' > 'hello' }, TRUE)) }
test.micro1853 <- function() { cat('1853\n'); stopifnot(identical({ 'hi' == 'hello' }, FALSE)) }
test.micro1854 <- function() { cat('1854\n'); stopifnot(identical({ 'hi' != 'hello' }, TRUE)) }
test.micro1855 <- function() { cat('1855\n'); stopifnot(identical({ 'hello' <= 'hi' }, TRUE)) }
test.micro1856 <- function() { cat('1856\n'); stopifnot(identical({ 'hello' >= 'hi' }, FALSE)) }
test.micro1857 <- function() { cat('1857\n'); stopifnot(identical({ 'hello' < 'hi' }, TRUE)) }
test.micro1858 <- function() { cat('1858\n'); stopifnot(identical({ 'hello' > 'hi' }, FALSE)) }
test.micro1859 <- function() { cat('1859\n'); stopifnot(identical({ 'hello' == 'hello' }, TRUE)) }
test.micro1860 <- function() { cat('1860\n'); stopifnot(identical({ 'hello' != 'hello' }, FALSE)) }
test.micro1861 <- function() { cat('1861\n'); stopifnot(identical({ 'a' <= 'b' }, TRUE)) }
test.micro1862 <- function() { cat('1862\n'); stopifnot(identical({ 'a' > 'b' }, FALSE)) }
test.micro1863 <- function() { cat('1863\n'); stopifnot(identical({ '2.0' == 2 }, FALSE)) }
test.micro1864 <- function() { cat('1864\n'); stopifnot(identical({ as.raw(15) > as.raw(10) }, TRUE)) }
test.micro1865 <- function() { cat('1865\n'); stopifnot(identical({ as.raw(15) < as.raw(10) }, FALSE)) }
test.micro1866 <- function() { cat('1866\n'); stopifnot(identical({ as.raw(15) >= as.raw(10) }, TRUE)) }
test.micro1867 <- function() { cat('1867\n'); stopifnot(identical({ as.raw(15) <= as.raw(10) }, FALSE)) }
test.micro1868 <- function() { cat('1868\n'); stopifnot(identical({ as.raw(10) >= as.raw(15) }, FALSE)) }
test.micro1869 <- function() { cat('1869\n'); stopifnot(identical({ as.raw(10) <= as.raw(15) }, TRUE)) }
test.micro1870 <- function() { cat('1870\n'); stopifnot(identical({ as.raw(15) == as.raw(10) }, FALSE)) }
test.micro1871 <- function() { cat('1871\n'); stopifnot(identical({ as.raw(15) != as.raw(10) }, TRUE)) }
test.micro1872 <- function() { cat('1872\n'); stopifnot(identical({ as.raw(15) == as.raw(15) }, TRUE)) }
test.micro1873 <- function() { cat('1873\n'); stopifnot(identical({ as.raw(15) != as.raw(15) }, FALSE)) }
test.micro1874 <- function() { cat('1874\n'); stopifnot(identical({ a <- as.raw(1) ; b <- as.raw(2) ; a < b }, TRUE)) }
test.micro1875 <- function() { cat('1875\n'); stopifnot(identical({ a <- as.raw(1) ; b <- as.raw(2) ; a > b }, FALSE)) }
test.micro1876 <- function() { cat('1876\n'); stopifnot(identical({ a <- as.raw(1) ; b <- as.raw(2) ; a == b }, FALSE)) }
test.micro1877 <- function() { cat('1877\n'); stopifnot(identical({ a <- as.raw(1) ; b <- as.raw(200) ; a < b }, TRUE)) }
test.micro1878 <- function() { cat('1878\n'); stopifnot(identical({ a <- as.raw(200) ; b <- as.raw(255) ; a < b }, TRUE)) }
test.micro1879 <- function() { cat('1879\n'); stopifnot(identical({ a <- 1 ; b <- a[2] ; a == b }, NA)) }
test.micro1880 <- function() { cat('1880\n'); stopifnot(identical({ a <- 1 ; b <- a[2] ; b > a }, NA)) }
test.micro1881 <- function() { cat('1881\n'); stopifnot(identical({ a <- 1L ; b <- a[2] ; a == b }, NA)) }
test.micro1882 <- function() { cat('1882\n'); stopifnot(identical({ a <- 1L ; b <- a[2] ; b > a }, NA)) }
test.micro1883 <- function() { cat('1883\n'); stopifnot(identical({ a <- 1L ; b <- 1[2] ; a == b }, NA)) }
test.micro1884 <- function() { cat('1884\n'); stopifnot(identical({ a <- 1L[2] ; b <- 1 ; a == b }, NA)) }
test.micro1885 <- function() { cat('1885\n'); stopifnot(identical({ a <- 1L[2] ; b <- 1 ; b > a }, NA)) }
test.micro1886 <- function() { cat('1886\n'); stopifnot(identical({ a <- 1 ; b <- 1L[2] ; a == b }, NA)) }
test.micro1887 <- function() { cat('1887\n'); stopifnot(identical({ a <- 1[2] ; b <- 1L ; b > a }, NA)) }
test.micro1888 <- function() { cat('1888\n'); stopifnot(identical({ a <- 1L ; b <- TRUE[2] ; a == b }, NA)) }
test.micro1889 <- function() { cat('1889\n'); stopifnot(identical({ a <- 1L[2] ; b <- TRUE ; a != b }, NA)) }
test.micro1890 <- function() { cat('1890\n'); stopifnot(identical({ a <- TRUE ; b <- 1L[2] ; a > b }, NA)) }
test.micro1891 <- function() { cat('1891\n'); stopifnot(identical({ a <- TRUE[2] ; b <- 1L ; a == b }, NA)) }
test.micro1892 <- function() { cat('1892\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) }, FALSE)) }
test.micro1893 <- function() { cat('1893\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1,2L) }, FALSE)) }
test.micro1894 <- function() { cat('1894\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1L,2L) ; f(1,2) }, FALSE)) }
test.micro1895 <- function() { cat('1895\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1L,2L) ; f(1L,2) }, FALSE)) }
test.micro1896 <- function() { cat('1896\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1L,2) ; f(1,2) }, FALSE)) }
test.micro1897 <- function() { cat('1897\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1L,2) ; f(1L,2L) }, FALSE)) }
test.micro1898 <- function() { cat('1898\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2L) ; f(1,2) }, FALSE)) }
test.micro1899 <- function() { cat('1899\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2L) ; f(1L,2L) }, FALSE)) }
test.micro1900 <- function() { cat('1900\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(TRUE,FALSE) ; f(TRUE,2) }, FALSE)) }
test.micro1901 <- function() { cat('1901\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(TRUE,FALSE) ; f(1L,2L) }, FALSE)) }
test.micro1902 <- function() { cat('1902\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(0L,TRUE) ; f(FALSE,2) }, FALSE)) }
test.micro1903 <- function() { cat('1903\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(0L,TRUE) ; f(0L,2L) }, FALSE)) }
test.micro1904 <- function() { cat('1904\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(0L,TRUE) ; f(2L,TRUE) }, TRUE)) }
test.micro1905 <- function() { cat('1905\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(TRUE,2L) ; f(FALSE,2) }, FALSE)) }
test.micro1906 <- function() { cat('1906\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(TRUE,2L) ; f(0L,2L) }, FALSE)) }
test.micro1907 <- function() { cat('1907\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f('hello', 'hi'[2]) }, NA)) }
test.micro1908 <- function() { cat('1908\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f('hello'[2], 'hi') }, NA)) }
test.micro1909 <- function() { cat('1909\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2, 1L[2]) }, NA)) }
test.micro1910 <- function() { cat('1910\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2[2], 1L) }, NA)) }
test.micro1911 <- function() { cat('1911\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2, 1[2]) }, NA)) }
test.micro1912 <- function() { cat('1912\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2[2], 1) }, NA)) }
test.micro1913 <- function() { cat('1913\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L, 1[2]) }, NA)) }
test.micro1914 <- function() { cat('1914\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L[2], 1) }, NA)) }
test.micro1915 <- function() { cat('1915\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L, 1L[2]) }, NA)) }
test.micro1916 <- function() { cat('1916\n'); stopifnot(identical({ f <- function(a,b) { a > b } ; f(1,2) ; f(1L,2) ; f(2L[2], 1L) }, NA)) }
test.micro1917 <- function() { cat('1917\n'); stopifnot(identical({ z <- TRUE; dim(z) <- c(1) ; dim(z == TRUE) }, 1L)) }
test.micro1921 <- function() { cat('1921\n'); stopifnot(identical({ 0/0 < c(1,2,3,4) }, c(NA, NA, NA, NA))) }
test.micro1922 <- function() { cat('1922\n'); stopifnot(identical({ 0/0 == c(1,2,3,4) }, c(NA, NA, NA, NA))) }
test.micro1923 <- function() { cat('1923\n'); stopifnot(identical({ x<-function(){1} ; x() }, 1)) }
test.micro1924 <- function() { cat('1924\n'); stopifnot(identical({ x<-function(z){z} ; x(TRUE) }, TRUE)) }
test.micro1925 <- function() { cat('1925\n'); stopifnot(identical({ x<-1 ; f<-function(){x} ; x<-2 ; f() }, 2)) }
test.micro1926 <- function() { cat('1926\n'); stopifnot(identical({ x<-1 ; f<-function(x){x} ; f(TRUE) }, TRUE)) }
test.micro1927 <- function() { cat('1927\n'); stopifnot(identical({ x<-1 ; f<-function(x){a<-1;b<-2;x} ; f(TRUE) }, TRUE)) }
test.micro1928 <- function() { cat('1928\n'); stopifnot(identical({ f<-function(x){g<-function(x) {x} ; g(x) } ; f(TRUE) }, TRUE)) }
test.micro1929 <- function() { cat('1929\n'); stopifnot(identical({ x<-1 ; f<-function(x){a<-1; b<-2; g<-function(x) {b<-3;x} ; g(b) } ; f(TRUE) }, 2)) }
test.micro1930 <- function() { cat('1930\n'); stopifnot(identical({ x<-1 ; f<-function(z) { if (z) { x<-2 } ; x } ; x<-3 ; f(FALSE) }, 3)) }
test.micro1931 <- function() { cat('1931\n'); stopifnot(identical({ f<-function() {z} ; z<-2 ; f() }, 2)) }
test.micro1932 <- function() { cat('1932\n'); stopifnot(identical({ x<-1 ; g<-function() { x<-12 ; f<-function(z) { if (z) { x<-2 } ; x } ; x<-3 ; f(FALSE) } ; g() }, 3)) }
test.micro1933 <- function() { cat('1933\n'); stopifnot(identical({ x<-function() { z<-211 ; function(a) { if (a) { z } else { 200 } } } ; f<-x() ; z<-1000 ; f(TRUE) }, 211)) }
test.micro1934 <- function() { cat('1934\n'); stopifnot(identical({ f<-function(a=1,b=2,c=3) {TRUE} ; f(,,) }, TRUE)) }
test.micro1935 <- function() { cat('1935\n'); stopifnot(identical({ f<-function(x=2) {x} ; f() }, 2)) }
test.micro1936 <- function() { cat('1936\n'); stopifnot(identical({ f<-function(a,b,c=2,d) {c} ; f(1,2,c=4,d=4) }, 4)) }
test.micro1937 <- function() { cat('1937\n'); stopifnot(identical({ f<-function(a,b,c=2,d) {c} ; f(1,2,d=8,c=1) }, 1)) }
test.micro1938 <- function() { cat('1938\n'); stopifnot(identical({ f<-function(a,b,c=2,d) {c} ; f(1,d=8,2,c=1) }, 1)) }
test.micro1939 <- function() { cat('1939\n'); stopifnot(identical({ f<-function(a,b,c=2,d) {c} ; f(d=8,1,2,c=1) }, 1)) }
test.micro1940 <- function() { cat('1940\n'); stopifnot(identical({ f<-function(a,b,c=2,d) {c} ; f(d=8,c=1,2,3) }, 1)) }
test.micro1941 <- function() { cat('1941\n'); stopifnot(identical({ f<-function(a=10,b,c=20,d=20) {c} ; f(4,3,5,1) }, 5)) }
test.micro1942 <- function() { cat('1942\n'); stopifnot(identical({ x<-1 ; z<-TRUE ; f<-function(y=x,a=z,b) { if (z) {y} else {z}} ; f(b=2) }, 1)) }
test.micro1943 <- function() { cat('1943\n'); stopifnot(identical({ x<-1 ; z<-TRUE ; f<-function(y=x,a=z,b) { if (z) {y} else {z}} ; f(2) }, 2)) }
test.micro1944 <- function() { cat('1944\n'); stopifnot(identical({ x<-1 ; f<-function(x=x) { x } ; f(x=x) }, 1)) }
test.micro1945 <- function() { cat('1945\n'); stopifnot(identical({ f<-function(z, x=if (z) 2 else 3) {x} ; f(FALSE) }, 3)) }
test.micro1946 <- function() { cat('1946\n'); stopifnot(identical({f<-function(a,b,c=2,d) {c} ; g <- function() f(d=8,c=1,2,3) ; g() ; g() }, 1)) }
test.micro1948 <- function() { cat('1948\n'); stopifnot(identical({ f<-function() { return(2) ; 3 } ; f() }, 2)) }
test.micro1949 <- function() { cat('1949\n'); stopifnot(identical({ x <- function(y) { sum(y) } ; f <- function() { x <- 1 ; x(1:10) } ; f() }, 55L)) }
test.micro1950 <- function() { cat('1950\n'); stopifnot(identical({ f <- sum ; f(1:10) }, 55L)) }
test.micro1951 <- function() { cat('1951\n'); stopifnot(identical({ x <- function(a,b) { a^b } ; f <- function() { x <- "sum" ; sapply(1, x, 2) } ; f() }, 3)) }
test.micro1952 <- function() { cat('1952\n'); stopifnot(identical({ x <- function(a,b) { a^b } ; g <- function() { x <- "sum" ; f <- function() { sapply(1, x, 2) } ; f() }  ; g() }, 3)) }
test.micro1953 <- function() { cat('1953\n'); stopifnot(identical({ x <- function(a,b) { a^b } ; f <- function() { x <- 211 ; sapply(1, x, 2) } ; f() }, 1)) }
test.micro1954 <- function() { cat('1954\n'); stopifnot(identical({ x <- function(a,b) { a^b } ; dummy <- sum ; f <- function() { x <- "dummy" ; sapply(1, x, 2) } ; f() }, 3)) }
test.micro1955 <- function() { cat('1955\n'); stopifnot(identical({ x <- function(a,b) { a^b } ; dummy <- sum ; f <- function() { x <- "dummy" ; dummy <- 200 ; sapply(1, x, 2) } ; f() }, 3)) }
test.micro1957 <- function() { cat('1957\n'); stopifnot(identical({ cnt <- 1 ; delayedAssign("z", evalat <<- cnt ) ; cnt <- 2 ; 'f<-' <- function(x, arg, value) { cnt <<- 4 ; arg * value } ; cnt <- 3; f(z, 12) <- 2 ; evalat }, 3)) }
test.micro1958 <- function() { cat('1958\n'); stopifnot(identical({ myapp <- function(f, x, y) { f(x,y) } ; myapp(function(x,y) { x + y }, 1, 2) ; myapp(sum, 1, 2) }, 3)) }
test.micro1959 <- function() { cat('1959\n'); stopifnot(identical({ myapp <- function(f, x, y) { f(x,y) } ; myapp(f = function(x,y) { x + y }, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) }, 3)) }
test.micro1960 <- function() { cat('1960\n'); stopifnot(identical({ myapp <- function(f, x, y) { f(x,y) } ; myapp(f = function(x,y) { x + y }, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) ; myapp(f = c, y = 10, x = 3) }, c(3, 10))) }
test.micro1961 <- function() { cat('1961\n'); stopifnot(identical({ myapp <- function(f, x, y) { f(x,y) } ; myapp(f = function(x,y) { x + y }, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) ; myapp(f = function(x,y) { x - y }, y = 10, x = 3) }, -7)) }
test.micro1962 <- function() { cat('1962\n'); stopifnot(identical({ myapp <- function(f, x, y) { f(x,y) } ; g <- function(x,y) { x + y } ; myapp(f = g, y = 1, x = 2) ; myapp(f = sum, x = 1, y = 2) ; myapp(f = g, y = 10, x = 3) ;  myapp(f = g, y = 11, x = 2) }, 13)) }
test.micro1963 <- function() { cat('1963\n'); stopifnot(identical({ f <- function(i) { if (i==2) { c <- sum }; c(1,2) } ; f(1) ; f(2) }, 3)) }
test.micro1964 <- function() { cat('1964\n'); stopifnot(identical({ f <- function(i) { if (i==2) { assign("c", sum) }; c(1,2) } ; f(1) ; f(2) }, 3)) }
test.micro1965 <- function() { cat('1965\n'); stopifnot(identical({ f <- function(i) { c(1,2) } ; f(1) ; c <- sum ; f(2) }, 3)) }
test.micro1966 <- function() { cat('1966\n'); stopifnot(identical({ f <- function(func, arg) { func(arg) } ; f(sum, c(3,2)) ; f(length, 1:4) }, 4L)) }
test.micro1967 <- function() { cat('1967\n'); stopifnot(identical({ f <- function(func, arg) { func(arg) } ; f(sum, c(3,2)) ; f(length, 1:4) ; f(length,1:3) }, 3L)) }
test.micro1968 <- function() { cat('1968\n'); stopifnot(identical({ f <- function(func, arg) { func(arg) } ; f(sum, c(3,2)) ; f(length, 1:4) ; f(function(i) {3}, 1) ; f(length,1:3) }, 3L)) }
test.micro1969 <- function() { cat('1969\n'); stopifnot(identical({ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; f(function(x) {TRUE}, 5) ; f(is.na, 4) }, 2)) }
test.micro1970 <- function() { cat('1970\n'); stopifnot(identical({ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) }, 1)) }
test.micro1971 <- function() { cat('1971\n'); stopifnot(identical({ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; h <- function(x) { x == x } ; f(h, 3) }, 1)) }
test.micro1972 <- function() { cat('1972\n'); stopifnot(identical({ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(is.na, 10) }, 2)) }
test.micro1973 <- function() { cat('1973\n'); stopifnot(identical({ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(c, 10) }, 1)) }
test.micro1974 <- function() { cat('1974\n'); stopifnot(identical({ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 3) ; f(function(x) { 3+4i }, 10) }, 1)) }
test.micro1975 <- function() { cat('1975\n'); stopifnot(identical({ f <- function(func, a) { if (func(a)) { 1 } else { 2 } } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(is.na, 10) }, 2)) }
test.micro1976 <- function() { cat('1976\n'); stopifnot(identical({ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4)  }, FALSE)) }
test.micro1977 <- function() { cat('1977\n'); stopifnot(identical({ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(is.na, 10) }, FALSE)) }
test.micro1978 <- function() { cat('1978\n'); stopifnot(identical({ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(length, 10) }, TRUE)) }
test.micro1979 <- function() { cat('1979\n'); stopifnot(identical({ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(g, 10) ; f(is.na,5) }, FALSE)) }
test.micro1980 <- function() { cat('1980\n'); stopifnot(identical({ f <- function(func, a) { func(a) && TRUE } ; g <- function(x) {TRUE} ; f(g, 5) ; f(is.na, 4) ; f(function(x) { x + x }, 10) }, TRUE)) }
test.micro1981 <- function() { cat('1981\n'); stopifnot(identical({ f<-function(i) { if(i==1) { 1 } else { j<-i-1 ; f(j) } } ; f(10) }, 1)) }
test.micro1982 <- function() { cat('1982\n'); stopifnot(identical({ f<-function(i) { if(i==1) { 1 } else { f(i-1) } } ; f(10) }, 1)) }
test.micro1983 <- function() { cat('1983\n'); stopifnot(identical({ f<-function(i) { if(i<=1) 1 else i*f(i-1) } ; f(10) }, 3628800)) }
test.micro1984 <- function() { cat('1984\n'); stopifnot(identical({ f<-function(i) { if(i<=1L) 1L else i*f(i-1L) } ; f(10L) }, 3628800L)) }
test.micro1985 <- function() { cat('1985\n'); stopifnot(identical({ f<-function(i) { if (i==1) { 1 } else if (i==2) { 1 } else { f(i-1) + f(i-2) } } ; f(10) }, 55)) }
test.micro1986 <- function() { cat('1986\n'); stopifnot(identical({ f<-function(i) { if (i==1L) { 1L } else if (i==2L) { 1L } else { f(i-1L) + f(i-2L) } } ; f(10L) }, 55L)) }
test.micro1987 <- function() { cat('1987\n'); stopifnot(identical({ f <- function(x = z) { z = 1 ; x } ; f() }, 1)) }
test.micro1988 <- function() { cat('1988\n'); stopifnot(identical({ z <- 1 ; f <- function(c = z) {  z <- z + 1 ; c  } ; f() }, 2)) }
test.micro1989 <- function() { cat('1989\n'); stopifnot(identical({ z <- 1 ; f <- function(c = z) { c(1,2) ; z <- z + 1 ; c  } ; f() }, 1)) }
test.micro1990 <- function() { cat('1990\n'); stopifnot(identical({ f <- function(a) { g <- function(b) { x <<- 2; b } ; g(a) } ; x <- 1 ; f(x) }, 2)) }
test.micro1991 <- function() { cat('1991\n'); stopifnot(identical({ f <- function(a) { g <- function(b) { a <<- 3; b } ; g(a) } ; x <- 1 ; f(x) }, 3)) }
test.micro1992 <- function() { cat('1992\n'); stopifnot(identical({ f <- function(x) { function() {x} } ; a <- 1 ; b <- f(a) ; a <- 10 ; b() }, 10)) }
test.micro1993 <- function() { cat('1993\n'); stopifnot(identical({ x<-function(foo,bar){foo*bar} ; x(f=10,2) }, 20)) }
test.micro1994 <- function() { cat('1994\n'); stopifnot(identical({ x<-function(foo,bar){foo*bar} ; x(fo=10, bar=2) }, 20)) }
test.micro1995 <- function() { cat('1995\n'); stopifnot(identical({ f <- function(...) { ..1 } ;  f(10) }, 10)) }
test.micro1996 <- function() { cat('1996\n'); stopifnot(identical({ f <- function(...) { x <<- 10 ; ..1 } ; x <- 1 ; f(x) }, 10)) }
test.micro1997 <- function() { cat('1997\n'); stopifnot(identical({ f <- function(...) { ..1 ; x <<- 10 ; ..1 } ; x <- 1 ; f(x) }, 1)) }
test.micro1998 <- function() { cat('1998\n'); stopifnot(identical({ f <- function(...) { ..1 ; x <<- 10 ; ..2 } ; x <- 1 ; f(100,x) }, 10)) }
test.micro1999 <- function() { cat('1999\n'); stopifnot(identical({ f <- function(...) { ..2 ; x <<- 10 ; ..1 } ; x <- 1 ; f(x,100) }, 10)) }
test.micro2000 <- function() { cat('2000\n'); stopifnot(identical({ g <- function(...) { 0 } ; f <- function(...) { g(...) ; x <<- 10 ; ..1 } ; x <- 1 ; f(x) }, 10)) }
test.micro2002 <- function() { cat('2002\n'); stopifnot(identical({ f <- function(...) { g <- function() { ..1 } ; g() } ; f(a=2) }, 2)) }
test.micro2003 <- function() { cat('2003\n'); stopifnot(identical({ f <- function(...) { ..1 <- 2 ; ..1 } ; f(z = 1) }, 1)) }
test.micro2004 <- function() { cat('2004\n'); stopifnot(identical({ g <- function(a,b) { a + b } ; f <- function(...) { g(...) }  ; f(1,2) }, 3)) }
test.micro2005 <- function() { cat('2005\n'); stopifnot(identical({ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(...,x=4) }  ; f(b=1,a=2) }, 6)) }
test.micro2006 <- function() { cat('2006\n'); stopifnot(identical({ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(x=4, ...) }  ; f(b=1,a=2) }, 6)) }
test.micro2007 <- function() { cat('2007\n'); stopifnot(identical({ g <- function(a,b,x) { a + b * x } ; f <- function(...) { g(x=4, ..., 10) }  ; f(b=1) }, 14)) }
test.micro2008 <- function() { cat('2008\n'); stopifnot(identical({ g <- function(...) { 0 } ; f <- function(...) { g(...) ; x <<- 10 ; ..1 } ; x <- 1 ; f(x) }, 10)) }
test.micro2009 <- function() { cat('2009\n'); stopifnot(identical({ g <- function(a,b,aa,bb) { a ; x <<- 10 ; aa ; c(a, aa) } ; f <- function(...) {  g(..., ...) } ; x <- 1; y <- 2; f(x, y) }, c(1, 1))) }
test.micro2010 <- function() { cat('2010\n'); stopifnot(identical({ f <- function(a, b) { a - b } ; g <- function(...) { f(1, ...) } ; g(b = 2) }, -1)) }
test.micro2011 <- function() { cat('2011\n'); stopifnot(identical({ f <- function(a, b) { a - b } ; g <- function(...) { f(1, ...) } ; g(a = 2) }, 1)) }
test.micro2012 <- function() { cat('2012\n'); stopifnot(identical({ f <- function(...) { g(...) } ;  g <- function(b=2) { b } ; f() }, 2)) }
test.micro2013 <- function() { cat('2013\n'); stopifnot(identical({ f <- function(a, barg) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(b=2) }, 3)) }
test.micro2014 <- function() { cat('2014\n'); stopifnot(identical({ f <- function(a, barg, ...) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(b=2,3) }, 3)) }
test.micro2015 <- function() { cat('2015\n'); stopifnot(identical({ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(be=2,du=3, 3) }, 4)) }
test.micro2016 <- function() { cat('2016\n'); stopifnot(identical({ f <- function(a, barg, bextra, dummy) { a + barg } ; g <- function(...) { f(a=1, ...) } ; g(1,2,3) }, 2)) }
test.micro2017 <- function() { cat('2017\n'); stopifnot(identical({ f <- function(a, b) { a * b } ; g <- function(...) { f(...,...) } ; g(3) }, 9)) }
test.micro2018 <- function() { cat('2018\n'); stopifnot(identical({ g <- function(...) { c(...,...) } ; g(3) }, c(3, 3))) }
test.micro2019 <- function() { cat('2019\n'); stopifnot(identical({ f <- function(...,d) { ..1 + ..2 } ; f(1,d=4,2) }, 3)) }
test.micro2020 <- function() { cat('2020\n'); stopifnot(identical({ f <- function(...,d) { ..1 + ..2 } ; f(1,2,d=4) }, 3)) }
test.micro2021 <- function() { cat('2021\n'); stopifnot(identical({ if(TRUE) 1 else 2 }, 1)) }
test.micro2022 <- function() { cat('2022\n'); stopifnot(identical({ if(FALSE) 1 else 2 }, 2)) }
test.micro2023 <- function() { cat('2023\n'); stopifnot(identical({ if(!FALSE) 1 else 2 }, 1)) }
test.micro2024 <- function() { cat('2024\n'); stopifnot(identical({ if(!TRUE) 1 else 2 }, 2)) }
test.micro2025 <- function() { cat('2025\n'); stopifnot(identical({ x <- 2 ; if (1==x) TRUE else 2 }, 2)) }
test.micro2026 <- function() { cat('2026\n'); stopifnot(identical({ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(TRUE) }, 1)) }
test.micro2027 <- function() { cat('2027\n'); stopifnot(identical({ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(FALSE) }, 2)) }
test.micro2028 <- function() { cat('2028\n'); stopifnot(identical({ f <- function(x) { if (x) 1 else 2 } ; f(1) ; f(1:3) }, 1)) }
test.micro2029 <- function() { cat('2029\n'); stopifnot(identical({ if (TRUE==FALSE) TRUE else FALSE }, FALSE)) }
test.micro2030 <- function() { cat('2030\n'); stopifnot(identical({ if (FALSE==TRUE) TRUE else FALSE }, FALSE)) }
test.micro2031 <- function() { cat('2031\n'); stopifnot(identical({ if (FALSE==1) TRUE else FALSE }, FALSE)) }
test.micro2032 <- function() { cat('2032\n'); stopifnot(identical({ f <- function(v) { if (FALSE==v) TRUE else FALSE } ; f(TRUE) ; f(1) }, FALSE)) }
test.micro2033 <- function() { cat('2033\n'); stopifnot(identical({ f <- function(a) { if (is.na(a)) { 1 } else { 2 } } ; f(5) ; f(1:3)}, 2)) }
test.micro2034 <- function() { cat('2034\n'); stopifnot(identical({ if (1:3) { TRUE } }, TRUE)) }
test.micro2035 <- function() { cat('2035\n'); stopifnot(identical({ if (c(0,0,0)) { TRUE } else { 2 } }, 2)) }
test.micro2036 <- function() { cat('2036\n'); stopifnot(identical({ if (c(1L,0L,0L)) { TRUE } else { 2 } }, TRUE)) }
test.micro2037 <- function() { cat('2037\n'); stopifnot(identical({ if (c(0L,0L,0L)) { TRUE } else { 2 } }, 2)) }
test.micro2038 <- function() { cat('2038\n'); stopifnot(identical({ f <- function(cond) { if (cond) { TRUE } else { 2 } } ; f(1:3) ; f(2) }, TRUE)) }
test.micro2039 <- function() { cat('2039\n'); stopifnot(identical({ f <- function(cond) { if (cond) { TRUE } else { 2 }  } ; f(c(TRUE,FALSE)) ; f(FALSE) }, 2)) }
test.micro2040 <- function() { cat('2040\n'); stopifnot(identical({ f <- function(cond) { if (cond) { TRUE } else { 2 }  } ; f(c(TRUE,FALSE)) ; f(1) }, TRUE)) }
test.micro2041 <- function() { cat('2041\n'); stopifnot(identical({ l <- quote({x <- 0 ; for(i in 1:10) { x <- x + i } ; x}) ; f <- function() { eval(l) } ; x <<- 10 ; f() }, 55)) }
test.micro2042 <- function() { cat('2042\n'); stopifnot(identical({ x<-210 ; repeat { x <- x + 1 ; break } ; x }, 211)) }
test.micro2043 <- function() { cat('2043\n'); stopifnot(identical({ x<-1 ; repeat { x <- x + 1 ; if (x > 11) { break } } ; x }, 12)) }
test.micro2044 <- function() { cat('2044\n'); stopifnot(identical({ x<-1 ; repeat { x <- x + 1 ; if (x <= 11) { next } else { break } ; x <- 1024 } ; x }, 12)) }
test.micro2045 <- function() { cat('2045\n'); stopifnot(identical({ x<-1 ; while(TRUE) { x <- x + 1 ; if (x > 11) { break } } ; x }, 12)) }
test.micro2046 <- function() { cat('2046\n'); stopifnot(identical({ x<-1 ; while(x <= 10) { x<-x+1 } ; x }, 11)) }
test.micro2047 <- function() { cat('2047\n'); stopifnot(identical({ x<-1 ; for(i in 1:10) { x<-x+1 } ; x }, 11)) }
test.micro2048 <- function() { cat('2048\n'); stopifnot(identical({ for(i in c(1,2)) { x <- i } ; x }, 2)) }
test.micro2049 <- function() { cat('2049\n'); stopifnot(identical({ f<-function(i) { if (i<=1) {1} else {r<-i; for(j in 2:(i-1)) {r=r*j}; r} }; f(10) }, 3628800)) }
test.micro2050 <- function() { cat('2050\n'); stopifnot(identical({ f<-function(i) { x<-integer(i); x[1]<-1; x[2]<-1; if (i>2) { for(j in 3:i) { x[j]<-x[j-1]+x[j-2] } }; x[i] } ; f(32) }, 2178309)) }
test.micro2051 <- function() { cat('2051\n'); stopifnot(identical({ f<-function(r) { x<-0 ; for(i in r) { x<-x+i } ; x } ; f(1:10) ; f(c(1,2,3,4,5)) }, 15)) }
test.micro2052 <- function() { cat('2052\n'); stopifnot(identical({ f<-function(r) { x<-0 ; for(i in r) { x<-x+i } ; x } ; f(c(1,2,3,4,5)) ; f(1:10) }, 55)) }
test.micro2053 <- function() { cat('2053\n'); stopifnot(identical({ l <- quote({for(i in c(1,2)) { x <- i } ; x }) ; f <- function() { eval(l) } ; f() }, 2)) }
test.micro2054 <- function() { cat('2054\n'); stopifnot(identical({ l <- quote(for(i in s) { x <- i }) ; s <- 1:3 ; eval(l) ; s <- 2:1 ; eval(l) ; x }, 1L)) }
test.micro2055 <- function() { cat('2055\n'); stopifnot(identical({ l <- quote({for(i in c(2,1)) { x <- i } ; x }) ; f <- function() { if (FALSE) i <- 2 ; eval(l) } ; f() }, 1)) }
test.micro2056 <- function() { cat('2056\n'); stopifnot(identical({ l <- quote(for(i in s) { x <- i }) ; s <- 1:3 ; eval(l) ; s <- NULL ; eval(l) ; x }, 3L)) }
test.micro2057 <- function() { cat('2057\n'); stopifnot(identical({ for(i in c(1,2,3,4)) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }, 2)) }
test.micro2058 <- function() { cat('2058\n'); stopifnot(identical({ f <- function() { for(i in c(1,2,3,4)) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x } ; f()  }, 2)) }
test.micro2059 <- function() { cat('2059\n'); stopifnot(identical({ l <- quote({ for(i in c(1,2,3,4)) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }) ; f <- function() { eval(l) } ; f()  }, 2)) }
test.micro2060 <- function() { cat('2060\n'); stopifnot(identical({ l <- quote({ for(i in 1:4) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }) ; f <- function() { eval(l) } ; f()  }, 2L)) }
test.micro2061 <- function() { cat('2061\n'); stopifnot(identical({ f <- function(s) { for(i in s) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x } ; f(2:1) ; f(c(1,2,3,4)) }, 2)) }
test.micro2062 <- function() { cat('2062\n'); stopifnot(identical({ f <- function() { for(i in 1:4) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x } ; f() }, 2L)) }
test.micro2063 <- function() { cat('2063\n'); stopifnot(identical({ for(i in 1:4) { if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }, 2L)) }
test.micro2064 <- function() { cat('2064\n'); stopifnot(identical({ i <- 0L ; while(i < 3L) { i <- i + 1 ; if (i == 1) { next } ; if (i==3) { break } ; x <- i ; if (i==4) { x <- 10 } } ; x }, 2)) }
test.micro2065 <- function() { cat('2065\n'); stopifnot(identical({ i <- 1 ; r <- NULL ; for(v in list(NA,1)) { r[i] <- typeof(v) ; i <- i + 1 } ; r }, c("logical", "double"))) }
test.micro2066 <- function() { cat('2066\n'); stopifnot(identical({ l <- quote(x[1] <- 1) ; f <- function() { eval(l) } ; x <- 10 ; f() ; x }, 10)) }
test.micro2067 <- function() { cat('2067\n'); stopifnot(identical({ l <- quote(x[1] <- 1) ; f <- function() { eval(l) ; x <<- 10 ; get("x") } ; x <- 20 ; f() }, 1)) }
test.micro2068 <- function() { cat('2068\n'); stopifnot(identical({ 1:3 %in% 1:10 }, c(TRUE, TRUE, TRUE))) }
test.micro2069 <- function() { cat('2069\n'); stopifnot(identical({ 1 %in% 1:10 }, TRUE)) }
test.micro2070 <- function() { cat('2070\n'); stopifnot(identical({ c("1L","hello") %in% 1:10 }, c(FALSE, FALSE))) }
test.micro2071 <- function() { cat('2071\n'); stopifnot(identical({ (1 + 2i) %in% c(1+10i, 1+4i, 2+2i, 1+2i) }, TRUE)) }
test.micro2072 <- function() { cat('2072\n'); stopifnot(identical({ as.logical(-1:1) %in% TRUE }, c(TRUE, FALSE, TRUE))) }
test.micro2073 <- function() { cat('2073\n'); stopifnot(identical({ x<-1:10; x[3] }, 3L)) }
test.micro2074 <- function() { cat('2074\n'); stopifnot(identical({ x<-1:10; x[3L] }, 3L)) }
test.micro2075 <- function() { cat('2075\n'); stopifnot(identical({ x<-c(1,2,3); x[3] }, 3)) }
test.micro2076 <- function() { cat('2076\n'); stopifnot(identical({ x<-c(1,2,3); x[3L] }, 3)) }
test.micro2077 <- function() { cat('2077\n'); stopifnot(identical({ x<-1:3; x[0-2] }, c(1L, 3L))) }
test.micro2078 <- function() { cat('2078\n'); stopifnot(identical({ x<-1:3; x[FALSE] }, integer(0))) }
test.micro2079 <- function() { cat('2079\n'); stopifnot(identical({ x<-1:3; x[TRUE] }, 1:3)) }
test.micro2080 <- function() { cat('2080\n'); stopifnot(identical({ x<-c(TRUE,TRUE,FALSE); x[0-2] }, c(TRUE, FALSE))) }
test.micro2081 <- function() { cat('2081\n'); stopifnot(identical({ x<-c(1,2);x[[0-1]] }, 2)) }
test.micro2082 <- function() { cat('2082\n'); stopifnot(identical({ x<-c(1,2);x[0-3] }, c(1, 2))) }
test.micro2083 <- function() { cat('2083\n'); stopifnot(identical({ x<-10; x[0-1] }, numeric(0))) }
test.micro2084 <- function() { cat('2084\n'); stopifnot(identical({ x<-10; x[NA] }, NA_real_)) }
test.micro2085 <- function() { cat('2085\n'); stopifnot(identical({ x <- c(a=1, b=2, c=3) ; x[2] }, structure(2, .Names = "b"))) }
test.micro2086 <- function() { cat('2086\n'); stopifnot(identical({ x <- c(a=1, b=2, c=3) ; x[[2]] }, 2)) }
test.micro2087 <- function() { cat('2087\n'); stopifnot(identical({ x <- c(a="A", b="B", c="C") ; x[-2] }, structure(c("A", "C"), .Names = c("a", "c")))) }
test.micro2088 <- function() { cat('2088\n'); stopifnot(identical({ x <- c(a=1+2i, b=2+3i, c=3) ; x[-2] }, structure(c(1+2i, 3+0i), .Names = c("a", "c")))) }
test.micro2089 <- function() { cat('2089\n'); stopifnot(identical({ x <- c(a=1, b=2, c=3) ; x[-2] }, structure(c(1, 3), .Names = c("a", "c")))) }
test.micro2090 <- function() { cat('2090\n'); stopifnot(identical({ x <- c(a=1L, b=2L, c=3L) ; x[-2] }, structure(c(1L, 3L), .Names = c("a", "c")))) }
test.micro2091 <- function() { cat('2091\n'); stopifnot(identical({ x <- c(a=TRUE, b=FALSE, c=NA) ; x[-2] }, structure(c(TRUE, NA), .Names = c("a", "c")))) }
test.micro2092 <- function() { cat('2092\n'); stopifnot(identical({ x <- c(a=as.raw(10), b=as.raw(11), c=as.raw(12)) ; x[-2] }, structure(as.raw(c(0x0a, 0x0c)), .Names = c("a", "c")))) }
test.micro2093 <- function() { cat('2093\n'); stopifnot(identical({ x <- c(a=1L, b=2L, c=3L) ; x[0] }, structure(integer(0), .Names = character(0)))) }
test.micro2094 <- function() { cat('2094\n'); stopifnot(identical({ x <- c(a=1L, b=2L, c=3L) ; x[10] }, structure(NA_integer_, .Names = NA_character_))) }
test.micro2095 <- function() { cat('2095\n'); stopifnot(identical({ x <- c(a=TRUE, b=FALSE, c=NA) ; x[0] }, structure(logical(0), .Names = character(0)))) }
test.micro2096 <- function() { cat('2096\n'); stopifnot(identical({ x <- c(TRUE, FALSE, NA) ; x[0] }, logical(0))) }
test.micro2097 <- function() { cat('2097\n'); stopifnot(identical({ x <- list(1L, 2L, 3L) ; x[10] }, list(NULL))) }
test.micro2098 <- function() { cat('2098\n'); stopifnot(identical({ x <- list(a=1L, b=2L, c=3L) ; x[0] }, structure(list(), .Names = character(0)))) }
test.micro2099 <- function() { cat('2099\n'); stopifnot(identical({ x <- c(a="A", b="B", c="C") ; x[10] }, structure(NA_character_, .Names = NA_character_))) }
test.micro2100 <- function() { cat('2100\n'); stopifnot(identical({ x <- c(a="A", b="B", c="C") ; x[0] }, structure(character(0), .Names = character(0)))) }
test.micro2101 <- function() { cat('2101\n'); stopifnot(identical({ x <- c(a=1+1i, b=2+2i, c=3+3i) ; x[10] }, structure(NA_complex_, .Names = NA_character_))) }
test.micro2102 <- function() { cat('2102\n'); stopifnot(identical({ x <- c(a=1+1i, b=2+2i, c=3+3i) ; x[0] }, structure(complex(0), .Names = character(0)))) }
test.micro2103 <- function() { cat('2103\n'); stopifnot(identical({ x <- c(a=as.raw(10), b=as.raw(11), c=as.raw(12)) ; x[10] }, structure(as.raw(0x00), .Names = NA_character_))) }
test.micro2104 <- function() { cat('2104\n'); stopifnot(identical({ x <- c(a=as.raw(10), b=as.raw(11), c=as.raw(12)) ; x[0] }, structure(raw(0), .Names = character(0)))) }
test.micro2105 <- function() { cat('2105\n'); stopifnot(identical({ x <- c(a=1, b=2, c=3) ; x[10] }, structure(NA_real_, .Names = NA_character_))) }
test.micro2106 <- function() { cat('2106\n'); stopifnot(identical({ x <- c(a=1, b=2, c=3) ; x[0] }, structure(numeric(0), .Names = character(0)))) }
test.micro2107 <- function() { cat('2107\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3,d=4) ; x["b"] }, structure(2, .Names = "b"))) }
test.micro2108 <- function() { cat('2108\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3,d=4) ; x["d"] }, structure(4, .Names = "d"))) }
test.micro2109 <- function() { cat('2109\n'); stopifnot(identical({ x <- 1 ; attr(x, "hi") <- 2; x[2] <- 2; attr(x, "hi") }, 2)) }
test.micro2110 <- function() { cat('2110\n'); stopifnot(identical({ x<-5:1 ; y <- -1L;  x[y] }, c(4L, 3L, 2L, 1L))) }
test.micro2111 <- function() { cat('2111\n'); stopifnot(identical({ x<-5:1 ; y <- 6L;  x[y] }, NA_integer_)) }
test.micro2112 <- function() { cat('2112\n'); stopifnot(identical({ x<-5:1 ; y <- 2L;  x[[y]] }, 4L)) }
test.micro2113 <- function() { cat('2113\n'); stopifnot(identical({ x<-as.list(5:1) ; y <- 2L;  x[[y]] }, 4L)) }
test.micro2114 <- function() { cat('2114\n'); stopifnot(identical({ x <- c(1,4) ; y <- -1L ; x[y] }, 4)) }
test.micro2115 <- function() { cat('2115\n'); stopifnot(identical({ x <- c(1,4) ; y <- 10L ; x[y] }, NA_real_)) }
test.micro2116 <- function() { cat('2116\n'); stopifnot(identical({ x <- c(1,4) ; y <- -1 ; x[y] }, 4)) }
test.micro2117 <- function() { cat('2117\n'); stopifnot(identical({ x <- c(1,4) ; y <- 10 ; x[y] }, NA_real_)) }
test.micro2118 <- function() { cat('2118\n'); stopifnot(identical({ x <- c(a=1,b=2) ; y <- 2L ; x[y] }, structure(2, .Names = "b"))) }
test.micro2119 <- function() { cat('2119\n'); stopifnot(identical({ x <- 1:4 ; y <- -1 ; x[y] }, 2:4)) }
test.micro2120 <- function() { cat('2120\n'); stopifnot(identical({ x <- 1:4 ; y <- 10 ; x[y] }, NA_integer_)) }
test.micro2121 <- function() { cat('2121\n'); stopifnot(identical({ x <- c(a=1,b=2) ; y <- 2 ; x[y] }, structure(2, .Names = "b"))) }
test.micro2122 <- function() { cat('2122\n'); stopifnot(identical({ x <- list(1,2,3,4) ; y <- 3 ; x[y] }, list(3))) }
test.micro2123 <- function() { cat('2123\n'); stopifnot(identical({ x <- list(1,2,3,4) ; y <- 3 ; x[[y]] }, 3)) }
test.micro2124 <- function() { cat('2124\n'); stopifnot(identical({ x <- list(1,4) ; y <- -1 ; x[y] }, list(4))) }
test.micro2125 <- function() { cat('2125\n'); stopifnot(identical({ x <- list(1,4) ; y <- 4 ; x[y] }, list(NULL))) }
test.micro2126 <- function() { cat('2126\n'); stopifnot(identical({ x <- list(a=1,b=4) ; y <- 2 ; x[y] }, structure(list(b = 4), .Names = "b"))) }
test.micro2127 <- function() { cat('2127\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; x <- c(a=1,b=2) ; f(x,"a") }, structure(1, .Names = "a"))) }
test.micro2128 <- function() { cat('2128\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; x <- c(a=1,b=2) ; f(x,"a") ; f(x,2) }, structure(2, .Names = "b"))) }
test.micro2129 <- function() { cat('2129\n'); stopifnot(identical({ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(list(1,2),TRUE) }, 1)) }
test.micro2130 <- function() { cat('2130\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(list(), NA) }, list(NULL))) }
test.micro2131 <- function() { cat('2131\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(integer(), NA) }, NA_integer_)) }
test.micro2132 <- function() { cat('2132\n'); stopifnot(identical({ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(1:2,-1) }, 2L)) }
test.micro2133 <- function() { cat('2133\n'); stopifnot(identical({ f <- function(x,i) { x[[i]] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(1:2,-2) }, 1L)) }
test.micro2134 <- function() { cat('2134\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(1:2,NA) }, c(NA_integer_, NA_integer_))) }
test.micro2135 <- function() { cat('2135\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(1:2,-4) }, 1:2)) }
test.micro2136 <- function() { cat('2136\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(a=1L,b=2L),0) }, structure(integer(0), .Names = character(0)))) }
test.micro2137 <- function() { cat('2137\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(1:2,0) }, integer(0))) }
test.micro2138 <- function() { cat('2138\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(1:2,-2) }, 1L)) }
test.micro2139 <- function() { cat('2139\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(TRUE,FALSE),NA) }, c(NA, NA))) }
test.micro2140 <- function() { cat('2140\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(TRUE,FALSE),-4) }, c(TRUE, FALSE))) }
test.micro2141 <- function() { cat('2141\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(TRUE,FALSE),0) }, logical(0))) }
test.micro2142 <- function() { cat('2142\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(a=TRUE,b=FALSE),0) }, structure(logical(0), .Names = character(0)))) }
test.micro2143 <- function() { cat('2143\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(TRUE,FALSE),-2) }, TRUE)) }
test.micro2144 <- function() { cat('2144\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(TRUE,FALSE),4) }, NA)) }
test.micro2145 <- function() { cat('2145\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(a=TRUE,b=FALSE),4) }, structure(NA, .Names = NA_character_))) }
test.micro2146 <- function() { cat('2146\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(list(1,2),-4) }, list(1, 2))) }
test.micro2147 <- function() { cat('2147\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(list(1,2),4) }, list(NULL))) }
test.micro2148 <- function() { cat('2148\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(list(a=1,b=2),4) }, structure(list("NA" = NULL), .Names = NA_character_))) }
test.micro2149 <- function() { cat('2149\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c("a","b"),4) }, NA_character_)) }
test.micro2150 <- function() { cat('2150\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c("a","b"),NA) }, c(NA_character_, NA_character_))) }
test.micro2151 <- function() { cat('2151\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c("a","b"),-4) }, c("a", "b"))) }
test.micro2152 <- function() { cat('2152\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c("a","b"),0) }, character(0))) }
test.micro2153 <- function() { cat('2153\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(a="a",b="b"),0) }, structure(character(0), .Names = character(0)))) }
test.micro2154 <- function() { cat('2154\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(1+2i,3+4i),NA) }, c(NA_complex_, NA_complex_))) }
test.micro2155 <- function() { cat('2155\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(1+2i,3+4i),-4) }, c(1+2i, 3+4i))) }
test.micro2156 <- function() { cat('2156\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(1+2i,3+4i),4) }, NA_complex_)) }
test.micro2157 <- function() { cat('2157\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(c(a=1+2i,b=3+4i),4) }, structure(NA_complex_, .Names = NA_character_))) }
test.micro2158 <- function() { cat('2158\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(as.raw(c(10,11)),-4) }, as.raw(c(0x0a, 0x0b)))) }
test.micro2159 <- function() { cat('2159\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(as.raw(c(10,11)),0) }, raw(0))) }
test.micro2160 <- function() { cat('2160\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(as.raw(c(10,11)),4) }, as.raw(0x00))) }
test.micro2161 <- function() { cat('2161\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; z <- c(1+2i,3+4i) ; attr(z, "my") <- 1 ; f(z,-10) }, c(1+2i, 3+4i))) }
test.micro2162 <- function() { cat('2162\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; z <- c(1,3) ; attr(z, "my") <- 1 ; f(z,-10) }, c(1, 3))) }
test.micro2163 <- function() { cat('2163\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; z <- c(1L,3L) ; attr(z, "my") <- 1 ; f(z,-10) }, c(1L, 3L))) }
test.micro2164 <- function() { cat('2164\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; z <- c(TRUE,FALSE) ; attr(z, "my") <- 1 ; f(z,-10) }, c(TRUE, FALSE))) }
test.micro2165 <- function() { cat('2165\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; z <- c(a="a",b="b") ; attr(z, "my") <- 1 ; f(z,-10) }, structure(c("a", "b"), .Names = c("a", "b")))) }
test.micro2166 <- function() { cat('2166\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; z <- c(a=as.raw(10),b=as.raw(11)) ; attr(z, "my") <- 1 ; f(z,-10) }, structure(as.raw(c(0x0a, 0x0b)), .Names = c("a", "b")))) }
test.micro2167 <- function() { cat('2167\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(1:3,c(TRUE,FALSE)) }, c(1L, 3L))) }
test.micro2168 <- function() { cat('2168\n'); stopifnot(identical({ f <- function(x,i) { x[i] } ; f(1:4, 2L) ; f(c(a=1), "a") ; f(1:3,c(1,2)) }, 1:2)) }
test.micro2169 <- function() { cat('2169\n'); stopifnot(identical({ x <- as.list(1:2) ; f <- function(i) { x[i] <- NULL ; x } ; f(1) ; f(NULL) }, list(1L, 2L))) }
test.micro2170 <- function() { cat('2170\n'); stopifnot(identical({ x <- 1:3 ; x[TRUE] <- 10 ; x }, c(10, 10, 10))) }
test.micro2171 <- function() { cat('2171\n'); stopifnot(identical({ x <- 1:3 ; x[[TRUE]] <- 10 ; x }, c(10, 2, 3))) }
test.micro2173 <- function() { cat('2173\n'); stopifnot(identical({ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b["hello"] <- NULL ; b }, list(1+2i, 3+4i))) }
test.micro2174 <- function() { cat('2174\n'); stopifnot(identical({ x<-1:5 ; x[3:4] }, 3:4)) }
test.micro2175 <- function() { cat('2175\n'); stopifnot(identical({ x<-1:5 ; x[4:3] }, c(4L, 3L))) }
test.micro2176 <- function() { cat('2176\n'); stopifnot(identical({ x<-c(1,2,3,4,5) ; x[4:3] }, c(4, 3))) }
test.micro2177 <- function() { cat('2177\n'); stopifnot(identical({ (1:5)[3:4] }, 3:4)) }
test.micro2178 <- function() { cat('2178\n'); stopifnot(identical({ x<-(1:5)[2:4] ; x[2:1] }, c(3L, 2L))) }
test.micro2179 <- function() { cat('2179\n'); stopifnot(identical({ x<-1:5;x[c(0-2,0-3)] }, c(1L, 4L, 5L))) }
test.micro2180 <- function() { cat('2180\n'); stopifnot(identical({ x<-1:5;x[c(0-2,0-3,0,0,0)] }, c(1L, 4L, 5L))) }
test.micro2181 <- function() { cat('2181\n'); stopifnot(identical({ x<-1:5;x[c(2,5,4,3,3,3,0)] }, c(2L, 5L, 4L, 3L, 3L, 3L))) }
test.micro2182 <- function() { cat('2182\n'); stopifnot(identical({ x<-1:5;x[c(2L,5L,4L,3L,3L,3L,0L)] }, c(2L, 5L, 4L, 3L, 3L, 3L))) }
test.micro2183 <- function() { cat('2183\n'); stopifnot(identical({ f<-function(x, i) { x[i] } ; f(1:3,3:1) ; f(1:5,c(0,0,0,0-2)) }, c(1L, 3L, 4L, 5L))) }
test.micro2184 <- function() { cat('2184\n'); stopifnot(identical({ f<-function(x, i) { x[i] } ; f(1:3,0-3) ; f(1:5,c(0,0,0,0-2)) }, c(1L, 3L, 4L, 5L))) }
test.micro2185 <- function() { cat('2185\n'); stopifnot(identical({ f<-function(x, i) { x[i] } ; f(1:3,0L-3L) ; f(1:5,c(0,0,0,0-2)) }, c(1L, 3L, 4L, 5L))) }
test.micro2186 <- function() { cat('2186\n'); stopifnot(identical({ x<-1:5 ; x[c(TRUE,FALSE)] }, c(1L, 3L, 5L))) }
test.micro2187 <- function() { cat('2187\n'); stopifnot(identical({ x<-1:5 ; x[c(TRUE,TRUE,TRUE,NA)] }, c(1L, 2L, 3L, NA, 5L))) }
test.micro2188 <- function() { cat('2188\n'); stopifnot(identical({ x<-1:5 ; x[c(TRUE,TRUE,TRUE,FALSE,FALSE,FALSE,FALSE,TRUE,NA)] }, c(1L, 2L, 3L, NA, NA))) }
test.micro2189 <- function() { cat('2189\n'); stopifnot(identical({ f<-function(i) { x<-1:5 ; x[i] } ; f(1) ; f(1L) ; f(TRUE) }, 1:5)) }
test.micro2190 <- function() { cat('2190\n'); stopifnot(identical({ f<-function(i) { x<-1:5 ; x[i] } ; f(1) ; f(TRUE) ; f(1L)  }, 1L)) }
test.micro2191 <- function() { cat('2191\n'); stopifnot(identical({ f<-function(i) { x<-1:5 ; x[i] } ; f(1) ; f(TRUE) ; f(c(3,2))  }, c(3L, 2L))) }
test.micro2192 <- function() { cat('2192\n'); stopifnot(identical({ f<-function(i) { x<-1:5 ; x[i] } ; f(1)  ; f(3:4) }, 3:4)) }
test.micro2193 <- function() { cat('2193\n'); stopifnot(identical({ f<-function(i) { x<-1:5 ; x[i] } ; f(c(TRUE,FALSE))  ; f(3:4) }, 3:4)) }
test.micro2194 <- function() { cat('2194\n'); stopifnot(identical({ x<-as.complex(c(1,2,3,4)) ; x[2:4] }, c(2+0i, 3+0i, 4+0i))) }
test.micro2195 <- function() { cat('2195\n'); stopifnot(identical({ x<-as.raw(c(1,2,3,4)) ; x[2:4] }, as.raw(c(0x02, 0x03, 0x04)))) }
test.micro2196 <- function() { cat('2196\n'); stopifnot(identical({ x<-c(1,2,3,4) ; names(x) <- c("a","b","c","d") ; x[c(10,2,3,0)] }, structure(c(NA, 2, 3), .Names = c(NA, "b", "c")))) }
test.micro2197 <- function() { cat('2197\n'); stopifnot(identical({ x<-c(1,2,3,4) ; names(x) <- c("a","b","c","d") ; x[c(10,2,3)] }, structure(c(NA, 2, 3), .Names = c(NA, "b", "c")))) }
test.micro2198 <- function() { cat('2198\n'); stopifnot(identical({ x<-c(1,2,3,4) ; names(x) <- c("a","b","c","d") ; x[c(-2,-4,0)] }, structure(c(1, 3), .Names = c("a", "c")))) }
test.micro2199 <- function() { cat('2199\n'); stopifnot(identical({ x<-c(1,2) ; names(x) <- c("a","b") ; x[c(FALSE,TRUE,NA,FALSE)] }, structure(c(2, NA), .Names = c("b", NA)))) }
test.micro2200 <- function() { cat('2200\n'); stopifnot(identical({ x<-c(1,2) ; names(x) <- c("a","b") ; x[c(FALSE,TRUE)] }, structure(2, .Names = "b"))) }
test.micro2201 <- function() { cat('2201\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3,d=4) ; x[character()] }, structure(numeric(0), .Names = character(0)))) }
test.micro2202 <- function() { cat('2202\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3,d=4) ; x[c("b","b","d","a","a")] }, structure(c(2, 2, 4, 1, 1), .Names = c("b", "b", "d", "a", "a")))) }
test.micro2203 <- function() { cat('2203\n'); stopifnot(identical({ x <- c(a=as.raw(10),b=as.raw(11),c=as.raw(12),d=as.raw(13)) ; f <- function(s) { x[s] } ; f(TRUE) ; f(1L) ; f(as.character(NA)) }, structure(as.raw(0x00), .Names = NA_character_))) }
test.micro2204 <- function() { cat('2204\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3,d=4) ; f <- function(s) { x[s] } ; f(TRUE) ; f(1L) ; f("b") }, structure(2, .Names = "b"))) }
test.micro2205 <- function() { cat('2205\n'); stopifnot(identical({ x <- c(a=as.raw(10),b=as.raw(11),c=as.raw(12),d=as.raw(13)) ; f <- function(s) { x[c(s,s)] } ; f(TRUE) ; f(1L) ; f(as.character(NA)) }, structure(as.raw(c(0x00, 0x00)), .Names = c(NA_character_, NA_character_)))) }
test.micro2206 <- function() { cat('2206\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3,d=4) ; f <- function(s) { x[c(s,s)] } ; f(TRUE) ; f(1L) ; f("b") }, structure(c(2, 2), .Names = c("b", "b")))) }
test.micro2207 <- function() { cat('2207\n'); stopifnot(identical({ x <- 1;  y<-c(1,1) ; x[y] }, c(1, 1))) }
test.micro2208 <- function() { cat('2208\n'); stopifnot(identical({ x <- 1L;  y<-c(1,1) ; x[y] }, c(1L, 1L))) }
test.micro2209 <- function() { cat('2209\n'); stopifnot(identical({ x <- TRUE;  y<-c(1,1) ; x[y] }, c(TRUE, TRUE))) }
test.micro2210 <- function() { cat('2210\n'); stopifnot(identical({ x <- "hi";  y<-c(1,1) ; x[y] }, c("hi", "hi"))) }
test.micro2211 <- function() { cat('2211\n'); stopifnot(identical({ x <- 1+2i;  y<-c(1,2) ; x[y] }, c(1+2i, complex(real=NA, i=NA)))) }
test.micro2212 <- function() { cat('2212\n'); stopifnot(identical({ f<-function(x,l) { x[l == 3] } ; f(c(1,2,3), c(1,2,3)) ; f(c(1,2,3), 1:3) ; f(1:3, c(3,3,2)) }, 1:2)) }
test.micro2213 <- function() { cat('2213\n'); stopifnot(identical({ f<-function(x,l) { x[l == 3] <- 4 } ; f(c(1,2,3), c(1,2,3)) ; f(c(1,2,3), 1:3) ; f(1:3, c(3,3,2)) }, 4)) }
test.micro2214 <- function() { cat('2214\n'); stopifnot(identical({ x <- c(TRUE,FALSE,TRUE) ; x[2:3] }, c(FALSE, TRUE))) }
test.micro2215 <- function() { cat('2215\n'); stopifnot(identical({ x <- c(1+2i,3+4i,5+6i) ; x[2:3] }, c(3+4i, 5+6i))) }
test.micro2216 <- function() { cat('2216\n'); stopifnot(identical({ x <- c(1+2i,3+4i,5+6i) ; x[c(2,3,NA)] }, c(3+4i, 5+6i, complex(real=NA, i=NA)))) }
test.micro2217 <- function() { cat('2217\n'); stopifnot(identical({ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,-4,-5)] }, 1+2i)) }
test.micro2218 <- function() { cat('2218\n'); stopifnot(identical({ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,-4,-5,-5)] }, 1+2i)) }
test.micro2219 <- function() { cat('2219\n'); stopifnot(identical({ x <- c(1+2i,3+4i,5+6i) ; x[c(-2,-3,-4,-5,-2)] }, 1+2i)) }
test.micro2220 <- function() { cat('2220\n'); stopifnot(identical({ x <- c(TRUE,FALSE,TRUE) ; x[integer()] }, logical(0))) }
test.micro2221 <- function() { cat('2221\n'); stopifnot(identical({ x <- c(1,2,3,2) ; x[x==2] }, c(2, 2))) }
test.micro2222 <- function() { cat('2222\n'); stopifnot(identical({ x <- c(1,2,3,2) ; x[c(3,4,2)==2] }, 3)) }
test.micro2223 <- function() { cat('2223\n'); stopifnot(identical({ x <- c(a=1,x=2,b=3,y=2) ; x[c(3,4,2)==2] }, structure(3, .Names = "b"))) }
test.micro2224 <- function() { cat('2224\n'); stopifnot(identical({ x <- c(a=1,x=2,b=3,y=2) ; x[c(3,4,2,1)==2] }, structure(3, .Names = "b"))) }
test.micro2225 <- function() { cat('2225\n'); stopifnot(identical({ x <- c(as.double(1:2000)) ; x[c(1,3,3,3,1:1996)==3] }, c(2, 3, 4, 7))) }
test.micro2226 <- function() { cat('2226\n'); stopifnot(identical({ x <- c(as.double(1:2000)) ; x[c(NA,3,3,NA,1:1996)==3] }, c(NA, 2, 3, NA, 7))) }
test.micro2227 <- function() { cat('2227\n'); stopifnot(identical({ x <- c(as.double(1:2000)) ; sum(x[rep(3, 2000)==3]) }, 2001000)) }
test.micro2228 <- function() { cat('2228\n'); stopifnot(identical({ x <- c(1,2,3,2) ; x[c(3,4,2,NA)==2] }, c(3, NA))) }
test.micro2229 <- function() { cat('2229\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,TRUE)) ; f(1:3,3:1) }, c(3L, 2L, 1L))) }
test.micro2230 <- function() { cat('2230\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,TRUE)) ; f(c(a=1,b=2,c=3),3:1) }, structure(c(3, 2, 1), .Names = c("c", "b", "a")))) }
test.micro2231 <- function() { cat('2231\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,NA)) }, c(1L, NA))) }
test.micro2232 <- function() { cat('2232\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(1:3, c(TRUE,FALSE,NA,NA,NA)) }, c(1L, NA, NA, NA))) }
test.micro2233 <- function() { cat('2233\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(c(a=1,b=2,c=3), c(TRUE,NA,FALSE,FALSE,TRUE)) }, structure(c(1, NA, NA), .Names = c("a", NA, NA)))) }
test.micro2234 <- function() { cat('2234\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(c(a=1,b=2,c=3), c(TRUE,NA)) }, structure(c(1, NA, 3), .Names = c("a", NA, "c")))) }
test.micro2235 <- function() { cat('2235\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(1:3, logical()) }, integer(0))) }
test.micro2236 <- function() { cat('2236\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(c(a=1L,b=2L,c=3L), logical()) }, structure(integer(0), .Names = character(0)))) }
test.micro2237 <- function() { cat('2237\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(c(a=1,b=2,c=3), character()) }, structure(numeric(0), .Names = character(0)))) }
test.micro2238 <- function() { cat('2238\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(c(1,2,3), character()) }, numeric(0))) }
test.micro2239 <- function() { cat('2239\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(c(1,2,3), c("hello","hi")) }, c(NA_real_, NA_real_))) }
test.micro2240 <- function() { cat('2240\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(1:3, c("h","hi")) ; f(1:3,TRUE) }, 1:3)) }
test.micro2241 <- function() { cat('2241\n'); stopifnot(identical({ x <- list(1,2,list(3)) ; x[[c(3,1)]] }, 3)) }
test.micro2243 <- function() { cat('2243\n'); stopifnot(identical({ x <- list(1,list(3)) ; x[[c(-1,1)]] }, 3)) }
test.micro2244 <- function() { cat('2244\n'); stopifnot(identical({ l <- list(1,list(2)) ; f <- function(i) { l[[i]] } ; f(c(2,1)) ; f(1) }, 1)) }
test.micro2247 <- function() { cat('2247\n'); stopifnot(identical({ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,-1)) }, 3)) }
test.micro2248 <- function() { cat('2248\n'); stopifnot(identical({ f <- function(i) { l[[i]] } ; l <- list(1, c(2,3)) ; f(c(2,-2)) }, 2)) }
test.micro2249 <- function() { cat('2249\n'); stopifnot(identical({ x <- list(a=1,b=2,d=list(x=3)) ; x[[c("d","x")]] }, 3)) }
test.micro2251 <- function() { cat('2251\n'); stopifnot(identical({ x <- list(a=1,b=2,d=list(x=3)) ; f <- function(i) { x[[i]] } ; f(c("d","x")) ; f("b") }, 2)) }
test.micro2252 <- function() { cat('2252\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(1:3,c(2,1)) ; f(1:3,c(TRUE,FALSE)) }, c(1L, 3L))) }
test.micro2253 <- function() { cat('2253\n'); stopifnot(identical({ f <- function(b,i) { b[i] } ; f(1:3,c(2,1)) ; f(1:3,NULL) }, integer(0))) }
test.micro2254 <- function() { cat('2254\n'); stopifnot(identical({ x<-1:3; x[1]<-100L; x }, c(100L, 2L, 3L))) }
test.micro2255 <- function() { cat('2255\n'); stopifnot(identical({ x<-c(1,2,3); x[2L]<-100L; x }, c(1, 100, 3))) }
test.micro2256 <- function() { cat('2256\n'); stopifnot(identical({ x<-c(1,2,3); x[2L]<-100; x }, c(1, 100, 3))) }
test.micro2257 <- function() { cat('2257\n'); stopifnot(identical({ x<-c(1,2,3); x[2]<-FALSE; x }, c(1, 0, 3))) }
test.micro2258 <- function() { cat('2258\n'); stopifnot(identical({ x<-1:5; x[2]<-1000; x[3] <- TRUE; x[8]<-3L; x }, c(1, 1000, 1, 4, 5, NA, NA, 3))) }
test.micro2259 <- function() { cat('2259\n'); stopifnot(identical({ x<-5:1; x[0-2]<-1000; x }, c(1000, 4, 1000, 1000, 1000))) }
test.micro2260 <- function() { cat('2260\n'); stopifnot(identical({ x<-c(); x[[TRUE]] <- 2; x }, 2)) }
test.micro2261 <- function() { cat('2261\n'); stopifnot(identical({ x<-1:2; x[[0-2]]<-100; x }, c(100, 2))) }
test.micro2262 <- function() { cat('2262\n'); stopifnot(identical({ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,3L) ; f(c(1L,2L),2,3) }, c(1, 3, 3, 4, 5))) }
test.micro2263 <- function() { cat('2263\n'); stopifnot(identical({ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,3L) ; f(c(1L,2L),8,3L) }, c(1L, 2L, 3L, 4L, 5L, NA, NA, 3L))) }
test.micro2264 <- function() { cat('2264\n'); stopifnot(identical({ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,FALSE) ; f(c(1L,2L),2,3) }, c(1, 3, 3, 4, 5))) }
test.micro2265 <- function() { cat('2265\n'); stopifnot(identical({ f<-function(x,i,v) { x<-1:5; x[i]<-v; x} ; f(c(1L,2L),1,FALSE) ; f(c(1L,2L),8,TRUE) }, c(1L, 2L, 3L, 4L, 5L, NA, NA, 1L))) }
test.micro2266 <- function() { cat('2266\n'); stopifnot(identical({ a <- c(1L,2L,3L); a <- 1:5; a[3] <- TRUE; a }, c(1L, 2L, 1L, 4L, 5L))) }
test.micro2267 <- function() { cat('2267\n'); stopifnot(identical({ x <- 1:3 ; x[2] <- "hi"; x }, c("1", "hi", "3"))) }
test.micro2268 <- function() { cat('2268\n'); stopifnot(identical({ x <- c(1,2,3) ; x[2] <- "hi"; x }, c("1", "hi", "3"))) }
test.micro2269 <- function() { cat('2269\n'); stopifnot(identical({ x <- c(TRUE,FALSE,FALSE) ; x[2] <- "hi"; x }, c("TRUE", "hi", "FALSE"))) }
test.micro2270 <- function() { cat('2270\n'); stopifnot(identical({ x <- c(2,3,4) ; x[1] <- 3+4i ; x  }, c(3+4i, 3+0i, 4+0i))) }
test.micro2271 <- function() { cat('2271\n'); stopifnot(identical({ b <- c(1,2) ; x <- b ; b[2L] <- 3 ; b }, c(1, 3))) }
test.micro2272 <- function() { cat('2272\n'); stopifnot(identical({ b <- c(1,2) ; b[0L] <- 3 ; b }, c(1, 2))) }
test.micro2273 <- function() { cat('2273\n'); stopifnot(identical({ b <- c(1,2) ; b[0] <- 1+2i ; b }, c(1+0i, 2+0i))) }
test.micro2274 <- function() { cat('2274\n'); stopifnot(identical({ b <- c(1,2) ; b[5L] <- 3 ; b }, c(1, 2, NA, NA, 3))) }
test.micro2275 <- function() { cat('2275\n'); stopifnot(identical({ b <- c(1,2) ; z <- c(10,11) ; attr(z,"my") <- 4 ; b[2] <- z ; b }, c(1, 10))) }
test.micro2276 <- function() { cat('2276\n'); stopifnot(identical({ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),10L) ; f(1,3) }, c(1, 3))) }
test.micro2277 <- function() { cat('2277\n'); stopifnot(identical({ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),10L) ; f(1L,3) }, c(1, 3))) }
test.micro2278 <- function() { cat('2278\n'); stopifnot(identical({ b <- c(1L,2L) ; b[3] <- 13L ; b }, c(1L, 2L, 13L))) }
test.micro2279 <- function() { cat('2279\n'); stopifnot(identical({ b <- c(1L,2L) ; b[0] <- 13L ; b }, 1:2)) }
test.micro2280 <- function() { cat('2280\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; b <- c(10L,2L) ; b[0] <- TRUE ; b }, c(10L, 2L))) }
test.micro2281 <- function() { cat('2281\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; b <- c(10L,2L) ; b[3] <- TRUE ; b }, c(10L, 2L, 1L))) }
test.micro2282 <- function() { cat('2282\n'); stopifnot(identical({ b <- c(1L,2L) ; b[2] <- FALSE ; b }, c(1L, 0L))) }
test.micro2283 <- function() { cat('2283\n'); stopifnot(identical({ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),TRUE) ; f(1L,3) }, c(1, 3))) }
test.micro2284 <- function() { cat('2284\n'); stopifnot(identical({ f <- function(b,v) { b[2] <- v ; b } ; f(c(1L,2L),TRUE) ; f(10,3) }, c(10, 3))) }
test.micro2285 <- function() { cat('2285\n'); stopifnot(identical({ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10) ; f(b,13L) }, c(1, 13))) }
test.micro2286 <- function() { cat('2286\n'); stopifnot(identical({ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10) ; f(1:3,13L) }, c(1L, 13L, 3L))) }
test.micro2287 <- function() { cat('2287\n'); stopifnot(identical({ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10) ; f(c(1,2),10) }, c(1, 10))) }
test.micro2288 <- function() { cat('2288\n'); stopifnot(identical({ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10L) ; f(1:3,13L) }, c(1L, 13L, 3L))) }
test.micro2289 <- function() { cat('2289\n'); stopifnot(identical({ b <- c(1,2) ; x <- b ; f <- function(b,v) { b[2L] <- v ; b } ; f(b,10L) ; f(b,13) }, c(1, 13))) }
test.micro2290 <- function() { cat('2290\n'); stopifnot(identical({ b <- c(1,2) ; z <- b ; b[3L] <- 3L ; b }, c(1, 2, 3))) }
test.micro2291 <- function() { cat('2291\n'); stopifnot(identical({ b <- c(1,2) ; z <- b ; b[-2] <- 3L ; b }, c(3, 2))) }
test.micro2292 <- function() { cat('2292\n'); stopifnot(identical({ b <- c(1,2) ; z <- b ; b[3L] <- FALSE ; b }, c(1, 2, 0))) }
test.micro2293 <- function() { cat('2293\n'); stopifnot(identical({ b <- c(1,2) ; z <- b ; b[-10L] <- FALSE ; b }, c(0, 0))) }
test.micro2294 <- function() { cat('2294\n'); stopifnot(identical({ f <- function(b,v) { b[2] <- v ; b } ; f(c(1,2),FALSE) ; f(10L,3) }, c(10, 3))) }
test.micro2295 <- function() { cat('2295\n'); stopifnot(identical({ f <- function(b,v) { b[2] <- v ; b } ; f(c(1,2),FALSE) ; f(10,3) }, c(10, 3))) }
test.micro2296 <- function() { cat('2296\n'); stopifnot(identical({ f <- function(b,v) { b[2] <- v ; b } ; f(c(TRUE,NA),FALSE) ; f(c(FALSE,TRUE),3) }, c(0, 3))) }
test.micro2297 <- function() { cat('2297\n'); stopifnot(identical({ f <- function(b,v) { b[2] <- v ; b } ; f(c(TRUE,NA),FALSE) ; f(3,3) }, c(3, 3))) }
test.micro2298 <- function() { cat('2298\n'); stopifnot(identical({ b <- c(TRUE,NA) ; z <- b ; b[-10L] <- FALSE ; b }, c(FALSE, FALSE))) }
test.micro2299 <- function() { cat('2299\n'); stopifnot(identical({ b <- c(TRUE,NA) ; z <- b ; b[4L] <- FALSE ; b }, c(TRUE, NA, NA, FALSE))) }
test.micro2300 <- function() { cat('2300\n'); stopifnot(identical({ b <- list(TRUE,NA) ; z <- b ; b[[4L]] <- FALSE ; b }, list(TRUE, NA, NULL, FALSE))) }
test.micro2301 <- function() { cat('2301\n'); stopifnot(identical({ b <- list(TRUE,NA) ; z <- b ; b[[-1L]] <- FALSE ; b }, list(TRUE, FALSE))) }
test.micro2302 <- function() { cat('2302\n'); stopifnot(identical({ f <- function(b,v) { b[[2]] <- v ; b } ; f(list(TRUE,NA),FALSE) ; f(3,3) }, c(3, 3))) }
test.micro2303 <- function() { cat('2303\n'); stopifnot(identical({ f <- function(b,v) { b[[2]] <- v ; b } ; f(list(TRUE,NA),FALSE) ; f(list(3),NULL) }, list(3))) }
test.micro2304 <- function() { cat('2304\n'); stopifnot(identical({ f <- function(b,v) { b[[2]] <- v ; b } ; f(list(TRUE,NA),FALSE) ; f(list(),NULL) }, list())) }
test.micro2305 <- function() { cat('2305\n'); stopifnot(identical({ f <- function(b,v) { b[[2]] <- v ; b } ; f(c("a","b"),"d") ; f(1:3,"x") }, c("1", "x", "3"))) }
test.micro2306 <- function() { cat('2306\n'); stopifnot(identical({ b <- c("a","b") ; z <- b ; b[[-1L]] <- "xx" ; b }, c("a", "xx"))) }
test.micro2307 <- function() { cat('2307\n'); stopifnot(identical({ b <- c("a","b") ; z <- b ; b[[3L]] <- "xx" ; b }, c("a", "b", "xx"))) }
test.micro2308 <- function() { cat('2308\n'); stopifnot(identical({ b <- c(1,2) ; b[3] <- 2+3i ; b }, c(1+0i, 2+0i, 2+3i))) }
test.micro2309 <- function() { cat('2309\n'); stopifnot(identical({ b <- c(1+2i,3+4i) ; b[3] <- 2 ; b }, c(1+2i, 3+4i, 2+0i))) }
test.micro2310 <- function() { cat('2310\n'); stopifnot(identical({ b <- c(TRUE,NA) ; b[3] <- FALSE ; b }, c(TRUE, NA, FALSE))) }
test.micro2311 <- function() { cat('2311\n'); stopifnot(identical({ b <- as.raw(c(1,2)) ; b[3] <- as.raw(13) ; b }, as.raw(c(0x01, 0x02, 0x0d)))) }
test.micro2312 <- function() { cat('2312\n'); stopifnot(identical({ b <- as.raw(c(1,2)) ; b[as.double(NA)] <- as.raw(13) ; b }, as.raw(c(0x01, 0x02)))) }
test.micro2313 <- function() { cat('2313\n'); stopifnot(identical({ b <- as.raw(c(1,2)) ; b[[-2]] <- as.raw(13) ; b }, as.raw(c(0x0d, 0x02)))) }
test.micro2314 <- function() { cat('2314\n'); stopifnot(identical({ b <- as.raw(c(1,2)) ; b[[-1]] <- as.raw(13) ; b }, as.raw(c(0x01, 0x0d)))) }
test.micro2315 <- function() { cat('2315\n'); stopifnot(identical({ x <- c(a=1+2i, b=3+4i) ; x["a"] <- 10 ; x }, structure(c(10+0i, 3+4i), .Names = c("a", "b")))) }
test.micro2316 <- function() { cat('2316\n'); stopifnot(identical({ x <- as.raw(c(10,11)) ; x["a"] <- as.raw(13) ; x }, structure(as.raw(c(0x0a, 0x0b, 0x0d)), .Names = c("", "", "a")))) }
test.micro2317 <- function() { cat('2317\n'); stopifnot(identical({ x <- 1:2 ; x["a"] <- 10+3i ; x }, structure(c(1+0i, 2+0i, 10+3i), .Names = c("", "", "a")))) }
test.micro2318 <- function() { cat('2318\n'); stopifnot(identical({ x <- c(a=1+2i, b=3+4i) ; x["a"] <- "hi" ; x }, structure(c("hi", "3+4i"), .Names = c("a", "b")))) }
test.micro2319 <- function() { cat('2319\n'); stopifnot(identical({ x <- 1:2 ; x["a"] <- 10 ; x }, structure(c(1, 2, 10), .Names = c("", "", "a")))) }
test.micro2320 <- function() { cat('2320\n'); stopifnot(identical({ x <- c(a=1,a=2) ; x["a"] <- 10L ; x }, structure(c(10, 2), .Names = c("a", "a")))) }
test.micro2321 <- function() { cat('2321\n'); stopifnot(identical({ x <- 1:2 ; x["a"] <- FALSE ; x }, structure(c(1L, 2L, 0L), .Names = c("", "", "a")))) }
test.micro2322 <- function() { cat('2322\n'); stopifnot(identical({ x <- c(aa=TRUE,b=FALSE) ; x["a"] <- 2L ; x }, structure(c(1L, 0L, 2L), .Names = c("aa", "b", "a")))) }
test.micro2323 <- function() { cat('2323\n'); stopifnot(identical({ x <- c(aa=TRUE) ; x[["a"]] <- list(2L) ; x }, structure(list(aa = TRUE, a = list(2L)), .Names = c("aa", "a")))) }
test.micro2324 <- function() { cat('2324\n'); stopifnot(identical({ x <- c(aa=TRUE) ; x["a"] <- list(2L) ; x }, structure(list(aa = TRUE, a = 2L), .Names = c("aa", "a")))) }
test.micro2325 <- function() { cat('2325\n'); stopifnot(identical({ x <- c(b=2,a=3) ; z <- x ; x["a"] <- 1 ; x }, structure(c(2, 1), .Names = c("b", "a")))) }
test.micro2327 <- function() { cat('2327\n'); stopifnot(identical({ x <- list(1,2) ; dim(x) <- c(2,1) ; x[3] <- NULL ; x }, list(1, 2))) }
test.micro2328 <- function() { cat('2328\n'); stopifnot(identical({ x <- list(1,2) ; dim(x) <- c(2,1) ; x[2] <- NULL ; x }, list(1))) }
test.micro2329 <- function() { cat('2329\n'); stopifnot(identical({ x <- list(1,2) ; dim(x) <- c(2,1) ; x[[2]] <- NULL ; x }, list(1))) }
test.micro2330 <- function() { cat('2330\n'); stopifnot(identical({ x <- list(1,2) ; x[0] <- NULL ; x }, list(1, 2))) }
test.micro2331 <- function() { cat('2331\n'); stopifnot(identical({ x <- list(1,2) ; x[NA] <- NULL ; x }, list(1, 2))) }
test.micro2332 <- function() { cat('2332\n'); stopifnot(identical({ x <- list(1,2) ; x[as.integer(NA)] <- NULL ; x }, list(1, 2))) }
test.micro2333 <- function() { cat('2333\n'); stopifnot(identical({ x <- list(1,2) ; x[-1] <- NULL ; x }, list(1))) }
test.micro2334 <- function() { cat('2334\n'); stopifnot(identical({ x <- list(3,4) ; x[[-1]] <- NULL ; x }, list(3))) }
test.micro2335 <- function() { cat('2335\n'); stopifnot(identical({ x <- list(3,4) ; x[[-2]] <- NULL ; x }, list(4))) }
test.micro2336 <- function() { cat('2336\n'); stopifnot(identical({ x <- list(a=3,b=4) ; x[["a"]] <- NULL ; x }, structure(list(b = 4), .Names = "b"))) }
test.micro2337 <- function() { cat('2337\n'); stopifnot(identical({ x <- list(a=3,b=4) ; x["z"] <- NULL ; x }, structure(list(a = 3, b = 4), .Names = c("a", "b")))) }
test.micro2338 <- function() { cat('2338\n'); stopifnot(identical({ x <- as.list(1:2) ; x[["z"]] <- NULL ; x }, list(1L, 2L))) }
test.micro2339 <- function() { cat('2339\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(1:2,"hi",3L) ; f(1:2,-2,10) }, c(10, 2))) }
test.micro2340 <- function() { cat('2340\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(1:2,"hi",3L) ; f(1:2,2,10) ; f(1:2,as.integer(NA), 10) }, c(1, 2))) }
test.micro2341 <- function() { cat('2341\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:2,"hi",3L) ; f(1:2,c(2),10) ; f(1:2,2, 10) }, c(1, 10))) }
test.micro2342 <- function() { cat('2342\n'); stopifnot(identical({ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[3] <- NULL ; b }, list(1+2i, 3+4i))) }
test.micro2343 <- function() { cat('2343\n'); stopifnot(identical({ l <- list(1,2) ; l[[2]] <- as.raw(13) ; l }, list(1, as.raw(0x0d)))) }
test.micro2344 <- function() { cat('2344\n'); stopifnot(identical({ a <- c(1,2,3) ; b <- a; a[1] <- 4L; a }, c(4, 2, 3))) }
test.micro2345 <- function() { cat('2345\n'); stopifnot(identical({ a <- c(1,2,3) ; b <- a; a[2] <- 4L; a }, c(1, 4, 3))) }
test.micro2346 <- function() { cat('2346\n'); stopifnot(identical({ a <- c(1,2,3) ; b <- a; a[3] <- 4L; a }, c(1, 2, 4))) }
test.micro2347 <- function() { cat('2347\n'); stopifnot(identical({ a <- c(2.1,2.2,2.3); b <- a; a[[1]] <- TRUE; a }, c(1, 2.2, 2.3))) }
test.micro2348 <- function() { cat('2348\n'); stopifnot(identical({ a <- c(2.1,2.2,2.3); b <- a; a[[2]] <- TRUE; a }, c(2.1, 1, 2.3))) }
test.micro2349 <- function() { cat('2349\n'); stopifnot(identical({ a <- c(2.1,2.2,2.3); b <- a; a[[3]] <- TRUE; a }, c(2.1, 2.2, 1))) }
test.micro2350 <- function() { cat('2350\n'); stopifnot(identical({ a <- c(TRUE,TRUE,TRUE); b <- a; a[[1]] <- FALSE; a }, c(FALSE, TRUE, TRUE))) }
test.micro2351 <- function() { cat('2351\n'); stopifnot(identical({ a <- c(TRUE,TRUE,TRUE); b <- a; a[[2]] <- FALSE; a }, c(TRUE, FALSE, TRUE))) }
test.micro2352 <- function() { cat('2352\n'); stopifnot(identical({ a <- c(TRUE,TRUE,TRUE); b <- a; a[[3]] <- FALSE; a }, c(TRUE, TRUE, FALSE))) }
test.micro2353 <- function() { cat('2353\n'); stopifnot(identical({ x<-c(1,2,3,4,5); x[3:4]<-c(300L,400L); x }, c(1, 2, 300, 400, 5))) }
test.micro2354 <- function() { cat('2354\n'); stopifnot(identical({ x<-c(1,2,3,4,5); x[4:3]<-c(300L,400L); x }, c(1, 2, 400, 300, 5))) }
test.micro2355 <- function() { cat('2355\n'); stopifnot(identical({ x<-1:5; x[4:3]<-c(300L,400L); x }, c(1L, 2L, 400L, 300L, 5L))) }
test.micro2356 <- function() { cat('2356\n'); stopifnot(identical({ x<-5:1; x[3:4]<-c(300L,400L); x }, c(5L, 4L, 300L, 400L, 1L))) }
test.micro2357 <- function() { cat('2357\n'); stopifnot(identical({ x<-5:1; x[3:4]<-c(300,400); x }, c(5, 4, 300, 400, 1))) }
test.micro2358 <- function() { cat('2358\n'); stopifnot(identical({ x<-1:5; x[c(0-2,0-3,0-3,0-100,0)]<-256; x }, c(256, 2, 3, 256, 256))) }
test.micro2359 <- function() { cat('2359\n'); stopifnot(identical({ x<-1:5; x[c(4,2,3)]<-c(256L,257L,258L); x }, c(1L, 257L, 258L, 256L, 5L))) }
test.micro2360 <- function() { cat('2360\n'); stopifnot(identical({ x<-c(1,2,3,4,5); x[c(TRUE,FALSE)] <- 1000; x }, c(1000, 2, 1000, 4, 1000))) }
test.micro2361 <- function() { cat('2361\n'); stopifnot(identical({ x<-c(1,2,3,4,5,6); x[c(TRUE,TRUE,FALSE)] <- c(1000L,2000L) ; x }, c(1000, 2000, 3, 1000, 2000, 6))) }
test.micro2362 <- function() { cat('2362\n'); stopifnot(identical({ x<-c(1,2,3,4,5); x[c(TRUE,FALSE,TRUE,TRUE,FALSE)] <- c(1000,2000,3000); x }, c(1000, 2, 2000, 3000, 5))) }
test.micro2363 <- function() { cat('2363\n'); stopifnot(identical({ x<-c(1,2,3,4,5); x[c(TRUE,FALSE,TRUE,TRUE,0)] <- c(1000,2000,3000); x }, c(3000, 2, 3, 4, 5))) }
test.micro2364 <- function() { cat('2364\n'); stopifnot(identical({ x<-1:3; x[c(TRUE, FALSE, TRUE)] <- c(TRUE,FALSE); x }, c(1L, 2L, 0L))) }
test.micro2365 <- function() { cat('2365\n'); stopifnot(identical({ x<-c(TRUE,TRUE,FALSE); x[c(TRUE, FALSE, TRUE)] <- c(FALSE,TRUE); x }, c(FALSE, TRUE, TRUE))) }
test.micro2366 <- function() { cat('2366\n'); stopifnot(identical({ x<-c(TRUE,TRUE,FALSE); x[c(TRUE, FALSE, TRUE)] <- c(1000,2000); x }, c(1000, 1, 2000))) }
test.micro2367 <- function() { cat('2367\n'); stopifnot(identical({ x<-11:9 ; x[c(TRUE, FALSE, TRUE)] <- c(1000,2000); x }, c(1000, 10, 2000))) }
test.micro2368 <- function() { cat('2368\n'); stopifnot(identical({ l <- double() ; l[c(TRUE,TRUE)] <-2 ; l}, c(2, 2))) }
test.micro2369 <- function() { cat('2369\n'); stopifnot(identical({ l <- double() ; l[c(FALSE,TRUE)] <-2 ; l}, c(NA, 2))) }
test.micro2370 <- function() { cat('2370\n'); stopifnot(identical({ a<- c('a','b','c','d'); a[3:4] <- c(4,5); a}, c("a", "b", "4", "5"))) }
test.micro2371 <- function() { cat('2371\n'); stopifnot(identical({ a<- c('a','b','c','d'); a[3:4] <- c(4L,5L); a}, c("a", "b", "4", "5"))) }
test.micro2372 <- function() { cat('2372\n'); stopifnot(identical({ a<- c('a','b','c','d'); a[3:4] <- c(TRUE,FALSE); a}, c("a", "b", "TRUE", "FALSE"))) }
test.micro2373 <- function() { cat('2373\n'); stopifnot(identical({ f<-function(i,v) { x<-1:5 ; x[i]<-v ; x } ; f(1,1) ; f(1L,TRUE) ; f(2,TRUE) }, c(1L, 1L, 3L, 4L, 5L))) }
test.micro2374 <- function() { cat('2374\n'); stopifnot(identical({ f<-function(i,v) { x<-1:5 ; x[[i]]<-v ; x } ; f(1,1) ; f(1L,TRUE) ; f(2,TRUE) }, c(1L, 1L, 3L, 4L, 5L))) }
test.micro2375 <- function() { cat('2375\n'); stopifnot(identical({ f<-function(i,v) { x<-1:5 ; x[i]<-v ; x } ; f(3:2,1) ; f(1L,TRUE) ; f(2:4,4:2) }, c(1L, 4L, 3L, 2L, 5L))) }
test.micro2376 <- function() { cat('2376\n'); stopifnot(identical({ f<-function(i,v) { x<-1:5 ; x[i]<-v ; x } ; f(c(3,2),1) ; f(1L,TRUE) ; f(2:4,c(4,3,2)) }, c(1, 4, 3, 2, 5))) }
test.micro2377 <- function() { cat('2377\n'); stopifnot(identical({ f<-function(b,i,v) { b[i]<-v ; b } ; f(1:4,4:1,TRUE) ; f(c(3,2,1),8,10) }, c(3, 2, 1, NA, NA, NA, NA, 10))) }
test.micro2378 <- function() { cat('2378\n'); stopifnot(identical({ f<-function(b,i,v) { b[i]<-v ; b } ; f(1:4,4:1,TRUE) ; f(c(3,2,1),8,10) ; f(c(TRUE,FALSE),TRUE,FALSE) }, c(FALSE, FALSE))) }
test.micro2379 <- function() { cat('2379\n'); stopifnot(identical({ x<-c(TRUE,TRUE,FALSE,TRUE) ; x[3:2] <- TRUE; x }, c(TRUE, TRUE, TRUE, TRUE))) }
test.micro2380 <- function() { cat('2380\n'); stopifnot(identical({ x<-1:3 ; y<-(x[2]<-100) ; y }, 100)) }
test.micro2381 <- function() { cat('2381\n'); stopifnot(identical({ x<-1:5 ; x[x[4]<-2] <- (x[4]<-100) ; x }, c(1, 100, 3, 2, 5))) }
test.micro2382 <- function() { cat('2382\n'); stopifnot(identical({ x<-1:5 ; x[3] <- (x[4]<-100) ; x }, c(1, 2, 100, 100, 5))) }
test.micro2383 <- function() { cat('2383\n'); stopifnot(identical({ x<-5:1 ; x[x[2]<-2] }, 4L)) }
test.micro2384 <- function() { cat('2384\n'); stopifnot(identical({ x<-5:1 ; x[x[2]<-2] <- (x[3]<-50) ; x }, c(5, 50, 50, 2, 1))) }
test.micro2385 <- function() { cat('2385\n'); stopifnot(identical({ v<-1:3 ; v[TRUE] <- 100 ; v }, c(100, 100, 100))) }
test.micro2386 <- function() { cat('2386\n'); stopifnot(identical({ v<-1:3 ; v[-1] <- c(100,101) ; v }, c(1, 100, 101))) }
test.micro2387 <- function() { cat('2387\n'); stopifnot(identical({ v<-1:3 ; v[TRUE] <- c(100,101,102) ; v }, c(100, 101, 102))) }
test.micro2388 <- function() { cat('2388\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3) ; x[2]<-10; x }, structure(c(1, 10, 3), .Names = c("a", "b", "c")))) }
test.micro2389 <- function() { cat('2389\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3) ; x[2:3]<-10; x }, structure(c(1, 10, 10), .Names = c("a", "b", "c")))) }
test.micro2390 <- function() { cat('2390\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3) ; x[c(2,3)]<-10; x }, structure(c(1, 10, 10), .Names = c("a", "b", "c")))) }
test.micro2391 <- function() { cat('2391\n'); stopifnot(identical({ x <- c(a=1,b=2,c=3) ; x[c(TRUE,TRUE,FALSE)]<-10; x }, structure(c(10, 10, 3), .Names = c("a", "b", "c")))) }
test.micro2392 <- function() { cat('2392\n'); stopifnot(identical({ x <- c(a=1,b=2) ; x[2:3]<-10; x }, structure(c(1, 10, 10), .Names = c("a", "b", "")))) }
test.micro2393 <- function() { cat('2393\n'); stopifnot(identical({ x <- c(a=1,b=2) ; x[c(2,3)]<-10; x }, structure(c(1, 10, 10), .Names = c("a", "b", "")))) }
test.micro2394 <- function() { cat('2394\n'); stopifnot(identical({ x <- c(a=1,b=2) ; x[3]<-10; x }, structure(c(1, 2, 10), .Names = c("a", "b", "")))) }
test.micro2395 <- function() { cat('2395\n'); stopifnot(identical({ x <- matrix(1:2) ; x[c(FALSE,FALSE,TRUE)]<-10; x }, c(1, 2, 10))) }
test.micro2396 <- function() { cat('2396\n'); stopifnot(identical({ x <- 1:2 ; x[c(FALSE,FALSE,TRUE)]<-10; x }, c(1, 2, 10))) }
test.micro2397 <- function() { cat('2397\n'); stopifnot(identical({ x <- c(a=1,b=2) ; x[c(FALSE,FALSE,TRUE)]<-10; x }, structure(c(1, 2, 10), .Names = c("a", "b", "")))) }
test.micro2398 <- function() { cat('2398\n'); stopifnot(identical({ x<-c(a=1,b=2,c=3) ; x[["b"]]<-200; x }, structure(c(1, 200, 3), .Names = c("a", "b", "c")))) }
test.micro2399 <- function() { cat('2399\n'); stopifnot(identical({ x<-c(a=1,b=2,c=3) ; x[["d"]]<-200; x }, structure(c(1, 2, 3, 200), .Names = c("a", "b", "c", "d")))) }
test.micro2400 <- function() { cat('2400\n'); stopifnot(identical({ x<-c() ; x[c("a","b","c","d")]<-c(1,2); x }, structure(c(1, 2, 1, 2), .Names = c("a", "b", "c", "d")))) }
test.micro2401 <- function() { cat('2401\n'); stopifnot(identical({ x<-c(a=1,b=2,c=3) ; x["d"]<-4 ; x }, structure(c(1, 2, 3, 4), .Names = c("a", "b", "c", "d")))) }
test.micro2402 <- function() { cat('2402\n'); stopifnot(identical({ x<-c(a=1,b=2,c=3) ; x[c("d","e")]<-c(4,5) ; x }, structure(c(1, 2, 3, 4, 5), .Names = c("a", "b", "c", "d", "e")))) }
test.micro2403 <- function() { cat('2403\n'); stopifnot(identical({ x<-c(a=1,b=2,c=3) ; x[c("d","a","d","a")]<-c(4,5) ; x }, structure(c(5, 2, 3, 4), .Names = c("a", "b", "c", "d")))) }
test.micro2404 <- function() { cat('2404\n'); stopifnot(identical({ a = c(1, 2); a[['a']] = 67; a; }, structure(c(1, 2, 67), .Names = c("", "", "a")))) }
test.micro2405 <- function() { cat('2405\n'); stopifnot(identical({ a = c(a=1,2,3); a[['x']] = 67; a; }, structure(c(1, 2, 3, 67), .Names = c("a", "", "", "x")))) }
test.micro2406 <- function() { cat('2406\n'); stopifnot(identical({ x <- c(TRUE,TRUE,TRUE,TRUE); x[2:3] <- c(FALSE,FALSE); x }, c(TRUE, FALSE, FALSE, TRUE))) }
test.micro2407 <- function() { cat('2407\n'); stopifnot(identical({ x <- c(TRUE,TRUE,TRUE,TRUE); x[3:2] <- c(FALSE,TRUE); x }, c(TRUE, TRUE, FALSE, TRUE))) }
test.micro2408 <- function() { cat('2408\n'); stopifnot(identical({ x <- c('a','b','c','d'); x[2:3] <- 'x'; x}, c("a", "x", "x", "d"))) }
test.micro2409 <- function() { cat('2409\n'); stopifnot(identical({ x <- c('a','b','c','d'); x[2:3] <- c('x','y'); x}, c("a", "x", "y", "d"))) }
test.micro2410 <- function() { cat('2410\n'); stopifnot(identical({ x <- c('a','b','c','d'); x[3:2] <- c('x','y'); x}, c("a", "y", "x", "d"))) }
test.micro2411 <- function() { cat('2411\n'); stopifnot(identical({ x <- c('a','b','c','d'); x[c(TRUE,FALSE,TRUE)] <- c('x','y','z'); x }, c("x", "b", "y", "z"))) }
test.micro2412 <- function() { cat('2412\n'); stopifnot(identical({ x <- c(TRUE,TRUE,TRUE,TRUE); x[c(TRUE,TRUE,FALSE)] <- c(10L,20L,30L); x }, c(10L, 20L, 1L, 30L))) }
test.micro2413 <- function() { cat('2413\n'); stopifnot(identical({ x <- c(1L,1L,1L,1L); x[c(TRUE,TRUE,FALSE)] <- c('a','b','c'); x}, c("a", "b", "1", "c"))) }
test.micro2414 <- function() { cat('2414\n'); stopifnot(identical({ x <- c(TRUE,TRUE,TRUE,TRUE); x[c(TRUE,TRUE,FALSE)] <- list(10L,20L,30L); x }, list(10L, 20L, TRUE, 30L))) }
test.micro2415 <- function() { cat('2415\n'); stopifnot(identical({ x <- c(); x[c('a','b')] <- c(1L,2L); x }, structure(1:2, .Names = c("a", "b")))) }
test.micro2416 <- function() { cat('2416\n'); stopifnot(identical({ x <- c(); x[c('a','b')] <- c(TRUE,FALSE); x }, structure(c(TRUE, FALSE), .Names = c("a", "b")))) }
test.micro2417 <- function() { cat('2417\n'); stopifnot(identical({ x <- c(); x[c('a','b')] <- c('a','b'); x }, structure(c("a", "b"), .Names = c("a", "b")))) }
test.micro2418 <- function() { cat('2418\n'); stopifnot(identical({ x <- list(); x[c('a','b')] <- c('a','b'); x }, structure(list(a = "a", b = "b"), .Names = c("a", "b")))) }
test.micro2419 <- function() { cat('2419\n'); stopifnot(identical({ x <- list(); x[c('a','b')] <- list('a','b'); x }, structure(list(a = "a", b = "b"), .Names = c("a", "b")))) }
test.micro2420 <- function() { cat('2420\n'); stopifnot(identical({ x = c(1,2,3,4); x[x %% 2 == 0] <- c(1,2,3,4); }, c(1, 2, 3, 4))) }
test.micro2421 <- function() { cat('2421\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, 10) ; f(1:2, 1:2, 11) }, c(11, 11))) }
test.micro2422 <- function() { cat('2422\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, TRUE) }, list(TRUE, TRUE))) }
test.micro2423 <- function() { cat('2423\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, 11L) }, list(11L, 11L))) }
test.micro2424 <- function() { cat('2424\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, TRUE) ;  f(list(1,2), 1:2, as.raw(10))}, list(as.raw(0x0a), as.raw(0x0a)))) }
test.micro2425 <- function() { cat('2425\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(list(1,2), 1:2, c(1+2i,3+4i))}, list(1+2i, 3+4i))) }
test.micro2426 <- function() { cat('2426\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(1:2, 1:2, c(10,5))}, c(10, 5))) }
test.micro2427 <- function() { cat('2427\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(list(1,2), 1:3, c(2,10,5)) }, list(2, 10, 5))) }
test.micro2428 <- function() { cat('2428\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2,3,4,5), 4:3, c(TRUE,NA)) }, list(1, 2, NA, TRUE, 5))) }
test.micro2429 <- function() { cat('2429\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2,3,4), seq(1L,4L,2L), c(TRUE,NA)) }, list(TRUE, 2, NA, 4))) }
test.micro2430 <- function() { cat('2430\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2),1:2,3:4) }, list(3L, 4L))) }
test.micro2431 <- function() { cat('2431\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2),1:2,c(4,3)) }, list(4, 3))) }
test.micro2432 <- function() { cat('2432\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2),1:2,c(1+2i,3+2i)) }, list(1+2i, 3+2i))) }
test.micro2433 <- function() { cat('2433\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,1+2i) }, c(1+2i, 1+2i, 10+0i))) }
test.micro2434 <- function() { cat('2434\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(3,NA)) }, c(3, NA, 10))) }
test.micro2435 <- function() { cat('2435\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(3L,NA)) }, c(3, NA, 10))) }
test.micro2436 <- function() { cat('2436\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(TRUE,FALSE)) }, c(1, 0, 10))) }
test.micro2437 <- function() { cat('2437\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),1:2,c(TRUE,FALSE)) ; f(c(10L,4L), 2:1, 1+2i) }, c(1+2i, 1+2i))) }
test.micro2438 <- function() { cat('2438\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10),-1:0,c(TRUE,FALSE)) }, c(1, 1, 0))) }
test.micro2439 <- function() { cat('2439\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,3,10), seq(2L,4L,2L) ,c(TRUE,FALSE)) }, c(1, 1, 10, 0))) }
test.micro2440 <- function() { cat('2440\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.double(1:5), seq(1L,6L,2L) ,c(TRUE,FALSE,NA)) }, c(1, 2, 0, 4, NA))) }
test.micro2441 <- function() { cat('2441\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.double(1:5), seq(7L,1L,-3L) ,c(TRUE,FALSE,NA)) }, c(NA, 2, 3, 0, 5, NA, 1))) }
test.micro2442 <- function() { cat('2442\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),2:1,1+2i) }, c(1+2i, 1+2i, 10+0i))) }
test.micro2443 <- function() { cat('2443\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),2:1,c(3,NA)) }, c(NA, 3, 10))) }
test.micro2444 <- function() { cat('2444\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),2:1,c(3L,NA)) }, c(NA, 3L, 10L))) }
test.micro2445 <- function() { cat('2445\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),1:2,c(TRUE,FALSE)) }, c(1L, 0L, 10L))) }
test.micro2446 <- function() { cat('2446\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1L,3L,10L),1:2,c(TRUE,FALSE)) ; f(c(10,4), 2:1, 1+2i) }, c(1+2i, 1+2i))) }
test.micro2447 <- function() { cat('2447\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:5, seq(1L,6L,2L) ,c(TRUE,FALSE,NA)) }, c(1L, 2L, 0L, 4L, NA))) }
test.micro2448 <- function() { cat('2448\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2, seq(1L,6L,2L) ,c(TRUE,FALSE,NA)) }, c(1L, 2L, 0L, NA, NA))) }
test.micro2449 <- function() { cat('2449\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,FALSE,NA),2:1,1+2i) }, c(1+2i, 1+2i, complex(real=NA, i=NA)))) }
test.micro2450 <- function() { cat('2450\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,NA,FALSE),2:1,c(TRUE,NA)) }, c(NA, TRUE, FALSE))) }
test.micro2451 <- function() { cat('2451\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,NA,FALSE),2:0,c(TRUE,NA)) }, c(NA, TRUE, FALSE))) }
test.micro2452 <- function() { cat('2452\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,NA,FALSE),3:4,c(TRUE,NA)) }, c(TRUE, NA, TRUE, NA))) }
test.micro2453 <- function() { cat('2453\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.logical(-3:3),seq(1L,7L,3L),c(TRUE,NA,FALSE)) }, c(TRUE, TRUE, TRUE, NA, TRUE, TRUE, FALSE))) }
test.micro2454 <- function() { cat('2454\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,FALSE),2:1,c(NA,NA)) ; f(c(TRUE,FALSE),1:2,3:4) }, 3:4)) }
test.micro2455 <- function() { cat('2455\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(TRUE,FALSE),2:1,c(NA,NA)) ; f(10:11,1:2,c(NA,FALSE)) }, c(NA, 0L))) }
test.micro2456 <- function() { cat('2456\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c("a","b"),2:1,1+2i) }, c("1+2i", "1+2i"))) }
test.micro2457 <- function() { cat('2457\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.character(-3:3),seq(1L,7L,3L),c("A","a","XX")) }, c("A", "-2", "-1", "a", "1", "2", "XX"))) }
test.micro2458 <- function() { cat('2458\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c("hello","hi","X"), -1:-2, "ZZ") }, c("hello", "hi", "ZZ"))) }
test.micro2459 <- function() { cat('2459\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c("hello","hi","X"), 3:4, "ZZ") }, c("hello", "hi", "ZZ", "ZZ"))) }
test.micro2460 <- function() { cat('2460\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c("hello","hi","X"), 1:2, c("ZZ","xx")) ; f(1:4,1:2,NA) }, c(NA, NA, 3L, 4L))) }
test.micro2461 <- function() { cat('2461\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c("hello","hi","X"), 1:2, c("ZZ","xx")) ; f(as.character(1:2),1:2,NA) }, c(NA_character_, NA_character_))) }
test.micro2462 <- function() { cat('2462\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1+2i,2+3i), 1:2, c(10+1i,2+4i)) }, c(10+1i, 2+4i))) }
test.micro2463 <- function() { cat('2463\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(as.raw(1:3), 1:2, as.raw(40:41)) }, as.raw(c(0x28, 0x29, 0x03)))) }
test.micro2464 <- function() { cat('2464\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(list(1,2), 1:2, c(TRUE,NA)) ;  f(1:2, c(0,0), c(1+2i,3+4i))}, c(1+0i, 2+0i))) }
test.micro2465 <- function() { cat('2465\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1:2, 3:4); f(c(TRUE,FALSE), 2:1, 1:2) }, c(2L, 1L))) }
test.micro2466 <- function() { cat('2466\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1:2, 3:4); f(3:4, 2:1, c(NA,FALSE)) }, c(0L, NA))) }
test.micro2467 <- function() { cat('2467\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(TRUE,FALSE,NA), 1:2, c(FALSE,TRUE)) }, c(FALSE, TRUE, NA))) }
test.micro2468 <- function() { cat('2468\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4), 1:2, c(NA,NA)) }, c(NA_real_, NA_real_))) }
test.micro2469 <- function() { cat('2469\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4), 1:2, c("hello","hi")) }, c("hello", "hi"))) }
test.micro2470 <- function() { cat('2470\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4,8), 1:2, list(3,TRUE)) }, list(3, TRUE, 8))) }
test.micro2472 <- function() { cat('2472\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); l <- list(3,5L) ; dim(l) <- c(2,1) ; f(5:6,1:2,c(3,4)) ; f(list(3,TRUE), 1:2, l) }, list(3, 5L))) }
test.micro2474 <- function() { cat('2474\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); l <- list(3,5L) ; dim(l) <- c(2,1) ; f(5:6,1:2,c(3,4)) ; m <- c(3,TRUE) ; dim(m) <- c(1,2) ; f(m, 1:2, l) }, list(3, 5L))) }
test.micro2475 <- function() { cat('2475\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4,8), -1:-2, 10) }, c(3, 4, 10))) }
test.micro2476 <- function() { cat('2476\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(c(3,4,8), 3:4, 10) }, c(3, 4, 10, 10))) }
test.micro2477 <- function() { cat('2477\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; f(1:8, seq(1L,7L,3L), c(10,100,1000)) }, c(10, 2, 3, 100, 5, 6, 1000, 8))) }
test.micro2478 <- function() { cat('2478\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:2,1:2,3:4); f(1:2,1:2,c(3,4)) ; z <- f(1:8, seq(1L,7L,3L), list(10,100,1000)) ; sum(as.double(z)) }, 1134)) }
test.micro2480 <- function() { cat('2480\n'); stopifnot(identical({ b <- 1:3 ; b[c(3,2)] <- list(TRUE,10) ; b }, list(1L, 10, TRUE))) }
test.micro2481 <- function() { cat('2481\n'); stopifnot(identical({ b <- as.raw(11:13) ; b[c(3,2)] <- list(2) ; b }, list(as.raw(0x0b), 2, 2))) }
test.micro2482 <- function() { cat('2482\n'); stopifnot(identical({ b <- as.raw(11:13) ; b[c(3,2)] <- as.raw(2) ; b }, as.raw(c(0x0b, 0x02, 0x02)))) }
test.micro2483 <- function() { cat('2483\n'); stopifnot(identical({ b <- c(TRUE,NA,FALSE) ; b[c(3,2)] <- FALSE ; b }, c(TRUE, FALSE, FALSE))) }
test.micro2484 <- function() { cat('2484\n'); stopifnot(identical({ b <- 1:4 ; b[c(3,2)] <- c(NA,NA) ; b }, c(1L, NA, NA, 4L))) }
test.micro2485 <- function() { cat('2485\n'); stopifnot(identical({ b <- c(TRUE,FALSE) ; b[c(3,2)] <- 5:6 ; b }, c(1L, 6L, 5L))) }
test.micro2486 <- function() { cat('2486\n'); stopifnot(identical({ b <- c(1+2i,3+4i) ; b[c(3,2)] <- 5:6 ; b }, c(1+2i, 6+0i, 5+0i))) }
test.micro2487 <- function() { cat('2487\n'); stopifnot(identical({ b <- 3:4 ; b[c(3,2)] <- c(1+2i,3+4i) ; b }, c(3+0i, 3+4i, 1+2i))) }
test.micro2488 <- function() { cat('2488\n'); stopifnot(identical({ b <- c("hello","hi") ; b[c(3,2)] <- c(2,3) ; b }, c("hello", "3", "2"))) }
test.micro2489 <- function() { cat('2489\n'); stopifnot(identical({ b <- 3:4 ; b[c(3,2)] <- c("X","xx") ; b }, c("3", "xx", "X"))) }
test.micro2490 <- function() { cat('2490\n'); stopifnot(identical({ b <- 3:4 ; b[c(0,1)] <- c(2,10,11) ; b }, c(2, 4))) }
test.micro2491 <- function() { cat('2491\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(3:4, c(1,2), c(10,11)) ; f(4:5, as.integer(NA), 2) }, c(4, 5))) }
test.micro2492 <- function() { cat('2492\n'); stopifnot(identical({ b <- c(1,4,5) ; x <- c(2,8,2) ; b[x==2] <- c(10,11) ; b }, c(10, 4, 11))) }
test.micro2493 <- function() { cat('2493\n'); stopifnot(identical({ b <- c(1,4,5) ; z <- b ; x <- c(2,8,2) ; b[x==2] <- c(10,11) ; b }, c(10, 4, 11))) }
test.micro2494 <- function() { cat('2494\n'); stopifnot(identical({ b <- c(1,4,5) ;  x <- c(2,2) ; b[x==2] <- c(10,11) ; b }, c(10, 11, 10))) }
test.micro2495 <- function() { cat('2495\n'); stopifnot(identical({ b <- c(1,2,5) ;  x <- as.double(NA) ; attr(x,"my") <- 2 ; b[c(1,NA,2)==2] <- x ; b }, c(1, 2, NA))) }
test.micro2496 <- function() { cat('2496\n'); stopifnot(identical({ b <- c(1,2,5) ;  x <- c(2,2,-1) ; b[x==2] <- c(10,11,5) ; b }, c(10, 11, 5))) }
test.micro2497 <- function() { cat('2497\n'); stopifnot(identical({ b <- c(1,2,5) ; b[integer()] <- NULL ; b }, c(1, 2, 5))) }
test.micro2499 <- function() { cat('2499\n'); stopifnot(identical({ b <- list(1,2,5) ; b[c(1,1,5)] <- NULL ; b }, list(2, 5, NULL))) }
test.micro2500 <- function() { cat('2500\n'); stopifnot(identical({ b <- list(1,2,5) ; b[c(-1,-4,-5,-1,-5)] <- NULL ; b }, list(1))) }
test.micro2501 <- function() { cat('2501\n'); stopifnot(identical({ b <- list(1,2,5) ; b[c(1,1,0,NA,5,5,7)] <- NULL ; b }, list(2, 5, NULL, NULL))) }
test.micro2502 <- function() { cat('2502\n'); stopifnot(identical({ b <- list(1,2,5) ; b[c(0,-1)] <- NULL ; b }, list(1))) }
test.micro2503 <- function() { cat('2503\n'); stopifnot(identical({ b <- list(1,2,5) ; b[c(1,NA)] <- NULL ; b }, list(2, 5))) }
test.micro2504 <- function() { cat('2504\n'); stopifnot(identical({ b <- list(x=1,y=2,z=5) ; b[c(0,-1)] <- NULL ; b }, structure(list(x = 1), .Names = "x"))) }
test.micro2505 <- function() { cat('2505\n'); stopifnot(identical({ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,-1)] <- NULL ; b }, list(1))) }
test.micro2507 <- function() { cat('2507\n'); stopifnot(identical({ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(-10,-20,0)] <- NULL ; b }, list())) }
test.micro2509 <- function() { cat('2509\n'); stopifnot(identical({ b <- list(1,2,5) ; dim(b) <- c(1,3) ; b[c(0,3,5)] <- NULL ; b }, list(1, 2, NULL))) }
test.micro2510 <- function() { cat('2510\n'); stopifnot(identical({ b <- c(1,2,5) ; b[logical()] <- NULL ; b }, c(1, 2, 5))) }
test.micro2511 <- function() { cat('2511\n'); stopifnot(identical({ b <- c(1,2,5) ; b[c(TRUE,FALSE,TRUE)] <- list(TRUE,1+2i) ; b }, list(TRUE, 2, 1+2i))) }
test.micro2515 <- function() { cat('2515\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c("a","XX","b"), c(FALSE,TRUE,TRUE), 21:22) }, c("a", "21", "22"))) }
test.micro2516 <- function() { cat('2516\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(10,12,3), c(FALSE,TRUE,TRUE), c("hi",NA)) }, c("10", "hi", NA))) }
test.micro2517 <- function() { cat('2517\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(10,12,3), c(FALSE,TRUE,TRUE), c(1+2i,10)) }, c(10+0i, 1+2i, 10+0i))) }
test.micro2518 <- function() { cat('2518\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(3+4i,5+6i), c(FALSE,TRUE,TRUE), c("hi",NA)) }, c("3+4i", "hi", NA))) }
test.micro2519 <- function() { cat('2519\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(3+4i,5+6i), c(FALSE,TRUE,TRUE), c(NA,1+10i)) }, c(3+4i, complex(real=NA, i=NA), 1+10i))) }
test.micro2520 <- function() { cat('2520\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(TRUE,FALSE), c(FALSE,TRUE,TRUE), c(NA,2L)) }, c(1L, NA, 2L))) }
test.micro2521 <- function() { cat('2521\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), c(NA,FALSE)) }, c(3L, NA, 0L))) }
test.micro2522 <- function() { cat('2522\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), 4:6) }, 3:5)) }
test.micro2523 <- function() { cat('2523\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,c(TRUE,FALSE,TRUE),5:6) ; f(c(TRUE,TRUE,FALSE), c(FALSE,TRUE,TRUE), c(TRUE,NA)) }, c(TRUE, TRUE, NA))) }
test.micro2524 <- function() { cat('2524\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), c(NA,FALSE)) }, c(3L, NA, 0L))) }
test.micro2525 <- function() { cat('2525\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,TRUE,TRUE), 4:6) }, 3:5)) }
test.micro2526 <- function() { cat('2526\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(c(1,2,3),c(TRUE,FALSE,TRUE),5:6) ; f(3:5, c(FALSE,NA), 4) }, c(3, 4, 5))) }
test.micro2527 <- function() { cat('2527\n'); stopifnot(identical({ b <- as.list(3:6) ; dim(b) <- c(4,1) ; b[c(TRUE,FALSE)] <- NULL ; b }, list(4L, 6L))) }
test.micro2528 <- function() { cat('2528\n'); stopifnot(identical({ b <- as.list(3:6) ; names(b) <- c("X","Y","Z","Q") ; b[c(TRUE,FALSE)] <- NULL ; b }, structure(list(Y = 4L, Q = 6L), .Names = c("Y", "Q")))) }
test.micro2529 <- function() { cat('2529\n'); stopifnot(identical({ b <- as.list(3:6) ; names(b) <- c("X","Y","Z","Q") ; b[c(FALSE,FALSE)] <- NULL ; b }, structure(list(X = 3L, Y = 4L, Z = 5L, Q = 6L), .Names = c("X", "Y", "Z", "Q")))) }
test.micro2531 <- function() { cat('2531\n'); stopifnot(identical({ b <- as.list(3:6) ; dim(b) <- c(1,4) ; b[c(FALSE,FALSE,TRUE)] <- NULL ; b }, list(3L, 4L, 6L))) }
test.micro2533 <- function() { cat('2533\n'); stopifnot(identical({ b <- as.list(3:5) ; dim(b) <- c(1,3) ; b[c(FALSE,TRUE,NA)] <- NULL ; b }, list(3L, 5L))) }
test.micro2534 <- function() { cat('2534\n'); stopifnot(identical({ b <- 1:3 ; b[integer()] <- 3:5 ; b }, 1:3)) }
test.micro2535 <- function() { cat('2535\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) }, list(1+2i, 2))) }
test.micro2536 <- function() { cat('2536\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(1:2, c(TRUE,FALSE), list(TRUE)) }, list(TRUE, 2L))) }
test.micro2537 <- function() { cat('2537\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), TRUE) }, list(TRUE, 2L))) }
test.micro2538 <- function() { cat('2538\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), 1+2i) }, list(1+2i, 2L))) }
test.micro2539 <- function() { cat('2539\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), 10) }, list(10, 2L))) }
test.micro2540 <- function() { cat('2540\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,FALSE), list(1+2i)) ; f(as.list(1:2), c(TRUE,FALSE), 10L) }, list(10L, 2L))) }
test.micro2541 <- function() { cat('2541\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,NA), list(1+2i)) }, list(1+2i, 2))) }
test.micro2542 <- function() { cat('2542\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(list(1,2), c(TRUE,NA), 10) }, list(10, 2))) }
test.micro2544 <- function() { cat('2544\n'); stopifnot(identical({ x <- list(1,0) ; x[as.logical(x)] <- c(10,11); x }, list(10, 0))) }
test.micro2545 <- function() { cat('2545\n'); stopifnot(identical({ x <- list(1,0) ; x[is.na(x)] <- c(10,11); x }, list(1, 0))) }
test.micro2546 <- function() { cat('2546\n'); stopifnot(identical({ x <- list(1,0) ; x[c(TRUE,FALSE)] <- x[2:1] ; x }, list(0, 0))) }
test.micro2548 <- function() { cat('2548\n'); stopifnot(identical({ x <- list(1,0) ; x[is.na(x)] <- c(10L,11L); x }, list(1, 0))) }
test.micro2549 <- function() { cat('2549\n'); stopifnot(identical({ x <- list(1,0) ; x[c(TRUE,TRUE)] <- c(TRUE,NA); x }, list(TRUE, NA))) }
test.micro2550 <- function() { cat('2550\n'); stopifnot(identical({ x <- list(1,0) ; x[logical()] <- c(TRUE,NA); x }, list(1, 0))) }
test.micro2551 <- function() { cat('2551\n'); stopifnot(identical({ x <- c(1,0) ; x[c(TRUE,TRUE)] <- c(TRUE,NA); x }, c(1, NA))) }
test.micro2552 <- function() { cat('2552\n'); stopifnot(identical({ x <- c(1,0) ; x[c(TRUE,TRUE)] <- 3:4; x }, c(3, 4))) }
test.micro2553 <- function() { cat('2553\n'); stopifnot(identical({ x <- c(1,0) ; x[logical()] <- 3:4; x }, c(1, 0))) }
test.micro2555 <- function() { cat('2555\n'); stopifnot(identical({ x <- c(1,0) ; z <- x ; x[c(NA,TRUE)] <- TRUE; x }, c(1, 1))) }
test.micro2556 <- function() { cat('2556\n'); stopifnot(identical({ x <- c(1,0)  ; x[is.na(x)] <- TRUE; x }, c(1, 0))) }
test.micro2557 <- function() { cat('2557\n'); stopifnot(identical({ x <- c(1,0)  ; x[c(TRUE,TRUE)] <- rev(x) ; x }, c(0, 1))) }
test.micro2558 <- function() { cat('2558\n'); stopifnot(identical({ x <- c(1,0) ; f <- function(v) { x[c(TRUE,TRUE)] <- v ; x } ; f(1:2) ; f(c(1,2)) }, c(1, 2))) }
test.micro2559 <- function() { cat('2559\n'); stopifnot(identical({ x <- c(1,0) ; f <- function(v) { x[c(TRUE,TRUE)] <- v ; x } ; f(1:2) ; f(1+2i) }, c(1+2i, 1+2i))) }
test.micro2562 <- function() { cat('2562\n'); stopifnot(identical({ x <- 1:2 ; x[c(TRUE,FALSE,FALSE,TRUE)] <- 3:4 ; x }, c(3L, 2L, NA, 4L))) }
test.micro2563 <- function() { cat('2563\n'); stopifnot(identical({ x <- 1:2 ; x[c(TRUE,FALSE,FALSE,NA)] <- 3L ; x }, c(3L, 2L, NA, NA))) }
test.micro2564 <- function() { cat('2564\n'); stopifnot(identical({ x <- 1:2 ; x[c(TRUE,NA)] <- 3L ; x }, c(3L, 2L))) }
test.micro2565 <- function() { cat('2565\n'); stopifnot(identical({ x <- c(1L,2L) ; x[c(TRUE,FALSE)] <- 3L ; x }, c(3L, 2L))) }
test.micro2566 <- function() { cat('2566\n'); stopifnot(identical({ x <- c(1L,2L) ; x[c(TRUE,NA)] <- 3L ; x }, c(3L, 2L))) }
test.micro2567 <- function() { cat('2567\n'); stopifnot(identical({ x <- c(1L,2L) ; x[TRUE] <- 3L ; x }, c(3L, 3L))) }
test.micro2568 <- function() { cat('2568\n'); stopifnot(identical({ x <- c(1L,2L,3L,4L) ; x[c(TRUE,FALSE)] <- 5:6 ; x }, c(5L, 2L, 6L, 4L))) }
test.micro2570 <- function() { cat('2570\n'); stopifnot(identical({ x <- c(1L,2L,3L,4L) ;  x[is.na(x)] <- 5:6 ; x }, 1:4)) }
test.micro2571 <- function() { cat('2571\n'); stopifnot(identical({ x <- c(1L,2L,3L,4L) ; x[c(TRUE,FALSE)] <- rev(x) ; x }, c(4L, 2L, 3L, 4L))) }
test.micro2572 <- function() { cat('2572\n'); stopifnot(identical({ x <- c(1L,2L) ; x[logical()] <- 3L ; x }, 1:2)) }
test.micro2573 <- function() { cat('2573\n'); stopifnot(identical({ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE)] <- c(FALSE,NA) ; b }, c(FALSE, NA, NA, TRUE))) }
test.micro2574 <- function() { cat('2574\n'); stopifnot(identical({ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,FALSE)] <- c(FALSE,NA) ; b }, c(FALSE, NA, FALSE, NA))) }
test.micro2575 <- function() { cat('2575\n'); stopifnot(identical({ b <- c(TRUE,NA,FALSE) ; b[c(TRUE,TRUE)] <- c(FALSE,NA) ; b }, c(FALSE, NA, FALSE))) }
test.micro2576 <- function() { cat('2576\n'); stopifnot(identical({ b <- c(TRUE,NA,FALSE) ; b[c(TRUE,FALSE,TRUE,TRUE)] <- c(FALSE,NA,NA) ; b }, c(FALSE, NA, NA, NA))) }
test.micro2577 <- function() { cat('2577\n'); stopifnot(identical({ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,TRUE,NA)] <- FALSE ; b }, c(FALSE, NA, FALSE, TRUE))) }
test.micro2578 <- function() { cat('2578\n'); stopifnot(identical({ b <- c(TRUE,NA,FALSE,TRUE) ; z <- b ; b[c(TRUE,FALSE,TRUE,NA)] <- FALSE ; b }, c(FALSE, NA, FALSE, TRUE))) }
test.micro2580 <- function() { cat('2580\n'); stopifnot(identical({ b <- c(TRUE,NA,FALSE,TRUE) ; b[c(TRUE,FALSE,TRUE,FALSE)] <- b ; b }, c(TRUE, NA, NA, TRUE))) }
test.micro2581 <- function() { cat('2581\n'); stopifnot(identical({ b <- c(TRUE,FALSE,FALSE,TRUE) ; b[b] <- c(TRUE,FALSE) ; b }, c(TRUE, FALSE, FALSE, FALSE))) }
test.micro2582 <- function() { cat('2582\n'); stopifnot(identical({ f <- function(b,i,v) { b[b] <- b ; b } ; f(c(TRUE,FALSE,FALSE,TRUE)) ; f(1:3) }, 1:3)) }
test.micro2583 <- function() { cat('2583\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(TRUE,FALSE,FALSE,TRUE),c(TRUE,FALSE), NA) ; f(1:4, c(TRUE,TRUE), NA) }, c(NA_integer_, NA_integer_, NA_integer_, NA_integer_))) }
test.micro2584 <- function() { cat('2584\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(c(TRUE,FALSE,FALSE,TRUE),c(TRUE,FALSE), NA) ; f(c(FALSE,FALSE,TRUE), c(TRUE,TRUE), c(1,2,3)) }, c(1, 2, 3))) }
test.micro2585 <- function() { cat('2585\n'); stopifnot(identical({ b <- c(TRUE,NA,FALSE,TRUE) ; b[logical()] <- c(FALSE,NA) ; b }, c(TRUE, NA, FALSE, TRUE))) }
test.micro2586 <- function() { cat('2586\n'); stopifnot(identical({ b <- c("a","b","c") ; b[c(TRUE,FALSE)] <- "X" ; b }, c("X", "b", "X"))) }
test.micro2587 <- function() { cat('2587\n'); stopifnot(identical({ b <- c("a","b","c") ; b[c(TRUE,FALSE,TRUE,TRUE)] <- "X" ; b }, c("X", "b", "X", "X"))) }
test.micro2588 <- function() { cat('2588\n'); stopifnot(identical({ b <- c("a","b","c") ; b[c(TRUE,FALSE,TRUE,NA)] <- "X" ; b }, c("X", "b", "X", NA))) }
test.micro2589 <- function() { cat('2589\n'); stopifnot(identical({ b <- c("a","b","c") ; b[c(TRUE,FALSE,NA)] <- "X" ; b }, c("X", "b", "c"))) }
test.micro2590 <- function() { cat('2590\n'); stopifnot(identical({ b <- c("a","b","c") ; b[logical()] <- "X" ; b }, c("a", "b", "c"))) }
test.micro2592 <- function() { cat('2592\n'); stopifnot(identical({ b <- c("a","b","c") ; b[logical()] <- "X" ; b }, c("a", "b", "c"))) }
test.micro2593 <- function() { cat('2593\n'); stopifnot(identical({ b <- c("a","b","c") ; b[c(FALSE,TRUE,TRUE)] <- c("X","y","z") ; b }, c("a", "X", "y"))) }
test.micro2594 <- function() { cat('2594\n'); stopifnot(identical({ b <- c("a","b","c") ; x <- b ; b[c(FALSE,TRUE,TRUE)] <- c("X","z") ; b }, c("a", "X", "z"))) }
test.micro2595 <- function() { cat('2595\n'); stopifnot(identical({ b <- c("a","b","c") ; b[is.na(b)] <- c("X","z") ; b }, c("a", "b", "c"))) }
test.micro2597 <- function() { cat('2597\n'); stopifnot(identical({ b <- c("a","b","c") ; b[c(TRUE,TRUE,TRUE)] <- rev(as.character(b)) ; b }, c("c", "b", "a"))) }
test.micro2598 <- function() { cat('2598\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(c("a","b","c"),c(TRUE,FALSE),c("A","X")) ; f(1:3,c(TRUE,FALSE),4) }, c(4, 2, 4))) }
test.micro2599 <- function() { cat('2599\n'); stopifnot(identical({ f <- function(b,i,v) { b[i] <- v ; b } ; f(c("a","b","c"),c(TRUE,FALSE),c("A","X")) ; f(c("A","X"),c(TRUE,FALSE),4) }, c("4", "X"))) }
test.micro2600 <- function() { cat('2600\n'); stopifnot(identical({ b <- c("a","b","c") ; b[c(TRUE,FALSE,TRUE)] <- c(1+2i,3+4i) ; b }, c("1+2i", "b", "3+4i"))) }
test.micro2601 <- function() { cat('2601\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(1:2,"hi",3L) ; f(1:2,c(2),10) ; f(1:2, -1, 10) }, c(1, 10))) }
test.micro2602 <- function() { cat('2602\n'); stopifnot(identical({ x <- c(); f <- function(i, v) { x[i] <- v ; x } ; f(1:2,3:4); f(c(1,2),c(TRUE,FALSE)) }, c(TRUE, FALSE))) }
test.micro2603 <- function() { cat('2603\n'); stopifnot(identical({ x <- c(); f <- function(i, v) { x[i] <- v ; x } ; f(1:2,3:4); f(c("a","b"),c(TRUE,FALSE)) }, structure(c(TRUE, FALSE), .Names = c("a", "b")))) }
test.micro2604 <- function() { cat('2604\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c("b","c","a"), 14:16) ; f(list(1,2,3), 2, NULL) }, list(1, 3))) }
test.micro2605 <- function() { cat('2605\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c("b","c","a"), 14:16) ; f(list(1,2,3), 3L, NULL) }, list(1, 2))) }
test.micro2606 <- function() { cat('2606\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c("b","c","a"), 14:16) ; f(list(1,2,3), 3:2, NULL) }, list(1))) }
test.micro2607 <- function() { cat('2607\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c("b","c","a"), 14:16) ; f(list(1,2,3), c(2,3), NULL) }, list(1))) }
test.micro2608 <- function() { cat('2608\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c("b","c","a"), 14:16) ; f(list(1,2,3), NULL, NULL) }, list(1, 2, 3))) }
test.micro2609 <- function() { cat('2609\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c("b","c","a"), 14:16) ; f(list(1,2,3), c(TRUE,TRUE,FALSE), NULL) }, list(3))) }
test.micro2610 <- function() { cat('2610\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c("b","c","a"), 14:16) ; l <- list(1,2,3) ; dim(l) <- c(1,3) ; z <- f(l, c(TRUE,TRUE,FALSE), NULL) ; z }, list(3))) }
test.micro2612 <- function() { cat('2612\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c("b","c","a"), 14:16) ; f(list(1,2,3), 3:1, 10) }, list(10, 10, 10))) }
test.micro2613 <- function() { cat('2613\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3, 1, TRUE) ; f(c(a=1,b=2,c=3), c("b","c","a"), 14:16) ; f(list(1,2,3), c(3,3,2), 10) }, list(1, 10, 10))) }
test.micro2614 <- function() { cat('2614\n'); stopifnot(identical({ list(1:4) }, list(1:4))) }
test.micro2615 <- function() { cat('2615\n'); stopifnot(identical({ list(1,list(2,list(3,4))) }, list(1, list(2, list(3, 4))))) }
test.micro2616 <- function() { cat('2616\n'); stopifnot(identical({ list(1,b=list(2,3)) }, structure(list(1, b = list(2, 3)), .Names = c("", "b")))) }
test.micro2617 <- function() { cat('2617\n'); stopifnot(identical({ list(1,b=list(c=2,3)) }, structure(list(1, b = structure(list(c = 2, 3), .Names = c("c", ""))), .Names = c("", "b")))) }
test.micro2618 <- function() { cat('2618\n'); stopifnot(identical({ list(list(c=2)) }, list(structure(list(c = 2), .Names = "c")))) }
test.micro2619 <- function() { cat('2619\n'); stopifnot(identical({ l<-list(1,2L,TRUE) ; l[[2]] }, 2L)) }
test.micro2620 <- function() { cat('2620\n'); stopifnot(identical({ l<-list(1,2L,TRUE) ; l[c(FALSE,FALSE,TRUE)] }, list(TRUE))) }
test.micro2621 <- function() { cat('2621\n'); stopifnot(identical({ l<-list(1,2L,TRUE) ; l[FALSE] }, list())) }
test.micro2622 <- function() { cat('2622\n'); stopifnot(identical({ l<-list(1,2L,TRUE) ; l[-2] }, list(1, TRUE))) }
test.micro2623 <- function() { cat('2623\n'); stopifnot(identical({ l<-list(1,2L,TRUE) ; l[NA] }, list(NULL, NULL, NULL))) }
test.micro2624 <- function() { cat('2624\n'); stopifnot(identical({ l<-list(1,2,3) ; l[c(1,2)] }, list(1, 2))) }
test.micro2625 <- function() { cat('2625\n'); stopifnot(identical({ l<-list(1,2,3) ; l[c(2)] }, list(2))) }
test.micro2626 <- function() { cat('2626\n'); stopifnot(identical({ x<-list(1,2L,TRUE,FALSE,5) ; x[2:4] }, list(2L, TRUE, FALSE))) }
test.micro2627 <- function() { cat('2627\n'); stopifnot(identical({ x<-list(1,2L,TRUE,FALSE,5) ; x[4:2] }, list(FALSE, TRUE, 2L))) }
test.micro2628 <- function() { cat('2628\n'); stopifnot(identical({ x<-list(1,2L,TRUE,FALSE,5) ; x[c(-2,-3)] }, list(1, FALSE, 5))) }
test.micro2629 <- function() { cat('2629\n'); stopifnot(identical({ x<-list(1,2L,TRUE,FALSE,5) ; x[c(-2,-3,-4,0,0,0)] }, list(1, 5))) }
test.micro2630 <- function() { cat('2630\n'); stopifnot(identical({ x<-list(1,2L,TRUE,FALSE,5) ; x[c(2,5,4,3,3,3,0)] }, list(2L, 5, FALSE, TRUE, TRUE, TRUE))) }
test.micro2631 <- function() { cat('2631\n'); stopifnot(identical({ x<-list(1,2L,TRUE,FALSE,5) ; x[c(2L,5L,4L,3L,3L,3L,0L)] }, list(2L, 5, FALSE, TRUE, TRUE, TRUE))) }
test.micro2632 <- function() { cat('2632\n'); stopifnot(identical({ m<-list(1,2) ; m[NULL] }, list())) }
test.micro2633 <- function() { cat('2633\n'); stopifnot(identical({ f<-function(x, i) { x[i] } ; f(list(1,2,3),3:1) ; f(list(1L,2L,3L,4L,5L),c(0,0,0,0-2)) }, list(1L, 3L, 4L, 5L))) }
test.micro2634 <- function() { cat('2634\n'); stopifnot(identical({ x<-list(1,2,3,4,5) ; x[c(TRUE,TRUE,TRUE,FALSE,FALSE,FALSE,FALSE,TRUE,NA)] }, list(1, 2, 3, NULL, NULL))) }
test.micro2635 <- function() { cat('2635\n'); stopifnot(identical({ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(1) ; f(1L) ; f(TRUE) }, list(1, 2, 3, 4, 5))) }
test.micro2636 <- function() { cat('2636\n'); stopifnot(identical({ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(1) ; f(TRUE) ; f(1L)  }, list(1))) }
test.micro2637 <- function() { cat('2637\n'); stopifnot(identical({ f<-function(i) { x<-list(1L,2L,3L,4L,5L) ; x[i] } ; f(1) ; f(TRUE) ; f(c(3,2))  }, list(3L, 2L))) }
test.micro2638 <- function() { cat('2638\n'); stopifnot(identical({ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(1)  ; f(3:4) }, list(3, 4))) }
test.micro2639 <- function() { cat('2639\n'); stopifnot(identical({ f<-function(i) { x<-list(1,2,3,4,5) ; x[i] } ; f(c(TRUE,FALSE))  ; f(3:4) }, list(3, 4))) }
test.micro2640 <- function() { cat('2640\n'); stopifnot(identical({ l<-(list(list(1,2),list(3,4))); l[[c(1,2)]] }, 2)) }
test.micro2641 <- function() { cat('2641\n'); stopifnot(identical({ l<-(list(list(1,2),list(3,4))); l[[c(1,-2)]] }, 1)) }
test.micro2642 <- function() { cat('2642\n'); stopifnot(identical({ l<-(list(list(1,2),list(3,4))); l[[c(1,-1)]] }, 2)) }
test.micro2643 <- function() { cat('2643\n'); stopifnot(identical({ l<-(list(list(1,2),list(3,4))); l[[c(1,TRUE)]] }, 1)) }
test.micro2644 <- function() { cat('2644\n'); stopifnot(identical({ l<-(list(list(1,2),c(3,4))); l[[c(2,1)]] }, 3)) }
test.micro2645 <- function() { cat('2645\n'); stopifnot(identical({ l <- list(a=1,b=2,c=list(d=3,e=list(f=4))) ; l[[c(3,2)]] }, structure(list(f = 4), .Names = "f"))) }
test.micro2646 <- function() { cat('2646\n'); stopifnot(identical({ l <- list(a=1,b=2,c=list(d=3,e=list(f=4))) ; l[[c(3,1)]] }, 3)) }
test.micro2647 <- function() { cat('2647\n'); stopifnot(identical({ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c("c","e")]] }, structure(4, .Names = "f"))) }
test.micro2648 <- function() { cat('2648\n'); stopifnot(identical({ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c("c","e", "f")]] }, 4)) }
test.micro2649 <- function() { cat('2649\n'); stopifnot(identical({ l <- list(c=list(d=3,e=c(f=4)), b=2, a=3) ; l[[c("c")]] }, structure(list(d = 3, e = structure(4, .Names = "f")), .Names = c("d", "e")))) }
test.micro2650 <- function() { cat('2650\n'); stopifnot(identical({ f <- function(b, i, v) { b[[i]] <- v ; b } ; f(1:3,2,2) ; f(1:3,"X",2) ; f(list(1,list(2)),c(2,1),4) }, list(1, list(4)))) }
test.micro2651 <- function() { cat('2651\n'); stopifnot(identical({ l<-list(1,2L,TRUE) ; l[[2]]<-100 ; l }, list(1, 100, TRUE))) }
test.micro2652 <- function() { cat('2652\n'); stopifnot(identical({ l<-list(1,2L,TRUE) ; l[[5]]<-100 ; l }, list(1, 2L, TRUE, NULL, 100))) }
test.micro2653 <- function() { cat('2653\n'); stopifnot(identical({ l<-list(1,2L,TRUE) ; l[[3]]<-list(100) ; l }, list(1, 2L, list(100)))) }
test.micro2654 <- function() { cat('2654\n'); stopifnot(identical({ v<-1:3 ; v[2] <- list(100) ; v }, list(1L, 100, 3L))) }
test.micro2655 <- function() { cat('2655\n'); stopifnot(identical({ v<-1:3 ; v[[2]] <- list(100) ; v }, list(1L, list(100), 3L))) }
test.micro2656 <- function() { cat('2656\n'); stopifnot(identical({ l <- list() ; l[[1]] <-2 ; l}, list(2))) }
test.micro2657 <- function() { cat('2657\n'); stopifnot(identical({ l<-list() ; x <- 1:3 ; l[[1]] <- x  ; l }, list(1:3))) }
test.micro2658 <- function() { cat('2658\n'); stopifnot(identical({ l <- list(1,2,3) ; l[2] <- list(100) ; l[2] }, list(100))) }
test.micro2659 <- function() { cat('2659\n'); stopifnot(identical({ l <- list(1,2,3) ; l[[2]] <- list(100) ; l[2] }, list(list(100)))) }
test.micro2660 <- function() { cat('2660\n'); stopifnot(identical({ m<-list(1,2) ; m[TRUE] <- NULL ; m }, list())) }
test.micro2661 <- function() { cat('2661\n'); stopifnot(identical({ m<-list(1,2) ; m[[TRUE]] <- NULL ; m }, list(2))) }
test.micro2662 <- function() { cat('2662\n'); stopifnot(identical({ m<-list(1,2) ; m[[1]] <- NULL ; m }, list(2))) }
test.micro2663 <- function() { cat('2663\n'); stopifnot(identical({ m<-list(1,2) ; m[[-1]] <- NULL ; m }, list(1))) }
test.micro2664 <- function() { cat('2664\n'); stopifnot(identical({ m<-list(1,2) ; m[[-2]] <- NULL ; m }, list(2))) }
test.micro2665 <- function() { cat('2665\n'); stopifnot(identical({ l <- matrix(list(1,2)) ; l[3] <- NULL ; l }, list(1, 2))) }
test.micro2668 <- function() { cat('2668\n'); stopifnot(identical({ l <- matrix(list(1,2)) ; l[4] <- NULL ; l }, list(1, 2, NULL))) }
test.micro2669 <- function() { cat('2669\n'); stopifnot(identical({ l <- list(a=1,b=2,c=3) ; l[1] <- NULL ; l }, structure(list(b = 2, c = 3), .Names = c("b", "c")))) }
test.micro2670 <- function() { cat('2670\n'); stopifnot(identical({ l <- list(a=1,b=2,c=3) ; l[3] <- NULL ; l }, structure(list(a = 1, b = 2), .Names = c("a", "b")))) }
test.micro2671 <- function() { cat('2671\n'); stopifnot(identical({ l <- list(a=1,b=2,c=3) ; l[5] <- NULL ; l}, structure(list(a = 1, b = 2, c = 3, NULL), .Names = c("a", "b", "c", "")))) }
test.micro2672 <- function() { cat('2672\n'); stopifnot(identical({ l <- list(a=1,b=2,c=3) ; l[4] <- NULL ; l}, structure(list(a = 1, b = 2, c = 3), .Names = c("a", "b", "c")))) }
test.micro2673 <- function() { cat('2673\n'); stopifnot(identical({ l <- list(a=1,b=2,c=3) ; l[[5]] <- NULL ; l}, structure(list(a = 1, b = 2, c = 3), .Names = c("a", "b", "c")))) }
test.micro2674 <- function() { cat('2674\n'); stopifnot(identical({ l <- list(a=1,b=2,c=3) ; l[[4]] <- NULL ; l}, structure(list(a = 1, b = 2, c = 3), .Names = c("a", "b", "c")))) }
test.micro2675 <- function() { cat('2675\n'); stopifnot(identical({ l <- list(1,2); l[0] <- NULL; l}, list(1, 2))) }
test.micro2676 <- function() { cat('2676\n'); stopifnot(identical({ l <- list(1,2,3) ; l[c(2,3)] <- c(20,30) ; l }, list(1, 20, 30))) }
test.micro2677 <- function() { cat('2677\n'); stopifnot(identical({ l <- list(1,2,3) ; l[c(2:3)] <- c(20,30) ; l }, list(1, 20, 30))) }
test.micro2678 <- function() { cat('2678\n'); stopifnot(identical({ l <- list(1,2,3) ; l[-1] <- c(20,30) ; l }, list(1, 20, 30))) }
test.micro2679 <- function() { cat('2679\n'); stopifnot(identical({ l <- list(1,2,3) ; l[-1L] <- c(20,30) ; l }, list(1, 20, 30))) }
test.micro2680 <- function() { cat('2680\n'); stopifnot(identical({ l <- list(1,2,3) ; l[c(FALSE,TRUE,TRUE)] <- c(20,30) ; l }, list(1, 20, 30))) }
test.micro2681 <- function() { cat('2681\n'); stopifnot(identical({ l <- list() ; l[c(TRUE,TRUE)] <-2 ; l }, list(2, 2))) }
test.micro2682 <- function() { cat('2682\n'); stopifnot(identical({ x <- 1:3 ; l <- list(1) ; l[[TRUE]] <- x ; l[[1]] }, 1:3)) }
test.micro2683 <- function() { cat('2683\n'); stopifnot(identical({ x<-list(1,2,3,4,5); x[3:4]<-c(300L,400L); x }, list(1, 2, 300L, 400L, 5))) }
test.micro2684 <- function() { cat('2684\n'); stopifnot(identical({ x<-list(1,2,3,4,5); x[4:3]<-c(300L,400L); x }, list(1, 2, 400L, 300L, 5))) }
test.micro2685 <- function() { cat('2685\n'); stopifnot(identical({ x<-list(1,2L,TRUE,TRUE,FALSE); x[c(-2,-3,-3,-100,0)]<-256; x }, list(256, 2L, TRUE, 256, 256))) }
test.micro2686 <- function() { cat('2686\n'); stopifnot(identical({ x<-list(1,2L,list(3,list(4)),list(5)) ; x[c(4,2,3)]<-list(256L,257L,258L); x }, list(1, 257L, 258L, 256L))) }
test.micro2687 <- function() { cat('2687\n'); stopifnot(identical({ x<-list(FALSE,NULL,3L,4L,5.5); x[c(TRUE,FALSE)] <- 1000; x }, list(1000, NULL, 1000, 4L, 1000))) }
test.micro2688 <- function() { cat('2688\n'); stopifnot(identical({ x<-list(11,10,9) ; x[c(TRUE, FALSE, TRUE)] <- c(1000,2000); x }, list(1000, 10, 2000))) }
test.micro2689 <- function() { cat('2689\n'); stopifnot(identical({ l <- list(1,2,3) ; x <- list(100) ; y <- x; l[1:1] <- x ; l[[1]] }, 100)) }
test.micro2690 <- function() { cat('2690\n'); stopifnot(identical({ l <- list(1,2,3) ; x <- list(100) ; y <- x; l[[1:1]] <- x ; l[[1]] }, list(100))) }
test.micro2691 <- function() { cat('2691\n'); stopifnot(identical({ v<-list(1,2,3) ; v[c(2,3,NA,7,0)] <- NULL ; v }, list(1, NULL, NULL, NULL))) }
test.micro2692 <- function() { cat('2692\n'); stopifnot(identical({ v<-list(1,2,3) ; v[c(2,3,4)] <- NULL ; v }, list(1))) }
test.micro2693 <- function() { cat('2693\n'); stopifnot(identical({ v<-list(1,2,3) ; v[c(-1,-2,-6)] <- NULL ; v }, list(1, 2))) }
test.micro2694 <- function() { cat('2694\n'); stopifnot(identical({ v<-list(1,2,3) ; v[c(TRUE,FALSE,TRUE)] <- NULL ; v }, list(2))) }
test.micro2695 <- function() { cat('2695\n'); stopifnot(identical({ v<-list(1,2,3) ; v[c()] <- NULL ; v }, list(1, 2, 3))) }
test.micro2696 <- function() { cat('2696\n'); stopifnot(identical({ v<-list(1,2,3) ; v[integer()] <- NULL ; v }, list(1, 2, 3))) }
test.micro2697 <- function() { cat('2697\n'); stopifnot(identical({ v<-list(1,2,3) ; v[double()] <- NULL ; v }, list(1, 2, 3))) }
test.micro2698 <- function() { cat('2698\n'); stopifnot(identical({ v<-list(1,2,3) ; v[logical()] <- NULL ; v }, list(1, 2, 3))) }
test.micro2699 <- function() { cat('2699\n'); stopifnot(identical({ v<-list(1,2,3) ; v[c(TRUE,FALSE)] <- NULL ; v }, list(2))) }
test.micro2700 <- function() { cat('2700\n'); stopifnot(identical({ v<-list(1,2,3) ; v[c(TRUE,FALSE,FALSE,FALSE,FALSE,TRUE)] <- NULL ; v }, list(2, 3, NULL, NULL))) }
test.micro2701 <- function() { cat('2701\n'); stopifnot(identical({ l<-list(a=1,b=2,c=3,d=4); l[c(-1,-3)] <- NULL ; l}, structure(list(a = 1, c = 3), .Names = c("a", "c")))) }
test.micro2702 <- function() { cat('2702\n'); stopifnot(identical({ l<-list(a=1,b=2,c=3,d=4); l[c(-1,-10)] <- NULL ; l}, structure(list(a = 1), .Names = "a"))) }
test.micro2703 <- function() { cat('2703\n'); stopifnot(identical({ l<-list(a=1,b=2,c=3,d=4); l[c(2,3)] <- NULL ; l}, structure(list(a = 1, d = 4), .Names = c("a", "d")))) }
test.micro2704 <- function() { cat('2704\n'); stopifnot(identical({ l<-list(a=1,b=2,c=3,d=4); l[c(2,3,5)] <- NULL ; l}, structure(list(a = 1, d = 4), .Names = c("a", "d")))) }
test.micro2705 <- function() { cat('2705\n'); stopifnot(identical({ l<-list(a=1,b=2,c=3,d=4); l[c(2,3,6)] <- NULL ; l}, structure(list(a = 1, d = 4, NULL), .Names = c("a", "d", "")))) }
test.micro2706 <- function() { cat('2706\n'); stopifnot(identical({ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,TRUE,FALSE,TRUE)] <- NULL ; l}, structure(list(c = 3), .Names = "c"))) }
test.micro2707 <- function() { cat('2707\n'); stopifnot(identical({ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,FALSE)] <- NULL ; l}, structure(list(b = 2, d = 4), .Names = c("b", "d")))) }
test.micro2708 <- function() { cat('2708\n'); stopifnot(identical({ l<-list(a=1,b=2,c=3,d=4); l[c(TRUE,FALSE,FALSE,TRUE,FALSE,NA,TRUE,TRUE)] <- NULL ; l}, structure(list(b = 2, c = 3, NULL, NULL), .Names = c("b", "c", "", "")))) }
test.micro2709 <- function() { cat('2709\n'); stopifnot(identical({ l <- list(a=1,b=2,c=3) ; l[["b"]] <- NULL ; l }, structure(list(a = 1, c = 3), .Names = c("a", "c")))) }
test.micro2710 <- function() { cat('2710\n'); stopifnot(identical({ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- NULL ; l }, list(1, list(2)))) }
test.micro2711 <- function() { cat('2711\n'); stopifnot(identical({ l <- list(1,list(2,c(3))) ; l[[c(2,2)]] <- 4 ; l }, list(1, list(2, 4)))) }
test.micro2712 <- function() { cat('2712\n'); stopifnot(identical({ l <- list(1,list(2,list(3))) ; l[[1]] <- NULL ; l }, list(list(2, list(3))))) }
test.micro2713 <- function() { cat('2713\n'); stopifnot(identical({ l <- list(1,list(2,list(3))) ; l[[1]] <- 5 ; l }, list(5, list(2, list(3))))) }
test.micro2714 <- function() { cat('2714\n'); stopifnot(identical({ l<-list(a=1,b=2,list(c=3,d=4,list(e=5:6,f=100))) ; l[[c(3,3,1)]] <- NULL ; l }, structure(list(a = 1, b = 2, structure(list(c = 3, d = 4, structure(list(    f = 100), .Names = "f")), .Names = c("c", "d", ""))), .Names = c("a", "b", "")))) }
test.micro2715 <- function() { cat('2715\n'); stopifnot(identical({ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c("c","f","zz")]] <- 100 ; l }, structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(c(1, 2, 3, 100), .Names = c("x", "y", "z",     "zz"))), .Names = c("d", "e", "f"))), .Names = c("a", "b", "c")))) }
test.micro2716 <- function() { cat('2716\n'); stopifnot(identical({ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c("c","f","z")]] <- 100 ; l }, structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(c(1, 2, 100), .Names = c("x", "y", "z"))), .Names = c("d", "e", "f"))), .Names = c("a", "b", "c")))) }
test.micro2717 <- function() { cat('2717\n'); stopifnot(identical({ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c("c","f")]] <- NULL ; l }, structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2), .Names = c("d", "e"))), .Names = c("a", "b", "c")))) }
test.micro2718 <- function() { cat('2718\n'); stopifnot(identical({ l<-list(a=1,b=2,c=3) ; l[c("a","a","a","c")] <- NULL ; l }, structure(list(b = 2), .Names = "b"))) }
test.micro2719 <- function() { cat('2719\n'); stopifnot(identical({ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c("c","f","zz")]] <- 100L ; l }, structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = structure(c(1L, 2L, 3L, 100L), .Names = c("x", "y", "z",     "zz"))), .Names = c("d", "e", "f"))), .Names = c("a", "b", "c")))) }
test.micro2720 <- function() { cat('2720\n'); stopifnot(identical({ l<-list(a=TRUE,b=FALSE,c=list(d=TRUE,e=FALSE,f=c(x=TRUE,y=FALSE,z=TRUE))) ; l[[c("c","f","zz")]] <- TRUE ; l }, structure(list(a = TRUE, b = FALSE, c = structure(list(d = TRUE,     e = FALSE, f = structure(c(TRUE, FALSE, TRUE, TRUE), .Names = c("x",     "y", "z", "zz"))), .Names = c("d", "e", "f"))), .Names = c("a", "b", "c")))) }
test.micro2721 <- function() { cat('2721\n'); stopifnot(identical({ l<-list(a="a",b="b",c=list(d="cd",e="ce",f=c(x="cfx",y="cfy",z="cfz"))) ; l[[c("c","f","zz")]] <- "cfzz" ; l }, structure(list(a = "a", b = "b", c = structure(list(d = "cd",     e = "ce", f = structure(c("cfx", "cfy", "cfz", "cfzz"), .Names = c("x",     "y", "z", "zz"))), .Names = c("d", "e", "f"))), .Names = c("a", "b", "c")))) }
test.micro2722 <- function() { cat('2722\n'); stopifnot(identical({ l<-list(a=1,b=2,c=list(d=1,e=2,f=c(x=1,y=2,z=3))) ; l[[c("c","f","zz")]] <- list(100) ; l }, structure(list(a = 1, b = 2, c = structure(list(d = 1, e = 2,     f = structure(list(x = 1, y = 2, z = 3, zz = list(100)), .Names = c("x",     "y", "z", "zz"))), .Names = c("d", "e", "f"))), .Names = c("a", "b", "c")))) }
test.micro2723 <- function() { cat('2723\n'); stopifnot(identical({ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c("c","f")]] <- 100L ; l }, structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = 100L), .Names = c("d", "e", "f"))), .Names = c("a", "b", "c")))) }
test.micro2724 <- function() { cat('2724\n'); stopifnot(identical({ l<-list(a=1L,b=2L,c=list(d=1L,e=2L,f=c(x=1L,y=2L,z=3L))) ; l[[c("c","f")]] <- list(haha="gaga") ; l }, structure(list(a = 1L, b = 2L, c = structure(list(d = 1L, e = 2L,     f = structure(list(haha = "gaga"), .Names = "haha")), .Names = c("d", "e", "f"))), .Names = c("a", "b", "c")))) }
test.micro2725 <- function() { cat('2725\n'); stopifnot(identical({ x<-c(1,2,3) ; y<-x ; x[2]<-100 ; y }, c(1, 2, 3))) }
test.micro2726 <- function() { cat('2726\n'); stopifnot(identical({ l<-list() ; x <- 1:3 ; l[[1]] <- x; x[2] <- 100L; l[[1]] }, 1:3)) }
test.micro2727 <- function() { cat('2727\n'); stopifnot(identical({ l <- list(1, list(2)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }, 2)) }
test.micro2728 <- function() { cat('2728\n'); stopifnot(identical({ l <- list(1, list(2,3,4)) ;  m <- l ; l[[c(2,1)]] <- 3 ; m[[2]][[1]] }, 2)) }
test.micro2729 <- function() { cat('2729\n'); stopifnot(identical({ x <- c(1L,2L,3L) ; l <- list(1) ; l[[1]] <- x ; x[2] <- 100L ; l[[1]] }, 1:3)) }
test.micro2730 <- function() { cat('2730\n'); stopifnot(identical({ l <- list(100) ; f <- function() { l[[1]] <- 2 } ; f() ; l }, list(100))) }
test.micro2731 <- function() { cat('2731\n'); stopifnot(identical({ l <- list(100,200,300,400,500) ; f <- function() { l[[3]] <- 2 } ; f() ; l }, list(100, 200, 300, 400, 500))) }
test.micro2732 <- function() { cat('2732\n'); stopifnot(identical({ x <-2L ; y <- x; x[1] <- 211L ; y }, 2L)) }
test.micro2733 <- function() { cat('2733\n'); stopifnot(identical({ f <- function() { l[1:2] <- x ; x[1] <- 211L  ; l[1] } ; l <- 1:3 ; x <- 10L ; f() }, 10L)) }
test.micro2734 <- function() { cat('2734\n'); stopifnot(identical({ x <- list(1,list(2,3),4) ; x[[c(2,3)]] <- 3 ; x }, list(1, list(2, 3, 3), 4))) }
test.micro2735 <- function() { cat('2735\n'); stopifnot(identical({ x <- list(1,list(2,3),4) ; z <- x[[2]] ; x[[c(2,3)]] <- 3 ; z }, list(2, 3))) }
test.micro2736 <- function() { cat('2736\n'); stopifnot(identical({ x <- list(1,list(2,3),4) ; z <- list(x,x) ; u <- list(z,z) ; u[[c(2,2,3)]] <- 6 ; unlist(u) }, c(1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 6))) }
test.micro2737 <- function() { cat('2737\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, 3) }, list(1, 3, 3))) }
test.micro2738 <- function() { cat('2738\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(list(1,2,3), 2L, NULL) }, list(1, 3))) }
test.micro2739 <- function() { cat('2739\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,list(3)), c(3,1), 4) ; f(c(1,2,3), "hello", 2) }, structure(c(1, 2, 3, 2), .Names = c("", "", "", "hello")))) }
test.micro2740 <- function() { cat('2740\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(x=3)),c("b","x"),10) }, structure(list(1, 2, b = structure(list(x = 10), .Names = "x")), .Names = c("", "", "b")))) }
test.micro2741 <- function() { cat('2741\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=c(x=3)),c("b","x"),10) }, structure(list(1, 2, b = structure(10, .Names = "x")), .Names = c("", "", "b")))) }
test.micro2742 <- function() { cat('2742\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(c(1,2,b=c(x=3)),c("b"),10) }, structure(c(1, 2, 3, 10), .Names = c("", "", "b.x", "b")))) }
test.micro2743 <- function() { cat('2743\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ; f(list(1,2,b=list(a=list(x=1,y=2),3),4),c("b","a","x"),10) }, structure(list(1, 2, b = structure(list(a = structure(list(x = 10,     y = 2), .Names = c("x", "y")), 3), .Names = c("a", "")),     4), .Names = c("", "", "b", "")))) }
test.micro2744 <- function() { cat('2744\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c("b","a"),10) ; f(list(a=1,b=2),"b",NULL) }, structure(list(a = 1), .Names = "a"))) }
test.micro2745 <- function() { cat('2745\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c("b","a"),10) ; f(list(a=1,b=list(2)),"b",double()) }, structure(list(a = 1, b = numeric(0)), .Names = c("a", "b")))) }
test.micro2746 <- function() { cat('2746\n'); stopifnot(identical({ f <- function(b,i,v) { b[[i]] <- v ; b } ;  f(list(1,2,b=list(a=1)),c("b","a"),10) ; f(list(a=1,b=c(a=2)),c(TRUE,TRUE),3) }, structure(list(a = 3, b = structure(2, .Names = "a")), .Names = c("a", "b")))) }
test.micro2747 <- function() { cat('2747\n'); stopifnot(identical({ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c(2,2,3,2)]] <- 10 ; l }, structure(list(a = 1, b = 2, cd = structure(list(c = 3, d = 4), .Names = c("c", "d"))), .Names = c("a", "b", "cd")))) }
test.micro2748 <- function() { cat('2748\n'); stopifnot(identical({ l <- list(a=1,b=2,cd=list(c=3,d=4)) ; x <- list(l,xy=list(x=l,y=l)) ; x[[c("xy","y","cd","d")]] <- 10 ; l }, structure(list(a = 1, b = 2, cd = structure(list(c = 3, d = 4), .Names = c("c", "d"))), .Names = c("a", "b", "cd")))) }
test.micro2749 <- function() { cat('2749\n'); stopifnot(identical({ a <- 'hello'; a[[5]] <- 'done'; a[[3]] <- 'muhuhu'; a; }, c("hello", NA, "muhuhu", NA, "done"))) }
test.micro2750 <- function() { cat('2750\n'); stopifnot(identical({ a <- 'hello'; a[[5]] <- 'done'; b <- a; b[[3]] <- 'muhuhu'; b; }, c("hello", NA, "muhuhu", NA, "done"))) }
test.micro2751 <- function() { cat('2751\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(1:3,"a",4) }, structure(c(1, 2, 3, 4), .Names = c("", "", "", "a")))) }
test.micro2752 <- function() { cat('2752\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(NULL,"a",4) }, structure(4, .Names = "a"))) }
test.micro2753 <- function() { cat('2753\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(NULL,c("a","X"),4:5) }, structure(4:5, .Names = c("a", "X")))) }
test.micro2754 <- function() { cat('2754\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(double(),c("a","X"),4:5) }, structure(c(4, 5), .Names = c("a", "X")))) }
test.micro2755 <- function() { cat('2755\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(double(),c("a","X"),list(3,TRUE)) }, structure(list(a = 3, X = TRUE), .Names = c("a", "X")))) }
test.micro2756 <- function() { cat('2756\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.raw(11:13),c("a","X"),list(3,TRUE)) }, structure(list(as.raw(0x0b), as.raw(0x0c), as.raw(0x0d), a = 3,     X = TRUE), .Names = c("", "", "", "a", "X")))) }
test.micro2757 <- function() { cat('2757\n'); stopifnot(identical({ b <- c(11,12) ; b[""] <- 100 ; b }, structure(c(11, 12, 100), .Names = c("", "", "")))) }
test.micro2758 <- function() { cat('2758\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(1,a=2),c("a","X","a"),list(3,TRUE,FALSE)) }, structure(list(1, a = FALSE, X = TRUE), .Names = c("", "a", "X")))) }
test.micro2759 <- function() { cat('2759\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c("a","X","a"),list(3,TRUE,FALSE)) }, structure(list(X = TRUE, a = FALSE), .Names = c("X", "a")))) }
test.micro2760 <- function() { cat('2760\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),as.character(NA),as.complex(23)) }, structure(c(13+0i, 14+0i, 23+0i), .Names = c("", "", NA)))) }
test.micro2761 <- function() { cat('2761\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),character(),as.complex(23)) }, c(13+0i, 14+0i))) }
test.micro2762 <- function() { cat('2762\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),c("","",""),as.complex(23)) }, structure(c(13+0i, 14+0i, 23+0i, 23+0i, 23+0i), .Names = c("", "", "", "", "")))) }
test.micro2763 <- function() { cat('2763\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.complex(c(13,14)),c("","",NA),as.complex(23)) }, structure(c(13+0i, 14+0i, 23+0i, 23+0i, 23+0i), .Names = c("", "", "", "", NA)))) }
test.micro2764 <- function() { cat('2764\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(as.raw(c(13,14)),c("a","X","a"),as.raw(23)) }, structure(as.raw(c(0x0d, 0x0e, 0x17, 0x17)), .Names = c("", "", "a", "X")))) }
test.micro2765 <- function() { cat('2765\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c("a","X","a","b"),list(3,TRUE,FALSE)) }, structure(list(X = TRUE, a = FALSE, b = 3), .Names = c("X", "a", "b")))) }
test.micro2766 <- function() { cat('2766\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c("X","b",NA),list(3,TRUE,FALSE)) }, structure(list(X = 3, a = 2, b = TRUE, "NA" = FALSE), .Names = c("X", "a", "b", NA)))) }
test.micro2767 <- function() { cat('2767\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c("X","b",NA),as.complex(10)) }, structure(c(10+0i, 2+0i, 10+0i, 10+0i), .Names = c("X", "a", "b", NA)))) }
test.micro2768 <- function() { cat('2768\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1:3,3:1,4:6) ; f(c(X=1,a=2),c("X","b",NA),1:3) }, structure(c(1, 2, 2, 3), .Names = c("X", "a", "b", NA)))) }
test.micro2769 <- function() { cat('2769\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(c(X=1,a=2),c("X","b",NA),c(TRUE,NA)) }, structure(c(1, 2, NA, 1), .Names = c("X", "a", "b", NA)))) }
test.micro2770 <- function() { cat('2770\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(c(X=1L,a=2L),c("X","b",NA),c(TRUE,NA,FALSE)) }, structure(c(1L, 2L, NA, 0L), .Names = c("X", "a", "b", NA)))) }
test.micro2771 <- function() { cat('2771\n'); stopifnot(identical({ f <- function(b, i, v) { b[i] <- v ; b } ; f(1+2i,3:1,4:6) ; f(list(X=1L,a=2L),c("X","b",NA),NULL) }, structure(list(a = 2L), .Names = "a"))) }
test.micro2772 <- function() { cat('2772\n'); stopifnot(identical({ b <- c(a=1+2i,b=3+4i) ; dim(b) <- c(2,1) ; b[c("a","b")] <- 3+1i ; b }, structure(c(1+2i, 3+4i, 3+1i, 3+1i), .Names = c("", "", "a", "b")))) }
test.micro2776 <- function() { cat('2776\n'); stopifnot(identical({ b <- list(1+2i,3+4i) ; dim(b) <- c(2,1) ; b[c("hello","hi")] <- NULL ; b }, list(1+2i, 3+4i))) }
test.micro2777 <- function() { cat('2777\n'); stopifnot(identical({ a <- TRUE; a[[2]] <- FALSE; a; }, c(TRUE, FALSE))) }
test.micro2778 <- function() { cat('2778\n'); stopifnot(identical({ x <- 1:3 ; f <- function() { x[2] <<- 100 } ; f() ; x }, c(1, 100, 3))) }
test.micro2779 <- function() { cat('2779\n'); stopifnot(identical({ x <- 1:3 ; f <- function() { x[2] <- 10 ; x[2] <<- 100 ; x[2] <- 1000 } ; f() ; x }, c(1, 100, 3))) }
test.micro2780 <- function() { cat('2780\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; m[1,2] }, 3L)) }
test.micro2781 <- function() { cat('2781\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; m[1,] }, c(1L, 3L, 5L))) }
test.micro2783 <- function() { cat('2783\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; m[,1] }, 1:2)) }
test.micro2793 <- function() { cat('2793\n'); stopifnot(identical({ m <- matrix(1:16, nrow=8) ; m[c(TRUE,FALSE),c(FALSE,TRUE), drop=TRUE]}, c(9L, 11L, 13L, 15L))) }
test.micro2794 <- function() { cat('2794\n'); stopifnot(identical({ m <- matrix(1:16, nrow=8) ; m[c(TRUE,FALSE,FALSE),c(FALSE,TRUE), drop=TRUE]}, c(9L, 12L, 15L))) }
test.micro2795 <- function() { cat('2795\n'); stopifnot(identical({ m <- matrix(1:6, nrow=3) ; f <- function(i,j) { m[i,j] } ; f(1,c(1,2)) ; f(1,c(-1,0,-1,-10)) }, 4L)) }
test.micro2796 <- function() { cat('2796\n'); stopifnot(identical({ m <- matrix(1:6, nrow=3) ; f <- function(i,j) { m[i,j] } ; f(1,c(1,2)) ; f(c(TRUE),c(FALSE,TRUE)) }, 4:6)) }
test.micro2797 <- function() { cat('2797\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; x<-2 ; m[[1,x]] }, 3L)) }
test.micro2798 <- function() { cat('2798\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; m[[1,2]] }, 3L)) }
test.micro2799 <- function() { cat('2799\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; f <- function(i,j) { m[i,j] } ;  f(1,1); f(1,1:3) }, c(1L, 3L, 5L))) }
test.micro2800 <- function() { cat('2800\n'); stopifnot(identical({ m <- matrix(1:4, nrow=2) ; m[[2,1,drop=FALSE]] }, 2L)) }
test.micro2801 <- function() { cat('2801\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; m[1:2,0:1] }, 1:2)) }
test.micro2802 <- function() { cat('2802\n'); stopifnot(identical({ m <- matrix(1:6, nrow=2) ; m[1:2,0:1] ; m[1:2,1:1] }, 1:2)) }
test.micro2803 <- function() { cat('2803\n'); stopifnot(identical({ a <- list(); a$a = 6; a; }, structure(list(a = 6), .Names = "a"))) }
test.micro2804 <- function() { cat('2804\n'); stopifnot(identical({ a <- list(); a[['b']] = 6; a; }, structure(list(b = 6), .Names = "b"))) }
test.micro2805 <- function() { cat('2805\n'); stopifnot(identical({ a <- list(a = 1, b = 2); a$a; }, 1)) }
test.micro2806 <- function() { cat('2806\n'); stopifnot(identical({ a <- list(a = 1, b = 2); a$b; }, 2)) }
test.micro2808 <- function() { cat('2808\n'); stopifnot(identical({ a <- list(a = 1, b = 2); a$a <- 67; a; }, structure(list(a = 67, b = 2), .Names = c("a", "b")))) }
test.micro2809 <- function() { cat('2809\n'); stopifnot(identical({ a <- list(a = 1, b = 2); a$b <- 67; a; }, structure(list(a = 1, b = 67), .Names = c("a", "b")))) }
test.micro2810 <- function() { cat('2810\n'); stopifnot(identical({ a <- list(a = 1, b = 2); a$c <- 67; a; }, structure(list(a = 1, b = 2, c = 67), .Names = c("a", "b", "c")))) }
test.micro2811 <- function() { cat('2811\n'); stopifnot(identical({ v <- list(xb=1, b=2, aa=3, aa=4) ; v$aa }, 3)) }
test.micro2813 <- function() { cat('2813\n'); stopifnot(identical({ x <- list(a=1, b=2) ; f <- function(x) { x$b } ; f(x) ; f(x) }, 2)) }
test.micro2814 <- function() { cat('2814\n'); stopifnot(identical({ x <- list(a=1, b=2) ; f <- function(x) { x$b } ; f(x) ; x <- list(c=2,b=10) ; f(x) }, 10)) }
test.micro2815 <- function() { cat('2815\n'); stopifnot(identical({ v <- list(xb=1, b=2, aa=3, aa=4) ; v$x }, 1)) }
test.micro2817 <- function() { cat('2817\n'); stopifnot(identical({ f <- function(v) { v$x } ; f(list(xa=1, xb=2, hello=3)) ; f(list(y=2,x=3)) }, 3)) }
test.micro2818 <- function() { cat('2818\n'); stopifnot(identical({ f <- function(v) { v$x } ; f(list(xa=1, xb=2, hello=3)) ; l <- list(y=2,x=3) ; f(l) ; l[[2]] <- 4 ; f(l) }, 4)) }
test.micro2819 <- function() { cat('2819\n'); stopifnot(identical({ a <- c(1,2); a$a = 3; a; }, structure(list(1, 2, a = 3), .Names = c("", "", "a")))) }
test.micro2820 <- function() { cat('2820\n'); stopifnot(identical({ l <- list(a=1,b=2,c=3) ; z <- l ; l$b <- 10 ; z }, structure(list(a = 1, b = 2, c = 3), .Names = c("a", "b", "c")))) }
test.micro2821 <- function() { cat('2821\n'); stopifnot(identical({ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1),11) }, structure(list(a = 1, z = 11), .Names = c("a", "z")))) }
test.micro2822 <- function() { cat('2822\n'); stopifnot(identical({ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2,z=3),10) }, structure(list(a = 1, b = 2, z = 10), .Names = c("a", "b", "z")))) }
test.micro2823 <- function() { cat('2823\n'); stopifnot(identical({ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(c(a=1,b=2,z=3),10) }, structure(list(a = 1, b = 2, z = 10), .Names = c("a", "b", "z")))) }
test.micro2824 <- function() { cat('2824\n'); stopifnot(identical({ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(list(a=1,z=2),10) }, structure(list(a = 1, z = 10), .Names = c("a", "z")))) }
test.micro2825 <- function() { cat('2825\n'); stopifnot(identical({ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(c(a=1,z=2),10) }, structure(list(a = 1, z = 10), .Names = c("a", "z")))) }
test.micro2826 <- function() { cat('2826\n'); stopifnot(identical({ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(l <- list(a=1,z=2),10) }, structure(list(a = 1, z = 10), .Names = c("a", "z")))) }
test.micro2827 <- function() { cat('2827\n'); stopifnot(identical({ f <- function(b,v) { b$z <- v ; b } ; f(l<-list(a=1,b=2,z=3),10) ; f(list(a=1,b=2),10) ; f(l <- list(a=1,z=2),10) ; l }, structure(list(a = 1, z = 2), .Names = c("a", "z")))) }
test.micro2828 <- function() { cat('2828\n'); stopifnot(identical({ x <- list(a=1,b=2,c=3) ; x$z <- NULL ; x }, structure(list(a = 1, b = 2, c = 3), .Names = c("a", "b", "c")))) }
test.micro2829 <- function() { cat('2829\n'); stopifnot(identical({ x <- list(a=1,b=2,c=3) ; x$a <- NULL ; x }, structure(list(b = 2, c = 3), .Names = c("b", "c")))) }
test.micro2831 <- function() { cat('2831\n'); stopifnot(identical({ f <- function(x, v) { x$a <- v ; x } ; x <- list(a=1,b=2,c=3) ; z <- x ; f(x, 10) ; f(x,NULL) }, structure(list(b = 2, c = 3), .Names = c("b", "c")))) }
