<template>
  <div class="upload-resume">
    <h1>上传简历</h1>
    <ResumeUploader />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import ResumeUploader from '../components/ResumeUploader.vue';

export default defineComponent({
  name: 'UploadResumeView',
  components: {
    ResumeUploader,
  },
});
</script>

<style scoped>
.upload-resume {
  background-color: white;
  padding: 20px;
  text-align: center;
}

h1 {
  color: green;
}
</style>