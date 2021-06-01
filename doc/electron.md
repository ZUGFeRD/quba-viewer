Electron development
=============


We learned a lot from Vuika 2019 book  
("Electron Projects: Build over 9 cross-platform desktop applications from scratch").

There is also a interesting and inspiring (e.g. tabs) open-source-project of a
[Electron based PDF Viewer](https://github.com/sagargurtu/lector).

IPC
-------------
There is a hard distinction between the main process and the browser process.
If the browser wants to tell anything to the main process it
sends an according signal, let's call it `click-button`

```
<script>
    const {ipcRenderer} = require('electron');
    function clickButton() {
        console.log("firing click-button signal");
        ipcRenderer.send('click-button', null, true)
    }
</script>
<button onclick="clickButton()" id="new-window">Open File</button>
```

and the main process picks it up via
```
ipcMain.on('click-button', (event, arg) => {
    console.log("click-button received ", event, arg);
    }
})
```
If the main process, e.g. the menu, wants to tell anything to the browser it also 
sends a signal, e.g.
```
    const window=BrowserWindow.getFocusedWindow();
    window.webContents.send('eventForBrowser', null, true)
```
and in the browser's script the event can be picked up like this
```
    ipcRenderer.on('eventForBrowser', (event, data) => {
        console.log("received event for browser", event, data)
    })
```