class Inv<K>
class Out<out K>
class In<in K>

fun <T> foo1(x: T, i1: Inv<T>, o1: Out<T>, y: T) {}

fun ttest(i: Inv<Int>, o: Out<String>) {
    foo(1.0, i, o, "")
}

fun <T> foo2(inv: Inv<T>, o: Out<T>, i: In<T>) {}

class A
class B
class C
fun bar(o: Out<B>, i: In<C>) {
    foo2(o, i)
}
