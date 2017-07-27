fun <T> id(x: T): T = x

fun ttest(): Int {
    return <error descr="[TYPE_MISMATCH] Type mismatch: inferred type is Any? but Int was expected"><error descr="[CONTRADICTION_IN_CONSTRAINT_SYSTEM] Contradictory requirements for type variable 'T':
should be a subtype of: Int (expected type for 'id')
should be a supertype of: String (for parameter 'x')">id</error>(id(""))</error>
}