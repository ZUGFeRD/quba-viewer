<template>
  <vue3-tabs-chrome
      :ref="setTabRef"
      :tabs="tabs"
      v-model="tab"
      :dragable="true"
      :swappable="true"
      :click="onClick"
      :class="[tabs?.length ? '' : 'display-none']"
      :on-close="onClose"
  >
    <template v-slot:after>
      <button
          class="btn"
          style="
          height: 20px;
          line-height: 20px;
          padding: 0 10px;
          margin-left: 0px;
        "
          @click="handleAdd"
      >
        +
      </button>
    </template>
  </vue3-tabs-chrome>
  <div class="btns"></div>
  <div v-if="currentTab">
    <div class="loader" v-if="showLoader">
      <div id="loading"></div>
      <h1>{{ t("validatingFile", {}, {locale: lang}) }}</h1>
    </div>
    <div v-if="currentTab.isPdf">
      <div :class="[currentTab?.isShowXML ? 'leftPart' : '']" class="pdfViewer">
        <PDFJSViewer v-bind:fileName="`${currentTab?.link}`"></PDFJSViewer>
      </div>
    </div>
    <div v-if="currentTab?.isShowXML || currentTab?.isXML"
         :class="{ rightPart: (currentTab?.isPdf)&&(currentTab?.isShowXML) }">
      <div class="full-height xmlViewer" id="xmlViewer" name="xmlViewer" v-html="currentTab?.content">
      </div>
    </div>
  </div>
  <div v-if="!currentTab" class="center" id="drag-box">
    <img src="../assets/img/logo_whitetext.svg"/><br/>

    {{ t("welcomeNote1", {}, {locale: lang}) }}<br/>
    {{ t("welcomeNote2", {}, {locale: lang}) }}<br/>
    <a class="example-link" @click="openLink">{{ t("Examples", {}, {locale: lang}) }}</a>
    <p class="note" v-if="version" style="text-align: center">
      {{ t("Version", {}, {locale: lang}) }} {{ version }}
    </p>
  </div>
  <div class="notification" v-if="showNofification">
    <p v-if="message">{{ message }}</p>
    <button @click="closeNotification">
      {{ t("close", {}, {locale: lang}) }}
    </button>
    <button v-if="showRestartButton" @click="restartApp">
      {{ t("restart", {}, {locale: lang}) }}
    </button>
  </div>
</template>

<script>
import Vue3TabsChrome from "vue3-tabs-chrome";
import "vue3-tabs-chrome/dist/vue3-tabs-chrome.css";
import {reactive, ref, isProxy, toRaw} from "vue";
import PDFJSViewer from "../components/PDFJSViewer.vue";
import {useI18n} from "vue-i18n";

