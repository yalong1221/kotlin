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

package org.jetbrains.kotlin.js.translate.general

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.js.backend.ast.JsImportedModule
import org.jetbrains.kotlin.js.backend.ast.JsName
import org.jetbrains.kotlin.js.backend.ast.JsProgram
import org.jetbrains.kotlin.js.backend.ast.JsProgramFragment
import org.jetbrains.kotlin.psi.KtFile

class AstGenerationResult(
        val program: JsProgram,
        val innerModuleName: JsName,
        val fragments: List<JsProgramFragment>,
        val fragmentMap: Map<KtFile, JsProgramFragment>,
        val newFragments: List<JsProgramFragment>,
        val fileMemberScopes: Map<KtFile, List<DeclarationDescriptor>>,
        val importedModuleList: List<JsImportedModule>
)