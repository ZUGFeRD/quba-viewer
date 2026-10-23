/*
 * Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.openindex.zugferd.manager.model

import de.openindex.zugferd.manager.utils.getString
import de.openindex.zugferd.quba.generated.resources.Res
import de.openindex.zugferd.quba.generated.resources.ValidationType_OTHER
import de.openindex.zugferd.quba.generated.resources.ValidationType_PDF
import de.openindex.zugferd.quba.generated.resources.ValidationType_XML
import org.jetbrains.compose.resources.StringResource

enum class ValidationType(val title: StringResource) {
    PDF(
        title = Res.string.ValidationType_PDF,
    ),
    XML(
        title = Res.string.ValidationType_XML,
    ),
    OTHER(
        title = Res.string.ValidationType_OTHER,
    ),

    ;

    @Suppress("unused")
    suspend fun translateTitle(): String = getString(title)
}
