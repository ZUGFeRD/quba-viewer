const Menu = require("electron").Menu;
const customTitlebar = require('custom-electron-titlebar');
const config = require("./configs/app.config");

const menu = null;
const platform = process.platform;

function MenuFactoryService(appMenu) {
  this.menu = appMenu;

  this.buildMenu = buildMenu;
}

const getMenuConfig = (app, mainWindow) => {
  
};

function buildMenu(app, mainWindow) {
  this.menu = Menu.buildFromTemplate(getMenuConfig(app, mainWindow));
  Menu.setApplicationMenu(this.menu);
}


module.exports = new MenuFactoryService(menu);
