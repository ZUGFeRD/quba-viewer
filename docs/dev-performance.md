# Entwicklerhinweise: Verarbeitung von Rechnungsdateien

Wie Quba eine PDF- oder XML-Rechnung einliest, als HTML darstellt und prüft, und worauf man
bei Änderungen achten muss. Stand: November 2026, Mustang 2.26.0, Saxon-HE 12.10.

## Ablauf beim Öffnen einer Datei

Visualisieren (`VisualsSectionState`) und Prüfen (`CheckSectionState`) holen alles über
`InvoiceDocuments` (`commonMain/.../utils/InvoiceDocuments.kt`):

| Aufruf | macht | Datei |
|---|---|---|
| `InvoiceDocuments.source(file)` | liest die Datei einmal: XML, formatiertes XML, PDF-Anhänge | `InvoiceSource.desktop.kt` |
| `InvoiceDocuments.html(file)` | XSLT nach HTML, danach ein Jsoup-Durchgang | `InvoiceHtml.desktop.kt` |
| `InvoiceDocuments.validation(file)` | Mustang-Validator, inklusive XML-Bericht für den Export | `Validation.desktop.kt` |

`InvoiceDocuments` merkt sich die Ergebnisse der letzten 6 Dateien. Schlüssel ist Pfad +
Größe + Änderungszeit (`PlatformFile.contentKey()`), eine auf der Platte geänderte Datei wird
also neu gelesen. Kommt eine zweite Anfrage, während die erste noch läuft (z. B. dieselbe Datei
in beiden Bereichen), wartet sie auf dasselbe Ergebnis. Die Arbeit läuft in einem eigenen
Scope, damit ein Bereichswechsel sie nicht abbricht. XSLT und Validierung laufen höchstens
`max(2, Kerne / 2)`-mal gleichzeitig, das Einlesen höchstens 3-mal.

Die beiden Section-States starten das Laden in einem eigenen `CoroutineScope` auf
`Dispatchers.Main`, nicht im Composable-Scope. Jeder Tab hat einen `loadJob`, der beim
Schließen abgebrochen wird. `addTabWithFile` kehrt sofort zurück.

`restoreSession` legt beim Start nur die Tabs an. Geladen wird im Hintergrund, der aktive Tab
zuerst. Deshalb steht es in `main.kt` nicht mehr in `runBlocking`.

## Einlesen (`InvoiceSource.desktop.kt`)

- PDF oder XML wird am Inhalt erkannt (`%PDF-` in den ersten 1024 Bytes), nicht an der Endung.
- Eine PDF wird genau einmal mit `Loader.loadPDF(File)` geöffnet. Dabei kommen Rechnungs-XML
  und alle Anhänge heraus. Früher wurde die PDF 4- bis 5-mal geladen, und `ZUGFeRDImporter` hat
  die ganze Rechnung zusätzlich in Java-Objekte umgerechnet (inklusive Summenprüfung), nur um
  das XML zu bekommen.
- Rechnungs-XML wird über dieselben Dateinamen gefunden wie bei Mustang (`factur-x.xml`,
  `zugferd-invoice.xml`, `xrechnung.xml`, `order-x.xml`, `cida.xml`). Fehlt so ein Name, wird
  eine andere eingebettete `.xml` mit Rechnungsinhalt genommen.
- Eingebettete Dateien in verschachtelten Name-Tree-Knoten werden mitgelesen.
- Bei `IOException` (Datei nach Drag & Drop noch gesperrt) gibt es bis zu 5 Versuche mit
  wachsender Pause.
- Formatiert wird nur XML, das noch nicht eingerückt ist (`isAlreadyIndented`). Der dom4j-Umweg
  kostet bei großen Dateien viel Zeit und Speicher.

## HTML (`InvoiceHtml.desktop.kt`)

`InvoiceHtmlRenderer` macht dasselbe wie `ZUGFeRDVisualizer.visualize(…, Language.DE)`, aber:

- Jedes Stylesheet wird einmal pro Programmlauf kompiliert. `main.kt` stößt die
  Kompilierung von `cii-xr.xsl` und `xrechnung-html.de.xsl` beim Start in einem
  Hintergrund-Thread an (`InvoiceHtmlRenderer.warmUp()`).
  Mustang kompiliert bei jeder neuen Visualizer-Instanz drei Stylesheets, und auch eine
  wiederverwendete Instanz kompiliert das HTML-Stylesheet bei jedem Aufruf neu (das Argument
  von `putIfAbsent` wird immer ausgewertet).
