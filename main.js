const SaxonJS = require('saxon-js');
const {app, BrowserWindow, ipcMain, ipcRenderer, Menu, dialog, protocol} = require('electron');
const {autoUpdater} = require('electron-updater');
const electronLocalshortcut = require('electron-localshortcut');
const pdfjsLib = require('pdfjs-dist/legacy/build/pdf');

const fs = require('fs');
const path = require('path');
//const url = require('url');

let win, aboutWin;

function createWindow() {
    win = new BrowserWindow({
        width: 800,
        height: 600,
        icon: './assets/img/logoonly.svg',
        webPreferences: {
            plugins: true,
            nodeIntegration: true,
            enableRemoteModule: true,
        },
        frame: false
    })

    win.loadFile('index.html');
    win.once('ready-to-show', () => {
        autoUpdater.checkForUpdatesAndNotify();
    });
}

function registerShortcuts() {
    electronLocalshortcut.register(win, 'CommandOrControl+B', () => {
        win.webContents.openDevTools();
    });
}
// Importing unhandled.
const unhandled = require('electron-unhandled');

unhandled({
    logger: () => {
        console.error();
    },
    showDialog: true,
    reportButton: (error) => {
        console.log('Report Button Initialized');
    }
});

app.on('ready', function () {
    createWindow()
    const template = [
        {
            label: 'File',
            id: 'file-open',
            accelerator: 'CmdOrCtrl+O',
            submenu: [
                {
                    label: 'Open File',
                    accelerator: 'CmdOrCtrl+O',
                    click() {
                        openFile();
                    }
                },
                {
                    type: 'separator'
                },
                {
                    label: 'Print',
                    id: 'file-print',
                    accelerator: 'CmdOrCtrl+P',
                    enabled: false,
                    click() {
                        win.webContents.send('file-print')
                    }
                },
                {
                    type: 'separator'
                },
                {
                    label: 'Exit',
                    click() {
                        app.quit()
                    }
                }
            ]
        },
        {
            label: 'Edit',
            submenu: [

                {role: 'cut'},
                {role: 'copy'},
                {role: 'paste'},
                {type: 'separator'},
                {role: 'delete'},
                {type: 'separator'},
                {role: 'selectall'}
            ]
        },
        {
            label: 'Help',
            submenu: [
                {
                    label: 'About',
                    click() {
                        openAboutWindow();
                    }
                }
            ]
        }
    ]

    const menucreate = Menu.buildFromTemplate(template)
    Menu.setApplicationMenu(menucreate)
    ipcMain.on('toggle-menu-items', (event, flag) => {
        menucreate.getMenuItemById('file-print').enabled = flag;
    });

    var newWindow = null

    function openAboutWindow() {
        if (newWindow) {
            newWindow.focus()
            return
        }

        newWindow = new BrowserWindow({
            height: 185,
            resizable: false,
            width: 400,
            title: '',
            parent: win,
            modal: true,
            minimizable: false,
            fullscreenable: false,
            webPreferences: {
                nodeIntegration: true
            },
        });
        newWindow.setMenuBarVisibility(false)

        newWindow.loadFile('about.html');

        newWindow.on('closed', function () {
            newWindow = null
        })
    }

    registerShortcuts();
});

const gotTheLock = app.requestSingleInstanceLock();

if (!gotTheLock) {
    app.quit();
} else {
    app.on('second-instance', (event, commandLine) => {
        // Someone tried to run a second instance, we should focus our window.
        if (win) {
            if (win.isMinimized()) {
                win.restore();
            }
            win.focus();
            win.webContents.send('external-file-open', commandLine);
        }
    });

    app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') {
            app.quit()
        }
    })

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow()
        }

    })

    ipcMain.on('app_version', (event) => {
        event.sender.send('app_version', {version: app.getVersion()});
    });

    ipcMain.on('open-examples', (event) => {
        //supposed to open https://github.com/ConnectingEurope/eInvoicing-EN16931/tree/master/cii/examples
        let exWin = new BrowserWindow({
            width: 800,
            height: 600,
            icon: './assets/img/logoonly.svg',
            webPreferences: {
                plugins: true,
                nodeIntegration: true,
                enableRemoteModule: true,
            },
            frame: true
        })
        exWin.loadURL("https://quba-viewer.org/beispiele/?pk_campaign=examples&pk_source=application")
    });
    autoUpdater.on('update-available', () => {
        win.webContents.send('update_available');
    });

    autoUpdater.on('update-downloaded', () => {
        win.webContents.send('update_downloaded');
    });

    ipcMain.on('restart_app', () => {
        autoUpdater.quitAndInstall();
    });
}

