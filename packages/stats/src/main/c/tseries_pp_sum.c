 void tseries_pp_sum (double* u, int* n, int* l, double* sum)
{
  int i, j;
  double tmp1, tmp2;

  tmp1 = 0.0;
  for (i=1; i<=(*l); i++)
  {
    tmp2 = 0.0;
    for (j=i; j<(*n); j++)
    {
      tmp2 += u[j]*u[j-i];
    }
    tmp2 *= 1.0-((double)i/((double)(*l)+1.0));
    tmp1 += tmp2;
  }
  tmp1 /= (double)(*n);
  tmp1 *= 2.0;
  (*sum) += tmp1;
}