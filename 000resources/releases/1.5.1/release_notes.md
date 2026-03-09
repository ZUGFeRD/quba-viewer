# Quba Viewer Release 1.5.1

## 🇩🇪 Deutsch
**Neues Release: Quba Viewer 1.5.1 ist da!**

Wir freuen uns, eine neue Version des Quba Viewers veröffentlichen zu können! Seit der Version 1.5.0 hat sich unter der Haube und an der Oberfläche einiges getan. Dieses Update bringt zahlreiche Verbesserungen, neue Funktionen und wichtige Fehlerbehebungen mit sich.

**Neue Funktionen & Verbesserungen**
- **Neue Handbücher & Hilfe:** Es wurden ausführliche Benutzerhandbücher auf Deutsch und Englisch integriert, die nun über ein neues Hilfe-Fenster in der App aufrufbar sind.
- **Windows-Installer:** Bei der Installation unter Windows (NSIS) kann nun der Zielpfad manuell ausgewählt werden.
- **Dateizuordnung:** Quba Viewer kann im Betriebssystem nun direkt als Standard-App für PDFs registriert werden.
- **Benutzeroberfläche:** Die Trennwand (Splitter) zwischen PDF- und XML-Suche wurde überarbeitet, inklusive neuer Buttons zur Größenanpassung. Auf dem Mac lässt sich die App nun komfortabel mit `Cmd+Q` schließen. Das App-Icon wurde aktualisiert.
- **Technische Upgrades:** Die Basis-Technologie wurde stark aktualisiert (Upgrade von Electron 20 auf Electron 33). Neue und verbesserte XSLT-Verarbeitung für ZF1.

**Fehlerbehebungen (Bugfixes)**
- **Anhänge (Attachments):** Ein Fehler bei der Vorschau von Anhängern (Bilder/PDFs) wurde behoben (#29).
- **Dateiformat-Erkennung:** XML- und PDF-Dateien werden nun noch zuverlässiger erkannt.
- **Datumsanzeige:** Ein Fehler wurde behoben, bei dem das Datum (BT-72) in bestimmten Fällen nicht angezeigt wurde.
- **Adressdarstellung:** Ein Problem mit der korrekten Zuordnung und Anzeige der Adresszeilen (Address Line 1 bis 3) wurde behoben (#80).
- **Übersetzungen:** Der Listenpreis (BT-148) wird nun korrekt mit "brutto" statt "netto" übersetzt.
- Behebung eines Problems mit der fehlenden Titelleiste im Info-Fenster (About Modal) auf dem Mac.
- Behebung eines internen Memory-Leaks zur Erhöhung der Performance und Stabilität.
- **Suchfunktion (Strg+F):** Ein Fehler wurde behoben, durch den die Suche aufgrund doppelter Ausführung nicht funktionierte.
- **Suchfunktion (Strg+F):** Die Suche fand Treffer nur im Tab „Übersicht". Nun wird auch in allen anderen Tabs (Positionen, Informationen, Anhänge, Bearbeitungshistorie) korrekt gesucht.

**Neue Funktionen & Verbesserungen (Nachtrag)**
- **Suchfunktion – Kein-Treffer-Hinweis:** Wenn die Suche keine Ergebnisse liefert, wird nun präzise angezeigt, wo nichts gefunden wurde: „Kein Treffer im HTML", „Kein Treffer im PDF" oder „Kein Treffer im HTML und PDF".
- **Suchfunktion – Automatischer Neustart:** Wird der Suchbegriff nach einer aktiven Suche geändert und erneut Enter gedrückt, startet die Suche automatisch neu – ohne das Eingabefeld komplett leeren zu müssen.

**Fehlerbehebungen (Nachtrag)**
- **Mehrere Rabatte pro Artikel (BT-147):** Bei ZUGFeRD EXTENDED-Rechnungen mit mehr als einem Rabatt pro Artikelposition (`AppliedTradeAllowanceCharge` × 2+) stürzte Quba mit dem Fehler *„supplied value contains 2 items"* ab. Die XSLT-Transformation verwendete `[1]`, wodurch bei mehreren Elementen ein Fehler ausgelöst wurde. Behoben durch Ersatz von `[1]` mit `sum()` und einer Leerprüfung — alle Rabatte werden nun korrekt aufsummiert und als Gesamtbetrag in BT-147 angezeigt. Getestet mit `test_warenrechnung_mehrere_rabatte.pdf` (Positionen mit 2, 3 und 4 Rabatten).

Den Download der neuen Version findest du wie immer auf unserer Website: https://quba-viewer.org/

---

## 🇬🇧 English
**New Release: Quba Viewer 1.5.1 is here!**

We are excited to announce a new release of the Quba Viewer! Since version 1.5.0 we have been hard at work, and this update brings numerous enhancements, new features, and critical bug fixes.

**New Features & Enhancements**
- **User Manuals & Help:** Integrated comprehensive user manuals in both English and German, accessible via a brand-new help window inside the app.
- **Windows Installer:** The NSIS Windows installer now allows you to choose a custom installation directory.
- **File Association:** Quba Viewer can now be registered directly as your operating system's default PDF viewer.
- **User Interface:** Improved the splitter between PDF and XML search views, adding new resizing buttons. Mac users can now close the app using the standard `Cmd+Q` shortcut. Replaced the app icon.
- **Technical Upgrades:** Massive underlying tech upgrade from Electron 20 to Electron 33. Added and improved XSLT handling for ZF1.

**Bug Fixes**
- **Attachments:** Fixed an issue where the preview for attachments (Images/PDFs) was not displaying correctly (#29).
- **Format Recognition:** Highly improved the reliable recognition of XML and PDF files.
- **Date Display:** Fixed an issue where the date (BT-72) was occasionally missing from the display.
- **Address Display:** Fixed an issue regarding the correct mapping and display of Address Lines 1 to 3 (#80).
- **Translations:** Corrected the translation of the list price (BT-148) from net to gross.
- Fixed a missing titlebar on the "About" modal for Mac users.
- Patched an internal memory leak to improve overall application stability and performance.
- **Search (Ctrl+F):** Fixed a bug that caused the search to malfunction due to duplicate execution.
- **Search (Ctrl+F):** The search was only finding results in the "Overview" tab. It now correctly searches across all tabs (Line Items, Information, Attachments, Processing History).

**New Features & Enhancements (Addendum)**
- **Search – No Results Indicator:** When the search finds no matches, a precise message is now shown indicating where nothing was found: "No match in HTML", "No match in PDF", or "No match in HTML and PDF".
- **Search – Automatic Restart:** If the search term is modified after an active search and Enter is pressed again, the search restarts automatically — no need to clear the input field first.

**Bug Fixes (Addendum)**
- **Multiple discounts per line item (BT-147):** In ZUGFeRD EXTENDED invoices with more than one discount per article (`AppliedTradeAllowanceCharge` × 2+), Quba crashed with the error *"supplied value contains 2 items"*. The XSLT stylesheet used `[1]`, which caused a failure when multiple elements were present. Fixed by replacing `[1]` with `sum()` combined with an empty-sequence guard — all discounts are now correctly summed and displayed as a total in BT-147. Tested with `test_warenrechnung_mehrere_rabatte.pdf` (line items with 2, 3, and 4 discounts).

You can download the new version as usual from our website: https://quba-viewer.org/
