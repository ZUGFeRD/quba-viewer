require('saxon-js');
const { app, BrowserWindow, Menu, dialog } = require('electron');
const fs = require('fs');


function createWindow () {
  const win = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      nodeIntegration: true
    }
  })

  win.loadFile('index.html')
}

app.on('ready',function(){
createWindow()

const template = [
  {
    label : 'File',
    submenu : [
      {
        label : 'Open File',
        accelerator: 'CmdOrCtrl+O',
        click(){
          openFile();
        }
        
      },
      {
        type : 'separator'
      },
      {
        label : 'Exit',
        click() {
              app.quit()
           }
      }
    ]
  },

  {
    label : 'Edit',
    submenu : [
      { role : 'undo'},
      { role : 'redo'},
      { type : 'separator'},
      { role : 'cut'},
      { role : 'copy'},
      { role : 'paste'},
      { type : 'separator'},
      { role : 'delete'},
      { type : 'separator'},
      { role : 'selectall'}
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
  const files = dialog.showOpenDialog(BrowserWindow , {
    path: '',
    properties : ['openFile'],
    filters : [{
      name: 'all',
      extensions : ['txt']
    }]
  }).then
  (
      result => {
          if (!result.canceled)
          {
              let paths = result.filePaths;
              if (paths && paths.length > 0) {
                console.log (SaxonJS);
                const content = fs.readFileSync(paths[0]).toString();
                 // console.log (content, paths[0]);

                  let test=SaxonJS.transform({
                    stylesheetFileName: "xrechnung-html.sef.json",
                    sourceFileName: paths[0],
                    destination: "serialized"
                 }, "async").then(output => { console.log(output.principalResult);
                 }).catch(output => { console.error("error",output); });
                 
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