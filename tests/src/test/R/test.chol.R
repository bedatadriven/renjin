#
# Renjin : JVM-based interpreter for the R language for the statistical analysis
# Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, a copy is available at
# https://www.gnu.org/licenses/gpl-2.0.txt
#


library(hamcrest)

test.chol <- function() {

    assertThat(chol(matrix(c(8,1,1,4),2,2)), identicalTo(
        structure(c(2.82842712474619, 0, 0.353553390593274, 1.96850196850295 ),
            .Dim = c(2L, 2L)), tol = 1e-6))

}

test.chol.00a7adfad0bf08ae955117a64220fd83 <- function() {


     expected <- structure(c(0x1.2a5cca6939b4dp+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x1.2cee731d0d6f4p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x1.210ca9da5e189p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x1.8d81671a3d322p-3, 0x0p+0, 0x1.2608e1c3d8665p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x1.49cfde760bd81p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x1.1801c1e0a6ebap+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x1.3aa5296179d85p-13, 0x0p+0,
    0x1.a425f7107e5bfp-3, 0x0p+0, 0x0p+0, 0x1.1175f9417330fp+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x1.02cc6bce03ed8p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x1.01e97a866cfd8p+0), .Dim = c(9L,
    9L))


    assertThat(chol(x=structure(c(1.3583398384708, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.38183187840811,
    0, 0.228160663474221, 0, 0, 0.000176367579302974, 0, 0, 0, 0,
    1.27486563459899, 0, 0, 0, 0, 0, 0, 0, 0.228160663474221, 0,
    1.35689266246681, 0, 0, 0.235659494904759, 0, 0, 0, 0, 0, 0,
    1.65978923927347, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.19634772057518,
    0, 0, 0, 0, 0.000176367579302974, 0, 0.235659494904759, 0, 0,
    1.18315164681959, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1.02198293973711,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 1.01499348556733), .Dim = c(9L, 9L
    )))
    ,  identicalTo( expected, tol = 1e-6 ) )
}

test.chol.pivot <- function() {

     expected <- structure(c(0x1.024c35feb6d28p-3, 0x0p+0, -0x1.4a498d5b66f1ap-4,
    0x1.3f38dc3f4263bp-5), .Dim = c(2L, 2L), pivot = 1:2, rank = 2L)


    assertThat(chol(pivot=TRUE,x=structure(c(0.0159067395343744, -0.010170036908562, -0.010170036908562,
    0.00802072276133621), .Dim = c(2L, 2L)))
    ,  identicalTo( expected, tol = 1e-6 ) )

}

