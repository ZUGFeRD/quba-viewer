const { Menu, BrowserWindow, ipcMain } =require('electron');
const axios = require('axios').default;
const xml2js = require('xml2js');
const FormData = require('form-data');
var nfs = require('fs');
const config = require('./config/app.config');
const menu = null;
let data;

function MenuFactoryService(menuList) {
  this.menu = menuList;
  this.buildMenu = buildMenu;
}

function buildMenu(app, mainWindow, i18n, openFile) {
  data = {
    title: i18n.t("About") + " " +"Quba",
    validationTitle: i18n.t("Validate"),
    appName: i18n.t("appName"),
    version: i18n.t("version") + " " + app.getVersion(),
  };
  const languageMenu = config.languages.map((languageCode) => {
    return {
      label: i18n.t(languageCode),
      type: "radio",
      checked: i18n.language === languageCode,
      click: () => {
        i18n.changeLanguage(languageCode);
      },
    };
  });
  const menuConfig = [
    {
      label: i18n.t("File"),
      id: "file-open",
      accelerator: "CmdOrCtrl+O",
      submenu: [
        {
          label: i18n.t("Open File"),
          accelerator: "CmdOrCtrl+O",
          click() {
            openFile();
          },
        },
        {
          type: "separator",
        },
        {
            label: i18n.t("Language"),
            submenu: languageMenu,
          },
          {
            type: "separator",
          },
          {
            label: i18n.t("Print"),
            id: "file-print",
            accelerator: "CmdOrCtrl+P",
            enabled: true,
            submenu: [
              {
                label: "PDF",
                click() {
                  mainWindow.webContents.send("file-print-pdf");
                },
              },
              {
                label: "XML",
                click() {
                  mainWindow.webContents.send("file-print-xml");
                },
              },
            ],
          },
        {
          type: "separator",
        },
        {
          label: i18n.t("Validate"),
          click() {
            mainWindow.webContents.send("validate-click");
          },
        },
        {
          type: "separator",
        },
        {
          label: i18n.t("Setting"),
          submenu: [
              {
                label: i18n.t("Login"),
                click() {
                  openLogin(mainWindow, app, i18n);
                },
              },
            ]
        },
        {
          type: "separator",
        },
        {
          label: i18n.t("Exit"),
          click() {
            app.quit();
          },
        },
      ],
    },
    {
      label: i18n.t("Edit"),
      submenu: [
        { label: i18n.t("Cut"),
          role: "Cut" },
        { label: i18n.t("Copy"),
          role: "Copy" },
        { label: i18n.t("Paste"),
          role: "Paste" },
        { type: "separator" },
        { label: i18n.t("Delete"),
          role: "Delete" },
        { type: "separator" },
        { label: i18n.t("Select all"),
          role: "Selectall" },
      ],
    },
    {
      label: i18n.t("Help"),
      submenu: [
        {
          label: i18n.t("About"),
          click() {
            openAboutWindow(mainWindow, app, i18n);
          },
        },
      ],
    },
   
  ];

  const appMenu = Menu.buildFromTemplate(menuConfig);
  Menu.setApplicationMenu(appMenu);
}

function openAboutWindow(mainWindow,  app, i18n) {
  newWindow = new BrowserWindow({
    height: 185,
    resizable: false,
    width: 400,
    title: data.title,
    parent: mainWindow,
    modal: true,
    minimizable: false,
    fullscreenable: false,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
    },
  });
  newWindow.setMenuBarVisibility(false);

  ipcMain.on("about-info", (event) => {
    event.sender.send("about-info", { ...data });
  });

  newWindow.loadFile("./app/about.html");

  newWindow.on("closed", function() {
    newWindow = null;
  });
}

function openValidationWindow(mainWindow, app, i18n) {
  newWindow = new BrowserWindow({
    height: 285,
    resizable: false,
    width: 600,
    title: data.validationTitle,
    parent: mainWindow,
    modal: true,
    minimizable: false,
    fullscreenable: false,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
    },
  });
  newWindow.setMenuBarVisibility(false);
  ipcMain.on("validation-info", (event) => {
    event.sender.send("validation-info", { 
      ...data,
      name: "",
     });
  });

  newWindow.loadFile("./app/validation.html");

  newWindow.on("closed", function() {
    newWindow = null;
  });
}

function openLogin(mainWindow, app, i18n) {
  let newWindow = new BrowserWindow({
    height: 340,
    width: 400,
    resizable: false,
    title: 'Login',
    alwaysOnTop: true,
    minimizable: false,
    fullscreenable: false,
  });
  newWindow.setMenu(null);
  newWindow.loadFile("./app/login.html");
  // newWindow.webContents.openDevTools();

  newWindow.on("closed", function () {
    newWindow = null;
  });
}


module.exports = new MenuFactoryService(menu);
