const { Menu, BrowserWindow, ipcMain,ipcRenderer } =require('electron');
const path = require("path");
const config = require('./config/app.config');
const Store = require("electron-store");
const store = new Store();
const menu = null;
let data,loginWindow;

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

  const mainSubMenu = [
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
      id: 'validate',
      label: i18n.t("Validate"),
      enabled: store.get("isLoggedIn") == true ? true : false,
      click() {
        const isLoggedIn = store.get("isLoggedIn");
        console.log("isLoggedIn", isLoggedIn);
        if (!isLoggedIn) {
          openLogin(mainWindow, app, i18n);
        } else {
          mainWindow.webContents.send("validate-click");
        }
      },
    },
    /*{
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
    },*/
  ];
  
  mainSubMenu.push({
    type: "separator",
  })
  mainSubMenu.push({
    id: 'logout',
    label: i18n.t("Logout"),
    visible: store.get("isLoggedIn") == true ? true : false,
    click() {
      store.delete("access_token");
      store.delete("isLoggedIn");
      console.log("Logout clicked.");
      
      ipcMain.emit('logout-submit');

      ipcMain.on('logout-submit', (event, data) => {
        console.log("inside logout-submit");
        // mainWindow.webContents.send('show-login-message', data);
        // if (data.type === 'success') {
        //   const accessToken = data.message;
        //   store.set("access_token", accessToken);
        //   store.set("isLoggedIn", true);
        //   Menu.getApplicationMenu().getMenuItemById('login').visible = false;
        //   Menu.getApplicationMenu().getMenuItemById('logout').visible = true;
        //   Menu.getApplicationMenu().getMenuItemById('validate').enabled = true;
        //   // mainWindow.webContents.send("validate-click");
        // }
      });
    },
  })

  mainSubMenu.push({
    id: 'login',
    label: i18n.t("Login"),
    visible: store.get("isLoggedIn") == true ? false : true,
    click() {
      openLogin(mainWindow, app, i18n);
    },
  })

  mainSubMenu.push({
    type: "separator",
  })
  mainSubMenu.push({
    label: i18n.t("Exit"),
    click() {
      app.quit();
    },
  })

  const menuConfig = [
    {
      label: i18n.t("File"),
      id: "file-open",
      accelerator: "CmdOrCtrl+O",
      submenu: mainSubMenu,
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


  console.log("menuConfig",menuConfig);

  const appMenu = Menu.buildFromTemplate(menuConfig);
  Menu.setApplicationMenu(appMenu);
}

function openAboutWindow(mainWindow,  app, i18n) {
  let newWindow = new BrowserWindow({
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
function openLogin(mainWindow, app, i18n) {
  if (loginWindow && loginWindow.close) {	
    loginWindow.close();	
  }
  loginWindow = new BrowserWindow({
    height: 340,
    width: 400,
    resizable: false,
    title: 'Login',
    minimizable: false,
    fullscreenable: false,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
    },
  });
  loginWindow.setMenu(null);
  loginWindow.loadFile("./app/login.html");
  loginWindow.on("closed", function () {
    loginWindow = null;
  });
  ipcMain.on('login-submit', (event, data) => {
    console.log("inside login-submit", data);
    mainWindow.webContents.send('show-login-message', data);
    if (data.type === 'success') {
      const accessToken = data.message;
      store.set("access_token", accessToken);
      store.set("isLoggedIn", true);
      Menu.getApplicationMenu().getMenuItemById('login').visible = false;
      Menu.getApplicationMenu().getMenuItemById('logout').visible = true;
      Menu.getApplicationMenu().getMenuItemById('validate').enabled = true;
      // mainWindow.webContents.send("validate-click");
    }
    loginWindow.close();
    //console.log("event", data);
  }) 
}


module.exports = new MenuFactoryService(menu);