test.chol.dimnames <- function() {

     expected <- structure(c(0x1.1d25e8aa33dcep+3, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, -0x1.26060f5fd3682p+0, 0x1.051ab6bb8a6e1p+3,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, -0x1.47fdc69e85dc7p-1,
    -0x1.6a3d35829d1b3p+0, 0x1.da667d1d1ca12p+2, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, -0x1.0ecfc05ecc179p+0, 0x1.a68e4be7380bcp-4,
    -0x1.2f85985960544p-1, 0x1.74636135ed991p+2, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, -0x1.2e69298a9b562p-4, -0x1.0f6e6b07ee1b9p-2,
    -0x1.845fe369b1bf2p-6, -0x1.8c99ae66f79aep-4, 0x1.b945c08bf11d8p+1,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x1.c81dd24ada8c3p-6, 0x1.4fd12735aaed8p-3,
    -0x1.7f8e7912db69ap-2, -0x1.86385b42b434ap+0, 0x1.72795cb6d83fp-4,
    0x1.8ced0cccfc1bbp+1, 0x0p+0, 0x0p+0, 0x0p+0, -0x1.6a8fb64d9be6dp-1,
    -0x1.c53f65479c3f8p-3, -0x1.61cb9133d12d1p-3, -0x1.59bef0f16fd22p-4,
    -0x1.f3e26d055f511p-3, 0x1.470036f5d2db2p-4, 0x1.41d0e1ca06adbp+1,
    0x0p+0, 0x0p+0, 0x1.e8d5943f2da8ep-5, -0x1.9f5709360faf7p-3,
    -0x1.1a524ccbe4bfbp-3, 0x1.3d42986641bc1p-4, -0x1.0321bcb1260dep-4,
    0x1.b9a82a20b24ddp-5, -0x1.8ea7da5c66397p-2, 0x1.cb7ab387b82f4p+0,
    0x0p+0, -0x1.a4700da5a02c9p-3, -0x1.d88502f2cd751p-3, -0x1.c6f174bc68e7ep-3,
    -0x1.3b71cd10c2bcfp-3, -0x1.9613d7c9a53e8p-4, -0x1.7240ed321bf36p-4,
    -0x1.649d2834b899fp-5, -0x1.a8c275469f847p-5, 0x1.3a019a02bc618p-4
    ), .Dim = c(9L, 9L), .Dimnames = list(c("", "npreg", "glu", "bp",
    "skin", "serum", "bmi", "ped", "age"), c("glu", "bmi", "bp",
    "age", "ped", "npreg", "serum", "skin", "")), pivot = c(3L, 7L,
    4L, 9L, 8L, 2L, 6L, 5L, 1L), rank = 9L)


    assertThat(base:::chol.default(pivot=TRUE,x=structure(c(0.19690810828078, -0.0148373497482383, -1.82933053384494,
    -1.18863258472411, -0.0214840700617395, 0.155345503962676, -1.64679851266713,
    -0.245285555780193, -0.571152560759099, -0.0148373497482381,
    12.1157290133236, 0.248071178532099, -3.02633478923477, 0.063437635054758,
    0.362858637587808, 1.30596660031224, 0.422764606345043, -8.65970924838853,
    -1.82933053384496, 0.248071178532076, 79.4037386186032, -5.70837971962777,
    0.531731265557509, -6.31003910027311, -10.2344099113302, -0.65789685522609,
    -9.42643895869013, -1.1886325847241, -3.02633478923479, -5.70837971962794,
    57.357830208406, -0.773092144040431, -0.51373339473551, -10.8099217312879,
    0.246659445784651, -3.86255605229768, -0.0214840700617405, 0.0634376350547546,
    0.531731265557522, -0.773092144040433, 3.44961972670415, -0.939154333852223,
    -1.72330632994944, -0.172981828411283, 0.448358008175248, 0.155345503962677,
    0.362858637587807, -6.31003910027312, -0.51373339473552, -0.939154333852221,
    6.97447945378614, -0.992494462206532, -0.718253039762346, 0.337528178419554,
    -1.64679851266712, 1.3059666003123, -10.2344099113305, -10.8099217312879,
    -1.72330632994945, -0.9924944622065, 67.8967402258476, -2.07804186764581,
    2.05674110574227, -0.245285555780192, 0.422764606345038, -0.657896855226096,
    0.246659445784633, -0.172981828411282, -0.718253039762346, -2.07804186764582,
    11.970500720561, -0.498580446095979, -0.571152560759098, -8.65970924838853,
    -9.42643895869021, -3.86255605229773, 0.448358008175236, 0.337528178419557,
    2.05674110574239, -0.498580446095987, 35.3368424460565), .Dim = c(9L,
    9L), .Dimnames = list(c("", "npreg", "glu", "bp", "skin", "serum",
    "bmi", "ped", "age"), c("", "npreg", "glu", "bp", "skin", "serum",
    "bmi", "ped", "age"))))
    ,  identicalTo( expected, tol = 1e-6 ) )

}

test.chol.extra.attributes <- function() {
     expected <- structure(0x1.6a09e667f3bcdp-1, .Dim = c(1L, 1L), .Dimnames = list(
        "(Intercept)", "(Intercept)"), stddev = structure(0x1.6a09e667f3bd1p-1, .Names = "(Intercept)"), correlation = structure(0x1p+0, .Dim = c(1L,
    1L), .Dimnames = list("(Intercept)", "(Intercept)")), pivot = 1L, rank = 1L)


    assertThat(base:::chol(pivot=TRUE,x=structure(0.5, .Dim = c(1L, 1L), .Dimnames = list("(Intercept)",
        "(Intercept)"), stddev = structure(0.707106781186548, .Names = "(Intercept)"), correlation = structure(1, .Dim = c(1L,
    1L), .Dimnames = list("(Intercept)", "(Intercept)"))))
    ,  identicalTo( expected, tol = 1e-6 ) )

}

