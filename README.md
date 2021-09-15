Quba-Viewer
=============

Quba-Viewer ([homepage](https://www.quba-viewer.org)) is a cross platform open source application to display 
structured (i.e., XML) and hybrid (Factur-X/ZUGFeRD PDF) electronic invoices.

Documentation-wise there is a doc RE [Architecture, Development, Debugging and testing](doc/development.md) in 
general and some Electron and E-Invoice peculiarities like [interprocess (IPC) communication, XSLT and codelists](doc/electron.md) in particular.

History
=============
<details>
<summary>1.1 21.09.2021</summary>
    - #6 New document shows still old data / invoice
    - Switch to vue.js
    - i18n: Support for EN and FR
    - support FX Referenzprofil XRechnung
</details>
<details>
<summary>1.0 23.08.2021</summary>
    - #5 Codelists are now resolved
    - Support for first FX Extended Element, i.e., Cash Discount in XML 
</details>
<details>
<summary>0.5 27.07.2021</summary>
    - Support for PDF
    - Support for Factur-X/ZUGFeRD
    - Display errors as dialog instead of hiding them 
</details>
<details>
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

Pedigree
-------------

![History of Quba](doc/History_of_Quba-02.svg "Pedigree of Quba")

The FeRD had published visualization XSLTs for ZF1 (~=UN/CEFACT C13B) as open source, unfortunately the ones for the UN/CEFACT C16B-based
version 2 remains proprietary. The Kosit released XSLT for both CII and UBL of the XRechnung (XR) which has been used for various online viewers
but also for offline viewers like Ultramarinviewer and Open XRechnung Toolbox. Quba uses this work added translations and
at least experimental support for Factur-X/ZUGFeRD profiles higher than EN16931.

Known issues
=============

  * While XRechnung, EN16931 and below should work, not all FX attributes/elements of the Extended Profile have yet been mapped, feel free to [report missing ones](https://github.com/ZUGFeRD/quba-viewer/issues) 