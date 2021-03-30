require('saxon-js');
const {app, BrowserWindow, ipcMain, ipcRenderer, Menu, dialog, protocol} = require('electron');
const { autoUpdater } = require('electron-updater');
const electronLocalshortcut = require('electron-localshortcut');


const fs = require('fs');
const path = require('path');
const url = require('url');


let win, aboutWin;;
function createWindow() {
     win = new BrowserWindow({
        width: 800,
        height: 600,
        icon: 'assets/img/logo_small_icon_only.png',
        webPreferences: {
            plugins: true,
            nodeIntegration: true,
            preload: path.join(__dirname, 'js/index.js')
        },
       // frame: false
    })

    //win.loadFile('index.html')

    
    win.loadFile('index.html');
     win.once('ready-to-show', () => {
        autoUpdater.checkForUpdatesAndNotify();
      });
    registerShortcuts();

  //  win.webContents.openDevTools();


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
/*
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
*/
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
          
            newWindow.on('closed', function() {
              newWindow = null
            })
          }

    /*  menu.getMenuItemById('about').click = () => {

  if (!aboutWin) {
        aboutWin = new BrowserWindow({
            width: 300,
            height: 150,
            resizable: false,
            frame: false,
            parent: win,
            modal: true,
            webPreferences: {
                nodeIntegration: true
            },
        });
        aboutWin.loadURL(`file://${__dirname}/about.html`);
            
        aboutWin.on('closed', () => {
            aboutWin = null;
        })

    
}}*/



registerShortcuts();
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
    event.sender.send('app_version', { version: app.getVersion() });
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

ipcMain.on('close-about', () => {
    if (aboutWin!==null) {
        aboutWin.close();
    }
});




//https://stackoverflow.com/questions/31529772/how-to-set-app-icon-for-electron-atom-shell-app
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
                    // console.log (content, paths[0]);

                    let test = SaxonJS.transform({
                        stylesheetFileName: "cii-xr.sef.json",
                        sourceFileName: paths[0],
                        destination: "serialized"
                    }, "async").then(output => {

                        //console.log("first transformation finished", output.principalResult);

                        let xrXML = output.principalResult;

                        let test = SaxonJS.transform({
                            stylesheetFileName: "xrechnung-html.sef.json",
                            sourceText: xrXML,
                            destination: "serialized"
                        }, "async").then(output => {
                            //console.log("second transformation finished", output.principalResult);
                            let HTML = output.principalResult;
                            win.webContents.send('HTML_TRANSFORMED', HTML);
                        }).catch(output => {
                            console.error("error", output);
                        });


                    }).catch(output => {
                        console.error("error", output);
                    });

                    // window.webContents.send('load', content);
                }
            }
        }
    );

    /*if (!files) return;
    const file =files[0];
    const filContent = fs.readFileSync(file).toString();
    console.log(fileContent)
  */
}