const SaxonJS = require('saxon-js');
const {app, BrowserWindow, ipcMain, ipcRenderer, Menu, dialog, protocol} = require('electron');
const {autoUpdater} = require('electron-updater');
const electronLocalShortcut = require("electron-localshortcut");
const pdfjsLib = require('pdfjs-dist/legacy/build/pdf');
const Store = require('electron-store');
const menuFactoryService = require("./menuConfig");

var i18next = require("i18next");
var Backend = require("i18next-node-fs-backend");
const i18nextOptions = require("./config/i18next.config");

const fs = require('fs');
const path = require('path');
const url = require('url');

let mainWindow;
let newWindow;

/*function changeLanguage(newISOcode) {
  i18next.changeLanguage(newISOcode);
  const store = new Store();

  store.set('language', newISOcode);
}*/

function createWindow() {
  mainWindow = new BrowserWindow({
        width: 800,
        height: 600,
        webPreferences: {
            plugins: true,
            nodeIntegration: true,
            enableRemoteModule: true,
            contextIsolation: false,
        },
        //frame: false
    })

  mainWindow.loadFile(path.join(__dirname, "../dist/index.html"));
  mainWindow.on("closed", function() {
    mainWindow = null;
    });

  menuFactoryService.buildMenu(app, mainWindow, i18next, openFile);
  i18next.on("languageChanged", (lng) => {
    menuFactoryService.buildMenu(app, mainWindow, i18next, openFile);
    mainWindow.webContents.send("language-change", lng);
  });

    mainWindow.once("ready-to-show", () => {
    autoUpdater.checkForUpdatesAndNotify();
  });

  setTimeout(() => {
    mainWindow.webContents.send("goToHome");
  }, 2000);
}


app.on("ready", async () => {
  const t = await i18next.use(Backend).init(i18nextOptions);
  createWindow();
  registerShortcuts();
});

  app.on("window-all-closed", function() {
    if (process.platform !== "darwin") app.quit();
  });
  
app.on("activate", function() {
    if (mainWindow === null) createWindow();
  });

  /*i18next.on("loaded", (loaded) => {
    
    const store = new Store();
// we need a check here and a default language because on a fresh installation we might get  null or undefined
    i18next.changeLanguage(store.get('language'));
    i18next.off("loaded");
  });
  
  i18next.on("languageChanged", (lng) => {
    console.log("languageChanged");
    setMenu();
  });*/
  
  function registerShortcuts() {
    electronLocalShortcut.register(mainWindow, "CommandOrControl+B", () => {
      mainWindow.webContents.openDevTools();
    });
  }
  
  /*function setMenu() {
    const menuConfig = [
      {
        // label: "File",
        label: i18next.t("File"),
        id: "file-open",
        accelerator: "CmdOrCtrl+O",
        submenu: [
          {
            // label: "Open File",
            label: i18next.t("OpenFile"),
            accelerator: "CmdOrCtrl+O",
            click() {
              openFile();
            },
          },
          {
            type: "separator",
          },
          {
          label: i18next.t("Language"),
          id: "language",
         
          submenu: [
          {
          label: i18next.t("German"),
          id: "lang-de",
          type: "normal",
          //icon: "../assets/img/german.png",
          click() {
            changeLanguage("de");
          },

        },
        {
        label: i18next.t("English"),
        id: "lang-en",
        type: "normal",
        click() {
          changeLanguage("en");
        },
      },
      {
      label: i18next.t("French"),
      id: "lang-fr",
      type: "normal",
      //icon:"../assets/img/german.png",
      click() {
        changeLanguage("fr");
      },
    },
  ]
  },
        {
          type: "separator",
        },
          {
            // label: "Print",
            label: i18next.t("print"),
            id: "file-print",
            accelerator: "CmdOrCtrl+P",
            enabled: false,
            click() {
              mainWindow.webContents.send("file-print");
            },
          },
          {
            type: "separator",
          },
          {
            // label: "Exit",
            label: i18next.t("exit"),
            click() {
              app.quit();
            },
          },
        ],
      },
      {
        // label: "Edit",
        label: i18next.t("Edit"),
        submenu: [
          { role: "cut" },
          { role: "copy" },
          { role: "paste" },
          { type: "separator" },
          { role: "delete" },
          { type: "separator" },
          { role: "selectall" },
        ],
      },
      {
        // label: "Help",
        label: i18next.t("Help"),
        submenu: [
          {
            // label: "About",
            label: i18next.t("about"),
            click() {
              openAboutWindow();
            },
          },
        ],
      },
    ];
    const menu = Menu.buildFromTemplate(menuConfig);
    Menu.setApplicationMenu(menu);
    ipcMain.on("toggle-menu-items", (event, flag) => {
      menu.getMenuItemById("file-print").enabled = flag;
    });
  }

  function openAboutWindow() {
    if (newWindow) {
      newWindow.focus();
      return;
    }

    newWindow = new BrowserWindow({
        height: 185,
        resizable: false,
        width: 400,
        title: "",
        parent: mainWindow,
        modal: true,
        minimizable: false,
        fullscreenable: false,
        webPreferences: {
          nodeIntegration: true,
        },
      });
      newWindow.setMenuBarVisibility(false);
    
      newWindow.loadFile("./about.html");
    
      newWindow.on("closed", function() {
        newWindow = null;
      });
    }*/

 
