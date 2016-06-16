
class Endpoint{

private:
  
  static int state_array[2][2][2];

  inline int state() const { return( state_array[query][left][closed] ); }
  
public:

  int index;
  double pos;
  bool query, left, closed;

  Endpoint(int i, double p, bool q, bool l, bool c); 
  
  inline bool operator< (const Endpoint& other) const {
    /*
      We assume that the calling code is aware of the difficulty in assessing
      equality for floating point numbers and that values are passed in as
      exactly equal (in their floating point representation) if and only if
      exact equality is intended by the calling code. Given this assumption,
      there is no need for a relative difference approach here.
    */
    if ( this->pos == other.pos ) 
      return( this->state() < other.state() );
    else
      return( this->pos < other.pos );
  }

  static void set_state_array( const int new_array[2][2][2] ) {
    int i, j, k;
    for( i = 0; i < 2; i++ ) 
      for( j = 0; j < 2; j++ ) 
        for( k = 0; k < 2; k++ ) 
          Endpoint::state_array[i][j][k] = new_array[i][j][k];
  }

  void R_print() const;

};


int test_endpoint() {
    Endpoint x(1, 2,  true, true, true);
    Endpoint y(2, 3,  true, true, true);
    return x < y;
}