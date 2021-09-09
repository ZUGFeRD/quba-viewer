/*const menuConfig = [
    {
      label: "File",
      id: "file-open",
      accelerator: "CmdOrCtrl+O",
      submenu: [
        {
          label: "Open File",
          accelerator: "CmdOrCtrl+O",
          click() {
            openFile();
          },
        },
        {
          type: "separator",
        },
        {
          label: "Print",
          id: "file-print",
          accelerator: "CmdOrCtrl+P",
          enabled: false,
          click() {
            win.webContents.send("file-print");
          },
        },
        {
          type: "separator",
        },
        {
          label: "Exit",
          click() {
            app.quit();
          },
        },
      ],
    },
    {
      label: "Edit",
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
      label: "Help",
      submenu: [
        {
          label: "About",
          click() {
            openAboutWindow();
          },
        },
      ],
    },
  ];
  
  exports.menuConfig = menuConfig;
  */