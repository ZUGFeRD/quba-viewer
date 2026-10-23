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

package de.openindex.zugferd.manager.gui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.quba.generated.resources.AppQuestionDialogNo
import de.openindex.zugferd.quba.generated.resources.AppQuestionDialogYes
import de.openindex.zugferd.quba.generated.resources.Res
import org.jetbrains.compose.resources.StringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QuestionDialog(
    question: String,
    acceptText: String = "👍",
    cancelText: String = "👎",
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) = BasicAlertDialog(
    onDismissRequest = onCancel,
    modifier = modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = AlertDialogDefaults.TonalElevation,
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
        ) {
            Text(
                text = question,
            )
            Spacer(
                modifier = Modifier
                    .height(24.dp),
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                TextButton(
                    onClick = { onAccept() },
                ) {
                    Text(text = acceptText)
                }

                TextButton(
                    onClick = { onCancel() },
                ) {
                    Text(text = cancelText)
                }
            }
        }
    }
}

@Composable
fun QuestionDialog(
    question: StringResource,
    acceptText: StringResource = Res.string.AppQuestionDialogYes,
    cancelText: StringResource = Res.string.AppQuestionDialogNo,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) = QuestionDialog(
    question = stringResource(question),
    acceptText = stringResource(acceptText),
    cancelText = stringResource(cancelText),
    onAccept = onAccept,
    onCancel = onCancel,
    modifier = modifier,
)
