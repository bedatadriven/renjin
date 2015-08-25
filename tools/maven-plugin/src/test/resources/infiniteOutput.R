
## Simulate a test that, for whatever reason,
## wants to churn out enough output to fill up a hard disk and crash a whole test run

while(TRUE) {
    print(1:1000)
}

print("done.")

