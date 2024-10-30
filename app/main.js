const SaxonJS = require("saxon-js");
const axios = require("axios").default;
var FormData = require("form-data");
const {
    app,
    BrowserWindow,
    Menu,
    ipcMain,
    dialog,
    ipcRenderer,
} = require("electron");
const os = require("os");
const {autoUpdater} = require("electron-updater");
const electronLocalShortcut = require("electron-localshortcut");
const pdfjsLib = require("pdfjs-dist/legacy/build/pdf");
const Store = require("electron-store");
const isDev = require("electron-is-dev");
const https = require("https");
const menuFactoryService = require("./menuConfig");
const jsdom = require("jsdom");
const { JSDOM } = jsdom;
const { parse } = require("ini");
const  { readFile} = require("node:fs/promises")

const i18next = require("i18next");
const Backend = require("i18next-fs-backend");
const i18nextOptions = require("./config/i18next.config");
const config = require("./config/app.config");

const fs = require("fs");
const path = require("path");
const store = new Store();

let mainWindow;
let eligibleSysLanguage=null;
if ((app.getLocale()=="de")||(app.getLocale()=="en")||(app.getLocale()=="fr")) {
    eligibleSysLanguage = app.getLocale();
}


//alert(text);
/*

//  Read INI file as text


//  Parse text data to object

 */
let currentLanguage = store.get("language") || eligibleSysLanguage || config.fallbackLng;
function createWindow() {
    mainWindow = new BrowserWindow({
        width: 800,
        height: 600,
        frame: true,
        webPreferences: {
            plugins: true,
            preload: path.join(__dirname, "preload.js"),
        },
    });

    mainWindow.loadFile(path.join(__dirname, "../dist/index.html"));

    mainWindow.on("closed", function () {
        //store.clear();
        //store.delete("access_token");
        //store.delete("isLoggedIn");
         mainWindow = null;
    });


    mainWindow.webContents.on(
        "new-window",
        function (
            event,
            url,
            frameName,
            disposition,
            options,
            additionalFeatures,
            referrer,
            postBody
        ) {
            event.preventDefault();
            const win = new BrowserWindow({
                webContents: options ? options.webContents : {},
                show: false,
            });
            win.once("ready-to-show", () => win.show());
            if (!options.webContents) {
                const loadOptions = {
                    httpReferrer: referrer,
                };
                if (postBody != null) {
                    const {data, contentType, boundary} = postBody;
                    loadOptions.postData = postBody.data;
                    loadOptions.extraHeaders = `content-type: ${contentType}; boundary=${boundary}`;
                }

                win.loadURL(url, loadOptions);
            }
            event.newGuest = win;
        }
    );

    mainWindow.once("ready-to-show", () => {
        mainWindow.webContents.send("language-change", currentLanguage);

        const appArgv = process.argv.slice(isDev ? 2 : 1);
        if (appArgv[0] && appArgv[0].toLowerCase().endsWith(".pdf")) {
            mainWindow.webContents.send("pdf-open", [appArgv[0], null]);
        } else if (appArgv[0] && appArgv[0].toLowerCase().endsWith(".xml")) {
            loadAndDisplayXML(appArgv[0]);
        }
    });

    menuFactoryService.buildMenu(app, mainWindow, i18next, openFile);
    i18next.on("languageChanged", (lng) => {
        i18next.options.lng = lng;
        store.set("language", lng);
        mainWindow.webContents.send("language-change", lng);
        menuFactoryService.buildMenu(app, mainWindow, i18next, openFile);
    });
    setTimeout(() => {
        mainWindow.webContents.send("goToHome");
    }, 200);
    menuFactoryService.buildMenu(app, mainWindow, i18next, openFile);
}

app.on("ready", async () => {
    const t = await i18next.use(Backend).init(i18nextOptions);
    createWindow();
    registerShortcuts();
});


app.on("window-all-closed", function () {
    const tempPath = path.join(app.getPath("temp"), app.getName());
    //store.clear();
    //store.delete("access_token");
    //store.delete("isLoggedIn");
    if (fs.existsSync(tempPath)) {
        try {
            fs.rmdirSync(tempPath, {recursive: true});
        } catch (err) {
        }
    } else {
    }
    if (process.platform !== "darwin") {
        app.quit();
    }
});

app.on("activate", function () {
    if (mainWindow === null) createWindow();
});

function registerShortcuts() {
    electronLocalShortcut.register(mainWindow, "CommandOrControl+B", () => {
        mainWindow.webContents.openDevTools();
    });
    electronLocalShortcut.register(mainWindow, "CommandOrControl+O", () => {
        openFile();
    });
}

const gotTheLock = app.requestSingleInstanceLock();
if (!gotTheLock) {
    app.quit();
} else {
    initApp();
}

