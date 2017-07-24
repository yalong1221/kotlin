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

    fun <T : Number> onlyLambda(<warning descr="[UNUSED_PARAMETER] Parameter 'x' is never used">x</warning>: () -> T) {}

    fun testOnlyLambda() {
        onlyLambda {
            val longLong = 123
            <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Number (from declared upper bound T)
lower bounds: String">longLong.toString()</error>
        }
    }

    fun testLambdaWithReturnIfExpression(): Int {
        return <error descr="[TYPE_MISMATCH] Type mismatch: inferred type is Unit but Int was expected">onlyLambda {
           if (3 > 2) {
               return@onlyLambda <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable 'T' cannot be inferred because of incompatible bounds:
upper bounds: Number (from declared upper bound T)
lower bounds: String, {Int & Byte & Short & Long}, TypeVariable(<TYPE-PARAMETER-FOR-IF-RESOLVE>)">"not a number"</error>
           }
           if (3 < 2) {
               <error descr="[RETURN_NOT_ALLOWED] 'return' is not allowed here">return</error> <error descr="[TYPE_MISMATCH] Type mismatch: inferred type is String but Int was expected">"also not an int"</error>
           }
           <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Type variable '<TYPE-PARAMETER-FOR-IF-RESOLVE>' cannot be inferred because of incompatible bounds:
upper bounds: TypeVariable(T), Number
lower bounds: String, {Int & Byte & Short & Long}">if (true) "" else 123</error>
        }</error>
    }