- Das Wurzelelement wird per StAX gelesen statt per DOM-Parse der ganzen Datei.
- Keine Temp-Dateien, kein Kopier-Thread.

Danach folgt ein einziger Jsoup-Durchgang (`postProcessInvoiceHtml`): eigenes CSS, BT-Nummern
in den Beschriftungen, Anhänge.

## Anhänge (`InvoiceAttachments.desktop.kt`, `Chrome.desktop.kt`)

Mustang erzeugt pro Anhang ein verstecktes `<div id=… filename=… mimeType=…>BASE64</div>` und
einen Link `onclick="downloadData(id)"`. Das Skript öffnet ein Popup, das Quba blockiert. Ein
Link, den `linkAttachments()` nicht umschreibt, springt deshalb nur an den Seitenanfang.

1. Enthält das `div` Base64-Daten (Anhang steckt im XML), sind das die Daten des Anhangs.
2. Sonst ist der Anhang eine in der PDF eingebettete Datei, meist per `#ef=<name>` (BT-124)
   referenziert. Der Name im Verweis weicht oft vom echten Dateinamen ab
   (`ElektronRapport_neu-red.pdf` gegenüber `EN16931_Elektron_ElektronRapport.pdf`,
   `Hotelrechung-Immo.pdf` gegenüber `…_Hotelrechnung-Immo.pdf`). `matchEmbeddedFiles()`
   ordnet alle offenen Links und alle eingebetteten Dateien gemeinsam zu, jede Datei höchstens
   einmal, in Durchgängen von streng nach locker: gleicher Name, gleicher Name ohne
   Groß/Klein, ein Name endet auf den anderen, gleicher Dateityp mit den meisten gemeinsamen
   Namensteilen (Gleichstand bleibt offen), einzige freie Datei dieses Typs, zuletzt die
   Reihenfolge, aber nur bei gleicher Anzahl und passenden Typen.
3. Was dann noch ohne Daten ist, bekommt statt des toten Links den Text
   „Anhang nicht in der Datei enthalten“.
4. Eingebettete Dateien, auf die kein Link zeigt und deren Inhalt nicht schon verlinkt ist,
   erscheinen im Tab „Anhänge“ (`#anlagen`) unter „Weitere in der PDF eingebettete Dateien“.

Jeder Anhang wird über `registerAttachment()` abgelegt, der Link zeigt auf
`zugferd-attachment://<schlüssel>`, der Base64-Text verschwindet aus dem `div`. Bei
XRechnung_Elektron schrumpft das HTML so von 1,3 MB auf 83 KB. Der Schlüssel enthält einen Hash
des Inhalts, damit gleichnamige Anhänge aus zwei Rechnungen sich nicht überschreiben.

Beim Klick (`openAttachment()`) schreibt Quba die Datei unter ihrem Namen in einen eigenen
Temp-Ordner. Fehlt die Endung, kommt sie aus dem MIME-Typ (`withExtension()`). PDF, Bilder
(PNG, JPEG, GIF, BMP, WebP, SVG), Text, XML und HTML öffnen im Anhangsfenster (eigener
`CefClient`). Alles andere geht an `java.awt.Desktop.open()`, also an das Programm, das das
Betriebssystem für die Endung eingestellt hat. Schlägt das fehl, erscheint ein Speichern-Dialog.

Geprüft mit allen Dateien in `samples/XML-Rechnung`: alle Anhänge der 6 PDFs mit Anhängen werden
zugeordnet. „Nicht enthalten“ erscheint nur bei XML-Dateien, die auf Dateien einer PDF
verweisen, und bei Verweisen auf eine externe URL.

## Patch für Mustangs `*-xr.xsl` (`XrSourcePathPatch`)

**Problem:** `cii-xr.xsl`, `ubl-invoice-xr.xsl`, `ubl-creditnote-xr.xsl` und `cio-xr.xsl`
schreiben für rund 200 Felder einen Quellpfad (`xr:src-path`). Auf jeder Ebene zählt das
Template alle vorherigen Geschwister mit gleichem Namen:

```xml
<xsl:value-of select="count(preceding-sibling::*[name(.) = name(current())]) + 1"/>
```

