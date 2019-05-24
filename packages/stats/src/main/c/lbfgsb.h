
static void active(int, double *, double *, int *, double *, int *,
		   int, int *, int *, int *);
static void bmv(int, double *, double *, int *, double *, double *, int *);
static void cauchy(int, double *, double *,
		   double *, int *, double *, int *, int *,
		   double *, double *, double *, int, double *,
		   double *, double *, double *, double *, int * ,
		   int *, double *, double *, double *, double *,
		   int *, int, double *, int *, double *);
static void cmprlb(int, int, double *,
		   double *, double *, double *, double *,
		   double *, double *, double *, double *, int *,
		   double *, int *, int *, int *, int *,
		   int *);
static void dcsrch(double *, double *, double *,
		   double, double, double,
		   double, double, char *);
static void dcstep(double *, double *,
		   double *, double *, double *, double *,
		   double *, double *, double *, int *, double *,
		   double *);
static void errclb(int, int, double,
		   double *, double *, int *, char *, int *, int *);
static void formk(int, int *, int *, int *, int *, int *, int *,
		  int *, double *, double *, int, double *,
		  double *, double *, double *, int *, int *, int *);
static void formt(int, double *, double *,
		  double *, int *, double *, int *);
static void freev(int, int *, int *,
		  int *, int *, int *, int *, int *, int *,
		  int *, int, int *);
static void hpsolb(int, double *, int *, int);
static void lnsrlb(int, double *, double *,
		   int *, double *, double *, double *, double *,
		   double *, double *, double *, double *,
		   double *, double *, double *, double *,
		   double *, double *, double *, int *, int *,
		   int *, int *, int *, char *, int *, int *,
		   char *);
static void mainlb(int, int, double *,
		   double *, double *, int *, double *, double *,
		   double, double *, double *, double *,
		   double *, double *, double *, double *,
		   double *, double *, double *, double *,
		   double *, double *, int *, int *, int *, char *,
		   int, char *, int *);
static void matupd(int, int, double *, double *, double *,
		   double *, double *, double *, int *, int *,
		   int *, int *, double *, double *, double *,
		   double *, double *);
static void projgr(int, double *, double *,
		   int *, double *, double *, double *);
static void subsm(int, int, int *, int *, double *, double *,
		  int *, double *, double *, double *, double *,
		  double *, int *, int *, int *, double *,
		  double *, int, int *);

static void prn1lb(int n, int m, double *l, double *u, double *x,
		   int iprint, double epsmch);
static void prn2lb(int n, double *x, double *f, double *g, int iprint,
		   int iter, int nfgv, int nact, double sbgnrm,
		   int nint, char *word, int iword, int iback,
		   double stp, double xstep);
static void prn3lb(int n, double *x, double *f, char *task, int iprint,
		   int info, int iter, int nfgv, int nintol, int nskip,
		   int nact, double sbgnrm, int nint,
		   char *word, int iback, double stp, double xstep,
		   int k);