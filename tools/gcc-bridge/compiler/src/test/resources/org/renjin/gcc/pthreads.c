
#include <pthread.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <ctype.h>

#include "assert.h"

struct thread_info {    /* Used as argument to thread_start() */
   pthread_t thread_id;        /* ID returned by pthread_create() */
   int       thread_num;       /* Application-defined thread # */
};

/* Thread start function: display address near top of our stack,
  and return upper-cased copy of argv_string */

static void *
thread_start(void *arg)
{
   struct thread_info *tinfo = arg;

   int *presult = malloc(sizeof(int));
   *presult = tinfo->thread_num * 2;
   return presult;
}


void test_threads()
{

   ASSERT(sizeof(pthread_t) == 4);

   int num_threads = 4;

   /* Initialize thread creation attributes */

   pthread_attr_t attr;
   ASSERT(pthread_attr_init(&attr) == 0);

   /* Allocate memory for pthread_create() arguments */

   struct thread_info *tinfo = calloc(num_threads, sizeof(struct thread_info));
   ASSERT(tinfo != NULL);

   /* Create one thread for each command-line argument */
    int tnum;
   for (tnum = 0; tnum < num_threads; tnum++) {
       tinfo[tnum].thread_num = tnum + 1;

       /* The pthread_create() call stores the thread ID into
          corresponding element of tinfo[] */

       ASSERT(pthread_create(&tinfo[tnum].thread_id, &attr,
                          &thread_start, &tinfo[tnum]) == 0);
   }

   /* Destroy the thread attributes object since it is no
      longer needed */

   ASSERT( pthread_attr_destroy(&attr) == 0);

   /* Now join with each thread, and display its returned value */
   void *res;

   for (tnum = 0; tnum < num_threads; tnum++) {
        printf("Starting thread %d\n", tnum);
       ASSERT( pthread_join(tinfo[tnum].thread_id, &res) == 0);

       int *presult = res;

       printf("Joined with thread %d; returned value was %d\n",
               tinfo[tnum].thread_num, *presult);
       ASSERT(*presult == tinfo[tnum].thread_num * 2);
       free(presult);
   }

   free(tinfo);
}

void main() {
    test_threads();
}

