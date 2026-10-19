<template>
  <div class="full-height" v-if="fileName">
   <iframe
      height="100%"
      width="100%"
      :src="fileName"
      id="viewer"
      name="viewer"
      title=""
      @load="onLoad"
    ></iframe>
  </div>
</template>

<script>
export default {
  name: "PDFJSViewer",
  props: {
    fileName: String,
  },
  methods: {
    onLoad(event) {
      // pdf.js would open dropped files inside this iframe, bypassing Quba's
      // tab and XML handling; open them like any other dropped file instead.
      event.target.contentWindow.addEventListener("drop", (dropEvent) => {
        dropEvent.preventDefault();
        dropEvent.stopPropagation();
        for (const f of dropEvent.dataTransfer.files) {
          window.api.sendOpenDraggedFile(f);
        }
      }, true);
    },
  },
  computed: {},
};
</script>
<style scoped>
.full-height {
  height: 100vh;
}
</style>