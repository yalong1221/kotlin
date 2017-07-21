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
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintPosition
import org.jetbrains.kotlin.resolve.calls.inference.model.IncorporationConstraintPosition
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintError
import org.jetbrains.kotlin.resolve.calls.model.CompletedKotlinCall

fun handleDiagnostics(c: Context, completedCall: CompletedKotlinCall) {
    val positionErrors = groupErrorsByPosition(c, completedCall)
    for ((initialPosition, incorporationPositions) in positionErrors) {
        for (incorporationPosition in incorporationPositions) {
            val lower = incorporationPosition.initialConstraint.a
            val upper = incorporationPosition.initialConstraint.b
            if (c.canBeProper(lower) && c.canBeProper(upper)) continue


        }
    }
}

fun groupErrorsByPosition(c: Context, completedCall: CompletedKotlinCall): Map<ConstraintPosition, List<IncorporationConstraintPosition>> {
    return completedCall.resolutionStatus.diagnostics
            .distinct()
            .filterIsInstance<NewConstraintError>()
            .groupBy({ it.position.from }) { it.position }
}

private fun List<NewConstraintError>.distinct(): List<NewConstraintError> {
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