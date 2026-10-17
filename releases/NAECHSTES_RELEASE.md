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
