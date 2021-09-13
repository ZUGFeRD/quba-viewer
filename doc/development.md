Architecture
=============
This viewer is a simple [electron](https://www.electronjs.org/) application (just use `npm start` to run)
which uses [XSLT files](https://github.com/itplr-kosit/xrechnung-visualization) to convert XML to HTML.


In the doc folder there is a documentation on [Electron fundamentals and XSLT details](doc/electron.md).

Development
=============


Set up one time with
```
npm install --global @vue/cli-service
npm install --global electron-builder
npm install --global electron-forge
npm update
```
then

```
npm run start
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


Deployment
=============
The final update can then be deployed by Github users who have been granted access
via a powershell using

```
$env:GH_TOKEN = '<private github access token>'
npm run publish
```

To access the console (not only of the browser windows but also of main.js)
start Quba in a shell, e.g.
`%AppData%\Local\Programs\quba_viewer\quba_viewer.exe`

