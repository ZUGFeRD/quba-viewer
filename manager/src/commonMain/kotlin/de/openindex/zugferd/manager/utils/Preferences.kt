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

package de.openindex.zugferd.manager.utils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import io.github.vinceglb.filekit.core.PlatformDirectory
import kotlinx.serialization.Serializable

class Preferences(data: PreferencesData) {

    //
    // Language.
    //

    private var _language = mutableStateOf(
        Language.getByCode(data.language) ?: getCurrentLanguage()
    )
    val language get() = _language.value
    fun setLanguage(language: Language) {
        _language.value = language
        setCurrentLanguage(
            language = _language.value,
            country = _country.value,
        )
    }


    //
    // Country.
    //

    private var _country = mutableStateOf(
        if (data.country != null && isValidCountryCode(data.country))
            data.country
        else
            null
    )
    val country get() = _country.value
    fun setCountry(country: String?) {
        _country.value = if (country != null && isValidCountryCode(country)) {
            country
        } else {
            null
        }
        setCurrentLanguage(
            language = _language.value,
            country = _country.value,
        )
    }


    //
    // Currency.
    //

    private var _currency = mutableStateOf(
        if (data.currency != null && isValidCurrencyCode(data.currency))
            data.currency
        else
            null
    )
    val currency get() = _currency.value
    fun setCurrency(currency: String?) {
        _currency.value = if (currency != null && isValidCurrencyCode(currency)) {
            currency
        } else {
            null
        }
    }


    //
    // VAT percentage.
    //

    private var _vatPercentage = mutableStateOf(
        if (data.vatPercentage != null && data.vatPercentage > 0)
            data.vatPercentage
        else
            null
    )
    val vatPercentage get() = _vatPercentage.value
    fun setVatPercentage(vatPercentage: Double?) {
        _vatPercentage.value = if (vatPercentage != null && vatPercentage > 0) {
            vatPercentage
        } else {
            0.toDouble()
        }
    }


    //
    // Color theme.
    //

    private var _darkMode = mutableStateOf(data.darkMode)
    val darkMode get() = _darkMode.value
    val isThemeDark get() = _darkMode.value == true
    val isThemeLight get() = _darkMode.value == false
    val isThemeAuto get() = _darkMode.value == null
    fun setDarkMode(darkMode: Boolean?) {
        _darkMode.value = darkMode
    }


    //
    // Window size.
    //

    private var _windowWidth = data.windowWidth
    private var _windowHeight = data.windowHeight
    val windowSize: DpSize
        get() = DpSize(
            width = (_windowWidth ?: 800).dp,
            height = (_windowHeight ?: 600).dp,
        )

    fun setWindowSize(width: Int?, height: Int?) {
        _windowWidth = width
        _windowHeight = height
    }


    //
    // Window position.
    //

    private var _windowX = data.windowX
    private var _windowY = data.windowY
    val windowPosition: WindowPosition
        get() {
            val x = _windowX
            val y = _windowY

            if (x == null || y == null) {
                return WindowPosition.Aligned(Alignment.Center)
            }

            return WindowPosition.Absolute(
                x = x.dp,
                y = y.dp,
            )
        }

    fun setWindowPosition(x: Int?, y: Int?) {
        _windowX = x
        _windowY = y
    }


    //
    // Previous directory used to open a PDF file.
    //

    private var _previousPdfLocation = data.previousPdfLocation
    val previousPdfLocation get() = _previousPdfLocation
    fun setPreviousPdfLocation(directory: PlatformDirectory?) {
        _previousPdfLocation = directory?.path
    }


    //
    // Previous directory used to save a PDF file.
    //

    private var _previousExportLocation = data.previousExportLocation
    val previousExportLocation get() = _previousExportLocation
    fun setPreviousExportLocation(directory: PlatformDirectory?) {
        _previousExportLocation = directory?.path
    }


    //
    // Previous sender used for invoices.
    //

    private var _previousSenderKey = data.previousSenderKey
    val previousSenderKey get() = _previousSenderKey
    fun setPreviousSenderKey(senderKey: UInt?) {
        _previousSenderKey = senderKey
    }


    //
    // Previous first product used for invoices.
    //

    private var _previousProductKey = data.previousProductKey
    val previousProductKey get() = _previousProductKey
    fun setPreviousProductKey(productKey: UInt?) {
        _previousProductKey = productKey
    }


    //
    // Remove attachments on invoice creation.
    //

    private var _autoRemoveAttachments = mutableStateOf(data.autoRemoveAttachments)
    val autoRemoveAttachments get() = _autoRemoveAttachments.value
    fun setAutoRemoveAttachments(enabled: Boolean) {
        _autoRemoveAttachments.value = enabled
    }

    //
    // Enable GPU acceleration in Chrome.
    //

    private var _chromeGpuEnabled = mutableStateOf(data.chromeGpuEnabled)
    val chromeGpuEnabled get() = _chromeGpuEnabled.value
    fun setChromeGpuEnabled(enabled: Boolean) {
        _chromeGpuEnabled.value = enabled
    }


    //
    // Open visualisation tabs (session restore).
    //

    private var _openTabPaths = data.openTabPaths
    private var _openTabSelectedIndex = data.openTabSelectedIndex
    val openTabPaths: List<String> get() = _openTabPaths
    val openTabSelectedIndex: Int get() = _openTabSelectedIndex
    fun setOpenTabs(paths: List<String>, selectedIndex: Int) {
        _openTabPaths = paths
        _openTabSelectedIndex = selectedIndex
    }


    @Suppress("MemberVisibilityCanBePrivate")
    fun toData(): PreferencesData =
        PreferencesData(
            language = language.code,
            country = country,
            currency = currency,
            vatPercentage = vatPercentage,
            darkMode = darkMode,
            windowWidth = _windowWidth,
            windowHeight = _windowHeight,
            windowX = _windowX,
            windowY = _windowY,
            previousPdfLocation = _previousPdfLocation,
            previousExportLocation = _previousExportLocation,
            previousSenderKey = _previousSenderKey,
            previousProductKey = _previousProductKey,

            autoRemoveAttachments = _autoRemoveAttachments.value,
            chromeGpuEnabled = _chromeGpuEnabled.value,
            openTabPaths = _openTabPaths,
            openTabSelectedIndex = _openTabSelectedIndex,
        )

    suspend fun save() {
        savePreferencesData(
            data = toData(),
        )
    }
}

@Serializable
data class PreferencesData(
    val version: Int = 1,
    val language: String? = null,
    val country: String? = null,
    val currency: String? = null,
    val vatPercentage: Double? = null,
    val darkMode: Boolean? = null,
    val windowWidth: Int? = null,
    val windowHeight: Int? = null,
    val windowX: Int? = null,
    val windowY: Int? = null,
    val previousPdfLocation: String? = null,
    val previousExportLocation: String? = null,
    val previousSenderKey: UInt? = null,
    val previousProductKey: UInt? = null,

    val autoRemoveAttachments: Boolean = false,
    val chromeGpuEnabled: Boolean = true,
    val openTabPaths: List<String> = emptyList(),
    val openTabSelectedIndex: Int = 0,
)

suspend fun loadPreferences(): Preferences =
    Preferences(loadPreferencesData())

expect suspend fun initPreferences(preferences: Preferences)

expect suspend fun loadPreferencesData(): PreferencesData

expect suspend fun savePreferencesData(data: PreferencesData)
