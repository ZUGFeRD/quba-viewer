Quba-Viewer
=============

Quba-Viewer ([homepage](https://www.quba-viewer.org)) is a cross platform open source application to display 
structured (i.e., XML) and hybrid (Factur-X/ZUGFeRD PDF) electronic invoices.



History
=============
<details>
<summary>0.1 31.03.2021</summary>
    - Initial release
    - Support for XRechnung (UN/CEFACT CII)
</details> 

Architecture
=============
So far, this is a simple [electron](https://www.electronjs.org/) application (just use `npm start` to run) which uses XSLT to convert XML to HTML,
more precisely it uses [saxon-js](https://www.saxonica.com/saxon-js/) as XSLT processor to apply
some [available](https://github.com/itplr-kosit/xrechnung-visualization) XSLT style sheets which were previously 
converted to .sef.json files using the xslt3 utility.

```
npm install --global xslt3
xslt3 -xsl:cii-xr.xsl -export:cii-xr.sef.json -t
xslt3 -xsl:xrechnung-html.xsl -export:xrechnung-html.sef.json -t
```

Since xrechnung-viewer.css and xrechnung-viewer.js are required via the XSLT file 
this will bypass the usual javascript loading routines, which is not an issue 
for a development version but a severe issue in a deployed version where those
files would have to be read from the ASAR file. 
The current workaround is copy/pasting the contents of those files into the
xrechung-html.xsl XSL file to generate a self sufficient xrechung-html.sef.json.


Since the input XML can be UN/CEFACT CII or UBL, the XRechnung visualization 
conversion is a two-step process, 
  * from the input XML to an intermediate XR format using `cii-xr.sef.json` and 
  * from this XR format to HTML using `xrechnung-html.sef.json`

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



Debugging
=============

Use CTRL+B to open the developer console.
`npm start` of course starts the application for local testing.

Deployment
=============

```
$env:GH_TOKEN = '<private github access token>'
npm run publish
```

To access the console (not only of the browser windows but also of main.js)
start Quba in a shell, e.g.
`%AppData%\Local\Programs\quba_viewer\quba_viewer.exe`

Known issues
=============

  * XRechnung(UBL) is not yet supported
  * the invoice output is german