ignore.test.chol.5 <- function() {

     expected <- structure(c(0x1.2a14f1be14f3fp-1, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, -0x1.17184722cb4c1p-3, 0x1.502ad0ff2bdf2p-2,
    0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, 0x0p+0, -0x1.5a23b8861ca64p-2,
    -0x1.cb006ea29c1e6p-6, 0x1.cc3a010feba9ep-2, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, 0x0p+0, -0x1.1eb55a6cba796p-2, -0x1.475e5ccfe1c73p-6,
    -0x1.3fbe2b696a121p-3, 0x1.1a960d14126bap-2, 0x0p+0, 0x0p+0,
    0x0p+0, 0x0p+0, -0x1.ffd610ce69369p-3, -0x1.8bbe04489e11cp-5,
    -0x1.eedae0b291be1p-4, 0x1.18a73dd539a42p-4, 0x1.3356e735efb62p-2,
    0x0p+0, 0x0p+0, 0x0p+0, -0x1.b2165330845bdp-4, -0x1.23a3a4f178b33p-2,
    -0x1.e1930d76f4076p-4, -0x1.41d90ebcc523bp-4, -0x1.5f3dd8e8827aep-3,
    0x1.83089f97d72b6p-2, 0x0p+0, 0x0p+0, -0x1.7c096fb01cf45p-3,
    -0x1.1e2ef4624c9p-3, -0x1.2457fdbb9601dp-3, -0x1.9efec64ea016bp-4,
    -0x1.69927d67d19cp-4, -0x1.8792ba1509543p-50, 0x1.f0b5e1ca2d58p-3,
    0x0p+0, -0x1.2c5cc07003a4bp-4, -0x1.970738ea57cebp-3, -0x1.ae12784d3419ap-4,
    -0x1.a7bca5f8a5fecp-3, -0x1.5f3dd8e88283fp-5, -0x1.9cbd40ae63c77p-50,
    -0x1.bd4c31be8c292p-49, 0x1.83089f97d72acp-2), .Dim = c(8L, 8L
    ), .Dimnames = list(c("", "x4", "y", "x2.1", "x2.2", "x3.1",
    "x3.2", "x3.3"), c("", "x4", "y", "x2.1", "x2.2", "x3.1", "x3.2",
    "x3.3")))

    actual <- base:::chol.default(x=structure(c(
           0x1.5b14c48532c2bp-2, -0x1.44f918412532dp-4, -0x1.9309e6732b61ap-3,  -0x1.4dd6902b3dc0bp-3,
          -0x1.29fc87cf13988p-3, -0x1.f971807e4e471p-5,  -0x1.ba82139ecbb74p-4, -0x1.5dbc8ae3c393cp-5,
          -0x1.44f918412532dp-4,  0x1.02c0f38ef0ef2p-3, 0x1.2e0616e23b66ap-5, 0x1.02d682e65ba6bp-5,
           0x1.2a2d0bac09187p-6, -0x1.43cf9f818a576p-4, -0x1.5148a8fb5ce45p-6,  -0x1.c4a029b5d336bp-5,
          -0x1.9309e6732b61ap-3, 0x1.2e0616e23b66ap-5,  0x1.44a703d23b232p-2, 0x1.9a2c00cd3e345p-6,
           0x1.025441a966edbp-5,  -0x1.2831c2e093d77p-7, 0x1.45b59d6baedc3p-9, -0x1.13d2087e99c41p-6,
          -0x1.4dd6902b3dc0bp-3, 0x1.02d682e65ba6bp-5, 0x1.9a2c00cd3e345p-6,  0x1.6f41126c55031p-3,
           0x1.bd477bcae142ap-4, 0x1.066e89ae55c69p-5,  0x1.9204b0b1f3cebp-5, -0x1.0973abd944a7fp-6,
          -0x1.29fc87cf13988p-3,  0x1.2a2d0bac09187p-6, 0x1.025441a966edbp-5, 0x1.bd477bcae142ap-4,
           0x1.64b154fc1276p-3, -0x1.3bdcb6f0682ep-9, 0x1.2e94ad44db2bbp-5,  0x1.bc958c7b4bbbap-7,
          -0x1.f971807e4e471p-5, -0x1.43cf9f818a576p-4,  -0x1.2831c2e093d77p-7, 0x1.066e89ae55c69p-5,
          -0x1.3bdcb6f0682ep-9,  0x1.23713a5631c2cp-2, 0x1.96ed3082f69f9p-4, 0x1.9af42aa08ea2cp-4,
          -0x1.ba82139ecbb74p-4, -0x1.5148a8fb5ce45p-6, 0x1.45b59d6baedc3p-9,  0x1.9204b0b1f3cebp-5,
           0x1.2e94ad44db2bbp-5, 0x1.96ed3082f69f9p-4,  0x1.35b1bf7d97d9ap-3, 0x1.4c3fb5af18d39p-4,
          -0x1.5dbc8ae3c393cp-5,  -0x1.c4a029b5d336bp-5, -0x1.13d2087e99c41p-6, -0x1.0973abd944a7fp-6,
           0x1.bc958c7b4bbbap-7, 0x1.9af42aa08ea2cp-4, 0x1.4c3fb5af18d39p-4,  0x1.f27dd91dab9e7p-3),
      .Dim = c(8L, 8L),
      .Dimnames = list(
          c("", "x4", "y", "x2.1", "x2.2", "x3.1", "x3.2", "x3.3"),
          c("", "x4", "y", "x2.1", "x2.2", "x3.1", "x3.2", "x3.3"))))


    assertThat(actual,  identicalTo( expected, tol = 1e-6 ) )


}