function transformAndDisplayCII(sourceFileName, content) {
    return transformAndDisplay(sourceFileName, content, path.join(__dirname, "cii-xr.sef.json"));
}

function transformAndDisplayUBL(sourceFileName, content) {
    return transformAndDisplay(sourceFileName, content, path.join(__dirname, "ubl-xr.sef.json"));
}

function transformAndDisplay(sourceFileName, content, stylesheetFileName) {

    SaxonJS.transform({
        stylesheetFileName,
        sourceText: content,
        destination: "serialized"
    }, "async").then(output => {

        //console.log("first transformation finished", output.principalResult);

        let xrXML = output.principalResult;

        let test = SaxonJS.transform({
            stylesheetFileName: path.join(__dirname, "xrechnung-html.sef.json"),
            sourceText: xrXML,
            destination: "serialized"
        }, "async").then(output => {
            //console.log("second transformation finished", output.principalResult);
            let HTML = output.principalResult;
            win.webContents.send('xml-open', [
                sourceFileName,
                `data:text/html;base64,${Buffer.from(HTML).toString('base64')}`,]); // send to be displayed
            return HTML;
        }).catch(output => {
            displayError("Exception", output.getMessage());
        });

    }).catch(output => {
        displayError("Exception", output.getMessage());
    });

}

function displayError(message, detail) {
    console.error(message, detail)
    const options = {
        type: 'error',
        buttons: ['OK'],
        defaultId: 1,
        title: 'Error',
        message, detail
    };

    dialog.showMessageBox(null, options, (response, checkboxChecked) => {
    });
}

function loadAndDisplayXML(filename) {
    try {
        const content = fs.readFileSync(filename).toString();
        var parser = require('fast-xml-parser');
        let json = parser.parse(content);
        for (let key in json) {
            // parse root node
            if (key.includes("CrossIndustryInvoice")) {

                transformAndDisplayCII(filename, content);
            } else if (key.includes("Invoice")) {
                transformAndDisplayUBL(filename, content);
            } else {
                displayError('File format not recognized', 'Is it a UBL 2.1 or UN/CEFACT 2016b XML file or PDF you are trying to open?');

            }
        }
    } catch (e) {
        displayError("Exception", e.message)
    }

}

function openFile() {
    dialog.showOpenDialog(BrowserWindow, {
        path: '',
        properties: ['openFile'],
        filters: [{
            name: 'all',
            extensions: ['txt']
        }]
    }).then
    (
        result => {
            if (!result.canceled) {

                let paths = result.filePaths;
                if (paths && paths.length > 0) {
                    //console.log(SaxonJS);
                    if (paths[0].toLowerCase().includes(".pdf")) {
                        win.webContents.send('pdf-open', [
                            paths[0], null]);
                        // check if the PDF contains embedded xml files

                        var loadingTask = pdfjsLib.getDocument(paths[0]);
                        loadingTask.promise.then(function (pdf) {
                            pdf.getAttachments().then(function (embeddedFiles) {
                                let embeddedXML = null;
                                if (Array.isArray(embeddedFiles)) {
                                    if ("factur-x.xml" in embeddedFiles) {
                                        embeddedXML = new TextDecoder().decode(embeddedFiles["factur-x.xml"]["content"]);
                                    }
                                    if ("zugferd-invoice.xml" in embeddedFiles) {
                                        // the embedded file can also be named zugferd-invoice.xml
                                        // if it contained uppercaps like ZUGFeRD-invoice.xml it would be ZF1
                                        embeddedXML = new TextDecoder().decode(embeddedFiles["zugferd-invoice.xml"]["content"]);
                                    }

                                }

                                if (embeddedXML !== null) {
                                    transformAndDisplayCII(paths[0] + " (embedded xml)", embeddedXML);
                                }
                            })
                            // you can now use *pdf* here
                    }).catch(error => displayError("Exception", error.getMessage()));

                    } else {
                        loadAndDisplayXML(paths[0]);
                    }
                }
            }
        }
    )
    ;

}

