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

package org.jetbrains.kotlin.load.java

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.load.java.AnnotationTypeQualifierResolver.NullabilityQualifierWithApplicability
import org.jetbrains.kotlin.load.java.AnnotationTypeQualifierResolver.QualifierApplicabilityType
import org.jetbrains.kotlin.load.java.typeEnhancement.NullabilityQualifier
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.constants.ArrayValue
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.resolve.constants.EnumValue
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult

private val TYPE_QUALIFIER_NICKNAME_FQNAME = FqName("javax.annotation.meta.TypeQualifierNickname")
private val TYPE_QUALIFIER_FQNAME = FqName("javax.annotation.meta.TypeQualifier")
private val TYPE_QUALIFIER_DEFAULT_FQNAME = FqName("javax.annotation.meta.TypeQualifierDefault")

interface AnnotationTypeQualifierResolver {
    fun resolveTypeQualifierAnnotation(annotationDescriptor: AnnotationDescriptor): AnnotationDescriptor?

    fun resolveDefaultNullabilityAnnotation(annotationDescriptor: AnnotationDescriptor): NullabilityQualifierWithApplicability?

    fun extractTypeQualifierNullability(annotationDescriptor: AnnotationDescriptor): NullabilityQualifier?

    enum class QualifierApplicabilityType {
        METHOD_RETURN_TYPE, VALUE_PARAMETER, FIELD, TYPE_USE
    }

    class NullabilityQualifierWithApplicability(
            private val nullabilityQualifier: NullabilityQualifier,
            private val applicability: Int
    ) {
        private fun isApplicableTo(elementType: QualifierApplicabilityType) = (applicability and (1 shl elementType.ordinal)) != 0

        operator fun component1() = nullabilityQualifier
        operator fun component2() = QualifierApplicabilityType.values().filter(this::isApplicableTo)
    }

    object Empty : AnnotationTypeQualifierResolver {
        override fun resolveTypeQualifierAnnotation(annotationDescriptor: AnnotationDescriptor): AnnotationDescriptor? = null

        override fun resolveDefaultNullabilityAnnotation(annotationDescriptor: AnnotationDescriptor): NullabilityQualifierWithApplicability? = null

        override fun extractTypeQualifierNullability(annotationDescriptor: AnnotationDescriptor): NullabilityQualifier? = null
    }
}

class AnnotationTypeQualifierResolverImpl(storageManager: StorageManager) : AnnotationTypeQualifierResolver {
    private val resolvedNicknames =
            storageManager.createMemoizedFunctionWithNullableValues(this::computeTypeQualifierNickname)

    private fun computeTypeQualifierNickname(classDescriptor: ClassDescriptor): AnnotationDescriptor? {
        if (!classDescriptor.annotations.hasAnnotation(TYPE_QUALIFIER_NICKNAME_FQNAME)) return null

        return classDescriptor.annotations.firstNotNullResult(this::resolveTypeQualifierAnnotation)
    }

    private fun resolveTypeQualifierNickname(classDescriptor: ClassDescriptor): AnnotationDescriptor? {
        if (classDescriptor.kind != ClassKind.ANNOTATION_CLASS) return null

        return resolvedNicknames(classDescriptor)
    }

    override fun resolveTypeQualifierAnnotation(annotationDescriptor: AnnotationDescriptor): AnnotationDescriptor? {
        val annotationClass = annotationDescriptor.annotationClass ?: return null
        if (annotationClass.isAnnotatedWithTypeQualifier) return annotationDescriptor

        return resolveTypeQualifierNickname(annotationClass)
    }

