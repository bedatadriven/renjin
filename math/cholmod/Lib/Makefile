#===============================================================================
# CHOLOMD/Lib/Makefile: for compiling the CHOLMOD library
#===============================================================================
include ${MkInclude}

default: lib

PKG_CPPFLAGS = -I../../AMD/Include -I../../AMD/Source \
        -I../../COLAMD/Include \
	-I../Include -I../../SuiteSparse_config -DNPARTITION -DNPRINT
#	 -I../../CCOLAMD \
#        -I../../CAMD/Include -I../../CAMD/Source \
#	-I../../Metis \

#-------------------------------------------------------------------------------
# ../Include/ directory contains all include files:
#-------------------------------------------------------------------------------

INC =   ../Include/cholmod.h \
	../Include/cholmod_blas.h \
	../Include/cholmod_check.h \
	../Include/cholmod_cholesky.h \
	../Include/cholmod_complexity.h \
	../Include/cholmod_config.h \
	../Include/cholmod_core.h \
	../Include/cholmod_internal.h \
	../Include/cholmod_matrixops.h \
	../Include/cholmod_modify.h \
	../Include/cholmod_partition.h \
	../Include/cholmod_supernodal.h \
	../Include/cholmod_template.h

#-------------------------------------------------------------------------------
# The 7 CHOLMOD library modules (int, double)
#-------------------------------------------------------------------------------

CORE = cholmod_aat.o cholmod_add.o cholmod_band.o \
	cholmod_change_factor.o cholmod_common.o cholmod_complex.o \
	cholmod_copy.o cholmod_dense.o cholmod_error.o cholmod_factor.o \
	cholmod_memory.o cholmod_sparse.o \
	cholmod_transpose.o cholmod_triplet.o \
        cholmod_version.o

CHECK = cholmod_check.o cholmod_read.o cholmod_write.o

CHOLESKY = cholmod_amd.o cholmod_analyze.o cholmod_colamd.o \
	cholmod_etree.o cholmod_factorize.o cholmod_postorder.o \
	cholmod_rcond.o cholmod_resymbol.o cholmod_rowcolcounts.o \
	cholmod_rowfac.o cholmod_solve.o cholmod_spsolve.o

MATRIXOPS = cholmod_drop.o cholmod_horzcat.o cholmod_norm.o \
	cholmod_scale.o cholmod_sdmult.o cholmod_ssmult.o \
	cholmod_submatrix.o cholmod_vertcat.o cholmod_symmetry.o

PARTITION = cholmod_ccolamd.o cholmod_csymamd.o \
	cholmod_metis.o cholmod_nesdis.o cholmod_camd.o

MODIFY = cholmod_rowadd.o cholmod_rowdel.o cholmod_updown.o

SUPERNODAL = cholmod_super_numeric.o cholmod_super_solve.o \
	cholmod_super_symbolic.o

DI = $(CORE) $(CHECK) $(CHOLESKY) $(MATRIXOPS) $(MODIFY) $(SUPERNODAL) # $(PARTITION)

#-------------------------------------------------------------------------------
# CHOLMOD library modules (long, double)
#-------------------------------------------------------------------------------

LCORE = cholmod_l_aat.o cholmod_l_add.o cholmod_l_band.o \
	cholmod_l_change_factor.o cholmod_l_common.o cholmod_l_complex.o \
	cholmod_l_copy.o cholmod_l_dense.o cholmod_l_error.o \
	cholmod_l_factor.o cholmod_l_memory.o \
	cholmod_l_sparse.o cholmod_l_transpose.o cholmod_l_triplet.o \
        cholmod_l_version.o

LCHECK = cholmod_l_check.o cholmod_l_read.o cholmod_l_write.o

LCHOLESKY = cholmod_l_amd.o cholmod_l_analyze.o cholmod_l_colamd.o \
	cholmod_l_etree.o cholmod_l_factorize.o cholmod_l_postorder.o \
	cholmod_l_rcond.o cholmod_l_resymbol.o cholmod_l_rowcolcounts.o \
	cholmod_l_rowfac.o cholmod_l_solve.o cholmod_l_spsolve.o

