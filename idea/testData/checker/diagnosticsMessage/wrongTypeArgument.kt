// WITH_RUNTIME

class Inv1<K>
fun <T> foo1(<warning descr="[UNUSED_PARAMETER] Parameter 'a' is never used">a</warning>: Inv1<T>, <warning descr="[UNUSED_PARAMETER] Parameter 'b' is never used">b</warning>: T) {}

fun <S, P> test1(a: Inv1<S>, b: P) {
    foo1(a, <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: S
equality constraints: S
lower bounds: P">b</error>)
}

//fun <S> test2(a: Inv1<S>, b: S?) {
//    foo1(a, <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
//upper bounds: S
//equality constraints: S
//lower bounds: S?">b</error>)
//}
//
//fun <T> foo2(<warning descr="[UNUSED_PARAMETER] Parameter 'a' is never used">a</warning>: T, <warning descr="[UNUSED_PARAMETER] Parameter 'b' is never used">b</warning>: Inv1<T>) {}
//
//fun <S, P> test3(a: S, b: Inv1<P>) {
//    foo2(a, <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
//upper bounds: P
//equality constraints: P
//lower bounds: S">b</error>)
//}
//
//fun <S> subCall(): S = TODO()
//
//fun <K, S> test4() {
//    foo1(subCall<Inv1<K>>(), <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
//upper bounds: K
//equality constraints: K
//lower bounds: S">subCall<S>()</error>)
//}
//
//class Inv2<K, V>
//fun <K, V> foo3(<warning descr="[UNUSED_PARAMETER] Parameter 'a' is never used">a</warning>: Inv2<K, V>, <warning descr="[UNUSED_PARAMETER] Parameter 'key' is never used">key</warning>: K) {}
//
//fun <T, S> test(a: Inv2<T, S>, v: S) {
//    foo3(a, <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'K' cannot be inferred because of incompatible bounds:
//upper bounds: T
//equality constraints: T
//lower bounds: S">v</error>)
//}