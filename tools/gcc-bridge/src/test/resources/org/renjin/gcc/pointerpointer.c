

void init_array(double **x) {  // ObjectPtr x
  double *y = malloc(sizeof(double)*10);  // double y[] = new double[10];
  int i;
  for(i=0;i<10;++i) {
    y[i] = i;
  }
  *x = y;  // x w
}

double sum_array(double* values, int length) {
  double sum = 0;
  int i=0;
  for(i=0;i!=length;++i) {
    sum += values[i];
  }
  return sum;
}


double test() {
  double *x;                    //    ICONST_1
                                //    ANEWARRAY org/renjin/gcc/runtime/DoublePtr
                                //    ASTORE 0

  x = 0;                        //    ALOAD 0
                                //    ICONST_0
                                //    ACONST_NULL
                                //    AASTORE

              
                                //   NEW org/renjin/gcc/runtime/ObjectPtr
                                //   DUP
                                //   ALOAD 0 (DoublePtr[1] )
                                //   ICONST_0 
                                //   INVOKESPECIAL org/renjin/gcc/runtime/ObjectPtr.<init> ([Ljava/lang/Object;I)V
  init_array(&x);               //   INVOKESTATIC org/renjin/gcc/PointerPointers.init_array (Lorg/renjin/gcc/runtime/ObjectPtr;)V


  // double *x.0 = x            //  ALOAD 0
                                //  ICONST_0
                                // AALOAD
                                // ICONST_0
                                // stack :: DoublePtr, int
                                

  return sum_array(x, 10);      //  ALOAD 0
                                //  ICONST_0
                                //  AALOAD  -> Loads DoublePtr[1] onto the stack
}