LMATRIXOPS = cholmod_l_drop.o cholmod_l_horzcat.o cholmod_l_norm.o \
	cholmod_l_scale.o cholmod_l_sdmult.o cholmod_l_ssmult.o \
	cholmod_l_submatrix.o cholmod_l_vertcat.o cholmod_l_symmetry.o

LPARTITION = cholmod_l_ccolamd.o cholmod_l_csymamd.o \
	cholmod_l_metis.o cholmod_l_nesdis.o cholmod_l_camd.o

LMODIFY = cholmod_l_rowadd.o cholmod_l_rowdel.o cholmod_l_updown.o

LSUPERNODAL = cholmod_l_super_numeric.o cholmod_l_super_solve.o \
	cholmod_l_super_symbolic.o

DL = $(LCORE) $(LCHECK) $(LCHOLESKY) $(LMATRIXOPS) $(LMODIFY) $(LSUPERNODAL) #$(LPARTITION)

#-------------------------------------------------------------------------------

OBJS = $(DI) $(DL)
#              ^^  FIXME?  MM thinks we should be able to only use $(DI)
#  well, Doug thought so, too .. but it's currently wrong:
# --> ../../chm_common.c  has a few  'cholmod_l_' left

LIB = ../../CHOLMOD.a

C = $(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS)

lib: $(LIB)

$(LIB): $(OBJS)
	$(AR) -rucs $(LIB) $(OBJS)

mostlyclean: clean

clean:
	@-rm -rf .libs _libs $(LIB)
	@-rm -f $(OBJS)

$(OBJ): $(INC)

#-------------------------------------------------------------------------------
# Check Module:
#-------------------------------------------------------------------------------

cholmod_check.o: ../Check/cholmod_check.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Check/cholmod_check.c -o $@
cholmod_read.o: ../Check/cholmod_read.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Check/cholmod_read.c -o $@
cholmod_write.o: ../Check/cholmod_write.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Check/cholmod_write.c -o $@

#-------------------------------------------------------------------------------

cholmod_l_check.o: ../Check/cholmod_check.c
	$(C) -DDLONG -c $(I) ../Check/cholmod_check.c -o $@

cholmod_l_read.o: ../Check/cholmod_read.c
	$(C) -DDLONG -c $(I) ../Check/cholmod_read.c -o $@

cholmod_l_write.o: ../Check/cholmod_write.c
	$(C) -DDLONG -c $(I) ../Check/cholmod_write.c -o $@


#-------------------------------------------------------------------------------
# Core Module:
#-------------------------------------------------------------------------------

cholmod_common.o: ../Core/cholmod_common.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_common.c -o $@
cholmod_dense.o: ../Core/cholmod_dense.c ../Core/t_cholmod_dense.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_dense.c -o $@
cholmod_factor.o: ../Core/cholmod_factor.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_factor.c -o $@
cholmod_change_factor.o: ../Core/cholmod_change_factor.c \
	../Core/t_cholmod_change_factor.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_change_factor.c -o $@
cholmod_memory.o: ../Core/cholmod_memory.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_memory.c -o $@
cholmod_sparse.o: ../Core/cholmod_sparse.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_sparse.c -o $@
cholmod_complex.o: ../Core/cholmod_complex.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_complex.c -o $@
cholmod_transpose.o: ../Core/cholmod_transpose.c ../Core/t_cholmod_transpose.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_transpose.c -o $@
cholmod_band.o: ../Core/cholmod_band.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_band.c -o $@
cholmod_copy.o: ../Core/cholmod_copy.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_copy.c -o $@
cholmod_triplet.o: ../Core/cholmod_triplet.c ../Core/t_cholmod_triplet.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_triplet.c -o $@
cholmod_error.o: ../Core/cholmod_error.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_error.c -o $@
cholmod_aat.o: ../Core/cholmod_aat.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_aat.c -o $@
cholmod_add.o: ../Core/cholmod_add.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_add.c -o $@

cholmod_version.o: ../Core/cholmod_version.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Core/cholmod_version.c -o $@

#-------------------------------------------------------------------------------

cholmod_l_common.o: ../Core/cholmod_common.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_common.c -o $@

