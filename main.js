require('saxon-js');
const {app, BrowserWindow, ipcMain, ipcRenderer, Menu, dialog, protocol} = require('electron');
const fs = require('fs');
const path = require('path');
const url = require('url');

let win;
function createWindow() {
     win = new BrowserWindow({
        width: 800,
        height: 600,
        webPreferences: {
            nodeIntegration: true
        }
    })

    //win.loadFile('index.html')
    win.loadURL(`file://${__dirname}/index.html`);


    win.webContents.openDevTools();

}


/*
function createWindow() {
  win = new BrowserWindow({width:800, height:600,
    webPreferences: {
      nodeIntegration: true
    }
  
  })
  win.loadURL(`file://${__dirname}/index.html`);
  win.webContents.openDevTools();
  

}
*/


/*ipcMain.on('click-button', (event, arg) => {
    console.log("click-button received ", event, arg);
    if (arg == 'true') {
        dialog.showOpenDialog(function (fileNames) {
            if (fileNames == undefined) {
                console.log("no file selected");
            } else {
                console.log("reading ", fileNames[0]);
                readFile(fileNames[0])
            }
        })
    }
})
console.log("click-button registered");
ipcMain.on('openFile', (event, arg) => {
    const {dialog} = require('electron')
    const fs = require('fs')

})

function readFile(filepath) {

    fs.readFile(filepath, 'utf-8', (err, data) => {
        if (err) {
            alert('an error :' + err.message)
            return
        }
        event.sender.send('fileData', data)
    })
}



function readFile(filepath) {
  
  fs.readFile(filepath, 'utf-8', (event,err,data)=>{
    if(err){
      alert('an error :' + err.message)
      return
    }
    event.sender.send('fileData', data)
  })
}*/

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

                },{
                    label: 'Send event to embedded browser',
                    click() {
                        const window=BrowserWindow.getFocusedWindow();
                        window.webContents.send('eventForBrowser', null, true)
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
    ]
    const menu = Menu.buildFromTemplate(template)
    Menu.setApplicationMenu(menu)
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