fun test(x: Any?, y: Any?) {
    x ?: y!!
    y.hashCode()
}

fun box(): String {
    test(1, null)
    return "OK"
}