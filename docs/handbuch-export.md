# Handbuch-Export (AsciiDoc → HTML & PDF)

Ausgangsdatei:

- `d:\[Project Name]\HANDBUCH_NEU.adoc`

Bilder (aktuell im Repo):

- `d:\Quba_kotlin\docs\screenshots\*.png`

Voraussetzung:

- Docker Desktop installiert und gestartet

---

## 1) PDF erzeugen (Bilder funktionieren immer)

PDF bettet die Bilder beim Rendern ein. Es reicht, dass die Bilddateien beim Export vorhanden sind.

PowerShell:

```powershell
cd d:\Quba_kotlin
New-Item -ItemType Directory -Force -Path .\build\handbuch | Out-Null

docker run --rm -v "${PWD}:/documents" asciidoctor/docker-asciidoctor `
  asciidoctor-pdf -o build/handbuch/HANDBUCH_NEU.pdf HANDBUCH_NEU.adoc
```

Ergebnis:

- `d:\Quba_kotlin\build\handbuch\HANDBUCH_NEU.pdf`

---

## 2) HTML erzeugen (2 Varianten)

### Variante A (empfohlen): Single-HTML mit eingebetteten Bildern

Die HTML-Datei ist „self-contained“ (keine extra Bilddateien neben der HTML nötig).

```powershell
cd d:\Quba_kotlin
New-Item -ItemType Directory -Force -Path .\build\handbuch | Out-Null

docker run --rm -v "${PWD}:/documents" asciidoctor/docker-asciidoctor `
  asciidoctor -a data-uri -o build/handbuch/HANDBUCH_NEU.html HANDBUCH_NEU.adoc
```

Ergebnis:

- `d:\Quba_kotlin\build\handbuch\HANDBUCH_NEU.html`

---

### Variante B: Normales HTML (Bilder als Dateien, nicht eingebettet)

Hier enthält die HTML nur `<img src="...">`-Referenzen. Damit die Bilder angezeigt werden, müssen sie relativ zur HTML-Datei erreichbar sein.

Da die HTML hier liegt:

- `d:\Quba_kotlin\build\handbuch\HANDBUCH_NEU.html`

…müssen die Bilder hier verfügbar sein:

- `d:\Quba_kotlin\build\handbuch\docs\screenshots\*.png`

PowerShell:

```powershell
cd d:\Quba_kotlin
New-Item -ItemType Directory -Force -Path .\build\handbuch | Out-Null

docker run --rm -v "${PWD}:/documents" asciidoctor/docker-asciidoctor `
  asciidoctor -o build/handbuch/HANDBUCH_NEU.html HANDBUCH_NEU.adoc

New-Item -ItemType Directory -Force -Path .\build\handbuch\docs\screenshots | Out-Null
Copy-Item -Force .\docs\screenshots\*.png .\build\handbuch\docs\screenshots\
```

Ergebnis:

- `d:\Quba_kotlin\build\handbuch\HANDBUCH_NEU.html`
- `d:\Quba_kotlin\build\handbuch\docs\screenshots\*.png`

---

## Merksatz: Wo müssen die Images liegen?

- Für PDF: Bilder müssen beim Rendern erreichbar sein (im Repo: `docs\screenshots\...` passt).
- Für HTML ohne `data-uri`: Bilder müssen relativ zur HTML-Datei erreichbar sein (hier: `build\handbuch\docs\screenshots\...`).
