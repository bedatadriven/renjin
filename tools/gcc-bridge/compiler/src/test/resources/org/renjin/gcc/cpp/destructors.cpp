
int global_val;

class Simple {
public:
	Simple() {};
	~Simple() { global_val++; };
};

extern "C" int run() {
	global_val = 0;
	{
		Simple a;
		Simple b;
		Simple c;
	}
	return global_val;
}