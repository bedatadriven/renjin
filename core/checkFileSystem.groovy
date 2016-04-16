
// Verify that the file system on which we are building is
// case sensitive. Otherwise package building can fail

f1 = new File("A")
f2 = new File("a")

f1.write "first"
f2.write "second"

// Verify that "a" is not overwritten by "A"
caseSensitive = f1.text.equals("first")

f1.delete()
f2.delete()

if(!caseSensitive) {
    throw new IllegalStateException("\n" +
            "***********************************************************\n" +
            "Renjin can only be built on a case-sensitive file system.\n" +
            "See BUILDING.md for more information.\n" +
            "***********************************************************")
}

