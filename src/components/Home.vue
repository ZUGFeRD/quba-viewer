<template>
  <vue3-tabs-chrome
    :ref="setTabRef"
    :tabs="tabs"
    v-model="tab"
    :dragable="false"
    :swappable="false"
    :click="onClick"
    :class="[tabs?.length ? '' : 'display-none']"
    :on-close="onClose"
  />
  <div class="btns">
  </div>

  <div v-if="currentTab">
    <div v-if="currentTab.isPdf">
      <div :class="[currentTab?.isShowXML ? 'left-part' : '']">
      <PDFJSViewer :fileName="currentTab?.link"></PDFJSViewer>
    </div>
    <div v-if="currentTab?.isShowXML" class="right-part">
        <button style="float: right" @click="hideXML">x</button>
        <div class="center-btn" v-if="!currentTab?.isShowingXMLSection">
          <button class="xml-btn" @click="showXML"> {{ t("showXML", {}, { locale: lang }) }}</button>
        </div>
        <iframe
          v-if="currentTab?.isShowingXMLSection"
          height="100%"
          width="100%"
          class="full-height"
          :src="currentTab?.content"
          title=""
        ></iframe>
        </div>
    </div>
    <div v-if="currentTab?.isXML">
      <iframe
        height="100%"
        width="100%"
        class="full-height"
        :src="currentTab?.content"
        title=""
      ></iframe>
    </div>
  </div>
  <div v-if="!currentTab" class="center-logo">
      <pre class="note-logo">
        <img src="../assets/img/logo_whitetext.svg">
    </pre>
  </div>
    <div v-if="!currentTab" class="center">
      <pre class="note">
      {{ t("welcomeNote1", {}, { locale: lang }) }}
      {{ t("welcomeNote2", {}, { locale: lang }) }}
       <a href=" https://quba-viewer.org/beispiele/?pk_campaign=examples&pk_source=application"> {{ t("Examples", {}, { locale: lang }) }}</a>
    </pre>
    <p class="note" v-if="version" style="text-align: center">
      {{ t("Version", {}, { locale: lang }) }} {{ version }}
    </p>
  </div>
  <div class="notification" v-if="showNofification">
    <p v-if="message">{{ message }}</p>
    <button @click="closeNotification">
      {{ t("close", {}, { locale: lang }) }}
    </button>
    <button v-if="showRestartButton" @click="restartApp">
      {{ t("restart", {}, { locale: lang }) }}
    </button>
  </div>
</template>

<script>
import Vue3TabsChrome from "vue3-tabs-chrome";
import "vue3-tabs-chrome/dist/vue3-tabs-chrome.css";
import { reactive, ref } from "vue";
import PDFJSViewer from "../components/PDFJSViewer.vue";
import { useI18n } from "vue-i18n";
const electron = window.require("electron");
const customTitlebar = window.require("custom-electron-titlebar");
const Menu = window.require("electron").remote.Menu;

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
    };
  },
  setup() {
    const tabRef = ref();
    const currentTab = ref(undefined);
    const tab = ref("google");
    const tabs = reactive([]);
    const { t, locale } = useI18n();

    const setTabRef = (el) => {
      tabRef.value = el;
      const currentTabObj = tabs.filter((item) => item.key === tab.value);

      if (currentTabObj.length) {
        currentTab.value = currentTabObj[0];
      }
    };

    const handleAdd = () => {
      const key = "tab" + Date.now();
      tabRef.value.addTab({
        label: "New Tab",
        key,
        favico: require("../assets/icons/pdf.svg"),
        link: "test new",
      });

      tab.value = key;
    };

    const handleRemove = () => {
      tabRef.value.removeTab(tab.value);
    };

    electron.ipcRenderer.on("pdf-open", (event, args) => {
      window.dispatchEvent(new Event("mousedown"));
      const path = args[0].replace(/^.*[\\\/]/, "");
      const key = "tab" + Date.now();
      const res = electron.ipcRenderer.sendSync("check-xml", args[0]);
      tabRef.value.addTab({
        label: path,
        key,
        favico: require("../assets/icons/pdf.svg"),
        link: args[0],
        isPdf: true,
        isShowXML: !!res,
        isShowingXMLSection: false,
        content: res,
      });

      tab.value = key;
    });

    electron.ipcRenderer.on("xml-open", (event, args) => {
      window.dispatchEvent(new Event("mousedown")); 
      const path = args[0].replace(/^.*[\\\/]/, "");
      const key = "tab" + Date.now();
      tabRef.value.addTab({
        label: path,
        key,
        favico: require("../assets/icons/xml.png"),
        link: args[0],
        isXML: true,
        content: args[1],
      });

      tab.value = key;
    });

    const onClick = (e) => {
     
    };
    const onClose = (tabObj, key, index) => {
      
      if (tabs.length === 1) {
        currentTab.value = undefined;
        
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
     let MyTitleBar = new customTitlebar.Titlebar({
       backgroundcolor : customTitlebar.Color.fromHex("#1f2c40"),
       shadow:true,
       icon:"../assets/img/logoonly.svg",
     });
    electron.ipcRenderer.on("language-change", (event, args) => {
      MyTitleBar.dispose();
      MyTitleBar = new customTitlebar.Titlebar({
       backgroundcolor : customTitlebar.Color.fromHex("#1f2c40"),
       shadow:true,
       icon:"../assets/img/logoonly.svg",
       menu:Menu.getApplicationMenu(),
        });
      MyTitleBar.updateTitle(this.t("appName", {}, {locale:args}));
      this.lang = args; 
    });
     electron.ipcRenderer.send("app_version");
    electron.ipcRenderer.on("app_version", (event, arg) => {
      electron.ipcRenderer.removeAllListeners("app_version");
      this.version = arg.version;
    });
        electron.ipcRenderer.on("update_available", () => {
      electron.ipcRenderer.removeAllListeners("update_available");
      this.message = this.t("updateAvailableNote", {}, { locale: this.lang });
    });

    electron.ipcRenderer.on("update_downloaded", () => {
      electron.ipcRenderer.removeAllListeners("update_downloaded");
      this.message = this.t("updateDownloadedNote", {}, { locale: this.lang });
      this.showRestartButton = true;
    });
  },
  methods: {
    closeNotification() {
      this.showNofification = false;
    },
    restartApp() {
      electron.ipcRenderer.send("restart_app");
    },
  },
};
</script>

<style scoped>
.display-none {
  display: none;
}
.center {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);

  padding: 10px;
}
.center-logo img {
  position: absolute;
  left: 50%;
  top: 35%;
  transform: translate(-50%, -50%);

  padding: 10px;
}
.full-height {
  height: 100vh;
}
.left-part {
  float: left;
  width: 50%;
}
.right-part {
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

.note a{
  color: white;
  font-size: 16px;
  font-weight: bold;
  position: absolute;
  left: 50%;

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
</style>