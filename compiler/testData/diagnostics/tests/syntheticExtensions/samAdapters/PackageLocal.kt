// FILE: KotlinFile1.kt
// IGNORE_IF_NEW_INFERENCE_ENABLED
package k

import JavaClass

fun foo(javaClass: JavaClass) {
    javaClass.<!INVISIBLE_MEMBER!>doSomething<!> { }
}

// FILE: KotlinFile2.kt
fun foo(javaClass: JavaClass) {
    javaClass.doSomething { }
}

// FILE: JavaClass.java
public class JavaClass {
    void doSomething(Runnable runnable) { runnable.run(); }
}