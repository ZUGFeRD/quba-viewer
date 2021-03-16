const { ipcRenderer } = require("electron/renderer");

ipcRenderer.on('HTML_TRANSFORMED', (event, html) => {
  document.write(html);
});