export default {
  name: "Home",
  props: {},
  components: {
    Vue3TabsChrome,
    PDFJSViewer,
  },
  data() {
    return {
      lang: "en",
      version: undefined,
      showNofification: false,
      message: undefined,
      showRestartButton: false,
      showLoader: false,
    };
  },
  setup() {
    const tabRef = ref();
    const currentTab = ref(undefined);
    const tab = ref("google");
    const tabs = reactive([]);
    const {t, locale} = useI18n();

    const setTabRef = (el) => {
      tabRef.value = el;
      const currentTabObj = tabs.filter((item) => item.key === tab.value);

      if (currentTabObj.length) {
          if ((currentTabObj[0] != null) && (typeof currentTabObj[0].isPdf !== "undefined")) {
            window.api.sendDocChange(currentTabObj[0].isPdf, currentTabObj[0].isShowXML);
          }
        }
        currentTab.value = currentTabObj[0];
      }


    const handleAdd = () => {
      const key = "tab" + Date.now();
      tabRef.value.addTab({
        label: "New Tab",
        key,
        favico: require("../assets/icons/pdf.svg"),
        link: "test new",
      });

      tab.value = key;
      window.api.sendOpenMenu();
    };

    const handleRemove = () => {
      tabRef.value.removeTab(tab.value);
    };

    window.api.onPdfOpen((event, args) => {
      const currentTabObj = tabs.filter((item) => item.key === tab.value);
      if (currentTabObj.length && currentTabObj[0].label === "New Tab") {
        tabRef.value.removeTab(tab.value);
      }
      window.dispatchEvent(new Event("mousedown")); // Stop opening file with pdf.js shortcut ctrl+O

      const path = args[0].replace(/^.*[\\\/]/, "");
      const key = "tab" + Date.now();
      const res = window.api.sendSyncCheckXml(args[0]);
      tabRef.value.addTab({
        label: path,
        key,
        favico: require("../assets/icons/pdf.svg"),
        link: `lib/pdfjs/web/viewer.html?file=${args[0]}`,
        isPdf: true,
        isShowXML: !!res,
        isShowingXMLSection: true,
        content: res,
        path: args[0],
      });
      tab.value = key;
    });

    window.api.onXmlOpen((event, args) => {

      const currentTabObj = tabs.filter((item) => item.key === tab.value);
      if (currentTabObj.length && currentTabObj[0].label === "New Tab") {
        tabRef.value.removeTab(tab.value);
      }
      window.dispatchEvent(new Event("mousedown"));
      const path = args[0].replace(/^.*[\\\/]/, "");
      const key = "tab" + Date.now();
      tabRef.value.addTab({
        label: path,
        key,
        favico: require("../assets/icons/xml.png"),
        link: args[0],
        isXML: true,
        isShowXML: true,
        isPdf: false,
        content: args[1],
        xmlFilePath: args[0],
        path: args[0],
      });

      tab.value = key;
      document.getElementById(b[0]).setAttribute("class", "btnAktiv");
    });

    window.api.onValidateComplete((event, args) => {
      let list = sessionStorage.getItem("validationResult");
      list = list ? JSON.parse(list) : [];
      let res = list.find((item) => item.path === args.path);
      if (res) {
        res = args;
      } else {
        list.push(args);
      }
      sessionStorage.setItem("validationResult", JSON.stringify(list));
    });


    const onClose = (tabObj, key, index) => {

      if (tabs.length === 1) {
        currentTab.value = undefined;
        window.api.sendLastTabClose();
      }
    };
    const showXML = () => {
      currentTab.value.isShowingXMLSection = true;
    };
    const hideXML = () => {
      currentTab.value.isShowingXMLSection = false;
    };
    return {
      tabs,
      tab,
      handleAdd,
      handleRemove,
      setTabRef,
      currentTab,
      onClose,
      showXML,
      hideXML,
      t,
      locale,
    };
  },
  mounted() {
    window.api.onLanguageChange((event, args) => {
      this.lang = args;
      window.api.updateMenuLanguage(this.t("appName", {}, {locale: this.lang}));
    });
    window.api.sendAppVersion();
    window.api.onAppVersion((event, arg) => {
      window.api.removeAllAppVersion();
      this.version = arg.version;
    });
    window.api.onUpdateAvailable(() => {
      window.api.removeAllUpdateAvailable();
      this.message = this.t("updateAvailableNote", {}, {locale: this.lang});
    });

    window.api.onUpdateDownloaded(() => {
      window.api.removeAllUpdateDownloaded();
      this.message = this.t("updateDownloadedNote", {}, {locale: this.lang});
      this.showRestartButton = true;
    });
    window.api.onFilePrintXml((event, args) => {
      window.print();
    });
    window.api.onFilePrintPdf((event, args) => {
      if (window.frames["viewer"]) {
        window.frames["viewer"].focus();
        window.frames["viewer"].print();
      }
    });

    window.api.onShowLoginMessage((event, args) => {
      if (args.type === 'success') {
        this.$swal({
          icon: 'success',
          //title: args.message
          title: `${this.t("Success", {}, {locale: this.lang})} <br>
          ${this.t("validateFiles", {}, {locale: this.lang})}`,
        });
      } else {
        const errMessage =
            args.code === "ERR_NETWORK"
                ? this.t("serverunreachable", {}, {locale: this.lang})
                : this.t("invalidcredentials", {}, {locale: this.lang});
        this.$swal({
          icon: 'error',
          title: errMessage || args.message,
        });
      }
    });

    window.api.onLogoutSubmit((event, args) => {
      this.$swal({
        icon: "success",
        title: `${this.t("Logout success", {}, {locale: this.lang})}`,
      });
    });

    window.api
        .onValidateClick((event, args) => {
          if (this.currentTab) {
            const validateFile = () => {
              const showMessage = (record) => {
                if (record.valid) {
                  this.$swal({
                    icon: "success",
                    title: this.t("Valid file", {}, {locale: this.lang}),
                  });
                } else {
                  this.$swal({
                    icon: "error",
                    title: this.t("Invalid file", {}, {locale: this.lang}),
                  });
                  this.addErrorToPopup(record?.error);
                }
              };
              const list = sessionStorage.getItem("validationResult");
              if (list) {
                const result = JSON.parse(list);
                const currentFileRecord = result.find((item) => item.path === this.currentTab.path);

                console.log("currentFileRecord", currentFileRecord);
                if (currentFileRecord) {
                  if (currentFileRecord?.valid) {
                    console.log("Valid");
                    this.$swal({
                      icon: 'success',
                      title: this.t('Valid file', {}, {locale: this.lang})
                    });
                  } else {
                    console.log("Invalid");
                    this.$swal({
                      icon: 'error',
                      title: this.t('Invalid file', {}, {locale: this.lang})
                    });
                    this.addErrorToPopup(currentFileRecord?.error);
                  }
                } else {
                  this.showLoader = true;
                  setTimeout(() => {
                    const res = window.api.sendSyncValidateFile(
                        this.currentTab.path
                    );
                    this.showLoader = false;
                    if (res) {
                      if (res.code === "ERR_NETWORK") {
                        this.$swal({
                          icon: "error",
                          title: this.t("serverunreachable", {}, {locale: this.lang}),
                        });
                      } else if (res.code === "ERR_UNAUTHORIZED") {
                        this.$swal({
                          icon: "error",
                          title: this.t("notLoggedIn", {}, {locale: this.lang}),
                        });
                      } else {
                        let list = sessionStorage.getItem("validationResult");
                        list = list ? JSON.parse(list) : [];
                        list.push(res);
                        sessionStorage.setItem(
                            "validationResult",
                            JSON.stringify(list)
                        );
                        showMessage(res);
                      }
                    }
                  }, 100);
                }
              } else {
                this.showLoader = true;
                setTimeout(() => {
                  const res = window.api.sendSyncValidateFile(
                      this.currentTab.path
                  );
                  this.showLoader = false;
                  if (res) {
                    if (res.code === "ERR_NETWORK") {
                      this.$swal({
                        icon: "error",
                        title: this.t("serverunreachable", {}, {locale: this.lang}),
                      });
                    } else if (res.code === "ERR_UNAUTHORIZED") {
                      this.$swal({
                        icon: "error",
                        title: this.t("notLoggedIn", {}, {locale: this.lang}),
                      });
                    } else {
                      let list = sessionStorage.getItem("validationResult");
                      list = list ? JSON.parse(list) : [];

                      list.push(res);
                      sessionStorage.setItem(
                          "validationResult",
                          JSON.stringify(list)
                      );
                      showMessage(res);
                    }
                  }
                });
              }
            };
            validateFile();
          }
        });

    document.addEventListener("drop", (event) => {
      event.preventDefault();
      //event.stopPropagation();

      for (const f of event.dataTransfer.files) {
        window.api.sendOpenDraggedFile(f.path);
      }
      return false;
    }, false);

    document.addEventListener("dragover", (event) => {
      event.preventDefault();

    }, false);

    document.addEventListener("dragenter", (event) => {
      event.preventDefault();


    }, false);

    document.addEventListener("dragleave", (event) => {
      event.preventDefault();
    }, false);
  },

  methods: {
    closeNotification() {
      this.showNofification = false;
    },
    restartApp() {
      window.api.sendRestartApp();
    },
    openLink(link) {

      window.api.sendOpenLink(link);
    },
    addErrorToPopup(errorMessage) {
      let title = document.getElementById("swal2-title");
      let text = document.createElement("p");
      text.textContent = this.t("Show more", {}, {locale: this.lang});
      text.classList.add("show-more");

      text.addEventListener("click", () => {
        let error = document.createElement("div");
        error.classList.add("error-list");
        error.textContent = errorMessage || '';
        text.parentNode.insertBefore(error, text.nextSibling);
        text.style.display = "none";
      });
      title.parentNode.insertBefore(text, title.nextSibling);
    },

  }
};


