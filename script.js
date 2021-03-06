'use strict';


(function () {
  const customTitlebar = require('custom-electron-titlebar')
  const { ipcRenderer, remote } = require('electron/renderer')
  const version = document.getElementById('version')
  const notification = document.getElementById('notification')
  const message = document.getElementById('message')
  const restartButton = document.getElementById('restart-button')

  ipcRenderer.send('app_version')
  ipcRenderer.on('app_version', (event, arg) => {
    ipcRenderer.removeAllListeners('app_version')
    version.innerText = 'Version ' + arg.version
  })

  ipcRenderer.on('update_available', () => {
    ipcRenderer.removeAllListeners('update_available')
    message.innerText = 'A new update is available. Downloading now...'
    notification.classList.remove('hidden')
  })

  ipcRenderer.on('update_downloaded', () => {
    ipcRenderer.removeAllListeners('update_downloaded')
    message.innerText = 'Update Downloaded. It will be installed on restart. Restart now?'
    restartButton.classList.remove('hidden')
    notification.classList.remove('hidden')
  })
  function openExamples() {
    ipcRenderer.send('open-examples', null, true)
  }
  function closeNotification() {
    notification.classList.add('hidden')
  }

  function restartApp() {
    ipcRenderer.send('restart_app')
  }

  class Viewer {
    constructor() {

      this._paths = []

      this._tabs = []

      this._contents = []

      this._buckets = 1

      this._currentTab = null

      this._currentBucket = 0

      this._computeStepTabs()

      this._titleBar = this._getTitleBar()

      this._tabContainer = document.getElementById('tabContainer')
      this._viewerElement = document.getElementById('viewer')
      this._leftSeekElement = document.getElementById('leftSeek')
      this._rightSeekElement = document.getElementById('rightSeek')
    }

    _computeStepTabs() {
      this.stepTabs = Math.floor(window.innerWidth / 100)
    }

    _getTitleBar() {
      return new customTitlebar.Titlebar({
        backgroundColor: customTitlebar.Color.fromHex('#333'),
        icon: './assets/img/logoonly.svg',
      })
    }

    _appendTabsToContainer(bucketPosition) {
      this._tabContainer.innerHTML = ''
      for (
        let i = bucketPosition * this.stepTabs;
        i < this._tabs.length && i < (bucketPosition + 1) * this.stepTabs;
        i++
      ) {
        this._tabContainer.append(this._tabs[i])
      }
    }

  
    _toggleSeek() {
      this._leftSeekElement.classList = []
      this._rightSeekElement.classList = []
      if (this._buckets > 1) {
        if (this._currentBucket === 0) {
          this._leftSeekElement.classList.add('inactive-seek')
          this._rightSeekElement.classList.add('active-seek')
        } else if (this._currentBucket === this._buckets - 1) {
          this._leftSeekElement.classList.add('active-seek')
          this._rightSeekElement.classList.add('inactive-seek')
        } else {
          this._leftSeekElement.classList.add('active-seek')
          this._rightSeekElement.classList.add('active-seek')
        }
      } else {
        this._leftSeekElement.classList.add('inactive-seek')
        this._rightSeekElement.classList.add('inactive-seek')
      }
    }

    _updateBuckets() {
      this._buckets = Math.ceil(this._tabs.length / this.stepTabs)
    }

    _adjustTabs() {
      this._updateBuckets()

      let currentPosition = this._tabs.indexOf(this._currentTab)
      let newBucketPosition = Math.floor(currentPosition / this.stepTabs)

      if (
        newBucketPosition !== this._currentBucket ||
        this._tabContainer.childElementCount !== this.stepTabs
      ) {
        this._appendTabsToContainer(newBucketPosition)
        this._currentBucket = newBucketPosition
      }

      this._toggleSeek()
    }


    _toggleBackgroundInfo(flag) {
      let visibility = flag ? 'visible' : 'hidden'
      document.getElementById('backgroundInfo').style.visibility = visibility
    }

    _createTabElement(pathName) {
      const filename = pathName.substring(pathName.lastIndexOf('\\') + 1)
      const tabElement = document.createElement('div')
      const labelElement = document.createElement('div')
      const closeElement = document.createElement('div')
      let that = this

      labelElement.innerHTML = filename
      labelElement.setAttribute('class', 'file-tab-label')

      closeElement.innerHTML = '&times;'
      closeElement.style.visibility = 'hidden'
      closeElement.setAttribute('class', 'file-tab-close')

      tabElement.classList.add('file-tab')
      tabElement.classList.add('inactive')
      tabElement.setAttribute('data-path', pathName)

      tabElement.append(labelElement)
      tabElement.append(closeElement)

      closeElement.addEventListener('click', event => {
        let positionToRemove = that._tabs.indexOf(tabElement)
        if (that._tabs.length === 1) {

          that._currentTab = null
          that._tabContainer.innerHTML = ''
          that._viewerElement.removeAttribute('src')
          that._toggleMenuItems(false)
          that._toggleBackgroundInfo(true)
        } else if (tabElement === that._currentTab) {

          let newCurrentPosition = positionToRemove

          if (positionToRemove === 0) {
            newCurrentPosition = 1
          } else {

            newCurrentPosition -= 1
          }

          that._switchTab(that._tabs[newCurrentPosition])
        }

        that._paths.splice(positionToRemove, 1)
        that._tabs.splice(positionToRemove, 1)
        that._updateBuckets()

        if (that._tabs.length > 0) {
     
          if (that._tabContainer.childElementCount === 1) {
            that._adjustTabs()
          } else {

            that._appendTabsToContainer(that._currentBucket)
          }
        } else {

          that._toggleTabContainer(false)
          that._updateTitle()
        }
        that._toggleSeek()
        event.stopPropagation()
      })

      tabElement.addEventListener('mouseover', event => {
        if (tabElement !== that._currentTab) {
          closeElement.style.visibility = 'visible'
        }
      })

      tabElement.addEventListener('mouseleave', event => {
        if (tabElement !== that._currentTab) {
          closeElement.style.visibility = 'hidden'
        }
      })

      tabElement.addEventListener('click', event => {
        if (tabElement !== that._currentTab) {
          that._switchTab(tabElement)
        }
      })

      return tabElement
    }

    _propagateClick() {
      window.dispatchEvent(new Event('mousedown'))
    }

    _setViewerEvents() {
      this._viewerElement.contentDocument.addEventListener('click', this._propagateClick)
      this._viewerElement.contentDocument.addEventListener('mousedown', this._propagateClick)
    }

    _openInViewer(pathName, fileContent) {
      this._viewerElement.src = fileContent
      this._viewerElement.onload = this._setViewerEvents.bind(this)
    }

    _focusCurrentTab() {
      this._tabs.forEach(tabElement => {
        tabElement.classList.remove('active')
        tabElement.classList.add('inactive')
        tabElement.getElementsByClassName('file-tab-close')[0].style.visibility = 'hidden'
      })
      this._currentTab.classList.remove('inactive')
      this._currentTab.classList.add('active')
      this._currentTab.getElementsByClassName('file-tab-close')[0].style.visibility = 'visible'
      this._openInViewer(
        this._paths[this._tabs.indexOf(this._currentTab)],
        this._contents[this._tabs.indexOf(this._currentTab)]
      )
    }


    _switchTab(tabElement) {
      if (this._currentTab !== tabElement) {
        this._currentTab = tabElement
        this._updateTitle(this._paths[this._tabs.indexOf(tabElement)])
        this._adjustTabs()
        this._focusCurrentTab()
      }
    }

    _toggleTabContainer(visible) {
      const visibility = visible ? 'visible' : 'hidden'
      this._tabContainer.style.visibility = visibility
      this._leftSeekElement.style.visibility = visibility
      this._rightSeekElement.style.visibility = visibility
    }


    _toggleMenuItems(flag) {
      ipcRenderer.send('toggle-menu-items', flag)
    }


    _addTab(pathName, fileContent) {

      if (this._tabs.length === 0) {
        this._toggleTabContainer(true)
        this._toggleMenuItems(true)
        this._toggleBackgroundInfo(false)
      }


      if (this._paths.indexOf(pathName) >= 0) {
        this._switchTab(this._tabs[this._paths.indexOf(pathName)])
        return
      }

      const tabElement = this._createTabElement(pathName)

      this._currentTab = tabElement
      this._tabs.push(tabElement)
      this._contents.push(fileContent)
      this._paths.push(pathName)
      this._tabContainer.append(tabElement)
      this._adjustTabs()
      this._focusCurrentTab()
    }

    _updateTitle(pathName) {
      if (pathName) {
        this._titleBar.updateTitle(pathName.substring(pathName.lastIndexOf('\\') + 1) + ' - Quba')
      } else {
        this._titleBar.updateTitle('Quba')
      }
    }

    _openFile(pathName, fileContent) {
      this._updateTitle(pathName)
      this._addTab(pathName, fileContent)
    }

    _setMenuItemEvents() {
      ipcRenderer.on('file-open', (event, args) => {
        this._propagateClick()
        this._openFile(args[0], args[1])
      })

      ipcRenderer.on('file-print', (event, args) => {
        window.frames['viewer'].focus()
        window.frames['viewer'].print()
      })
    }

    run() {
      this._setMenuItemEvents()
      this._setViewerEvents()
    }
  }

  const application = new Viewer()
  application.run()
  const linkExamples = document.getElementById("linkExamples")
  linkExamples.addEventListener("click", function (event) {   openExamples() })
})()
