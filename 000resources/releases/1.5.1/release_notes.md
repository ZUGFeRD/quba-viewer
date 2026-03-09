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

You can download the new version as usual from our website: https://quba-viewer.org/
