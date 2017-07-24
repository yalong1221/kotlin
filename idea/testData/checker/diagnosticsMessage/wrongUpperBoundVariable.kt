// WITH_RUNTIME

package very.very.long.name.of.pckg

class Inv1<K>

fun <T : Any> foo1(<warning descr="[UNUSED_PARAMETER] Parameter 'receiver' is never used">receiver</warning>: Inv1<T>) {}
fun <K> test(c: Inv1<K>) {
    foo1(<error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Any (from declared upper bound T), K
equality constraints: K
">c</error>)
}

class Inv2<T, K>
fun <T : Any, K : Any> foo2(<warning descr="[UNUSED_PARAMETER] Parameter 'a' is never used">a</warning>: Inv2<T, K>) {}
fun <S, V> test(c: Inv2<S, V>) {
    foo2(<error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'K' cannot be inferred because of incompatible bounds:
upper bounds: Any (from declared upper bound K), V
equality constraints: V
"><error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Any (from declared upper bound T), S
equality constraints: S
">c</error></error>)
}


fun <K> subCallNullableUpperBound(): Inv1<K> = TODO()
fun <K : Any> subCallNullable(): Inv1<K?> = TODO()

fun <S> test() {
    foo1(<error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Any (from declared upper bound T), S
equality constraints: S
">subCallNullableUpperBound<S>()</error>)
    foo1(<error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Any (from declared upper bound T), S?
equality constraints: S?
">subCallNullable<<error descr="[UPPER_BOUND_VIOLATED] Type argument is not within its bounds: should be subtype of 'Any'">S</error>>()</error>)
}
