fun <T> foo(<warning descr="[UNUSED_PARAMETER] Parameter 'x' is never used">x</warning>: T, <warning descr="[UNUSED_PARAMETER] Parameter 'l' is never used">l</warning>: (T) -> Unit) {}

fun testWrongParameterTypeOfLambda() {
    foo("", <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Contradictory requirements for type variable 'T':
should be a subtype of: Byte? (from argument for parameter 'l')
should be a supertype of: String (from argument for parameter 'x')">{ x: Byte? -> }</error>)
}

fun <T : Number> fooReturn(<warning descr="[UNUSED_PARAMETER] Parameter 'x' is never used">x</warning>: T, <warning descr="[UNUSED_PARAMETER] Parameter 'l' is never used">l</warning>: () -> T) {}

fun myTest() {
    fooReturn(1) {
        val someExpr = ""
        <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Contradictory requirements for type variable '<TYPE-PARAMETER-FOR-IF-RESOLVE>':
should be a subtype of: TypeVariable(T), Number
should be a supertype of: String (from argument for parameter 'thenBranch'), {Int & Byte & Short & Long} (from argument for parameter 'elseBranch')">if (true) someExpr else 2</error>
    }
}

fun testLambdaLastExpression() {
    fooReturn(1) {
        val longLongLambda = ""
        <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Contradictory requirements for type variable 'T':
should be a subtype of: Number (from declared upper bound T)
should be a supertype of: {Int & Byte & Short & Long} (from argument for parameter 'x'), String">longLongLambda</error>
    }

    fooReturn(<error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Contradictory requirements for type variable 'T':
should be a subtype of: Number (from declared upper bound T)
should be a supertype of: String (from argument for parameter 'x'), Int">""</error>) {
    val long = 3
    long
}
}

fun <T : Number> onlyLambda(<warning descr="[UNUSED_PARAMETER] Parameter 'x' is never used">x</warning>: () -> T) {}

fun testOnlyLambda() {
    onlyLambda {
        val longLong = 123
        <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Contradictory requirements for type variable 'T':
        should be a subtype of: Number (from declared upper bound T)
        should be a supertype of: String">longLong.toString()</error>
    }
}

fun testLambdaWithReturnIfExpression(): Int {
    return <error descr="[TYPE_MISMATCH] Type mismatch: inferred type is Unit but Int was expected">onlyLambda {
    if (3 > 2) {
        return@onlyLambda <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Contradictory requirements for type variable 'T':
should be a subtype of: Number (from declared upper bound T)
should be a supertype of: String, {Int & Byte & Short & Long}, TypeVariable(<TYPE-PARAMETER-FOR-IF-RESOLVE>)">"not a number"</error>
    }
    if (3 < 2) {
        <error descr="[RETURN_NOT_ALLOWED] 'return' is not allowed here">return</error> <error descr="[TYPE_MISMATCH] Type mismatch: inferred type is String but Int was expected">"also not an int"</error>
    }
    <error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Contradictory requirements for type variable '<TYPE-PARAMETER-FOR-IF-RESOLVE>':
should be a subtype of: TypeVariable(T), Number
should be a supertype of: String (from argument for parameter 'thenBranch'), {Int & Byte & Short & Long} (from argument for parameter 'elseBranch')">if (true) "" else 123</error>
}</error>
}