const gotTheLock = app.requestSingleInstanceLock();
if (!gotTheLock) {
  app.quit();
} else {
  listenEvents();
}

function listenEvents() {
    app.on("second-instance", (event, commandLine) => {
      if (mainWindow) {
        if (mainWindow.isMinimized()) {
          mainWindow.restore();
        }
        mainWindow.focus();
        mainWindow.webContents.send("external-file-open", commandLine);
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
      event.sender.send("app_version", { version: app.getVersion() });
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
      console.log('coming here in check-xml');
      try {
        var loadingTask = pdfjsLib.getDocument(filePath);
        loadingTask.promise
          .then(function(pdf) {
            pdf.getAttachments().then(function(embeddedFiles) {
              let embeddedXML = null;
              if (typeof embeddedFiles == "object") {
                if (embeddedFiles && embeddedFiles["factur-x.xml"]) {
                  embeddedXML = new TextDecoder().decode(
                    embeddedFiles["factur-x.xml"]["content"]
                  );
                }
                if (embeddedFiles && embeddedFiles["zugferd-invoice.xml"]) {
                  // the embedded file can also be named zugferd-invoice.xml
                  // if it contained uppercaps like ZUGFeRD-invoice.xml it would be ZF1
                  embeddedXML = new TextDecoder().decode(
                    embeddedFiles["zugferd-invoice.xml"]["content"]
                  );
                }
              }
              if (embeddedXML !== null) {
                transformAndDisplayCII(
                  filePath + " (embedded xml)",
                  embeddedXML,
                  false
                ).then((res) => {
                  event.returnValue = res ? res : undefined;
                });
              } else {
                event.returnValue = undefined;
              }
            });
            // you can now use *pdf* here
          })
          .catch((error) => {
            displayError("Exception", error.getMessage());
          });
      } catch (error) {
        event.returnValue = undefined;
        console.error("Error", error);
      }
    });
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
        // parse root node
        if (key.includes("CrossIndustryInvoice")) {
          transformAndDisplayCII(filename, content, true);
        } else if (key.includes("Invoice")) {
          transformAndDisplayUBL(filename, content, true);
        } else {
          displayError(
            "File format not recognized",
            "Is it a UBL 2.1 or UN/CEFACT 2016b XML file or PDF you are trying to open?"
          );
        }
      }
    } catch (e) {
      displayError("Exception", e.message);
    }
  }
  
  function transformAndDisplayCII(sourceFileName, content, shouldDisplay) {
    return transformAndDisplay(
      sourceFileName,
      content,
      path.join(__dirname, "xslt/cii-xr.sef.json"),
      shouldDisplay
    );
  }
  
  function transformAndDisplayUBL(sourceFileName, content, shouldDisplay) {
    return transformAndDisplay(
      sourceFileName,
      content,
      path.join(__dirname, "xslt/ubl-xr.sef.json"),
      shouldDisplay
    );
  }
  
  function transformAndDisplay(
    sourceFileName,
    content,
    stylesheetFileName,
    shouldDisplay
  ) {
    return SaxonJS.transform(
      {
        stylesheetFileName,
        sourceText: content,
        destination: "serialized",
      },
      "async"
    )
      .then((output) => {
        let xrXML = output.principalResult;
  
        return SaxonJS.transform(
          {           
            stylesheetFileName: path.join(__dirname, "xslt/xrechnung-html.sef.json"),
            sourceText: xrXML,
            destination: "serialized",
          },
          "async"
        )
          .then((response) => {
            let HTML = response.principalResult;
            const htmlStr = `data:text/html;base64,${Buffer.from(HTML).toString(
              "base64"
            )}`;
            if (shouldDisplay) {
              mainWindow.webContents.send("xml-open", [sourceFileName, htmlStr]); // send to be displayed
            }
            return htmlStr;
          })
          .catch((error) => {
            displayError("Exception", error.getMessage());
          });
      })
      .catch((output) => {
        displayError("Exception", output.getMessage());
      });
  }
  function displayError(message, detail) {
    console.error(message, detail);
    const options = {
      type: "error",
      buttons: ["OK"],
      defaultId: 1,
      title: "Error",
      message,
      detail,
    };
    dialog.showMessageBox(null, options, (response, checkboxChecked) => {});
  }

