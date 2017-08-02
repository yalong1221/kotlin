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

package org.jetbrains.kotlin.backend.js.lower

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.bridges.generateBridgesForFunctionDescriptor
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ReceiverParameterDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrValueParameterImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.js.naming.encodeSignature
import org.jetbrains.kotlin.resolve.calls.components.substitute
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeProjectionImpl
import org.jetbrains.kotlin.types.TypeSubstitutor

class BridgeLowering() : ClassLoweringPass {
    override fun lower(irClass: IrClass) {
        if (irClass.descriptor.kind == ClassKind.ANNOTATION_CLASS) return

        val functions = irClass.descriptor.unsubstitutedMemberScope.getContributedDescriptors(DescriptorKindFilter.CALLABLES)
                .filterIsInstance<FunctionDescriptor>()
                .filterNot { it.modality == Modality.ABSTRACT || it is ConstructorDescriptor || it.dispatchReceiverParameter == null }
        if (functions.isEmpty()) return

        for (function in functions) {
            val bridgesToGenerate = generateBridgesForFunctionDescriptor(
                    function,
                    { SignatureAndDescriptor(encodeSignature(it), it) },
                    { false })

            for ((fromDescriptor, toDescriptor) in bridgesToGenerate.asSequence().map { (from, to) -> from.descriptor to to.descriptor }) {
                if (fromDescriptor != toDescriptor) {
                    createBridge(irClass, fromDescriptor, toDescriptor)
                }
            }
        }
    }

    private fun createBridge(irClass: IrClass, fromDescriptor: FunctionDescriptor, toDescriptor: FunctionDescriptor) {
        val newFunction = IrFunctionImpl(irClass.startOffset, irClass.endOffset, irClass.origin, fromDescriptor)
        val delegateCall = IrCallImpl(irClass.startOffset, irClass.endOffset, IrSimpleFunctionSymbolImpl(toDescriptor))
        for ((i, paramDescriptor) in fromDescriptor.valueParameters.withIndex()) {
            val irValueParameter = IrValueParameterImpl(
                    irClass.startOffset, irClass.endOffset, irClass.origin,
                    paramDescriptor)
            newFunction.valueParameters += irValueParameter
            var getParamExpr: IrExpression = IrGetValueImpl(
                    irClass.startOffset, irClass.endOffset,
                    IrValueParameterSymbolImpl(paramDescriptor))

            val targetType = toDescriptor.valueParameters[i].type
            if (paramDescriptor.type != targetType) {
                getParamExpr = IrTypeOperatorCallImpl(
                        irClass.startOffset, irClass.endOffset,
                        paramDescriptor.type, IrTypeOperator.IMPLICIT_CAST, targetType, getParamExpr)
            }
            delegateCall.putValueArgument(i, getParamExpr)
        }

        val dispatchReceiver = irClass.thisReceiver!!
        newFunction.dispatchReceiverParameter = dispatchReceiver
        delegateCall.dispatchReceiver = IrGetValueImpl(irClass.startOffset, irClass.endOffset, dispatchReceiver.symbol)

        newFunction.body = IrExpressionBodyImpl(delegateCall)
        irClass.addMember(newFunction)
    }

    private fun copyFunctionDescriptor(function: FunctionDescriptor): FunctionDescriptor {
        val functionCopy = SimpleFunctionDescriptorImpl.create(
                function.containingDeclaration, Annotations.EMPTY, function.name,
                CallableMemberDescriptor.Kind.SYNTHESIZED, function.source)

        val parameterMap = function.typeParameters.associate {
            it to TypeParameterDescriptorImpl.createForFurtherModification(
                    functionCopy, it.annotations, it.isReified, it.variance, it.name, it.index, it.source)
        }
        val substitutor = TypeSubstitutor.create(parameterMap.entries.associate { (key, value) ->
            key.typeConstructor to TypeProjectionImpl(KotlinTypeFactory.simpleType(
                    Annotations.EMPTY, value.typeConstructor, emptyList(), false))
        })

        val receiverParameterDescriptorCopy = ReceiverParameterDescriptorImpl()

        functionCopy.initialize(
                function.extensionReceiverParameter?.type?.unwrap()?.substitute(substitutor)
        )
    }

    internal class SignatureAndDescriptor(val signature: String, val descriptor: FunctionDescriptor) {
        override fun equals(other: Any?): Boolean =
            this === other || signature == (other as SignatureAndDescriptor).signature

        override fun hashCode(): Int = signature.hashCode()
    }
}