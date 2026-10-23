# Quba 2.0 — Was ist neu?

> Veröffentlicht am 17. Mai 2026

---

## Neu in dieser Version

**Mehrere Dokumente gleichzeitig öffnen**
Du kannst jetzt mehrere Rechnungen in separaten Tabs öffnen und zwischen ihnen wechseln.

**Tabs umsortieren**
Tabs lassen sich durch Ziehen in die gewünschte Reihenfolge bringen — wie in Google Chrome. Der gezogene Tab folgt dem Mauszeiger sofort, die anderen Tabs weichen animiert zur Seite aus und zeigen so live die neue Zielposition an.

**Tabs bleiben nach Neustart erhalten**
Beim nächsten App-Start werden alle zuletzt geöffneten Dokumente automatisch wiederhergestellt. Manuell geschlossene Tabs werden dabei nicht wiederhergestellt.

**Geteilte Ansicht mit Splitter**
PDF und HTML/XML lassen sich gleichzeitig nebeneinander anzeigen. Der Splitter zwischen beiden Bereichen lässt sich flüssig verschieben.

**Ansicht umschalten**
Per Schaltfläche in der Toolbar lässt sich zwischen PDF-Ansicht, HTML/XML-Ansicht und der geteilten Ansicht wechseln.

**Hinweis zu geöffneten Dokumenten**
Ein kleines Informations-Symbol in der Toolbar weist darauf hin, dass geöffnete Dokumente beim Beenden der Anwendung nicht automatisch gespeichert werden. So bleibt stets klar, dass die Tabs nur für die aktuelle Sitzung geöffnet sind.

**Hinweis bei normalen PDF-Dateien**
Wird eine PDF-Datei geöffnet, die keine eingebetteten E-Rechnungsdaten enthält, erscheint nun ein deutlicher Hinweis. So ist sofort ersichtlich, dass es sich nicht um eine ZUGFeRD-Rechnung handelt.

**Anhänge herunterladen**
In ZUGFeRD-Rechnungen eingebettete Anhänge werden in der HTML-Ansicht angezeigt und können direkt heruntergeladen werden.

**Mehrere Dokumente gleichzeitig prüfen**
Auch im Prüfen-Bereich können jetzt mehrere Dokumente in separaten Tabs geöffnet werden — wie in Visualisieren. Tabs zeigen sich im Chrome-Stil: animierte Breite, Scrollen bei Überlauf, Drag & Drop zum Umsortieren. Der „+"-Button steht direkt neben dem letzten Tab, nicht am rechten Rand. Jeder Tab zeigt links ein Status-Icon: ein Haken-Symbol wenn die Validierung erfolgreich war, ein ×-Symbol bei Fehlern, ein Ladekreis während der Prüfung läuft.

**Tab-Kontextmenü (Rechtsklick)**
Ein Rechtsklick auf einen Tab öffnet ein Menü mit folgenden Aktionen: „Schließen", „Andere Tabs schließen", „Tabs rechts schließen" sowie „In Visualisierung öffnen" (aus Prüfen) bzw. „In Prüfen öffnen" (aus Visualisieren). Das Menü erscheint direkt am angeklickten Tab und wird als natives Fenster dargestellt — es wird nicht mehr vom Dokumentinhalt (HTML/PDF) verdeckt.

**Datei per Klick öffnen — mit Mehrfachauswahl**
In allen drei Bereichen (Visualisieren, Prüfen, Erstellen) lässt sich durch einen einfachen Klick auf den leeren Bereich ein Datei-Dialog öffnen — zusätzlich zum Drag & Drop. In Visualisieren und Prüfen können dabei mehrere Dateien auf einmal ausgewählt und in separaten Tabs geöffnet werden. Der leere Bereich zeigt jetzt eine gestrichelte Drop-Zone mit Icon und Hinweistext, sodass klar ist, was zu tun ist.

**Suche im Prüfen-Bereich**
Mit Strg+F kannst du jetzt auch im Prüfen-Bereich den XML- und HTML-Inhalt durchsuchen. Das Lupen-Icon zeigt beim Hovern einen Tooltip mit den verfügbaren Tastenkombinationen (Strg+F, Enter, Esc).

**Gespeicherte Kontakte und Rechnungsposten direkt auswählen**
Im Erstellen-Bereich zeigen Absender-, Empfänger- und Rechnungsposten-Felder jetzt ein durchsuchbares Dropdown mit allen in den Einstellungen gespeicherten Einträgen. Ein Klick in das Feld genügt — ohne den `+`-Button. Der `+`-Button bleibt für neue Einträge erhalten.