cholmod_l_dense.o: ../Core/cholmod_dense.c ../Core/t_cholmod_dense.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_dense.c -o $@

cholmod_l_factor.o: ../Core/cholmod_factor.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_factor.c -o $@

cholmod_l_change_factor.o: ../Core/cholmod_change_factor.c \
	../Core/t_cholmod_change_factor.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_change_factor.c -o $@

cholmod_l_memory.o: ../Core/cholmod_memory.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_memory.c -o $@

cholmod_l_sparse.o: ../Core/cholmod_sparse.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_sparse.c -o $@

cholmod_l_complex.o: ../Core/cholmod_complex.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_complex.c -o $@

cholmod_l_transpose.o: ../Core/cholmod_transpose.c ../Core/t_cholmod_transpose.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_transpose.c -o $@

cholmod_l_band.o: ../Core/cholmod_band.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_band.c -o $@

cholmod_l_copy.o: ../Core/cholmod_copy.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_copy.c -o $@

cholmod_l_triplet.o: ../Core/cholmod_triplet.c ../Core/t_cholmod_triplet.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_triplet.c -o $@

cholmod_l_error.o: ../Core/cholmod_error.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_error.c -o $@

cholmod_l_aat.o: ../Core/cholmod_aat.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_aat.c -o $@

cholmod_l_add.o: ../Core/cholmod_add.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_add.c -o $@

cholmod_l_version.o: ../Core/cholmod_version.c
	$(C) -DDLONG -c $(I) ../Core/cholmod_version.c -o $@


#-------------------------------------------------------------------------------
# Cholesky Module:
#-------------------------------------------------------------------------------

cholmod_amd.o: ../Cholesky/cholmod_amd.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_amd.c -o $@
cholmod_analyze.o: ../Cholesky/cholmod_analyze.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_analyze.c -o $@
cholmod_colamd.o: ../Cholesky/cholmod_colamd.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_colamd.c -o $@
cholmod_etree.o: ../Cholesky/cholmod_etree.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_etree.c -o $@
cholmod_factorize.o: ../Cholesky/cholmod_factorize.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_factorize.c -o $@
cholmod_postorder.o: ../Cholesky/cholmod_postorder.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_postorder.c -o $@
cholmod_rcond.o: ../Cholesky/cholmod_rcond.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_rcond.c -o $@
cholmod_resymbol.o: ../Cholesky/cholmod_resymbol.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_resymbol.c -o $@
cholmod_rowcolcounts.o: ../Cholesky/cholmod_rowcolcounts.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_rowcolcounts.c -o $@
cholmod_solve.o: ../Cholesky/cholmod_solve.c ../Cholesky/t_cholmod_lsolve.c \
	../Cholesky/t_cholmod_ltsolve.c ../Cholesky/t_cholmod_solve.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_solve.c -o $@
cholmod_spsolve.o: ../Cholesky/cholmod_spsolve.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_spsolve.c -o $@
cholmod_rowfac.o: ../Cholesky/cholmod_rowfac.c ../Cholesky/t_cholmod_rowfac.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Cholesky/cholmod_rowfac.c -o $@

#-------------------------------------------------------------------------------

cholmod_l_amd.o: ../Cholesky/cholmod_amd.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_amd.c -o $@

cholmod_l_analyze.o: ../Cholesky/cholmod_analyze.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_analyze.c -o $@

cholmod_l_colamd.o: ../Cholesky/cholmod_colamd.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_colamd.c -o $@

cholmod_l_etree.o: ../Cholesky/cholmod_etree.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_etree.c -o $@

cholmod_l_factorize.o: ../Cholesky/cholmod_factorize.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_factorize.c -o $@

cholmod_l_postorder.o: ../Cholesky/cholmod_postorder.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_postorder.c -o $@

cholmod_l_rcond.o: ../Cholesky/cholmod_rcond.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_rcond.c -o $@

cholmod_l_resymbol.o: ../Cholesky/cholmod_resymbol.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_resymbol.c -o $@

cholmod_l_rowcolcounts.o: ../Cholesky/cholmod_rowcolcounts.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_rowcolcounts.c -o $@

