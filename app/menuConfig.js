const { Menu, BrowserWindow, ipcMain, ipcRenderer, safeStorage } = require("electron");
const path = require("path");
const config = require("./config/app.config");
const Store = require("electron-store");
const store = new Store();
const menu = null;
let loginWindow, aboutWindowTranslation, loginWindowTranslation;

function MenuFactoryService(menuList) {
  this.menu = menuList;
  this.buildMenu = buildMenu;
}

function buildMenu(app, mainWindow, i18n, openFile, openDir) {
  initializeTranslation(app, i18n);
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
  if (typeof store.get("showIds")=="undefined") { // e.g. on first start
    store.set("showIds",false);
  }
  let doShowIds = store.get("showIds");

  const mainSubMenu = [
    {
      label: i18n.t("Open File"),
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
      label: i18n.t("Show IDs"),
      type: "checkbox",
      checked: doShowIds,
      click() {
        doShowIds=!doShowIds;
        store.set("showIds", doShowIds);
      },
    },
    {
      type: "separator",
    },
    {
      label: i18n.t("Print"),
      id: "file-print",
      accelerator: "CmdOrCtrl+P",
      enabled: false,
      submenu: [
        {
          label: "PDF",
          id: "file-print-pdf",
          enabled: false,
          click() {
            mainWindow.webContents.send("file-print-pdf");
          },
        },
        {
          label: "XML",
          id: "file-print-xml",
          enabled: false,
          click() {
            /*
            mainWindow.webContents.print will only print a part of the first page->copy the xmlViewer html contents
            into a separate window
            * */
            mainWindow.webContents.executeJavaScript("document.getElementById('xmlViewer').innerHTML").then( (result) => {
              // get HTML into result, create invisible window
              let newWindow = new BrowserWindow({
                show: false,
                height: 185,
                resizable: false,
                width: 400,
                parent: mainWindow,
                modal: true,
                minimizable: false,
                fullscreenable: false,
                webPreferences: {
                  nodeIntegration: true,
                  contextIsolation: false,
                },
              });
              // start loading from result
              newWindow.loadURL('data:text/html;charset=utf-8,'+encodeURIComponent(result));
              // wait for load
              newWindow.webContents.on('did-finish-load', () => {
                // after load start printing
                newWindow.webContents.print({}, (success, failureReason) => {
                  // after printing dispose invisible window
                  newWindow.close();
                });
              });
            });
          },
        },
      ],
    },
    {
      type: "separator",
    },
    {
      id: "validate",
      enabled: false,
      label: i18n.t("Validate"),
      // enabled: store.get("isLoggedIn") == true ? true : false,
      click() {
        const isLoggedIn = store.get("isLoggedIn");
        if (!isLoggedIn) {
          openLogin(mainWindow, app, i18n);
        } else {
          mainWindow.webContents.send("validate-click");
        }
      },
    },
    {
      type: "separator",
    },
    {
      id: "logout",
      label: i18n.t("Logout"),
      visible: store.get("isLoggedIn") == true ? true : false,
      click() {
        store.delete("access_token");
        store.delete("isLoggedIn");

        mainWindow.webContents.send("logout-submit");
        Menu.getApplicationMenu().getMenuItemById("logout").visible = false;

        ipcMain.on("logout-submit", (event, data) => {
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
  ];

  const menuConfig = [
    {
      label: i18n.t("File"),
      id: "file-open",
      submenu: mainSubMenu,
    },
    {
      label: i18n.t("Edit"),
      submenu: [
        { label: i18n.t("Cut"), role: "Cut" },
        { label: i18n.t("Copy"), role: "Copy" },
        { label: i18n.t("Paste"), role: "Paste" },
        { type: "separator" },
        { label: i18n.t("Delete"), role: "Delete" },
        { type: "separator" },
        { label: i18n.t("Select all"), role: "Selectall" },
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

function initializeTranslation(app, i18n) {
  aboutWindowTranslation = {
    title: i18n.t("About") + " " + "Quba",
    validationTitle: i18n.t("Validate"),
    appName: i18n.t("appName"),
    license: i18n.t("licenseText"),
    version: i18n.t("version") + " " + app.getVersion(),
  };

  loginWindowTranslation = {
    login: i18n.t("Login"),
    demoUser: i18n.t("Demo User"),
    username: i18n.t("Username"),
    password: i18n.t("Password"),
    //privacyPolicy: i18n.t("Please validate only anonymized test data without any personal information (sender, recipient, bank credentials) unless you have a subscription with a contract for commissioned data processing."),
    privacyPolicy: i18n.t("Privacy Policy"),
    policyText: i18n.t("privacyPolicyText"),
    subscription: i18n.t("Subscription"),
    accept: i18n.t("Accept"),
  };
}

function openAboutWindow(mainWindow, app, i18n) {
  let newWindow = new BrowserWindow({
    height: 185,
    resizable: false,
    width: 400,
    title: aboutWindowTranslation.title,
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
    event.sender.send("about-info", { ...aboutWindowTranslation });
  });

  newWindow.loadFile("./app/about.html");

  newWindow.on("closed", function () {
    newWindow = null;
  });
}
function openLogin(mainWindow, app, i18n) {
  if (loginWindow && loginWindow.close) {
    loginWindow.close();
  }
  loginWindow = new BrowserWindow({
    height: 500,
    width: 400,
    resizable: false,
    title: i18n.t("Login"),
    minimizable: false,
    fullscreenable: false,
    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
    },
  });
  loginWindow.setMenu(null);
 // console.log("deb abc",safeStorage.isEncryptionAvailable());
  loginWindow.loadFile("./app/login.html");
  loginWindow.on("closed", function () {
    loginWindow = null;
  });

  ipcMain.on("get-login-info", (event) => {
    event.sender.send("get-login-info", { ...loginWindowTranslation });
  });

  ipcMain.on("login-submit", (event, data) => {
    mainWindow.webContents.send("show-login-message", data);
    if (data.type === "success") {
      const accessToken = data.message;
      store.set("access_token", accessToken);

      store.set("isLoggedIn", true);
      // Menu.getApplicationMenu().getMenuItemById("login").visible = false;
      Menu.getApplicationMenu().getMenuItemById("logout").visible = true;
      // Menu.getApplicationMenu().getMenuItemById("validate").enabled = true;
      // mainWindow.webContents.send("validate-click");
    }
    loginWindow.close();
  });
}

module.exports = new MenuFactoryService(menu);
