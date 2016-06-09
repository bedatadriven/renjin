
class Rectangle {
public:
  Rectangle();
  ~Rectangle();
  int width;
  int height;
  int depth;
  int volume();
};

Rectangle::Rectangle() {
	width = 0;
	height = 0;
	depth = 0;
}
Rectangle::~Rectangle() {
}

int Rectangle::volume() {
	return width * height * depth;
}

extern "C" Rectangle* create() {
	return new Rectangle();
}