    override fun resolveDefaultNullabilityAnnotation(annotationDescriptor: AnnotationDescriptor): NullabilityQualifierWithApplicability? {
        val typeQualifierDefaultAnnotatedClass =
                annotationDescriptor.annotationClass?.takeIf { it.annotations.hasAnnotation(TYPE_QUALIFIER_DEFAULT_FQNAME) }
                ?: return null

        val anno = annotationDescriptor.annotationClass!!.annotations.findAnnotation(TYPE_QUALIFIER_DEFAULT_FQNAME)!!
        val allValueArguments = anno.allValueArguments
        val elementTypesMask =
                allValueArguments
                        .flatMap { (parameter, argument) ->
                            if (parameter == JvmAnnotationNames.DEFAULT_ANNOTATION_MEMBER_NAME)
                                argument.mapConstantToQualifierApplicabilityTypes()
                            else
                                emptyList()
                        }
                        .fold(0) { acc: Int, applicabilityType -> acc or (1 shl applicabilityType.ordinal) }

        val typeQualifier =
                    typeQualifierDefaultAnnotatedClass.annotations.firstNotNullResult(this::resolveTypeQualifierAnnotation)
                ?: return null
        val nullabilityQualifier = checkExtractTypeQualifierNullability(typeQualifier, false) ?: return null
        return NullabilityQualifierWithApplicability(nullabilityQualifier, elementTypesMask)
    }

    override fun extractTypeQualifierNullability(annotationDescriptor: AnnotationDescriptor): NullabilityQualifier? {
        return checkExtractTypeQualifierNullability(annotationDescriptor)
    }

    private fun checkExtractTypeQualifierNullability(annotationDescriptor: AnnotationDescriptor,
                                                     checkTypeQualifieAnnotation: Boolean = true): NullabilityQualifier? {
        val annotationFqName = annotationDescriptor.fqName ?: return null
        val typeQualifier =
                when {
                    annotationFqName == JAVAX_NONNULL_ANNOTATION -> annotationDescriptor
                    else -> resolveTypeQualifierAnnotation(annotationDescriptor)
                            ?.takeIf { it.fqName == JAVAX_NONNULL_ANNOTATION }
                } ?: return extractFallbackTypeQualifierNullability(annotationDescriptor, checkTypeQualifieAnnotation)

        val enumEntryDescriptor =
                typeQualifier.allValueArguments.values.singleOrNull()?.value
                // if no argument is specified, use default value: NOT_NULL
                ?: return NullabilityQualifier.NOT_NULL

        if (enumEntryDescriptor !is ClassDescriptor) return null

        return when (enumEntryDescriptor.name.asString()) {
            "ALWAYS" -> NullabilityQualifier.NOT_NULL
            "MAYBE" -> NullabilityQualifier.NULLABLE
            else -> null
        }
    }

    /**
     * Extracts nullability information from an annotation annotated with one of JSR-305 nullability
     * annotations when the JSR-305 classses are not on the project classpath (required for Spring).
     */
    private fun extractFallbackTypeQualifierNullability(annotationDescriptor: AnnotationDescriptor,
                                                        checkTypeQualifierAnnotation: Boolean): NullabilityQualifier? {
        val annotationClass = annotationDescriptor.annotationClass ?: return null
        if (checkTypeQualifierAnnotation) {
            if (!annotationClass.annotations.hasAnnotation(TYPE_QUALIFIER_FQNAME) &&
                !annotationClass.annotations.hasAnnotation(TYPE_QUALIFIER_NICKNAME_FQNAME)) {
                return null
            }
        }

        if (annotationClass.annotations.hasAnnotation(JAVAX_CHECKFORNULL_ANNOTATION)) {
            return NullabilityQualifier.NULLABLE
        }

        return null
    }

    private fun ConstantValue<*>.mapConstantToQualifierApplicabilityTypes(): List<QualifierApplicabilityType> =
        when (this) {
            is ArrayValue -> value.flatMap { it.mapConstantToQualifierApplicabilityTypes() }
            is EnumValue -> listOfNotNull(
                    when (value.name.identifier) {
                        "METHOD" -> QualifierApplicabilityType.METHOD_RETURN_TYPE
                        "FIELD" -> QualifierApplicabilityType.FIELD
                        "PARAMETER" -> QualifierApplicabilityType.VALUE_PARAMETER
                        "TYPE_USE" -> QualifierApplicabilityType.TYPE_USE
                        else -> null
                    }
            )
            else -> emptyList()
        }
}

private val ClassDescriptor.isAnnotatedWithTypeQualifier: Boolean
    get() = annotations.hasAnnotation(TYPE_QUALIFIER_FQNAME)
