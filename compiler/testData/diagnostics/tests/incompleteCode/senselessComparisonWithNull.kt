// IGNORE_IF_NEW_INFERENCE_ENABLED
// !DIAGNOSTICS: -UNUSED_EXPRESSION
package d

fun foo(a : IntArray) {
    if (null == <!FUNCTION_EXPECTED!>a<!>()<!SYNTAX!><!>
<!SYNTAX!><!>}