function initApp() {
    readInstallerConfigAndPerformAutoUpdate();
    app.on("second-instance", (event, commandLine) => {
        if (mainWindow) {
            if (mainWindow.isMinimized()) {
                mainWindow.restore();
            }
            mainWindow.focus();

            if (commandLine.length && commandLine[commandLine.length - 1]) {
                const path = commandLine[commandLine.length - 1];
                if (path.toLowerCase().includes(".pdf")) {
                    mainWindow.webContents.send("pdf-open", [path, null]);
                } else {
                    loadAndDisplayXML(path);
                }
            }
            // mainWindow.webContents.send("external-file-open", commandLine);
        }
    });
    app.on("window-all-closed", () => {
        if (process.platform !== "darwin") {
            app.quit();
        }
    });
    app.on("activate", () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
    ipcMain.on("app_version", (event) => {
        event.sender.send("app_version", {version: app.getVersion()});
    });

    ipcMain.on("toggle-menu-items", (event, flag) => {
        menu.getMenuItemById("file-print").enabled = flag;
    });

    ipcMain.on("doc-change", (event, params) => {
        let setEnabled = params.visible;
        Menu.getApplicationMenu().getMenuItemById("file-print").enabled = setEnabled;
        Menu.getApplicationMenu().getMenuItemById("file-print-pdf").enabled = setEnabled && params.pdf;
        Menu.getApplicationMenu().getMenuItemById("file-print-xml").enabled = setEnabled && params.xml;
        Menu.getApplicationMenu().getMenuItemById("validate").enabled = setEnabled && params.xml;
    });

    autoUpdater.on("update-available", () => {
        mainWindow.webContents.send("update_available");
    });

    autoUpdater.on("update-downloaded", () => {
        mainWindow.webContents.send("update_downloaded");
    });

    ipcMain.on("restart_app", () => {
        autoUpdater.quitAndInstall();
    });

    ipcMain.on("check-xml", async (event, filePath) => {
        //   check if the PDF contains embedded xml files

        try {
            var loadingTask = pdfjsLib.getDocument(filePath);
            loadingTask.promise
                .then(function (pdf) {
                    pdf.getAttachments().then(function (embeddedFiles) {
                        let embeddedXML = null;
                        let isOrderX = false;
                        if (typeof embeddedFiles == "object" && embeddedFiles !== null) {
                            if (embeddedFiles["factur-x.xml"]) {
                                embeddedXML = new TextDecoder().decode(
                                    embeddedFiles["factur-x.xml"]["content"]
                                );
                            }
                            // flat file embedding
                            if ((typeof embeddedFiles["Embedded XML"] != "undefined") && (typeof embeddedFiles["Embedded XML"]["filename"] != "undefined")
                                && (
                                    (embeddedFiles["Embedded XML"]["filename"] == "factur-x.xml")
                                    || (embeddedFiles["Embedded XML"]["filename"] == "zugferd-invoice.xml")
                                    || (embeddedFiles["Embedded XML"]["filename"] == "xrechnung.xml")
                                    || (embeddedFiles["Embedded XML"]["filename"] == "order-x.xml")
                                )) {
                                embeddedXML = new TextDecoder().decode(
                                    embeddedFiles["Embedded XML"]["content"]
                                );
                                if (embeddedFiles["Embedded XML"]["filename"] == "order-x.xml") {
                                   isOrderX=true;
                                }

                            } else {
                                // tree file embedding
                                if (embeddedFiles["zugferd-invoice.xml"]) {
                                    // the embedded file can also be named zugferd-invoice.xml
                                    // if it contained uppercaps like ZUGFeRD-invoice.xml it would be ZF1
                                    embeddedXML = new TextDecoder().decode(
                                        embeddedFiles["zugferd-invoice.xml"]["content"]
                                    );
                                }
                                if (embeddedFiles["xrechnung.xml"]) {
                                    embeddedXML = new TextDecoder().decode(
                                        embeddedFiles["xrechnung.xml"]["content"]
                                    );
                                }
                                if (embeddedFiles["order-x.xml"]) {
                                    isOrderX=true;
                                    embeddedXML = new TextDecoder().decode(
                                        embeddedFiles["order-x.xml"]["content"]
                                    );
                                }
                            }
                        }
                        if (embeddedXML !== null) {
                            if (isOrderX) {
                                transformAndDisplayCIO(
                                    filePath + " (embedded xml)",
                                    embeddedXML,
                                    false
                                ).then((res) => {
                                    event.returnValue = res ? res : undefined;
                                });
                            } else {
                                transformAndDisplayCII(
                                    filePath + " (embedded xml)",
                                    embeddedXML,
                                    false
                                ).then((res) => {
                                    event.returnValue = res ? res : undefined;
                                });
                            }
                        } else {
                            event.returnValue = undefined;
                        }
                    });
                })

                .catch((error) => {
                    event.returnValue = undefined;
                    displayError("Exception", error.getMessage());
                });
        } catch (error) {
            event.returnValue = undefined;
        }
    });

    ipcMain.on("validate-file", async (event, filePath) => {
        validateFile(filePath).then(
            (res) => {
                event.returnValue = res;
            },
            (error) => {
                event.returnValue = error;
            }
        );
    });

    ipcMain.on("open-link", (event) => {
        let exWin = new BrowserWindow({
            width: 800,
            height: 600,
            icon:
                process.platform === "win32"
                    ? "../assets/img/favicon.ico"
                    : "../assets/img/logoonly.svg",
        });
        exWin.setMenu(null);
        exWin.loadURL(
            "https://quba-viewer.org/beispiele/?pk_campaign=examples&pk_source=application"
        );
    });

    ipcMain.on("open-dragged-file", (event, filePath) => {
        if (filePath.toLowerCase().includes(".pdf")) {
            mainWindow.webContents.send("pdf-open", [filePath, null]);
        } else if (filePath.toLowerCase().includes(".xml")) {
            loadAndDisplayXML(filePath);
        }
    });
    ipcMain.on("open-menu", (event, arg) => {
        openFile();
    });


}

/***
 the installer config is the local AppConfig.ini, which has been filled with entries depending on the
 parameters passed to the *installer*
 */
async function readInstallerConfigAndPerformAutoUpdate() {

    let doUpdate=true;
    // getAppPath() returns resources/asar
    try {
        let text = await readFile(app.getAppPath() + `\\..\\..\\AppConfig.ini`, {
            encoding: 'utf-8'
        });
        const config = parse(text)
        doUpdate= config.AutoUpdate.performOnStartup;

    } catch (err){
        // Here you get the error when the file was not found,
        // but you also get any other error
        console.error(err);
    };
    if (doUpdate) {
        autoUpdater.checkForUpdatesAndNotify();
    } else {
//        console.log("deb  I wouldnt");
    }

}

function openFile() {
    dialog
        .showOpenDialog(BrowserWindow, {
            path: "",
            properties: ["openFile"],
            filters: [
                {
                    name: "all",
                    extensions: ["txt"],
                },
            ],
        })
        .then((result) => {
            if (!result.canceled) {
                let paths = result.filePaths;
                if (paths && paths.length > 0) {
                    if (paths[0].toLowerCase().includes(".pdf")) {
                        mainWindow.webContents.send("pdf-open", [paths[0], null]);
                    } else {
                        loadAndDisplayXML(paths[0]);
                    }

                    //validateFile(paths[0]);
                }
            }
        });
}

function loadAndDisplayXML(filename) {
    try {
        const content = fs.readFileSync(filename).toString();
        var parser = require("fast-xml-parser");
        let json = parser.parse(content);
        for (let key in json) {
            if (key.includes("CrossIndustryInvoice")) {
                transformAndDisplayCII(filename, content, true);
            } else if (key.includes("SCRDMCCBDACIOMessageStructure")) {
                transformAndDisplayCIO(filename, content, true);
            } else if (key.includes("Invoice")) {
                transformAndDisplayUBL(filename, content, true);
            } else if (key.includes("CreditNote")) {
                transformAndDisplayUBLCN(filename, content, true);
            } else {
                displayError(
                    "File format not recognized",
                    "Is it a UBL 2.1 or UN/CEFACT 2016b XML file or PDF you are trying to open?"
                );
            }
        }
    } catch (e) {
        const errMessage = e?.message ? e.message : e;
        displayError("Exception", errMessage);
    }
}

function transformAndDisplayCII(sourceFileName, content, shouldDisplay) {
    return transformAndDisplay(
        sourceFileName,
        content,
        path.join(__dirname, "xslt", "cii-xr.sef.json"),
        shouldDisplay
    );
}


function transformAndDisplayCIO(sourceFileName, content, shouldDisplay) {
    return transformAndDisplay(
        sourceFileName,
        content,
        path.join(__dirname, "xslt", "cio-xr.sef.json"),
        shouldDisplay
    );
}

function transformAndDisplayUBL(sourceFileName, content, shouldDisplay) {
    return transformAndDisplay(
        sourceFileName,
        content,
        path.join(__dirname, "xslt", "ubl-xr.sef.json"),
        shouldDisplay
    );
}

function transformAndDisplayUBLCN(sourceFileName, content, shouldDisplay) {
    return transformAndDisplay(
        sourceFileName,
        content,
        path.join(__dirname, "xslt", "ubl-creditnote-xr.sef.json"),
        shouldDisplay
    );
}

function transformAndDisplay(
    sourceFileName,
    content,
    stylesheetFileName,
    shouldDisplay
) {

    const dom = new JSDOM(content, {contentType: "application/xml"});
    const doc = dom.window.document;
//cbc:InvoiceTypeCode for bis biling, ubl order would be <cbc:ProfileID>urn:fdc:peppol.eu:poacc:bis:ordering:3</cbc:ProfileID>
    const typecode = doc.evaluate("//rsm:ExchangedDocument/ram:TypeCode", doc, null, 2, null).stringValue;
    const isOrder = (typecode == 220) || (typecode == 231);

    return SaxonJS.transform(
        {
            stylesheetFileName,
            sourceText: content,
            destination: "serialized"
        },
        "async"
    )
        .then((output) => {
            let xrXML = output.principalResult;
            let translations = i18next.getDataByLanguage(currentLanguage).translation;
            if (isOrder) {
                translations["bt1"] = i18next.t("bt1_order"); //Rechnungsnummer->Bestellnummer
                translations["bt2"] = i18next.t("bt2_order");//Rechnungsdatum
                translations["bt3"] = i18next.t("bt3_order");//rechnungart
                translations["bg22"] = i18next.t("bg22_order");//Gesmtbeträge der Rechnung
                translations["bt25"] = i18next.t("bt25_order");//rechnungsnummer
                translations["bt26"] = i18next.t("bt26_order");//rechnungsdatum
                translations["details"] = i18next.t("details_order");//Rechnungsdaten
            }

            return SaxonJS.transform(
                {
                    stylesheetFileName: path.join(
                        __dirname,
                        "xslt",
                        "xrechnung-html.uni.sef.json"
                    ),
                    sourceText: xrXML,
                    destination: "serialized",
                    stylesheetParams: {
                        "isOrder": isOrder,
                        "showIds": store.get("showIds"),
                        "Q{}i18n": translations
                    }
                },
                "async"
            )
                .then((response) => {
                    let HTML = response.principalResult;
                    if (shouldDisplay) {
                        mainWindow.webContents.send("xml-open", [
                            sourceFileName,
                            HTML
                        ]);
                    }
                    return HTML;
                })
                .catch((error) => {
                    const errMessage = error?.message ? error.message : error;
                    displayError("Exception", errMessage);
                });
        })
        .catch((output) => {
            d
            const errMessage = output?.message ? output.message : output;
            displayError("Exception", errMessage);
        });
}

function displayError(message, detail) {
    const options = {
        type: "error",
        buttons: ["OK"],
        defaultId: 1,
        title: "Error",
        message,
        detail,
    };
    dialog.showMessageBox(null, options, (response, checkboxChecked) => {
    });

}

function validateFile(xmlFilePath) {
    return new Promise((resolve, reject) => {
        const xml2js = require("xml2js");
        var status = false;
        var parser = new xml2js.Parser();
        const formData = new FormData();
        formData.append("file", fs.createReadStream(xmlFilePath));
        const accessToken = store.get("access_token");
        //console.log("accessToken", accessToken);
        const agent = new https.Agent({rejectUnauthorized: false});
        axios
            .post(
                "https://gw.usegroup.de:8243/mustang/mustang/validate",
                formData,
                {
                    headers: {
                        Authorization: "Bearer " + accessToken,
                        "Content-Type": "multipart/form-data",
                    },
                    httpsAgent: agent,
                }
            )
            .then(function (response) {
                /* console.log("response==>", response);*/

                parser.parseString(response.data, function (err, result) {
                    const error =
                        result?.validation?.xml?.[0]?.messages?.[0]?.error?.[0]?._;
                    const criterion =
                        result?.validation?.xml?.[0]?.messages?.[0]?.error?.[0]?.$
                            ?.criterion;
                    status = result?.validation?.summary[0]?.$?.status ?? "Invalid";
                    const isValid = status === "valid";
                    //          console.log("error", error);
                    //          console.log("status", status);
                    const request = {
                        path: xmlFilePath,
                        valid: isValid,
                        error: error,
                        criterion: criterion,
                    };
                    //mainWindow.webContents.send("validate-complete", request);
                    // return new Promise((resolve, reject) => {
                    resolve(request);
                    // });
                });
            })
            .catch(function (error) {
                console.log(error);
                let request = {
                    path: xmlFilePath,
                    valid: false,
                };
                if (error?.response?.status === 401) {
                    store.delete("access_token");
                    store.delete("isLoggedIn");
                    request.code = 'ERR_UNAUTHORIZED';
                    reject(request);
                } else {
                    request.code = 'ERR_NETWORK';
                    //mainWindow.webContents.send("validate-complete", request);
                    // return new Promise((resolve, reject) => {
                    reject(request);
                    // });
                }
                // return new Promise((resolve, reject) => {
                //reject(null);
                // });
            });
    });
}
