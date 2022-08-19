const { contextBridge, ipcRenderer, Menu, app, remote } = require('electron');
const { Titlebar, Color } = require("custom-electron-titlebar");

let myTitleBar;

const setTitleBar = () => {
  let options = {
    backgroundColor: Color.fromHex("#1f2c40"),
    shadow: true,
    icon: "../src/assets/img/logoonly.svg",
  }
  myTitleBar = new Titlebar(options);
}

window.addEventListener('DOMContentLoaded', () => {
  setTitleBar();
});

const API = {
  sendOpenMenu: () => {
    ipcRenderer.send('open-menu');
  },
  sendValidation: () => {
    ipcRenderer.send('open-validation');
  },
  sendSyncCheckXml: (path) => {
    return ipcRenderer.sendSync('check-xml', path);
  },
  sendAppVersion: () => {
    ipcRenderer.send('app_version');
  },
  sendOpenDraggedFile: (path) => {
    ipcRenderer.send('open-dragged-file', path);
  },
  sendRestartApp: () => {
    ipcRenderer.send('restart_app');
  },
  sendOpenLink: () => {
    ipcRenderer.send('open-link');
  },
  onPdfOpen: (callback) => {
    ipcRenderer.on("pdf-open", (event, ...args) => callback(event, ...args));    
  },
  onXmlOpen: (callback) => {
    ipcRenderer.on("xml-open", (event, ...args) => callback(event, ...args));    
  },
  onLanguageChange: (callback) => {
    ipcRenderer.on("language-change", (event, ...args) => callback(event, ...args));    
  },
  onAppVersion: (callback) => {
    ipcRenderer.on("app_version", (event, ...args) => callback(event, ...args));    
  },
  onUpdateAvailable: (callback) => {
    ipcRenderer.on("update_available", (event, ...args) => callback(event, ...args));    
  },
  onUpdateDownloaded: (callback) => {
    ipcRenderer.on("update_downloaded", (event, ...args) => callback(event, ...args));    
  },
  onFilePrintXml: (callback) => {
    ipcRenderer.on("file-print-xml", (event, ...args) => callback(event, ...args));    
  },
  onFilePrintPdf: (callback) => {
    ipcRenderer.on("file-print-pdf", (event, ...args) => callback(event, ...args));    
  },
  removeAllAppVersion: () => {
    ipcRenderer.removeAllListeners('app_version');
  },
  removeAllUpdateAvailable: () => {
    ipcRenderer.removeAllListeners('update_available');
  },
  removeAllUpdateDownloaded: () => {
    ipcRenderer.removeAllListeners('update_downloaded');
  },
  onFileValidate: (callback) => {
    ipcRenderer.on("file-validate", (event, ...args) => callback(event, ...args));
  },
  setMenu: () => {
    setTitleBar();
  },
  updateMenuLanguage: (appName) => {
    if (myTitleBar) {
      myTitleBar.dispose();

    setTitleBar();
    myTitleBar.updateTitle(appName);
    }
  }
}

contextBridge.exposeInMainWorld('api', API);