const { ipcRenderer } = require("electron/renderer");

ipcRenderer.on('HTML_TRANSFORMED', (event, html) => {
  document.open();
  document.write(html);
  document.close();
});
