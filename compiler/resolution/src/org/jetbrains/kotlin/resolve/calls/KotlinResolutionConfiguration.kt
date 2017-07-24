/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.calls

val USE_NEW_INFERENCE = true

val REPORT_MISSING_NEW_INFERENCE_DIAGNOSTIC = true

class Inv1<K>

//fun <T> foo(x: T, l: (T) -> Unit) {}
//
//fun test() {
//    foo("", { x: Byte? -> })
//}

//fun <K> subCallNullableUpperBound(): Inv1<K> = TODO()
//fun <K : Any> subCallNullable(): Inv1<K?> = TODO()
//
//fun <S> test() {
//    foo1(subCallNullableUpperBound<S>())
//    foo1(subCallNullable<S>())
//}

//fun <T> foo(x: T, l: (T) -> Unit) {}

fun <T> foo(x: T, l: () -> T) {
}

//fun <T> onlyLambda(l: () -> T) {}
//
//fun testLambdaWithReturns(): Int {
//    return onlyLambda {
//        if (3 > 2) {
//            return@onlyLambda "not a number"
//        }
//        if (3 < 2) {
//            return "also not an int"
//        }
//        if (true) "something" else 123
//    }
//}

//fun <T> foo()
//
//fun test() {
//    foo("", { x: Byte? -> })
//}