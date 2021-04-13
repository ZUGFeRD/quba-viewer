require('saxon-js');
const {app, BrowserWindow, ipcMain, ipcRenderer, Menu, dialog, protocol} = require('electron');
const {autoUpdater} = require('electron-updater');
const electronLocalshortcut = require('electron-localshortcut');

const fs = require('fs');
const path = require('path');
const url = require('url');

let win, aboutWin;
;

function createWindow() {
    win = new BrowserWindow({
        width: 800,
        height: 600,
        icon: 'assets/img/logo.png',
        webPreferences: {
            plugins: true,
            nodeIntegration: true,
        },
        // frame: false
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

app.on('ready', function () {
    createWindow()

    const template = [
        {
            label: 'File',
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
                {role: 'undo'},
                {role: 'redo'},
                {type: 'separator'},
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

function transformCII(sourceFileName) {
    return transform(sourceFileName, path.join(__dirname, "cii-xr.sef.json"));
}

function transformUBL(sourceFileName) {
    return transform(sourceFileName, path.join(__dirname, "ubl-xr.sef.json"));
}

function transform(sourceFileName, stylesheetFileName) {

    let test = SaxonJS.transform({
        stylesheetFileName,
        sourceFileName,
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
            win.webContents.send('HTML_TRANSFORMED', HTML); // send to be displayed
            return HTML;
        }).catch(output => {
            console.error("error", output);
        });

    }).catch(output => {
        console.error("error", output);
    });

}

function openFile() {
    const files = dialog.showOpenDialog(BrowserWindow, {
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
                        const content = fs.readFileSync(paths[0]).toString();
                        var parser = require('fast-xml-parser');
                        let json = parser.parse(content);
                        for (let key in json) {
                            // parse root node
                            if (key.includes("CrossIndustryInvoice")) {
                                transformCII(paths[0]);
                            } else if (key.includes("Invoice")) {
                                transformUBL(paths[0]);
                            } else {
                                console.error("XML format not recognized");

                            }
                        }

                    }

                    // console.log (content, paths[0]);
                }

            }
        )
    ;

}