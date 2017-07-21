// !DIAGNOSTICS: -UNUSED_PARAMETER
// WITH_RUNTIME

class MyHashMap<K, V>

fun <K, V> myGet(receiver: MyHashMap<K, V>, x: K, y: V) {}
fun <K : Any, V : Any> myGet(receiver: MyHashMap<K, V>) {}

fun <K, V> test(c: MyHashMap<K, V>, x: K, y: V) {
//    myGet(c, y, x)
    myGet(c)
//    myGet(c, y, subCall())
}

fun <V> subCall(): V = TODO()



//fun <K : Any> myGet(receiver: K) {}
//
//fun <K> test(c: K) {
//    myGet(c)
//}

//class MyHashMap<K, V>
//
//fun <K : Any, V> myGet(receiver: MyHashMap<K, V>) {}
//
//fun <K, V> test(c: MyHashMap<K, V>) {
//    myGet(c)
//}

//fun <K, V> test(c: HashMap<K, V>, x: K, y: K) {
//    c[x] = y
//}
//
//operator fun <K, V> MutableMap<K, V>.set(key: K, value: V): Unit {
//}

//class MyHashMap<K, V>
//
//fun <K, V> test(c: MyHashMap<K, V>, x: K, y: V) {
//    c[x] = y
//}
//
//operator fun <K : Any, V> MyHashMap<K, V>.set(key: K, value: V): Unit {
//}


//fun <T> foo() : T? { return null; }
//
//fun <T> bar(): T {
//    return run { foo() }
//}


//fun foo(x: Any?, y: () -> Unit) {}
//fun foo(y: () -> Unit, x: Any?) {}
//fun test1(y: Any?) {
//    foo(y!!) { y.hashCode() }
//}
//
//fun test2(y: Any?) {
//    foo(y!!, { y.hashCode() })
//}
//
//fun test3(y: Any?) {
//    foo({ y.hashCode() }, y!!)
//}
//class In<in T>
//class Out<out T>
//class Inv<T>
//
//interface A
//interface B
//
//fun <T> testSafeRange(x: Inv<T>, y: In<T>) {
//}
//
//fun <T> outIn(x: Out<T>, y: In<T>) {}
//
//fun <T, K> box(x: Out<T>, y: In<K>) {
//    outIn(x, y)
//}


//class Inv1<E>
//class Inv2<E>
//
//class A<T> {
//    fun foo(x: Inv1<T>) {}
////    fun foo(x: Inv2<T>) {}
//}
//
//fun foo(a: A<*>) {
//    a.foo(Inv1<String>())
//}