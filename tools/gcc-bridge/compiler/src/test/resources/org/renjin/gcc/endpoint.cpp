

// Included from intervals CRAN package
// Author: Richard Bourgon <bourgon.richard@gene.com>
// Maintainer: Edzer Pebesma <edzer.pebesma@uni-muenster.de>
// License: Artistic-2.0


#include "endpoint.h"
#include "assert.h"
#include <stdio.h>
#include <algorithm>
#include <utility>

//////// Ordering for tied endpoints

/*
  We use two different tied endpoint orderings, one for reduce, and a different
  one for interval_overlap. These should be set by any code that uses sorting,
  so we initialize to -1, and have the sort method check this and throw an error
  if it's still found.

  Let Q/T be query/target, L/R be left/right, and O/C be open/closed. Our
  ordering, when pos is effectively tied, is then:

    QRO < TRO ... < TLC < QLC < QRC < TRC ... < TLO < QLO
     0     1         2     3     4     5         6     7

  The basic principals are, for similar closure, start targets before
  queries but finish them after queries. For abutting intervals, start new
  intervals before finishing old ones, unless one or both endpoints are
  open, in which case we should finish old intervals first.
*/

int Endpoint::state_array[2][2][2] = {{{0,0},{0,0}},{{0,0},{0,0}}};


const int reduce_order[2][2][2] = {
  {{2,4},{3,1}}, // Target: {{ ), ] }, { (, [ }}      
  {{0,0},{0,0}}  // Query:  {{ ), ] }, { (, [ }}
};



//////// Endpoint methods

Endpoint::Endpoint(int i, double p, bool q, bool l, bool c) { 
  index = i; pos = p; query = q; left = l; closed = c; 
}

void Endpoint::R_print() const {
  printf(
	  "index = %d, pos = %f (%s, %s, %s)\n",
	  index, pos,
	  query ? "query" : "target",
	  left ? "left" : "right",
	  closed ? "closed" : "open"
	  );
}




//////// Endpoints methods

Endpoints::Endpoints( const double * pos, const int * closed, int n, bool query, bool is_full ) {
  /*
    The pos pointer should point to an n x 2 array of endpoints, and the closed
    pointer, to either an array of booleans of the same size (if full = true)
    or an array of two booleans (if full = false). Note that R uses int, not
    bool, for logicals. Intervals with R numeric NA in either slot are
    dropped. 
  */
      printf("size = %d\n", this->size());

  int i;
  this->reserve( 2 * n );
  for ( i = 0; i < n; i++ ) {
   // if ( ISNA( pos[i] ) || ISNA( pos[i+n] ) ) continue;
    this->push_back( Endpoint( i, pos[i], query, true, (bool) closed[ is_full ? i : 0 ] ) );
    this->push_back( Endpoint( i, pos[i+n], query, false, (bool) closed[ is_full ? i+n : 1 ] ) ); 
  }
} 

void Endpoints::R_print() const {
  Endpoints::const_iterator it;
  for ( it = this->begin(); it < this->end(); it++ )
    it->R_print();
}

extern "C" Endpoint*  test_endpoints() {

   ASSERT(sizeof(bool) == 1);
    
   double e[] = { 1, 2, 3, 4, 3, 3, 6, 8 };
   int n = 4;

   int c[] = {1, 1};
   
   Endpoints ep ( e, c, n, false, false );
   printf("ep.size() = %d\n", ep.size());
   printf("ep = \n\n");
   ep.R_print();
   
   printf("swapped:\n\n");
   std::swap(ep[0], ep[1]);
   ep.R_print();

   Endpoint::set_state_array( reduce_order );

   printf("about to sort\n");
   sort( ep.begin(), ep.end() );
   
   printf("sorted:\n\n");
   ep.R_print();
//   
//   index = 0, pos = 1.000000 (target, left, closed)
//   index = 1, pos = 2.000000 (target, left, closed)
//   index = 0, pos = 3.000000 (target, right, closed)
//   index = 1, pos = 3.000000 (target, right, closed)
//   index = 2, pos = 3.000000 (target, left, closed)
//   index = 3, pos = 4.000000 (target, left, closed)
//   index = 2, pos = 6.000000 (target, right, closed)
//   index = 3, pos = 8.000000 (target, right, closed)
   
   ASSERT(ep[0].pos == 1)
   ASSERT(ep[1].pos == 2)
   ASSERT(ep[2].pos == 3)
   ASSERT(ep[3].pos == 3)
   ASSERT(ep[4].pos == 3)
   ASSERT(ep[5].pos == 4)
   ASSERT(ep[6].pos == 6)
   ASSERT(ep[7].pos == 8)

   return &ep.front();
}

extern "C" Endpoint* alloc_endpoints() {
   double e[] = { 1, 2};
   int c[] = {1, 1};
   int n = 1;
  
   Endpoints ep ( e, c, n, false, false );
   return &ep.front();
}
