##Lab 3 Writeup

Tristan Hill, Catherine Youngblood, Abeve Tayachow

###1. a. 

Test case:

```scala
int a = 1
int test1(){
    int result = a + 1;
    return result;
}
int test2() {
    int a = 2;
    int result2 = test1();
    return result2;
}
```

In the case that this is executed with static scoping, test1 and test2 will both return 2. However, if executed with dynamic scoping, test1 will return 2, but test2 will return 3.

###2.c. 

Yes, the evaluation order is deterministic. By this judgement form, given an e as input, we will always recieve e prime as output, and always know what to do next--this is essentially the definition of a deterministic evaluation order.

###3. 

As it's written, the language will evaluate the left side (e1) first, followed by the right side (e2). We know this because of the rule

e1 -> e1prime
___________________________
e1 bop e2 -> e1prime bop e2

Which precedes all rules regarding the instance that e2 -> e2 prime. To change the evaluation order, we would just have to make the first rule

e2 -> e2prime
____________________________
e1 bop e2 -> e1 bop e2prime

and change references to e2 to e1 in later rules in the same fashion.


###4.a.
 
Short circuit evaluation can, depending on the circumstances, save on overall computing time. For instance, in the case of the expression x && y, if we determine that x is false then using short circuit evaluation there is no need to actually determine y--we can simply return false. In the event that x and y were more complicated than just variables, this could cut down the overall processing time significantly.

###4. b. 

Yes, it does indeed short circuit! We know this because of the two rules, DoAndTrue and DoAndFalse. if e1 evaluates to true, DoAndTrue tells us to return whatever e2 evaluates to--however, if e1 evaluates to false, we simply return e1's evaluation (so, false). In other words, if e1 evaluates to true we have to continue on to e2, but if e1 evaluates to false we short circuit and just return false without wasting time on e2.