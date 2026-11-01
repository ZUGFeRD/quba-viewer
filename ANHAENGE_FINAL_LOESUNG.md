# Anhänge in ZUGFeRD-PDFs anzeigen — Finale Lösung

## Überblick

ZUGFeRD/XRechnung-PDFs können Anhänge enthalten (z. B. Aufmaß als PNG, Rapport als PDF).
Diese Anhänge sind als eingebettete Dateien im PDF gespeichert und werden in der
HTML-Visualisierung als „Öffnen"-Links angezeigt. Ziel war: Klick auf den Link →
Anhang öffnet sich in einem Popup-Fenster innerhalb der App.

---

## Architektur der Lösung

### Beteiligte Dateien

| Datei | Aufgabe |
|---|---|
| `VisualsSectionState.kt` | Lädt PDF, ruft Extraktion und HTML-Nachbearbeitung auf |
| `Pdf.desktop.kt` | Extrahiert Anhänge aus PDF, bereitet HTML auf |
| `Chrome.desktop.kt` | Fängt Klicks im Browser ab, öffnet Popup-Fenster |

---

## Vollständiger Ablauf

### 1. Benutzer öffnet eine PDF-Datei

`VisualsSectionState.loadFileInTab()` wird aufgerufen:

```
PDF-Datei
    │
    ├─► getXmlFromPdf()              → ZUGFeRD-XML aus PDF extrahieren
    ├─► getHtmlVisualizationFromXML() → HTML via XSLT (Mustangproject) erzeugen
    ├─► getAttachmentsFromPdf()       → Anhänge (Name + Bytes) aus PDF extrahieren
    └─► postProcessHtmlForAttachments() → HTML nachbearbeiten
```

### 2. Anhänge aus PDF extrahieren (`getAttachmentsFromPdf`)

PDFBox liest die eingebetteten Dateien aus dem PDF-Namensbaum:

```kotlin
PDDocumentNameDictionary(doc.documentCatalog).embeddedFiles
    → für jeden Eintrag: name + ef.createInputStream().readBytes()
    → Ergebnis: List<Pair<String, ByteArray>>  // z.B. [("EN16931_Elektron_Aufmass.png", ...)]
```

Die Bytes werden **nur im RAM** gehalten — kein Schreiben auf die Festplatte.

### 3. HTML-Visualisierung erzeugen

Das XSLT (`xrechnung-html.de.ids.xsl`) erzeugt für jeden Anhang zwei HTML-Elemente:

```html
<!-- BT-124: Referenz auf den Anhang (extern, #ef= Prefix) -->
<div class="boxzeile">
  <div id="BT-124">
    <a href="#ef=Aufmass.png">Aufmass.png</a>
  </div>
</div>

<!-- BT-125: Öffnen-Link + leeres Hidden-Div -->
<div class="boxzeile">
  <div id="BT-125">
    <a href="#" onclick="downloadData('13130162')">Öffnen</a>
  </div>
  <div id="13130162" filename="" mimetype="image/png" style="display:none;"></div>
</div>
```

**Problem:** Das Hidden-Div ist **leer** — bei `#ef=`-Anhängen liegen die Bytes
im PDF, nicht als Base64 im XML. Das XSLT trägt also keinen Inhalt ein.

### 4. HTML nachbearbeiten (`postProcessHtmlForAttachments`)

Jsoup verarbeitet das HTML und transformiert jeden „Öffnen"-Link:

**Schritt 1 — Dateinamen auflösen:**

Das Hidden-Div hat kein `filename`-Attribut (leer bei `#ef=`-Anhängen).
Lösung: **zwei Ebenen nach oben** im DOM (`div.parent()?.parent()`) zum
`boxtabelle`-Container, der beide Zeilen (BT-124 und BT-125) enthält.
Dort findet sich der `#ef=Aufmass.png`-Link aus BT-124.

```
boxtabelle (Großvater)
├── boxzeile → BT-124 → <a href="#ef=Aufmass.png">  ← hier steht der Dateiname
└── boxzeile → BT-125 → Hidden-Div (filename="")     ← hier suchen wir
```

**Schritt 2 — Datei zuordnen (Fallback-Strategien):**

```
1. Exakter Name-Vergleich
2. Groß-/Kleinschreibung ignorieren
3. endsWith-Match  ("Aufmass.png" → "EN16931_Elektron_Aufmass.png")
4. Index-basiert (n-ter Anhang → n-tes Div)
5. Base64-Inhalt-Vergleich (wenn Div doch Inhalt hat)
6. Einzelanhang-Fallback (wenn nur ein Anhang vorhanden)
```

**Schritt 3 — Link ersetzen:**

```kotlin
// Bytes in globaler Map registrieren
globalAttachmentData[filename] = Pair(bytes, mimeType)

// Link umschreiben
link.attr("href", "zugferd-attachment://EN16931_Elektron_Aufmass.png")
link.removeAttr("onclick")
link.removeAttr("download")
link.text("Anhang öffnen")
```

Das fertige HTML enthält jetzt:
```html
<a href="zugferd-attachment://EN16931_Elektron_Aufmass.png">Anhang öffnen</a>
```

