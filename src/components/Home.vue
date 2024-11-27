<template>
  <vue3-tabs-chrome style="flex-shrink: 1"
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

      <!-- Layout- Resize Buttons aligned to the right -->
      <div class="view-buttons" v-if="isBothAvailable">
        <button @click="setLayout('50-50')" class="layout-btn">
          <i class="ph ph-square-split-horizontal"></i>
        </button>

        <button @click="toggle30_70" class="layout-btn">
          <i class="ph ph-arrows-left-right"></i>
        </button>

        <button @click="setLayout('100-0')" class="layout-btn">
          <i class="ph ph-file-pdf"></i>
        </button>

        <button @click="setLayout('0-100')" class="layout-btn" style="margin-right: 10px">
          <i class="ph ph-file-code"></i>
        </button>
      </div>

    </template>

  </vue3-tabs-chrome>

  <!--  Search Field-->
  <div v-if="showSearchInput" class="search-overlay">
    <div class="search-container">
      <i class="ph ph-magnifying-glass search-icon"></i>
      <input
          ref="searchInput"
          v-model="searchText"
          type="text"
          placeholder="Suchbegriff eingeben..."
          @keypress.enter="handleSearch"
      />
      <div class="action-button" @click="handleSearch">
        <i class="ph ph-magnifying-glass"></i>
      </div>
      <div class="action-button" @click="closeSearch">
        <i class="ph ph-x-circle"></i>
      </div>
    </div>
  </div>

  <!-- Start PDF/XML sections -->
  <div v-if="currentTab" style="flex-grow: 1;overflow: hidden; display: flex;flex-direction: row">
    <div class="loader" v-if="showLoader">
      <div id="loading"></div>
      <h1>{{ t("validatingFile", {}, {locale: lang}) }}</h1>
    </div>

    <!-- Splitter -->
    <div class="splitter-container" ref="container" v-if="isBothAvailable">
      <div class="pane" :style="{ flexBasis: leftPaneWidth + '%' }">
        <div v-if="currentTab.isPdf" class="full-height" style="flex: 1"
             :class="[currentTab?.isShowXML ? 'leftPart' : '']">
          <div id="pdfContainer" class="pdfViewer full-height">
            <PDFJSViewer ref="pdfViewer" v-bind:fileName="`${currentTab?.link}`"></PDFJSViewer>
          </div>
        </div>
      </div>
      <!-- Splitter line -->
      <div class="splitter" @mousedown="startDragging">
        <i class="ph ph-dots-three-vertical" id="spliter-icon"></i>
      </div>
      <div class="pane" id="xmll" :style="{ flexBasis: 100 - leftPaneWidth + '%' }">
        <div v-if="currentTab?.isShowXML || currentTab?.isXML" class="full-height" style="flex: 1"
             :class="{ rightPart: (currentTab?.isPdf)&&(currentTab?.isShowXML) }">
          <div ref="xmlViewer" class="xmlViewer full-height" id="xmlViewer" v-html="currentTab?.content">
          </div>
        </div>
      </div>
    </div>

    <!-- Full PDF or XML View when only one is present -->
    <div v-else-if="currentTab.isPdf" style="flex: 1">
      <div id="pdfContainer" class="pdfViewer full-height">
        <PDFJSViewer ref="pdfViewer" v-bind:fileName="`${currentTab?.link}`"></PDFJSViewer>
      </div>
    </div>

    <div v-else-if="currentTab.isXML" style="flex: 1">
      <div ref="xmlViewer" class="xmlViewer full-height" id="xmlViewer" v-html="currentTab?.content">
      </div>
    </div>
    <!-- Full PDF or XML View when only one is present -->

  </div>
  <!-- End PDF/XML sections-->
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
import {reactive, ref} from "vue";
import PDFJSViewer from "../components/PDFJSViewer.vue";
import {useI18n} from "vue-i18n";
import $ from 'jquery';