cholmod_l_solve.o: ../Cholesky/cholmod_solve.c ../Cholesky/t_cholmod_lsolve.c \
	../Cholesky/t_cholmod_ltsolve.c ../Cholesky/t_cholmod_solve.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_solve.c -o $@

cholmod_l_spsolve.o: ../Cholesky/cholmod_spsolve.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_spsolve.c -o $@

cholmod_l_rowfac.o: ../Cholesky/cholmod_rowfac.c ../Cholesky/t_cholmod_rowfac.c
	$(C) -DDLONG -c $(I) ../Cholesky/cholmod_rowfac.c -o $@

#-------------------------------------------------------------------------------
# Partition Module:
#-------------------------------------------------------------------------------

cholmod_ccolamd.o: ../Partition/cholmod_ccolamd.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Partition/cholmod_ccolamd.c -o $@
cholmod_csymamd.o: ../Partition/cholmod_csymamd.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Partition/cholmod_csymamd.c -o $@
cholmod_camd.o: ../Partition/cholmod_camd.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Partition/cholmod_camd.c -o $@
cholmod_metis.o: ../Partition/cholmod_metis.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Partition/cholmod_metis.c -o $@
cholmod_nesdis.o: ../Partition/cholmod_nesdis.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Partition/cholmod_nesdis.c -o $@

#-------------------------------------------------------------------------------

cholmod_l_ccolamd.o: ../Partition/cholmod_ccolamd.c
	$(C) -DDLONG -c $(I) ../Partition/cholmod_ccolamd.c -o $@

cholmod_l_csymamd.o: ../Partition/cholmod_csymamd.c
	$(C) -DDLONG -c $(I) ../Partition/cholmod_csymamd.c -o $@

cholmod_l_camd.o: ../Partition/cholmod_camd.c
	$(C) -DDLONG -c $(I) ../Partition/cholmod_camd.c -o $@

cholmod_l_metis.o: ../Partition/cholmod_metis.c
	$(C) -DDLONG -c $(I) ../Partition/cholmod_metis.c -o $@

cholmod_l_nesdis.o: ../Partition/cholmod_nesdis.c
	$(C) -DDLONG -c $(I) ../Partition/cholmod_nesdis.c -o $@


#-------------------------------------------------------------------------------
# MatrixOps Module:
#-------------------------------------------------------------------------------

cholmod_horzcat.o: ../MatrixOps/cholmod_horzcat.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../MatrixOps/cholmod_horzcat.c -o $@
cholmod_norm.o: ../MatrixOps/cholmod_norm.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../MatrixOps/cholmod_norm.c -o $@
cholmod_scale.o: ../MatrixOps/cholmod_scale.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../MatrixOps/cholmod_scale.c -o $@
cholmod_drop.o: ../MatrixOps/cholmod_drop.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../MatrixOps/cholmod_drop.c -o $@
cholmod_sdmult.o: ../MatrixOps/cholmod_sdmult.c \
	../MatrixOps/t_cholmod_sdmult.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../MatrixOps/cholmod_sdmult.c -o $@
cholmod_ssmult.o: ../MatrixOps/cholmod_ssmult.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../MatrixOps/cholmod_ssmult.c -o $@
cholmod_submatrix.o: ../MatrixOps/cholmod_submatrix.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../MatrixOps/cholmod_submatrix.c -o $@
cholmod_vertcat.o: ../MatrixOps/cholmod_vertcat.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../MatrixOps/cholmod_vertcat.c -o $@
cholmod_symmetry.o: ../MatrixOps/cholmod_symmetry.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../MatrixOps/cholmod_symmetry.c -o $@

#-------------------------------------------------------------------------------

cholmod_l_horzcat.o: ../MatrixOps/cholmod_horzcat.c
	$(C) -DDLONG -c $(I) ../MatrixOps/cholmod_horzcat.c -o $@

cholmod_l_norm.o: ../MatrixOps/cholmod_norm.c
	$(C) -DDLONG -c $(I) ../MatrixOps/cholmod_norm.c -o $@