/* Tab-Container aufbauen **************************************************/

var a = new Array("uebersicht", "details", "zusaetze", "anlagen", "laufzettel");
var b = new Array("menueUebersicht", "menueDetails", "menueZusaetze", "menueAnlagen", "menueLaufzettel");

export function show(e) {
  var i = 0;
  var j = 1;
  for (var t = 0; t < b.length; t++) {
    if (b[t] === e.id) {
      i = t;
      if (i > 0) {
        j = 0;
      } else {
        j = i + 1;
      }
      break;
    }
  }
  e.setAttribute("class", "btnAktiv");
  for (var k = 0; k < b.length; k++) {
    if (k === i && (document.getElementById(a[k]) != null)) {
      document.getElementById(a[k]).style.display = "block";
      if (i === j)
        j = i + 1;
    } else {
      if (document.getElementById(a[k]) != null) {
        document.getElementById(a[j]).style.display = "none";
        document.getElementById(b[j]).setAttribute("class", "btnInaktiv");
        j += 1;
      }
    }
  }
}

window.show = show; // make function available to inline javascript


/* Eingebettete Binaerdaten runterladen   ************************************/


export function base64_to_binary(data) {
  var chars = atob(data);
  var bytes = new Array(chars.length);
  for (var i = 0; i < chars.length; i++) {
    bytes[i] = chars.charCodeAt(i);
  }
  return new Uint8Array(bytes);
}

