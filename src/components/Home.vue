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
  ><template v-slot:after>
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
 <div class="btns">
  </div>
  <div v-if="currentTab">
    <div v-if="currentTab.isPdf">
      <div :class="[currentTab?.isShowXML ? 'left-part' : '']">
      <PDFJSViewer v-bind:fileName="`${currentTab?.link}`"></PDFJSViewer>
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
          id="xmlViewer"
          name="xmlViewer"
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
        id="xmlViewer"
          name="xmlViewer"
      ></iframe>
    </div>
  </div>
    <div v-if="!currentTab" class="center" id="drag-box">
        <img src="../assets/img/logo_whitetext.svg"><br>
      
      {{ t("welcomeNote1", {}, { locale: lang }) }}<br>
      {{ t("welcomeNote2", {}, { locale: lang }) }}<br>
       <a class="example-link" @click="openLink">{{t("Examples", {}, { locale: lang })}}</a>
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
      console.log("filepath", args[0]);
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
        isShowingXMLSection: false,
        content: res,
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
    window.api.onLanguageChange((event, args) => {
      this.lang = args; 
      window.api.updateMenuLanguage(this.t("appName", {}, { locale: this.lang }));
    });
     window.api.sendAppVersion();
      window.api.onAppVersion((event, arg) => {
      window.api.removeAllAppVersion();
      this.version = arg.version;
    });
      window.api.onUpdateAvailable(() => {
      window.api.removeAllUpdateAvailable();
      this.message = this.t("updateAvailableNote", {}, { locale: this.lang });
    });

      window.api.onUpdateDownloaded(() => {
      window.api.removeAllUpdateDownloaded();
      this.message = this.t("updateDownloadedNote", {}, { locale: this.lang });
      this.showRestartButton = true;
    });
    window.api.onFilePrintXml((event, args) => {
      if (window.frames["xmlViewer"]) {
        window.frames["xmlViewer"].focus();
        window.frames["xmlViewer"].print();
      }
    });
      window.api.onFilePrintPdf((event, args) => {
      if (window.frames["viewer"]) {
        window.frames["viewer"].focus();
        window.frames["viewer"].print();
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

  },
};
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
a.example {
  color: #fff;
}

.example-link{
  text-decoration: underline;
} 
</style>