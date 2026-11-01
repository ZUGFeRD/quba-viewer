# Lösung: ZUGFeRD-Anhänge im Popup-Fenster anzeigen

## Problem

In der Visualisierungsansicht erzeugt das XSLT (`xrechnung-html.de.ids.xsl`) für BT-125-Anhänge Links der Form:

```html
<a href="#" onClick="downloadData('13130162')">Öffnen</a>
<div id="13130162" filename="" mimetype="" style="display:none;"></div>
```

Bei Anhängen, die als eingebettete PDF-Dateien gespeichert sind (BT-124 `#ef=dateiname`), ist das versteckte `<div>` **leer** — die Bytes liegen nicht als Base64 im XML, sondern als eingebettete Datei im PDF.

Ziel war: Klick auf „Öffnen" → Anhang in einem Popup-Fenster innerhalb der App anzeigen.

---

## Ursachenanalyse

### 1. Leeres `<div>` bei `#ef=`-Anhängen
Das XSLT schreibt für `#ef=`-Referenzen keinen Base64-Inhalt in das Hidden-Div. Die eigentlichen Bytes müssen per PDFBox aus dem PDF extrahiert werden.

### 2. DOM-Struktur (XSLT-Ausgabe)
```
<div class="boxtabelle boxinhalt borderSpacing">   ← Großvater-Element
  <div class="boxzeile">                           ← BT-124-Zeile
    <div id="BT-124"><a href="#ef=Aufmass.png">…</a></div>
  </div>
  <div class="boxzeile">                           ← BT-125-Zeile
    <div id="BT-125"><a onClick="downloadData('id')">Öffnen</a></div>
    <div id="id" mimetype="" filename="" style="display:none;"></div>
  </div>
</div>
```

Der `#ef=`-Link befindet sich in der **Geschwister-Zeile** (BT-124), nicht im direkten Eltern-Element des Hidden-Divs. Suche musste daher **zwei Ebenen hoch** (`div.parent()?.parent()`).

---

## Implementierung

### Schritt 1 – Anhänge aus PDF extrahieren (`Pdf.desktop.kt`)

```kotlin
actual fun getAttachmentsFromPdf(pdf: PlatformFile): List<Pair<String, ByteArray>> {
    // PDFBox: PDDocumentNameDictionary → embedded files
    // Gibt Liste von (Dateiname, Bytes) zurück
}
```

### Schritt 2 – HTML-Nachbearbeitung (`Pdf.desktop.kt`)

Funktion `postProcessHtmlForAttachments(html, attachments)`:

1. Anhänge als Temp-Dateien speichern (`zugferd_attachments_XXXX/`)
2. Alle `div[filename], div[mimetype]` im HTML suchen
3. Für jedes Div: Dateinamen über `#ef=`-Link ermitteln — **Suche im Großvater-Element** (`div.parent()?.parent()`), nicht im direkten Elternteil
4. Fallback-Strategien: exakter Name → `endsWith`-Match → Index-basiert → Base64-Inhalt → Einzelanhang
5. Link umschreiben:
   - `href` → `data:mime;base64,...`
   - `download` → Dateiname
   - `onclick` entfernen
   - Text: „Öffnen" → „Download"
6. Dateinamen in globaler Map registrieren: `globalAttachmentTempFiles[filename] = tempFile`

### Schritt 3 – `expect`/`actual`-Deklarationen (`Pdf.kt`)

```kotlin
expect fun getAttachmentsFromPdf(pdf: PlatformFile): List<Pair<String, ByteArray>>
expect fun postProcessHtmlForAttachments(html: String, attachments: List<Pair<String, ByteArray>>): String
```

### Schritt 4 – Integration in `VisualsSectionState.kt`

```kotlin
val attachments = if (!isXml) getAttachmentsFromPdf(file) else emptyList()
val rawHtml = if (isXml) getHtmlVisualizationFromXML(filePath)?.trimToNull()
              else getHtmlVisualizationFromPdf(file)?.trimToNull()
tab.html = rawHtml?.let { postProcessHtmlForAttachments(it, attachments) }
```

