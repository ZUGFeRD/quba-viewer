# Quba — Geplant für das nächste Release

Funktionen, die bewusst aus Quba 2.0 herausgenommen wurden und im nächsten Release umgesetzt werden sollen.

---

## Globale Suche / Command Bar („Search or jump to…", ⌘K)

**Status:** Aus Release 2.0 entfernt (war nur ein Platzhalter ohne Funktion).

**Ziel**
Ein zentrales Suchfeld in der Titelleiste, über das man per Tastatur schnell suchen und navigieren kann — ähnlich der Command-Palette in VS Code oder GitHub.

**Position & Design (aus dem Redesign)**
- Mittig in der `QubaTitleBar` (3-Spalten-Layout: links Bereichstitel, Mitte Suche, rechts Aktionen)
- Breite 320 dp, Höhe 24 dp, Eckenradius 6 dp
- Hintergrund `colors.surface`, Rahmen 1 dp `colors.border`, Innenabstand horizontal 10 dp
- Platzhaltertext „Search or jump to…" in `typo.small`, Farbe `colors.text4` (muss übersetzt werden)
- Rechts ein `KbdChip` mit dem Tastenkürzel (`KbdChip` ist in `QubaTitleBar.kt` weiterhin vorhanden)

**Offene Anforderungen für die Umsetzung**
- Tastenkürzel plattformabhängig: `⌘K` auf macOS, `Strg+K` auf Windows/Linux (Anzeige im `KbdChip` entsprechend anpassen)
- Klick auf das Feld oder Tastenkürzel öffnet ein Overlay mit Eingabe und Trefferliste, ESC schließt
- Mögliche Suchziele:
  - Bereiche anspringen (Visualisieren, Prüfen, Erzeugen, Einstellungen)
  - Geöffnete Tabs in Visualisieren / Prüfen
  - Zuletzt geöffnete Dateien
  - Gespeicherte Kontakte und Rechnungsposten
  - Aktionen (z. B. „Datei öffnen", „Theme wechseln", „Validierungsbericht exportieren")
- Abgrenzung zur bestehenden Inhaltssuche (Strg+F in Visualisieren/Prüfen): Strg+F bleibt die Suche im Dokument, ⌘K/Strg+K ist die globale Navigation
- Tastaturkürzel muss auch funktionieren, wenn der JCEF-Viewer (PDF/HTML) den Fokus hat — gleiches Routing wie bei Strg+F verwenden
- Platzhaltertext und Treffer-Kategorien in allen unterstützten Sprachen übersetzen

**Ursprünglicher Platzhalter-Code (zum Wiederherstellen)**
Aufruf in `QubaTitleBar`, zwischen linkem und rechtem Bereich:

```kotlin
// Center — ⌘K command bar
CommandBar(modifier = Modifier.width(320.dp))
```

Composable:

```kotlin
@Composable
private fun CommandBar(modifier: Modifier = Modifier) {
    val colors = LocalQubaColors.current
    val typo = LocalQubaTypography.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp),
    ) {
        Text(
            text = "Search or jump to…",
            style = typo.small.copy(color = colors.text4),
            modifier = Modifier.weight(1f),
        )
        // ⌘K kbd
        KbdChip(text = "⌘K")
    }
}
```

**Betroffene Datei:** `manager/src/commonMain/kotlin/de/openindex/zugferd/manager/gui/QubaTitleBar.kt`

---

## Tab-Kontextmenü: „In Erzeugen öffnen“

**Status:** Offen.

**Beobachtung**
Im Kontextmenü eines Dokument-Tabs (Rechtsklick) in Visualisieren gibt es „In Prüfen öffnen“, aber keine Option, das Dokument im Bereich Erzeugen zu öffnen.

**Ziel**
- Eintrag „In Erzeugen öffnen“ im Tab-Kontextmenü ergänzen
- Analog in Prüfen jeweils die anderen Bereiche als Ziel anbieten (Visualisieren, Erzeugen)

---

## Erzeugen: Geöffnetes Dokument schließen / Tab-Leiste

**Status:** Offen.

**Beobachtung**
Wird in Erzeugen ein Dokument geöffnet, kann man es nicht wieder schließen und nicht zum leeren Ausgangszustand zurückkehren.

**Ziel**
- Schließen-Möglichkeit für das geöffnete Dokument
- Prüfen, ob eine Tab-Leiste analog zu Visualisieren / Prüfen (Schließen-X, „+“, Kontextmenü) technisch und logisch sinnvoll ist

**Offene Fragen**
- Mehrere Rechnungen gleichzeitig im Editor (echte Tabs, inkl. Behandlung ungespeicherter Änderungen pro Tab) oder nur ein Dokument mit Schließen-Funktion?

---

## Split-Ansicht: Zurücksetzen auf 50/50

**Status:** Offen.

**Beobachtung**
In der geteilten Ansicht (HTML/XML links, PDF rechts) lässt sich die Trennlinie manuell verschieben, danach gibt es aber keine Möglichkeit, zur Standardaufteilung 50/50 zurückzukehren.

**Ziel**
- Erneuter Klick auf den aktiven Split-Button oben rechts setzt die Aufteilung auf 50/50 zurück
- Optional zusätzlich: Doppelklick auf die Trennlinie setzt ebenfalls auf 50/50 zurück

---

## Anhänge: Nur exakte Dateinamen zuordnen

**Status:** Entscheidung offen. Umgesetzt ist derzeit die Zuordnung über Namensähnlichkeit (Commit `edbbd10`).

**Hintergrund**
Viele Rechnungen verweisen auf Anhänge, die in der PDF eingebettet sind, per `#ef=<Dateiname>` (BT-124). Laut ZUGFeRD-Standard soll dieser Name dem Namen der eingebetteten Datei entsprechen. In der Praxis weicht er oft ab, z. B. `#ef=ElektronRapport_neu-red.pdf` gegenüber der Datei `EN16931_Elektron_ElektronRapport.pdf`.

**Aktuelles Verhalten**
`matchEmbeddedFiles()` ordnet in sechs Durchgängen zu: gleicher Name, gleicher Name ohne Groß/Klein, ein Name endet auf den anderen, gemeinsame Namensteile bei gleichem Dateityp, einzige freie Datei des Typs, Reihenfolge (nur bei gleicher Anzahl und passenden Typen). Bei Gleichstand bleibt der Link offen. So öffnen alle Anhänge der Beispiel-PDFs. Restrisiko: Bei teilweise ähnlichen Namen kann ein falscher Anhang geöffnet werden.

**Alternative: strenger Abgleich**
- Ein Link wird nur mit einer eingebetteten Datei verbunden, wenn der Name exakt passt (wahlweise ohne Unterscheidung von Groß/Klein).
- Alle übrigen Links zeigen „Anhang nicht in der Datei enthalten“.
- Jede nicht zugeordnete Datei steht im Tab „Anhänge“ unter „Weitere in der PDF eingebettete Dateien“. Diese Liste gibt es schon.

**Vorteile**
- Öffnet nie einen falschen Anhang.
- Weniger Code, leichter nachzuvollziehen.

**Nachteile**
- Bei abweichendem Namen steht der Anhang nicht direkt neben seiner Kennung (BT-122), sondern nur in der Liste darunter. Ein Klick mehr, und der Nutzer muss selbst erkennen, welche Datei zu welchem Eintrag gehört.
- In den Beispieldateien betrifft das alle fünf Anhänge, die nur in der PDF stecken: `EN16931_Elektron.pdf` (2), `EN16931_Reisekostenabrechnung.pdf` (2), `EN16931_Betriebskostenabrechnung.pdf` (1). Bei keinem passt der Name exakt. Anhänge, die im XML selbst enthalten sind (z. B. alle `XRECHNUNG_*`-Dateien), sind nicht betroffen.

**Zwischenlösung**
Exakter Name plus „ein Name endet auf den anderen“ (Durchgänge 1 bis 3). Dann bleiben `#ef=Aufmass.png` und `#ef=Taxi-Nue-und-Berlin.pdf` direkt verlinkt, `ElektronRapport_neu-red.pdf`, `Hotelrechung-Immo.pdf` und `Betriebskostenabrechnung.pdf` landen in der Liste. Ganz ohne Risiko ist das nicht: `Rechnung.pdf` passt auch auf `Hotelrechnung.pdf`.

**Umsetzung (kleiner Aufwand)**
- `manager/src/desktopMain/kotlin/de/openindex/zugferd/manager/utils/InvoiceAttachments.desktop.kt`, Funktion `matchEmbeddedFiles()`: Durchgänge 3 bis 6 entfernen (für die Zwischenlösung 4 bis 6). Hinweistext, Liste der übrigen Dateien und Öffnen nach Dateityp bleiben unverändert.
- Abschnitt „Anhänge“ in `docs/dev-performance.md` und den Eintrag in `releases/RELEASE_QUBA_2.0.md` anpassen.

**Offene Frage**
- Komfort (direkter Link, kleines Fehlerrisiko) oder Sicherheit (nie falsch, ein Klick mehr)?
