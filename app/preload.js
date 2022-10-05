const { contextBridge, ipcRenderer, Menu, app, remote } = require('electron');
const { Titlebar, Color } = require("custom-electron-titlebar");

let myTitleBar;

const setTitleBar = () => {
  let options = {
    backgroundColor: Color.fromHex("#1f2c40"),
    shadow: true,
    icon: "../src/assets/img/logoonly.svg",
  };
  myTitleBar = new Titlebar(options);
};

window.addEventListener('DOMContentLoaded', () => {
  setTitleBar();
});

const API = {
  sendOpenMenu: () => {
    ipcRenderer.send('open-menu');
  },
  sendSyncCheckXml: (path) => {
    return ipcRenderer.sendSync('check-xml', path);
  },
  sendSyncValidateFile: (path) => {
    const response = ipcRenderer.sendSync("validate-file", path);
    if (response.code === 'ERR_UNAUTHORIZED') {
      myTitleBar.dispose();
      setTitleBar();
    }
    return response;
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
  sendLoginSubmit: () => {	
    console.log("sendLoginSubmit");	
    ipcRenderer.send('login-submit');	
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
    ipcRenderer.on("app_version", (event, ...args) => 
    callback(event, ...args));    
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
  onValidateComplete: (callback) => {	
    ipcRenderer.on("validate-complete", (event, ...args) => callback(event, ...args));	
  },
  onValidateClick: (callback) => {
    ipcRenderer.on("validate-click", (event, ...args) => callback(event, ...args));    
  },
  onShowLoginMessage: (callback) => {	
    ipcRenderer.on("show-login-message", (event, ...args) => 
    {
      if (args[0].type === 'success') {
        myTitleBar.dispose();
        setTitleBar();
      }
      console.log("onShowLoginMessage", args);
      /*if (args[0].isDefaultUser) {
        localStorage.setItem('isDefaultUser', true);
      } else {
        localStorage.setItem('isDefaultUser', false);
      }*/
    callback(event, ...args)});
    
  },
  onLogoutSubmit: (callback) => {
    ipcRenderer.on("logout-submit", (event, ...args) => {
      myTitleBar.dispose();
      setTitleBar();
      callback(event, ...args);
    });
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
  setMenu: () => {
    setTitleBar();
  },
  updateMenuLanguage: (appName) => {
    if (myTitleBar) {
      myTitleBar.dispose();

    setTitleBar();
    myTitleBar.updateTitle(appName);
    }
  },
};

contextBridge.exposeInMainWorld('api', API);