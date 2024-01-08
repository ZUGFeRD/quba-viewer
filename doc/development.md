Architecture
=============
This viewer is a simple [electron](https://www.electronjs.org/) application (just use `npm start` to run)
which uses [XSLT files](https://github.com/itplr-kosit/xrechnung-visualization) to convert XML to HTML.


In the doc folder there is a documentation on [Electron fundamentals and XSLT details](doc/electron.md).

Development
=============


Set up one time with
```
npm install @vue/cli-service
npm install electron-builder
npm install electron-forge
npm update
```
then

```
npm start
```


Debugging and testing
=============

Use CTRL+B to open the developer console.
`npm start` of course starts the application for local testing.

```
npm run start
```
should tell you if the software runs

```
npm run pack
```
will build a executable installer in releases.

If you do not run it from the start menu, but in a shell directly from
`\Users\<your user name>\AppData\Local\Programs\quba_viewer\quba_viewer.exe`
you will also see potential error output on the console.

Common issues include listing runtime dependencies as devDepencencies
or not listing files to be included in the deployment in the build.files.filter
(both issues can be fixed in the package.json).

Test ideas:     build (pack) version opens and displays and has every file it needs
if settings keep thr set language, files open, opens side by side, rects on tab click, reacts on show xml click, resolves codelists and opens dev console
performs autoupdate, opens and closes example page

i18n
=============
vue-18n-next is used.
For unknown reasons there is a  src/transaltion/de.json and a app/locales/de/translation.json
https://github.com/ZUGFeRD/quba-viewer/issues/36

uninstall %AppData%/Roaming/Quba
Deployment
=============
The final update can then be deployed by Github users who have been granted access
via a powershell using

```
$env:GH_TOKEN = '<private github access token>'
npm run publish
```
Actually only the Mac can build all three versions, there and on Linux use `export GH_TOKEN=<your token>`. 
Since there is an error in the build please use `npm run build` afterwards to also create the
autoupdate files `latest.yml`, `latest-mac.yml` and `latest-linux.yml` and upload them manually to
the release in Github.

To access the console (not only of the browser windows but also of main.js)
start Quba in a shell, e.g.
`%AppData%\Local\Programs\quba_viewer\quba_viewer.exe`

On the mac please change in package.json from --win to --mac --linux
Uninstall 