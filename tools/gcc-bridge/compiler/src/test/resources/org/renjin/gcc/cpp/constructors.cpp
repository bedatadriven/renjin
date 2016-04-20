
class NoConstructor {
public:
	int val;
};

class DefaultConstructor {
public:
	int val;
	DefaultConstructor() : val(1) {};
};

class CustomConstructor {
public:
	int val;
	CustomConstructor(int x) : val(x) {};
};

class CopyConstructor {
public:
	int val;
	CopyConstructor(int x) : val(x) {};
	CopyConstructor(const CopyConstructor &src) : val(src.val) {};
};

class BothConstructors {
public:
	int val;
	BothConstructors() : val(1) {};
	BothConstructors(int x) : val(x) {};
};

int run() {
	NoConstructor a;
	a.val = 1;

	DefaultConstructor b;

	CustomConstructor c(1);

	BothConstructors d;
	BothConstructors e(1);

	return a.val + b.val + c.val + d.val + e.val;
}