export default {
  name: "Home",
  props: {},
  components: {
    Vue3TabsChrome,
    PDFJSViewer,
  },
  data() {
    return {
      leftPaneWidth: 50, // Initial width of the left pane in percentage
      isDragging: false, // Status indicating if dragging is currently active
      searchText: "", // Text entered for search
      showSearchInput: false, // Controls the visibility of the search input field
      lang: "en",
      version: undefined,
      showNofification: false,
      message: undefined,
      showRestartButton: false,
      showLoader: false,
      currentTab: null,
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
        currentTab.value = currentTabObj[0]; // currentTab wird gesetzt
        console.log("Aktueller Tab nach setTabRef:", currentTab.value); // Debugging
        window.api.sendDocChange(currentTab.value.isPdf, currentTab.value.isShowXML);
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

    const afterTabShow = () => {
      $(document).ready(function () {
        $("#" + b[0]).siblings().removeClass("tab").removeClass("btnAnaktiv").addClass("btnInaktiv");
        $("#" + b[0]).removeClass("tab").removeClass("btnInaktiv").addClass("btnAktiv");
      });
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
        isXML: !!res, // Dies überprüft, ob XML vorhanden ist
        isShowXML: !!res,
        isShowingXMLSection: true,
        content: res,
        path: args[0],
      });
      tab.value = key;
      currentTab.value = tabs.find(tab => tab.key === key);  // Update currentTab
      afterTabShow();
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
      currentTab.value = tabs.find(tab => tab.key === key);  // Update currentTab

      afterTabShow();
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
    // Add event listener for Ctrl + F
    document.addEventListener("keydown", this.handleKeydown);
    // Attach the keydown event listener when the component is mounted
    window.addEventListener("keydown", this.handleEscKey);
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


    window.addEventListener("drop", (event) => {
      event.preventDefault();
      //event.stopPropagation();
      for (const f of event.dataTransfer.files) {
        window.api.sendOpenDraggedFile(f);
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
    window.addEventListener("resize", this.handleResize);

  },
  beforeUnmount() {
    // Remove event listeners to avoid memory leaks
    document.removeEventListener("keydown", this.handleKeydown);
    window.removeEventListener("keydown", this.handleEscKey);

  },
  computed: {
    // Computed property to check if both PDF and XML are available
    isBothAvailable() {
      console.log("isPdf:", this.currentTab.isPdf);
      console.log("isXML:", this.currentTab.isXML);
      console.log("isBothAvailable:", this.currentTab.isPdf && this.currentTab.isXML);
      return this.currentTab.isPdf && this.currentTab.isXML;
    }
  },
  methods: {
    startDragging(event) {
      this.isDragging = true;

      // Disable pointer events for the iframe to allow mouse events to propagate
      const iframe = this.$refs.pdfViewer?.$el.querySelector("iframe");
      if (iframe) iframe.style.pointerEvents = "none";

      document.addEventListener("mousemove", this.onMouseMove);
      document.addEventListener("mouseup", this.stopDragging);

      event.preventDefault(); // Prevent default behavior
    },
    onMouseMove(event) {
      // Resize the widht for probably working of splitter
      document.getElementById("xmll").style.width = '0';
      if (!this.isDragging) return;

      // Get container bounds
      const container = this.$refs.container;
      const containerRect = container.getBoundingClientRect();

      // Calculate the new width for the left pane
      const offsetX = event.clientX - containerRect.left;
      const newWidthPercentage = (offsetX / containerRect.width) * 100;

      // Constrain the width between 0% and 100% (recommended 10% 90%)
      this.leftPaneWidth = Math.max(0, Math.min(100, newWidthPercentage));
    },
    stopDragging() {
      this.isDragging = false;

      // Re-enable pointer events for the iframe
      const iframe = this.$refs.pdfViewer?.$el.querySelector("iframe");
      if (iframe) iframe.style.pointerEvents = "auto";

      document.removeEventListener("mousemove", this.onMouseMove);
      document.removeEventListener("mouseup", this.stopDragging);
    },
    handleKeydown(event) {
      // Check if Ctrl+F (or Cmd+F on macOS) is pressed
      if ((event.ctrlKey || event.metaKey) && event.key === "f") {
        event.preventDefault(); // Prevent the default browser search functionality
        this.showSearchInput = true; // Display the search field

        // Wait for the search input to render and focus on it
        this.$nextTick(() => {
          const searchInput = this.$refs.searchInput; // Reference to the input field
          if (searchInput) {
            searchInput.focus();
          }
        });
      }
    },
    closeSearch() {
      this.showSearchInput = false; // Hide search field
    },
    handleEscKey(event) {
      if (event.key === "Escape") {
        this.closeSearch();
      }
    },
    handleSearch() {
      this.searchPDF();  // Perform search in the PDF
      this.searchXML();  // Perform search in the XML
    },
    /*searchPDF() {
      const iframe = document.getElementById("viewer"); // Access the iframe
      const iframeDocument = iframe.contentDocument || iframe.contentWindow.document; // Get the iframe's document

      if (iframeDocument && iframeDocument.defaultView.PDFViewerApplication) {
        const pdfViewerApp = iframeDocument.defaultView.PDFViewerApplication;
        const findController = pdfViewerApp.findController;

        if (pdfViewerApp && findController) {
          // Trigger PDF search
          findController.executeCommand("find", {
            query: this.searchText,
            caseSensitive: false,  // Ignore case
            highlightAll: true,    // Highlight all matches
            findPrevious: false,   // Search forward
          });

          // Monitor for search result updates
          const checkMatch = setInterval(() => {
            const currentPage = findController._selected.pageIdx;

            if (currentPage >= 0) {
              clearInterval(checkMatch); // Stop checking after match found

              const page = pdfViewerApp.pdfViewer.getPageView(currentPage);

              if (page && page.div) {
                // Smooth scroll to the matched page
                page.div.scrollIntoView({behavior: "smooth", block: "center"});
              }
            }
          }, 100); // Check every 100ms
        } else {
          console.error("PDFViewerApplication or FindController not found.");
        }
      } else {
        console.error("iframe or PDFViewerApplication unavailable.");
      }
    },*/
    /*    searchXML() {
      const xmlViewer = this.$refs.xmlViewer;
      if (!xmlViewer || !this.searchText) {
        console.error("XML Viewer oder Suchtext fehlt.");
        return;
      }

      // Remove previous highlights
      this.removeHighlights(xmlViewer);

      // Search and highlight
      const matchFound = this.searchAndHighlight(xmlViewer, this.searchText);

      if (!matchFound) {
        console.warn("Kein Treffer gefunden.");
      }
    },*/
    searchPDF() {
      const iframe = document.getElementById("viewer"); // Zugriff auf das iframe
      const iframeDocument = iframe.contentDocument || iframe.contentWindow.document; // Zugriff auf das iframe-Dokument

      if (iframeDocument && iframeDocument.defaultView.PDFViewerApplication) {
        const pdfViewerApp = iframeDocument.defaultView.PDFViewerApplication;
        const findController = pdfViewerApp.findController;

        if (pdfViewerApp && findController) {
          // Sicherstellen, dass der Suchtext ordnungsgemäß verarbeitet wird
          const searchText = this.searchText.trim();

          // PDF-Suche auslösen
          findController.executeCommand("find", {
            query: searchText,
            caseSensitive: false,  // Groß-/Kleinschreibung ignorieren
            highlightAll: true,    // Alle Treffer markieren
            findPrevious: false,   // Vorwärts suchen
          });

          // Überwache die Suchergebnisse und scrolle bei Treffer
          const checkMatch = setInterval(() => {
            const currentPage = findController._selected.pageIdx;

            if (currentPage >= 0) {
              clearInterval(checkMatch); // Stoppe das Überprüfen nach gefundenem Treffer

              const page = pdfViewerApp.pdfViewer.getPageView(currentPage);

              if (page && page.div) {
                // Weiches Scrollen zur gefundenen Seite
                page.div.scrollIntoView({behavior: "smooth", block: "center"});
              }
            }
          }, 100); // Alle 100ms nach Ergebnissen suchen
        } else {
          console.error("PDFViewerApplication oder FindController nicht gefunden.");
        }
      } else {
        console.error("iframe oder PDFViewerApplication nicht verfügbar.");
      }
    },
    searchXML() {
      const xmlViewer = this.$refs.xmlViewer;
      if (!xmlViewer || !this.searchText) {
        console.error("XML Viewer oder Suchtext fehlt.");
        return;
      }

      // Entferne vorherige Markierungen
      this.removeHighlights(xmlViewer);

      // Stelle sicher, dass der Suchtext korrekt behandelt wird
      const searchQuery = this.searchText.trim();

      // Suche und markiere den gesamten Suchbegriff
      const matchFound = this.searchAndHighlight(xmlViewer, searchQuery);

      if (!matchFound) {
        console.warn("Kein Treffer gefunden.");
      }
    },
    removeHighlights(element) {
      const highlights = element.querySelectorAll(".highlight");
      highlights.forEach((highlight) => {
        const parent = highlight.parentNode;
        if (parent) {
          parent.replaceChild(document.createTextNode(highlight.textContent), highlight);
        }
      });
    },
    searchAndHighlight(node, searchText) {
      let matchFound = false;

      // Prüfe, ob es ein Textknoten ist
      if (node.nodeType === Node.TEXT_NODE) {
        const text = node.textContent;
        const searchIndex = text.toLowerCase().indexOf(searchText.toLowerCase());

        if (searchIndex !== -1) {
          matchFound = true;

          // Text in Knoten aufteilen und Highlight einfügen
          const beforeText = text.substring(0, searchIndex);
          const highlightedText = text.substring(searchIndex, searchIndex + searchText.length);
          const afterText = text.substring(searchIndex + searchText.length);

          // Erstellt die neuen Knoten mit Hervorhebung
          const beforeNode = document.createTextNode(beforeText);
          const highlightNode = document.createElement("span");
          highlightNode.className = "highlight";
          highlightNode.textContent = highlightedText;
          console.log("Highlight Node: ", highlightNode); // Debug

          const afterNode = document.createTextNode(afterText);

          // Ersetze den ursprünglichen Textknoten
          const parent = node.parentNode;
          if (parent) {
            parent.replaceChild(afterNode, node);
            parent.insertBefore(highlightNode, afterNode);
            parent.insertBefore(beforeNode, highlightNode);
            console.log("Parent after insertions: ", parent); // Debug

            // Scrolle zur ersten Hervorhebung
            highlightNode.scrollIntoView({behavior: "smooth", block: "center"});
          }
        }
      } else if (node.nodeType === Node.ELEMENT_NODE && node.childNodes.length > 0) {
        // Rekursive Durchsuchung für Knoten mit Kindern
        node.childNodes.forEach((child) => {
          const childMatch = this.searchAndHighlight(child, searchText);
          if (childMatch) {
            matchFound = true;
          }
        });
      }

      return matchFound;
    },
    // Set layout based on predefined options ( for Resizing Buttons)
    setLayout(layout) {
      console.log("isPdf:", this.currentTab.isPdf);
      console.log("isXML:", this.currentTab.isXML);
      console.log("isBothAvailable:", this.currentTab.isPdf && this.currentTab.isXML);
      // reset the width
      document.getElementById("xmll").style.width = '0';
      if (layout === '50-50') {
        this.leftPaneWidth = 50; // Both 50%
      } else if (layout === '100-0') {
        this.leftPaneWidth = 100; // Full PDF, no XML
      } else if (layout === '0-100') {
        this.leftPaneWidth = 0; // Full XML, no PDF
      }
    },
    // Toggle between 30% and 70% for PDF and XML
    toggle30_70() {
      this.leftPaneWidth = this.leftPaneWidth === 30 ? 70 : 30; // Switch between 30-70 and 70-30
    },
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
  },
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


.splitter-container {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
}

.splitter {
  display: flex;
  justify-content: center;
  align-items: center;
  transition: background-color 0.3s ease;
}

#spliter-icon {
  font-weight: bold;
  font-size: 1.5rem;
  position: relative;
  animation: flash 1.5s infinite; /* Blitz-Animation */
}

/* Hover-Effekt */
.splitter:hover {
  background-color: #333; /* Dunkler Hintergrund */
}

.splitter:hover #spliter-icon {
  animation: none; /* Animation stoppen beim Hover */
  transform: scale(1.2); /* Optional: Vergrößern beim Hover */
  color: #fff; /* Farbe ändern */
}

/* Blitz-Animation */
@keyframes flash {
  0%, 100% {
    opacity: 1; /* Volle Sichtbarkeit */
  }
  50% {
    opacity: 0; /* Unsichtbar für Blitz */
  }
}

.pane {
  height: 100%;
}

.splitter {
  width: 5px;
  background: #ccc;
  cursor: ew-resize;
  height: 100%;
}

.search-overlay {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  padding: 10px;
  border-radius: 30px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  z-index: 99999;
  max-width: 500px;
  width: 100%;
}

.search-container {
  display: flex;
  align-items: center;
  width: 100%;
}

.search-container input {
  flex: 1;
  border: none;
  padding: 10px 15px;
  font-size: 16px;
  border-radius: 30px;
  background: #f5f5f5;
  outline: none;
  margin-right: 10px;
  color: #333;
}

.search-container input::placeholder {
  color: #aaa;
}

.search-icon {
  margin-right: 8px;
  font-size: 18px;
  color: #666;
}

.action-button {
  background: none;
  border: none;
  margin-left: 5px;
  cursor: pointer;
  font-size: 18px;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  transition: background 0.3s ease, color 0.3s ease;
}

.action-button:hover {
  background: #f0f0f0;
  color: #000;
}

.action-button i {
  font-size: 20px;
}


.view-buttons {
  display: flex;

}

.layout-btn {
  //background-color: #c6c4c4;
  color: black;
  border: none;
  padding: 6px; /* Kleinere Polsterung */
  font-size: 16px; /* Kleinere Schriftgröße für das Icon */
  border-radius: 5px; /* Runde Buttons */
  transition: background-color 0.3s, transform 0.3s;
  display: flex;
  justify-items: center;
  align-items: center;
  align-content: center;
  justify-content: center;
  width: 25px; /* Kleinere Breite */
  height: 30px; /* Kleinere Höhe */
}

.layout-btn i {
  font-size: 18px; /* Größe der Icons */
}

.layout-btn:hover {
  background-color: #45a049;
  transform: scale(1.1); /* Vergrößert den Button bei Hover */
}

.layout-btn:active {
  background-color: #3e8e41;
}

.layout-btn:focus {
  outline: none;
}

.layout-btn:disabled {
  background-color: #bdbdbd;
  cursor: not-allowed;
}

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
  background-color: #EBF2F7;
}

.full-height {
  height: 100%;
}

.leftPart {

}

.rightPart {
  height: 100%;
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
  height: 100%;
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

@media print {
  .vue3-tabs-chrome {
    display: none !important;
  }

  .pdfViewer {
    display: none !important;
  }

  .xmlViewer {
    overflow: visible;
  }

  .divHide {
    display: block !important;
  }

  .rightPart {
    width: 100%;
  }
}

</style>