window.base64_to_binary = base64_to_binary; // make function available to inline javascript

export function downloadData(element_id) {
  var data_element = document.getElementById(element_id);
  var mimetype = data_element.getAttribute('mimeType');
  var filename = data_element.getAttribute('filename');
  var text = data_element.innerHTML;
  var binary = base64_to_binary(text);
  var blob = new Blob([binary], {
    type: mimetype, size: binary.length
  });

  if (window.navigator && window.navigator.msSaveOrOpenBlob) {
    // IE
    window.navigator.msSaveOrOpenBlob(blob, filename);
  } else {
    // Non-IE
    var url = window.URL.createObjectURL(blob);
    window.open(url);
  }
}

window.downloadData = downloadData; // make function available to inline javascript


</script>

<style scoped>
.display-none {
  display: none;
}

.center {
  position: absolute;
  text-align: center;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  color: white;
  padding: 10px;
}

.center img {
  margin: auto;
  display: block;
  padding: 10px;
}

.xmlViewer {
  overflow: auto;
}

.full-height {
  height: 100vh;
}

.leftPart {
  float: left;
  width: 50%;
}

.rightPart {
  float: right;
  width: 50%;
  height: 100vh;
  background: #1e1e1e;
}

.center-btn {
  margin: 0;
  position: absolute;
  left: 70%;
  top: 60%;
  -ms-transform: translateY(-50%);
  transform: translateY(-50%);
}

.xml-btn {
  border: none;
  color: black;
  padding: 16px 32px;
  text-align: center;
  text-decoration: none;
  display: inline-block;
  font-size: 16px;
  margin: 4px 2px;
  transition-duration: 0.4s;
  cursor: pointer;
  border-radius: 4px;
  font-weight: bold;
}

.xml-btn:hover {
  background-color: #008cba;
  color: white;
}

.note {
  color: white;
  font-size: 16px;
  font-weight: bold;
}

.notification {
  position: fixed;
  bottom: 20px;
  left: 20px;
  width: 200px;
  padding: 20px;
  border-radius: 5px;
  background-color: white;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);
}

a.example {
  color: #fff;
}

.example-link {
  text-decoration: underline;
}

.xmlPart {
  overflow-y: scroll;
  height: 100vh;
  width: 100%;
  padding-bottom: 70px;
}

.loader {
  display: flex;
  flex-direction: column;
  color: #fff;
  text-align: center;
  justify-content: center;
  align-items: center;
  height: 100%;
  width: 100%;
  position: absolute;
  background: linear-gradient(rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0.8));
}

#loading {
  display: inline-block;
  width: 50px;
  height: 50px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: #fff;
  animation: spin 1s ease-in-out infinite;
  -webkit-animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to {
    -webkit-transform: rotate(360deg);
  }
}

@-webkit-keyframes spin {
  to {
    -webkit-transform: rotate(360deg);
  }
}

@media print
{
  .vue3-tabs-chrome
  {
    display: none !important;
  }
  .pdfViewer {
    display: none !important;
  }
  .xmlViewer {
    overflow: visible;
  }
  .divHide {
    display: block!important;
  }
  .rightPart {
    width: 100%;
  }


}

</style>