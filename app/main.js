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
const store = new Store();

let mainWindow;
let currentLanguage = store.get("language") || config.fallbackLng;

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
        frame: false
    })

  mainWindow.loadFile(path.join(__dirname, "../dist/index.html"));
  mainWindow.on("closed", function() {
    mainWindow = null;
    });

  menuFactoryService.buildMenu(app, mainWindow, i18next, openFile);
  i18next.on("languageChanged", (lng) => {
    currentLanguage = lng;
    store.set("language", lng);
    mainWindow.webContents.send("language-change", lng);
    menuFactoryService.buildMenu(app, mainWindow, i18next, openFile);
    
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

  function registerShortcuts() {
    electronLocalShortcut.register(mainWindow, "CommandOrControl+B", () => {
      mainWindow.webContents.openDevTools();
    });
  }

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
      path.join(__dirname, "xslt", "cii-xr.sef.json"),
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
            stylesheetFileName: path.join(__dirname, "xslt", "xrechnung-html." + currentLanguage + ".sef.json"),
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

