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

Since the input XML can be UN/CEFACT CII or UBL, the XRechnung visualization 
conversion is a two-step process, 
  * from the input XML to an intermediate XR format using `cii-xr.sef.json` and 
  * from this XR format to HTML using `xrechnung-html.sef.json`

Development
=============
Setup with npm install --global electron-builder
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