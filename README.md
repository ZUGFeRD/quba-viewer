Quba-Viewer
=============

Quba-Viewer ([homepage](https://www.quba-viewer.org)) is a cross platform open source application to display 
structured (i.e., XML) and hybrid (Factur-X/ZUGFeRD PDF) electronic invoices.



History
=============
<details>
<summary>0.5 27.07.2021</summary>
    - Support for PDF
    - Support for Factur-X/ZUGFeRD
    - Display errors as dialog instead of hiding them 
</details> 
<summary>0.2 10.06.2021</summary>
    - Support for XRechnung (UBL)
    - "dark" theming
    - possibility to open multiple files at the same time in the same viewer (tabs)
    - print 
</details> 
<details>
<summary>0.1 2021-03-31</summary>
    - Initial release
    - Support for XRechnung (UN/CEFACT CII)
    - release for Windows on 2021-03-31, for Linux on 2101-04-16 and for Mac on 2021-04-27
</details> 

Architecture
=============
This viewer is a simple [electron](https://www.electronjs.org/) application (just use `npm start` to run) 
which uses [XSLT files](https://github.com/itplr-kosit/xrechnung-visualization) to convert XML to HTML.


In the doc folder there is a documentation on [Electron fundamentals and XSLT details](doc/electron.md).

Development
=============


Set up one time with 
```

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
npm run build
```
will build a version, which you can install e.g. with `dist\quba_viewer Setup 0.2.0.exe`.
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

Known issues
=============

  * the invoice output is german