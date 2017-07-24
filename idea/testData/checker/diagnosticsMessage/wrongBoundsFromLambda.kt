fun <T> foo(<warning descr="[UNUSED_PARAMETER] Parameter 'x' is never used">x</warning>: T, <warning descr="[UNUSED_PARAMETER] Parameter 'l' is never used">l</warning>: (T) -> Unit) {}

fun testWrongParameterTypeOfLambda() {
    foo("", <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Byte?
lower bounds: String">{ x: Byte? -> }</error>)
}

fun <T : Number> fooReturn(<warning descr="[UNUSED_PARAMETER] Parameter 'x' is never used">x</warning>: T, <warning descr="[UNUSED_PARAMETER] Parameter 'l' is never used">l</warning>: () -> T) {}

fun testLambdaLastExpression() {
    fooReturn(1) {
        val longLongLambda = ""
        <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Number (from declared upper bound T)
lower bounds: {Int & Byte & Short & Long}, String">longLongLambda</error>
    }

    fooReturn(<error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Number (from declared upper bound T)
lower bounds: String, Int">""</error>) {
    val long = 3
    long
}
}

fun testNoParameter() {
    fooReturn(1) <error descr="[TYPE_MISMATCH] Type mismatch: inferred type is (Any) -> TypeVariable(T) but () -> Any? was expected">{ x ->
        <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Number (from declared upper bound T)
lower bounds: {Int & Byte & Short & Long}, Any">x</error> }</error>
    }


    fun <T : Number> onlyLambda(<warning descr="[UNUSED_PARAMETER] Parameter 'x' is never used">x</warning>: () -> T) {}

    fun testOnlyLambda() {
        onlyLambda {
            val longLong = 123
            <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Number (from declared upper bound T)
lower bounds: String">longLong.toString()</error>
        }
    }