cholmod_l_scale.o: ../MatrixOps/cholmod_scale.c
	$(C) -DDLONG -c $(I) ../MatrixOps/cholmod_scale.c -o $@

cholmod_l_drop.o: ../MatrixOps/cholmod_drop.c
	$(C) -DDLONG -c $(I) ../MatrixOps/cholmod_drop.c -o $@

cholmod_l_sdmult.o: ../MatrixOps/cholmod_sdmult.c \
	../MatrixOps/t_cholmod_sdmult.c
	$(C) -DDLONG -c $(I) ../MatrixOps/cholmod_sdmult.c -o $@

cholmod_l_ssmult.o: ../MatrixOps/cholmod_ssmult.c
	$(C) -DDLONG -c $(I) ../MatrixOps/cholmod_ssmult.c -o $@

cholmod_l_submatrix.o: ../MatrixOps/cholmod_submatrix.c
	$(C) -DDLONG -c $(I) ../MatrixOps/cholmod_submatrix.c -o $@

cholmod_l_vertcat.o: ../MatrixOps/cholmod_vertcat.c
	$(C) -DDLONG -c $(I) ../MatrixOps/cholmod_vertcat.c -o $@

cholmod_l_symmetry.o: ../MatrixOps/cholmod_symmetry.c
	$(C) -DDLONG -c $(I) ../MatrixOps/cholmod_symmetry.c -o $@

#-------------------------------------------------------------------------------
# Modify Module:
#-------------------------------------------------------------------------------

cholmod_rowadd.o: ../Modify/cholmod_rowadd.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Modify/cholmod_rowadd.c -o $@
cholmod_rowdel.o: ../Modify/cholmod_rowdel.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Modify/cholmod_rowdel.c -o $@
cholmod_updown.o: ../Modify/cholmod_updown.c \
	../Modify/t_cholmod_updown.c ../Modify/t_cholmod_updown_numkr.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Modify/cholmod_updown.c -o $@

#-------------------------------------------------------------------------------

cholmod_l_rowadd.o: ../Modify/cholmod_rowadd.c
	$(C) -DDLONG -c $(I) ../Modify/cholmod_rowadd.c -o $@

cholmod_l_rowdel.o: ../Modify/cholmod_rowdel.c
	$(C) -DDLONG -c $(I) ../Modify/cholmod_rowdel.c -o $@

cholmod_l_updown.o: ../Modify/cholmod_updown.c \
	../Modify/t_cholmod_updown.c ../Modify/t_cholmod_updown_numkr.c
	$(C) -DDLONG -c $(I) ../Modify/cholmod_updown.c -o $@


#-------------------------------------------------------------------------------
# Supernodal Module:
#-------------------------------------------------------------------------------

cholmod_super_numeric.o: ../Supernodal/cholmod_super_numeric.c \
        ../Supernodal/t_cholmod_gpu.c \
	../Supernodal/t_cholmod_super_numeric.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Supernodal/cholmod_super_numeric.c -o $@
cholmod_super_symbolic.o: ../Supernodal/cholmod_super_symbolic.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Supernodal/cholmod_super_symbolic.c -o $@
cholmod_super_solve.o: ../Supernodal/cholmod_super_solve.c \
	../Supernodal/t_cholmod_super_solve.c
	$(CC) $(ALL_CPPFLAGS) $(ALL_CFLAGS) -c ../Supernodal/cholmod_super_solve.c -o $@

#-------------------------------------------------------------------------------

cholmod_l_super_numeric.o: ../Supernodal/cholmod_super_numeric.c \
	../Supernodal/t_cholmod_super_numeric.c
	$(C) -DDLONG -c $(I) ../Supernodal/cholmod_super_numeric.c -o $@

cholmod_l_super_symbolic.o: ../Supernodal/cholmod_super_symbolic.c
	$(C) -DDLONG -c $(I) ../Supernodal/cholmod_super_symbolic.c -o $@

cholmod_l_super_solve.o: ../Supernodal/cholmod_super_solve.c \
	../Supernodal/t_cholmod_super_solve.c
	$(C) -DDLONG -c $(I) ../Supernodal/cholmod_super_solve.c -o $@
