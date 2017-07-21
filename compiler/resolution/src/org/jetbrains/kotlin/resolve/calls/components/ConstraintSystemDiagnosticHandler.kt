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

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.resolve.calls.components.KotlinCallCompleter.Context
import org.jetbrains.kotlin.resolve.calls.inference.model.*
import org.jetbrains.kotlin.resolve.calls.model.CompletedKotlinCall
import org.jetbrains.kotlin.resolve.calls.tower.ResolutionCandidateApplicability

fun handleDiagnostics(c: Context, completedCall: CompletedKotlinCall) {
    val positionErrors = groupErrorsByPosition(c, completedCall)
    for ((position, incorporationPositionsWithTypeVariables) in positionErrors) {
        val variablesWithConstraints = incorporationPositionsWithTypeVariables.mapNotNull { (_, typeVariable) ->
            if (typeVariable == null) return@mapNotNull null
            if (c.canBeProper(typeVariable.defaultType)) return@mapNotNull null

            c.variableConstraints(typeVariable)
        }.distinctBy { it.typeVariable }

        if (variablesWithConstraints.isEmpty()) continue

        // Each position can refer to the same type variables, we'll fix it later
        // Also, probably it's enough to show error only about one type parameter
        for (variableWithConstraint in variablesWithConstraints) {
            c.addError(AggregatedConstraintError(position, variableWithConstraint.typeVariable, divideByConstraints(variableWithConstraint)))
        }
    }
}

data class SortedConstraints(val upper: List<Constraint>, val equality: List<Constraint>, val lower: List<Constraint>)

private fun divideByConstraints(variableWithConstraints: VariableWithConstraints): SortedConstraints {
    return with(variableWithConstraints.constraints) {
        SortedConstraints(getWith(ConstraintKind.UPPER), getWith(ConstraintKind.EQUALITY), getWith(ConstraintKind.LOWER))
    }
}

private fun List<Constraint>.getWith(kind: ConstraintKind) = filter { it.kind == kind }

private fun groupErrorsByPosition(c: Context, completedCall: CompletedKotlinCall): Map<ConstraintPosition, List<PositionWithTypeVariable>> {
    return completedCall.resolutionStatus.diagnostics
            .filterIsInstance<NewConstraintError>()
            .filter { it.candidateApplicability != ResolutionCandidateApplicability.INAPPLICABLE_WRONG_RECEIVER }
            .distinctErrors()
            .groupBy({ it.position.from }) { PositionWithTypeVariable(it.position, it.typeVariable) }
}

private fun List<NewConstraintError>.distinctErrors(): List<NewConstraintError> {
    val distinctDiagnostics = mutableListOf<NewConstraintError>()
    for (diagnostic in this) {
        val added = distinctDiagnostics.any { otherDiagnostic ->
            diagnostic.lowerType == otherDiagnostic.lowerType &&
            diagnostic.upperType == otherDiagnostic.upperType &&
            diagnostic.position == otherDiagnostic.position &&
            diagnostic.candidateApplicability == otherDiagnostic.candidateApplicability
        }

        if (!added) distinctDiagnostics.add(diagnostic)
    }

    return distinctDiagnostics
}

private data class PositionWithTypeVariable(val position: IncorporationConstraintPosition, val typeVariable: NewTypeVariable?)