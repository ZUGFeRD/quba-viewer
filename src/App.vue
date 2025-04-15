<template>
  <Home></Home>
</template>

<script>
import Home from "./components/Home.vue";

export default {
  name: "App",
  components: {
    Home,
  },
};

</script>

<style>
/*for xml search*/
.highlight {
  background-color: yellow !important;
  color: black !important;
  font-weight: bold !important;
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* background: #1e1e1e; */
}
body {
  margin: 0;
  background: #1e1e1e;
}
.titlebar,
.titlebar > * {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
.error-list {
  padding: 12px;
  word-break: break-word;
}
.show-more {
  text-align: center;
  cursor: pointer;
}
</style>
