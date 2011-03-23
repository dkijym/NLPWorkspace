// 3 Aug 05 ... added preName feature for titles (not for n's)
pattern set emails;

emailBegin := "Dear" | "dear" | "hey" | "Hey" | "Hi" | "Hello" | "hello" | "hi";
emailBeginning := emailBegin [constit cat=name];
when emailBeginning	add [constit rtype=true];