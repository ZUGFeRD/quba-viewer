Electron development
=============


We learned a lot from Vuika 2019 book  
("Electron Projects: Build over 9 cross-platform desktop applications from scratch").

There is also a interesting and inspiring (e.g. tabs) open-source-project of a
[Electron based PDF Viewer](https://github.com/sagargurtu/lector).

IPC
-------------
There is a hard distinction between the main process and the browser process.
If the browser wants to tell anything to the main process it
sends an according signal, let's call it `click-button`

```
<script>
    const {ipcRenderer} = require('electron');
    function clickButton() {
        console.log("firing click-button signal");
        ipcRenderer.send('click-button', null, true)
    }
</script>
<button onclick="clickButton()" id="new-window">Open File</button>
```

and the main process picks it up via
```
ipcMain.on('click-button', (event, arg) => {
    console.log("click-button received ", event, arg);
    }
})
```
If the main process, e.g. the menu, wants to tell anything to the browser it also 
sends a signal, e.g.
```
    const window=BrowserWindow.getFocusedWindow();
    window.webContents.send('eventForBrowser', null, true)
```
and in the browser's script the event can be picked up like this
```
    ipcRenderer.on('eventForBrowser', (event, data) => {
        console.log("received event for browser", event, data)
    })
```

XSLT/Saxon
-------------
Quba uses [saxon-js](https://www.saxonica.com/saxon-js/) as XSLT processor to apply
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

of course if the root note is detected to be UBL
the first step will be using `ubl-xr.sef.json`.

Have a look at
[the saxon documentaion](https://www.saxonica.com/saxon-js/documentation/index.html) for further info.

To preview changes we try in the XSLT files, we sometimes use
[the Java version of Saxon](https://sourceforge.net/projects/saxon/files/Saxon-HE/10/Java/) to test our changes
in the usual (Kosit) two step process like 
```
java -jar saxon-he-10.5.jar factur-x.xml cii-xr.xsl  >xr.xr
java -jar \Users\jstaerk\workspace\XMLexamples\saxon-he-10.5.jar xr.xr xrechnung-html.xsl  >xr.html
```


Codelists
-------------


Codelists are lists of attribute values, like for countries, currencies, units or tax exemption codes.
The [codelists](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Registry+of+supporting+artefacts+to+implement+EN16931) 
for UBL and CII are the same and they are maintained by the CEF (not to be confused with CEFACT, which is a UN body).

Apparently the [OpenXRechnungToolbox](https://github.com/jcthiele/OpenXRechnungToolbox) has downloaded them 
and made them [available](https://github.com/jcthiele/xrechnung-visualization-codelist-resolve/) 
as patch on [Kosit's XRechnung Visualization](https://github.com/itplr-kosit/xrechnung-visualization).
Quba is using this patch so that instead of 15 times a "H87" of a product can be resolved into the more unterstandable
15 times a "piece" of a product: unit-wise apart from "Piece" H87 or C61 "one", there are thousands of unit codes also covering 
e.g. sixpack, hours, kilogram, metres, square metres, litres or cubic foot.



Influence
-------------

The FeRD had published visualization XSLTs for ZF1 (~=UN/CEFACT C13B) as open source, unfortunately the ones for the UN/CEFACT C16B-based 
version 2 remains proprietary. The Kosit released XSLT for both CII and UBL of the XRechnung (XR) which has been used for various online viewers
but also for offline viewers like Ultramarinviewer and Open XRechnung Toolbox. As mentioned, Quba uses this work and will add translation and 
at least experimental support for Factur-X/ZUGFeRD profiles higher than EN16931.

![History of Quba](History_of_Quba-02.svg "Logo Title Text 1")