### 5. HTML wird im CEF-Browser angezeigt

Die Visualisierungsansicht zeigt das nachbearbeitete HTML im eingebetteten
Chromium-Browser (JCEF). Der „Anhang öffnen"-Link ist jetzt sichtbar.

### 6. Benutzer klickt auf „Anhang öffnen"

CEF versucht zur URL `zugferd-attachment://EN16931_Elektron_Aufmass.png`
zu navigieren. Das löst `onBeforeBrowse` im `CefRequestHandler` aus.

### 7. Klick wird abgefangen (`onBeforeBrowse` in `Chrome.desktop.kt`)

```kotlin
if (url.startsWith("zugferd-attachment://")) {
    val filename = URLDecoder.decode(url.removePrefix("zugferd-attachment://"), "UTF-8")
    val (bytes, _) = globalAttachmentData[filename]!!

    // Temp-Datei erst JETZT anlegen (on-demand, nicht beim Laden des PDFs)
    val tempFile = File.createTempFile("zugferd_att_", "_$filename")
    tempFile.deleteOnExit()   // automatisch gelöscht beim App-Ende
    tempFile.writeBytes(bytes)

    openAttachmentInWindow(tempFile.toURI().toString(), filename)
    return true  // Navigation im Hauptbrowser abbrechen
}
```

**Warum Temp-Datei on-demand?**
- Bytes sind bereits im RAM (`globalAttachmentData`)
- Disk-I/O nur wenn der Benutzer wirklich klickt
- `deleteOnExit()` sorgt für automatische Bereinigung
- `file://`-URLs funktionieren zuverlässig in CEF für alle Dateitypen (PDF, PNG, XML …)

### 8. Popup-Fenster wird geöffnet (`openAttachmentInWindow`)

```kotlin
// Eigener CefClient pro Popup (nicht den geteilten CEF_CLIENT verwenden —
// ein zweiter Browser am selben Client rendert blank)
val popupClient = CEF_APP!!.createClient()
val popupBrowser = popupClient.createBrowser(fileUrl, CefRendering.DEFAULT, false)

val frame = JFrame(filename)          // Fenstertitel = Dateiname
frame.setSize(900, 700)
frame.contentPane.add(popupBrowser.uiComponent, BorderLayout.CENTER)

// Frame zuerst sichtbar machen → natives Fenster-Handle existiert,
// bevor CEF versucht hineinzurendern
frame.isVisible = true
frame.contentPane.validate()

// Aufräumen beim Schließen
frame.addWindowListener {
    popupBrowser.close(true)
    popupClient.dispose()
}
```

### 9. Ergebnis

Der Anhang wird im Popup-Fenster angezeigt:
- **PDF** → CEF's eingebauter PDF-Viewer
- **PNG/JPG** → CEF's Bildanzeige
- **XML** → CEF zeigt den Quelltext an

---

## Datenfluss-Diagramm

```
PDF-Datei öffnen
       │
       ▼
getAttachmentsFromPdf()
  PDFBox liest eingebettete Dateien
  → List<Pair<String, ByteArray>>  [im RAM]
       │
       ▼
postProcessHtmlForAttachments()
  Jsoup: Hidden-Divs finden
  Dateinamen auflösen (#ef= → Großvater-DOM)
  globalAttachmentData[filename] = (bytes, mimeType)
  Link: href="zugferd-attachment://filename"
       │
       ▼
HTML im CEF-Browser angezeigt
  → "Anhang öffnen" Button sichtbar
       │
  [Benutzer klickt]
       │
       ▼
onBeforeBrowse()  ← CEF fängt zugferd-attachment:// ab
  bytes aus globalAttachmentData holen
  Temp-Datei schreiben (deleteOnExit)
  → file:///tmp/zugferd_att_xxx_Aufmass.png
       │
       ▼
openAttachmentInWindow(fileUrl, filename)
  neuer CefClient + CefBrowser
  JFrame(filename) als Popup
  → Inhalt wird angezeigt
```

---

## Wichtige Design-Entscheidungen

| Entscheidung | Begründung |
|---|---|
| Bytes im RAM (`globalAttachmentData`) statt sofort auf Disk | Kein unnötiges Disk-I/O beim Laden; Temp-Datei nur bei tatsächlichem Klick |
| Temp-Datei statt `data:` URL | `data:application/pdf;base64,...` rendert blank in CEF |
| Temp-Datei statt `http://localhost:PORT/` | Lokale HTTP-Requests werden von neuen CEF-Clients blockiert |
| Eigener `CefClient` pro Popup | Zweiter Browser am geteilten `CEF_CLIENT` rendert blank |
| `frame.isVisible = true` vor `validate()` | Natives Fenster-Handle muss existieren, bevor CEF rendert |
| `zugferd-attachment://` Custom-Scheme | Kein `download`-Attribut nötig; `onBeforeBrowse` statt `onBeforeDownload`; verhindert Navigation des Hauptbrowsers |
| Großvater-DOM (`div.parent()?.parent()`) | `#ef=`-Dateiname steht in BT-124 (Geschwister-Zeile), nicht im direkten Elternteil des Hidden-Divs |