**Navigation auf-/zuklappen**
Die linke Sidebar lässt sich auf Knopfdruck zuklappen (nur Icons) oder aufklappen (Icons mit Beschriftung). Der Zustand bleibt während der Sitzung erhalten.

**Theme-Schnellumschalter in der Sidebar**
Hell, Dunkel und Automatisch lassen sich jetzt direkt über die Sidebar umschalten — ohne Umweg über die Einstellungen. Im aufgeklappten Zustand erscheinen alle drei Optionen als kompakte Segmentleiste. Im zugeklappten Zustand wechselt ein einzelnes Icon per Klick zyklisch zwischen den Modi.

**Einheitliches Tab-Design in Visualisieren und Prüfen**
Beide Bereiche verwenden jetzt dasselbe Chrome-artige Tab-Design: grauer Strip-Hintergrund, weißer aktiver Tab mit leichtem Schatten, transparente inaktive Tabs. Das Erscheinungsbild ist damit konsistent und klar ablesbar.

**Ansichtswechsel vollständig übersetzt**
Die Schaltfläche zum Umschalten der Ansicht (PDF / Geteilt / Html-Xml) ist jetzt in allen unterstützten Sprachen übersetzt. Die Sidebar-Buttons „Zuklappen" und „Aufklappen" ebenfalls.

**Fehlerort im XML anzeigen**
Bei Validierungsfehlern wird jetzt der genaue Ort im XML-Dokument angezeigt.

**Validierungsbericht exportieren**
Der Prüfbericht kann als XML-Datei gespeichert werden.

---

## Behobene Probleme

- Die linke Navigation war schlecht sichtbar und wurde häufig übersehen.
- Das Eingabefeld für Ersteller, Empfänger und Rechnungsposten öffnete keinen Dialog — Pflichtfelder waren nicht erkennbar.
- In den Einstellungen gespeicherte Kontakte erschienen im Erstellen-Bereich nicht zur Auswahl.
- Fehlende Pflichtfelder beim Erstellen einer Rechnung wurden nicht hervorgehoben.
- Der „E-Rechnung erzeugen"-Button erschien nie, auch wenn alle Pflichtfelder ausgefüllt waren.
- In das Datumsfeld konnte nicht direkt getippt werden.
- Die Suchfunktion (Strg+F) in der Visualisierungsansicht war kaum zu finden.
- Strg+F öffnete die Suche immer in der Visualisierungsansicht, auch wenn man sich im Prüfen-Bereich befand. Ursache waren drei veraltete Key-Event-Handler (AWT-Dispatcher, JCEF-Keyboard-Handler, Compose-Handler), die nicht auf die aktive Section abgestimmt waren. Alle Handler wurden bereinigt und auf ein einheitliches Routing umgestellt.
- Im Ersteller/Empfänger-Dialog wurden die Buttons am unteren Rand abgeschnitten — das Fenster musste manuell vergrößert werden. Der Dialoginhalt ist jetzt scrollbar, die Buttons bleiben immer sichtbar.
- Das Prüfen-Symbol sah genauso aus wie das Such-Symbol — jetzt ist es ein Haken.
- Das Euro-Zeichen (€) war in der Währungsauswahl schwer zu finden — jetzt springt die Liste beim Tippen direkt zum passenden Eintrag.
- Beim Datumsfeld wurde ein Bleistift-Icon angezeigt — jetzt ist es ein Kalender-Icon.
- Das App-Beenden-Symbol war ein generisches ×-Icon — jetzt ist es ein Shutdown-Symbol.
- Die Theme-Auswahl-Buttons in den Einstellungen sahen immer grau und deaktiviert aus. Der aktive Zustand ist jetzt klar hervorgehoben.
- Die Kategorien „Workspace", „Kontakt" und „E-Rechnung" in den Einstellungen waren optisch nicht von normalen Menüpunkten zu unterscheiden.
- Beim Öffnen einer normalen PDF wurde der Inhalt in bestimmten Situationen nicht angezeigt — die Anzeigefläche blieb leer. Dies ist nun behoben.
- Beim Wechsel zwischen den Anzeigemodi (z. B. von „Geteilt" zu „PDF") wurde der PDF-Inhalt gelegentlich nicht mehr dargestellt. Dieser Fehler wurde behoben.

---

## Bekannte Einschränkungen

- Im Dark Mode bleibt der HTML-Anzeigebereich hell (wird in einer späteren Version behoben).