Für jedes Feld in Position 2000 werden also 2000 Positionen durchgezählt. Die Laufzeit wächst
quadratisch: 618 Positionen 2,4 s, 1.236 Positionen 6,2 s, 2.472 Positionen 22 s (nur
dieser Schritt).

**Lösung:** Beim Kompilieren wird der Stylesheet-Text zur Laufzeit geändert, Mustangs JAR
bleibt unverändert. Eine globale Variable berechnet alle Geschwisterpositionen in einem
Durchgang als XPath-3.1-Map (Schlüssel `generate-id()`), das Template schlägt nur noch nach.
Die erzeugten Pfade bleiben gleich. Neue Laufzeit: 0,9 s / 1,8 s / 3,4 s für den ganzen
Weg bis zum HTML.

**Absicherung:** Der Patch greift nur, wenn das Template per Regex genau einmal gefunden wird.
Sonst, oder wenn das geänderte Stylesheet nicht kompiliert, nutzt Quba das Original und
schreibt eine Warnung ins Log.

### Nach jedem Mustang-Update prüfen

1. App starten, eine CII-Rechnung öffnen, im Log nach dieser Zeile suchen:
   `XSLT 'cii-xr.xsl' kompiliert in … ms (optimiert: true)`.
   Steht dort `optimiert: false` oder `Optimierung nicht anwendbar`, hat Mustang das Template
   geändert. Dann `XrSourcePathPatch.SIBLING_INDEX` an den neuen Text anpassen (die
   Stylesheets liegen im Mustang-`library`-JAR unter `stylesheets/`).
2. Ausgabe vergleichen: für alle Dateien in `samples/XML-Rechnung/CII` und `UBL`
   `InvoiceHtmlRenderer.render(bytes)` gegen
   `ZUGFeRDVisualizer().visualize(pfad, Language.DE)` laufen lassen. Beide Strings müssen
   gleich sein. Beim Umbau im November 2026 waren es 58 von 58.
3. Prüfen, ob Mustang inzwischen eine Sprachoption oder andere Stylesheets nutzt, die
   `InvoiceHtmlRenderer` nachbilden müsste.

## XML-Ansicht (`gui/XmlViewer.kt`)

- `LazyColumn` mit einer Zeile pro XML-Zeile. Zeilen über 2.000 Zeichen (Base64-Anhänge)
  werden in mehrere Zeilen aufgeteilt, die Zeilennummer steht nur an der ersten.
- Der Zeilenindex besteht aus `IntArray`s. Bis 300.000 Zeichen wird er sofort berechnet,
  darüber im Hintergrund.
- Eingefärbt wird nur, was sichtbar ist, jede Zeile für sich. Kommentarbereiche werden vorab
  gesucht, damit mehrzeilige Kommentare grau bleiben.
- Tabs werden als 4 Leerzeichen angezeigt. Geist Mono hat kein Zeichen für Tab, Compose zeigt
  sonst Kästchen. Die Suchtreffer werden auf die verschobenen Positionen umgerechnet.
- Grenze: Die Textauswahl umfasst nur die geladenen Zeilen. Dafür gibt es den Knopf
  „gesamtes XML kopieren“.

## Messwerte (November 2026)

Gemessen mit einem temporären `main` gegen den alten Ablauf (nachgebaut aus der vorherigen
Version), Median aus 3 Läufen bei warmer JVM:

| Datei | vorher | jetzt |
|---|---|---|
| `CII/EN16931_Einfach.cii.xml` (13 KB) | 1.580 ms | 51 ms |
| `CII/XRECHNUNG_Elektron.cii.xml` (1,3 MB) | 1.638 ms | 121 ms |
| `UBL/XRECHNUNG_Reisekostenabrechnung.ubl.xml` (1 MB) | 2.850 ms | 96 ms |
| `FX/EN16931_Elektron.pdf` (1 MB) | 1.501 ms | 32 ms |
| `FX/XRECHNUNG_Elektron.pdf` (2 MB) | 1.780 ms | 138 ms |
| `big.xml` (3,1 MB, 1.236 Positionen) | 10.639 ms | 2.677 ms |
| PDF aus `big.xml` (5 MB) | 16.804 ms | 2.647 ms |
| `big.xml` verdoppelt (6,4 MB, 2.472 Positionen) | 29.131 ms | 5.237 ms |

Nicht gemessen: Validierung (unverändert bis auf das Zusammenlegen doppelter Aufrufe) und die
Ladezeit der HTML-Seite im eingebetteten Chromium.
