const { Menu, BrowserWindow } = require("electron");
const config = require('./config/app.config');
const menu = null;
let newWindow;

function MenuFactoryService(menuList) {
  this.menu = menuList;
  this.buildMenu = buildMenu;
}

function buildMenu(app, mainWindow, i18n, openFile) {
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
          enabled: false,
          click() {
            mainWindow.webContents.send("file-print");
          },
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
            openAboutWindow(mainWindow);
          },
        },
      ],
    },
   
  ];

  const appMenu = Menu.buildFromTemplate(menuConfig);
  Menu.setApplicationMenu(appMenu);
}

function openAboutWindow(mainWindow) {
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

  newWindow.loadFile("./app/about.html");

  newWindow.on("closed", function() {
    newWindow = null;
  });
}

module.exports = new MenuFactoryService(menu);
