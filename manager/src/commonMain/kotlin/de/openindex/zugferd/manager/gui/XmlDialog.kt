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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.openindex.zugferd.manager.utils.pluralStringResource
import de.openindex.zugferd.manager.utils.stringResource
import de.openindex.zugferd.manager.utils.title
import de.openindex.zugferd.quba.generated.resources.AppXmlDialogClose
import de.openindex.zugferd.quba.generated.resources.AppXmlDialogTitle
import de.openindex.zugferd.quba.generated.resources.Res
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

@Composable
fun XmlDialog(
    title: String = stringResource(Res.string.AppXmlDialogTitle).title(),
    xml: String,
    onDismissRequest: () -> Unit,
) {
    //val appState = LocalAppState.current
    //appState.setLocked(true)

    DialogWindow(
        onCloseRequest = {
            onDismissRequest()
            //appState.setLocked(false)
        },
        title = title,
        width = 700.dp,
        height = 600.dp,
    ) {
        XmlDialogContent(
            //title = title,
            title = null,
            xml = xml,
            onDismissRequest = {
                onDismissRequest()
                //appState.setLocked(false)
            },
        )
    }

    /*
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        XmlDialogContent(
            title = title,
            xml = xml,
            onDismissRequest = onDismissRequest,
        )
    }
    */
}

@Composable
@Suppress("unused")
fun XmlDialog(
    title: StringResource = Res.string.AppXmlDialogTitle,
    xml: String,
    onDismissRequest: () -> Unit,
) = XmlDialog(
    title = stringResource(title).title(),
    xml = xml,
    onDismissRequest = onDismissRequest,
)

@Composable
@Suppress("unused")
fun XmlDialog(
    title: PluralStringResource,
    titleQuantity: Int,
    xml: String,
    onDismissRequest: () -> Unit,
) = XmlDialog(
    title = pluralStringResource(title, titleQuantity).title(),
    xml = xml,
    onDismissRequest = onDismissRequest,
)

/**
 * Contents of the XML dialog.
 */
@Composable
@Suppress("SameParameterValue")
private fun XmlDialogContent(
    title: String?,
    xml: String,
    onDismissRequest: () -> Unit,
) {
    Box(
        modifier = Modifier
            .border(border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.primaryContainer))
            .shadow(elevation = 12.dp)
            //.fillMaxWidth(),
            .fillMaxWidth(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                // Dialog title, if available.
                if (title != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleLarge,
                            softWrap = false,
                        )
                    }
                }

                // XML viewer.
                XmlViewer(
                    xml = xml.trim(),
                    modifier = Modifier
                        .weight(1f, fill = true),
                )

                //Spacer(Modifier.weight(1f, true))

                // Bottom row.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f, true),
                    )

                    // Close button.
                    Button(
                        onClick = { onDismissRequest() },
                    ) {
                        Label(
                            text = Res.string.AppXmlDialogClose,
                        )
                    }
                }
            }
        }
    }
}
