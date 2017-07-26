fun <T : S, S : Number> foo(x: T, y: S) {}

fun test1(i: Int) {
    foo(i, "")
}

fun test2(i: Int) {
    foo("", i)
}

class Inv<T>
fun <T : S, S : K, K> bar(x: T, y: S, z: Inv<K>) {}

fun test3(inv: Inv<Double>) {
    bar("", "", inv)
}