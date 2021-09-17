<template>
  <div class="full-height" v-if="fileName">
   <iframe
      height="100%"
      width="100%"
      :src="fileName"
      id="viewer"
      name="viewer"
      title=""
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

  },
  computed: {},
};
</script>
<style scoped>
.full-height {
  height: 100vh;
}
</style>