### Schritt 5 – CEF-Download abfangen (`Chrome.desktop.kt`)

```kotlin
client.addDownloadHandler(object : CefDownloadHandlerAdapter() {
    override fun onBeforeDownload(
        browser, downloadItem, suggestedName, callback
    ) {
        val filename = suggestedName ?: "anhang"
        val tempFile = globalAttachmentTempFiles[filename]
        val windowUrl = if (tempFile != null && tempFile.exists())
            tempFile.toURI().toString()   // file:// statt data: (PDFs rendern sonst leer)
        else
            downloadItem?.url
        if (windowUrl != null) openAttachmentInWindow(windowUrl, filename)
        // callback.Continue() NICHT aufrufen → Download wird abgebrochen
    }
})
```

### Schritt 6 – Popup-Fenster mit eigenem CefClient (`Chrome.desktop.kt`)

```kotlin
private fun openAttachmentInWindow(url: String, title: String) {
    SwingUtilities.invokeLater {
        // Eigener CefClient pro Popup!
        // Den gemeinsamen CEF_CLIENT zu verwenden führt zu leerem Fensterinhalt.
        val popupClient = CEF_APP!!.createClient()
        val popupBrowser = popupClient.createBrowser(url, CefRendering.DEFAULT, false)

        val frame = JFrame(title)
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.setSize(900, 700)
        frame.setLocationRelativeTo(null)
        frame.contentPane.add(popupBrowser.uiComponent, BorderLayout.CENTER)

        // Frame zuerst sichtbar machen, damit das native Fenster-Handle
        // existiert, bevor CEF versucht hineinzurendern.
        frame.isVisible = true
        frame.contentPane.validate()

        // Aufräumen beim Schließen
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosed(e: WindowEvent?) {
                popupBrowser.close(true)
                popupClient.dispose()
            }
        })
    }
}
```

---

## Kritische Erkenntnisse

| Problem | Ursache | Fix |
|---|---|---|
| „Öffnen" tut nichts | `downloadData()` öffnet Blank-Popup (blockiert) | Link per Jsoup auf `data:` umschreiben |
| Leerer Div für `#ef=`-Anhänge | Bytes sind im PDF, nicht im XML | PDFBox-Extraktion + globale Map |
| `#ef=`-Link nicht gefunden | Suche nur im direkten Elternteil | `div.parent()?.parent()` (Großvater = `boxtabelle`) |
| Download statt Anzeige | CEF leitet `data:`+`download`-Attribut an DownloadHandler | `onBeforeDownload` abfangen, `Continue()` nicht aufrufen |
| PDF rendert leer in Popup | `data:application/pdf` rendert blank in CEF | `file://`-Temp-Pfad aus `globalAttachmentTempFiles` verwenden |
| Endlosschleife (unendliche Fenster) | `onBeforeBrowse` intercepted `data:` → neues Fenster → wieder `onBeforeBrowse` | `data:` in `onBeforeBrowse` durchlassen (`return false`) |
| Popup-Fenster leer (weißer Inhalt) | Zweiter Browser am selben `CefClient` rendert nicht | Pro Popup eigenen `CefClient` via `CEF_APP!!.createClient()` erstellen |

---

## Geänderte Dateien

- `manager/src/desktopMain/kotlin/…/utils/Pdf.desktop.kt` — `getAttachmentsFromPdf`, `postProcessHtmlForAttachments`
- `manager/src/commonMain/kotlin/…/utils/Pdf.kt` — `expect`-Deklarationen
- `manager/src/commonMain/kotlin/…/sections/VisualsSectionState.kt` — Integration
- `manager/src/desktopMain/kotlin/…/utils/Chrome.desktop.kt` — Download-Handler, `openAttachmentInWindow`
