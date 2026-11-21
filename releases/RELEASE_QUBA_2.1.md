# Quba 2.1: Was ist neu?

> Stand: 21. November 2026. Veröffentlichung geplant für Anfang/Mitte Dezember 2026. Bis dahin kommt noch die neue Extended-Visualisierung aus Mustang dazu (#177).

Quba 2.1 baut auf 2.0 auf und enthält alle Änderungen und Fehlerbehebungen bis zum 21. November 2026. Die meisten betreffen die Geschwindigkeit beim Öffnen von Rechnungen, den Umgang mit Anhängen und die Sprache der Rechnungsansicht.

---

## Neu in dieser Version

**Rechnungen öffnen deutlich schneller**
Einlesen, Umwandeln und Anzeigen von Rechnungen gehen jetzt schneller, am meisten bei großen Dateien. Messwerte mit den Beispielrechnungen, bei bereits gestarteter App:

| Datei | 2.0 | 2.1 |
|---|---|---|
| kleine XML-Rechnung (13 KB) | 1,6 s | 0,05 s |
| ZUGFeRD-PDF (1 MB) | 1,5 s | 0,03 s |
| XRechnung-PDF mit Anhängen (2 MB) | 1,8 s | 0,14 s |
| XML-Rechnung mit 1.236 Positionen (3,1 MB) | 10,6 s | 2,7 s |
| PDF mit 1.236 Positionen (5 MB) | 16,8 s | 2,6 s |

Ist dieselbe Datei in Visualisieren und Prüfen geöffnet, verarbeitet Quba sie nur einmal.

**Schneller Programmstart mit wiederhergestellten Tabs**
Das Fenster erscheint sofort, auch wenn beim letzten Mal viele oder große Dateien geöffnet waren. Der zuletzt aktive Tab lädt zuerst, die anderen folgen im Hintergrund.

**XML-Ansicht überarbeitet**
Die XML-Ansicht zeigt Zeilennummern und färbt Tags, Attribute, Werte und Kommentare ein. Die Suche (Strg+F) springt zum Treffer. Ein Knopf oben rechts kopiert das gesamte XML in die Zwischenablage. Auch XML-Dateien mit mehreren Megabyte lassen sich flüssig scrollen. Bisher fror die Ansicht bei großen Dateien ein, und ab 500 KB fielen die Farben weg.

**Lange Meldungslisten im Prüfen-Bereich**
Validierungsmeldungen erscheinen in Blöcken zu je 100, der Knopf „… weitere Mitteilungen anzeigen“ holt die nächsten. Der Export des Prüfberichts nutzt das vorhandene Ergebnis und prüft die Datei nicht noch einmal.

**Bereits geöffnete Datei**
Öffnest du eine Datei, die schon in einem Tab offen ist, wechselt Quba zu diesem Tab, statt sie ein zweites Mal zu laden.

**Anhänge jedes Dateityps öffnen**
PDF, Bilder, Text und XML öffnen im Anhangsfenster von Quba. Andere Dateitypen wie Excel, Word, CSV oder ZIP öffnet Quba mit dem Programm, das im Betriebssystem dafür eingestellt ist. Gibt es keins, erscheint „Speichern unter“.

**Alle eingebetteten Dateien sichtbar**
Dateien, die in der PDF stecken, auf die die Rechnung aber nicht verweist, stehen im Tab „Anhänge“ unter „Weitere in der PDF eingebettete Dateien“, mit Dateigröße.

**Trennlinie der geteilten Ansicht**
Die Trennlinie zwischen HTML/XML und PDF hat in der Mitte einen Griff und lässt sich auf einer breiteren Fläche fassen. Beim Überfahren und Ziehen färben sich Linie und Griff in der Akzentfarbe.

---

## Behobene Probleme

- Die Rechnung erschien in der HTML-Ansicht und im PDF-Export immer auf Deutsch, auch wenn Quba auf Englisch oder Französisch eingestellt war. Jetzt folgt sie der eingestellten Sprache (#172).
- Nach einem Sprachwechsel blieben bereits geöffnete Rechnungen in der alten Sprache, bis Quba neu gestartet wurde. Jetzt wechseln sie sofort.
- Im Rechtsklick-Menü der HTML-Ansicht blieben „Zurück“, „Drucken…“ und „Seitenquelltext anzeigen“ nach einem Sprachwechsel in der Sprache, mit der Quba gestartet wurde.
- Die Beschriftungen rund um Anhänge, etwa „Anhang öffnen“ und der Titel des Speichern-Dialogs, waren immer deutsch.
- Waren zwei Rechnungen mit gleichnamigem Anhang geöffnet, konnte „Anhang öffnen“ die Datei der anderen Rechnung zeigen.
- Weicht der Name im Verweis der Rechnung vom Namen der eingebetteten Datei ab (z. B. `#ef=ElektronRapport_neu-red.pdf` gegenüber `EN16931_Elektron_ElektronRapport.pdf`), entschied bisher die Reihenfolge, welche Datei geöffnet wird. Jetzt ordnet Quba über Namensteile und Dateityp zu.
- Ließ sich ein Anhang keiner Datei zuordnen, sprang ein Klick auf „Öffnen“ nur an den Seitenanfang. Jetzt steht dort „Anhang nicht in der Datei enthalten“.
- Wurde während des Ladens in einen anderen Bereich gewechselt, fehlte danach die HTML-Ansicht, oder der Ladekreis im Prüfen-Bereich drehte sich endlos.
- Brach die Validierung mit einem internen Fehler ab, drehte sich der Ladekreis endlos. Jetzt steht der Fehler in der Meldungsliste.
- Zwei Dateien mit gleichem Namen aus verschiedenen Ordnern galten als dieselbe Datei. Die zweite ließ sich in manchen Fällen nicht öffnen.
- Enthielt eine PDF eingebettete Rechnungsdaten, aber keine XMP-Metadaten, zeigte Quba kein XML an.
- Bei jedem Öffnen einer PDF blieb eine temporäre XML-Datei im Temp-Ordner liegen.

---

## Bekannte Einschränkungen

- In der XML-Ansicht erfasst die Textauswahl mit der Maus nur die sichtbaren Zeilen. Für das gesamte XML gibt es den Kopier-Knopf oben rechts.
