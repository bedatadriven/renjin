

class Rectangle {
    int width, height;
  public:
    void set_values (int,int);
    int area () {return width*height;}
} rect;


void Rectangle::set_values (int x, int y) {
  width = x;
  height = y;
}

int calc_area () {
  Rectangle rect;
  rect.set_values (3,4);
  return rect.area();
}