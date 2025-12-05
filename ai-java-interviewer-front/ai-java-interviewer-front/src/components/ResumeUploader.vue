<template>
  <div class="resume-uploader">
    <h2>上传简历</h2>
    <input type="file" @change="onFileChange" />
    <button class="upload-button" @click="uploadResume">上传</button>
    <p v-if="message" :class="{'error-message': isError, 'success-message': !isError}">{{ message }}</p>
  </div>
</template>

<script lang="ts">
import { ref } from 'vue';

export default {
  name: 'ResumeUploader',
  setup() {
    const file = ref<File | null>(null);
    const message = ref<string>('');
    const isError = ref<boolean>(false);

    const onFileChange = (event: Event) => {
      const target = event.target as HTMLInputElement;
      if (target.files && target.files.length > 0) {
        file.value = target.files[0];
        message.value = '';
      }
    };

    const uploadResume = async () => {
      if (!file.value) {
        message.value = '请先选择一个文件';
        isError.value = true;
        return;
      }

      // 这里可以调用 API 上传简历
      // 假设有一个 uploadResume API 函数
      try {
        // await uploadResumeAPI(file.value);
        message.value = '简历上传成功！';
        isError.value = false;
      } catch (error) {
        message.value = '上传失败，请重试。';
        isError.value = true;
      }
    };

    return {
      onFileChange,
      uploadResume,
      message,
      isError,
    };
  },
};
</script>

<style scoped>
.resume-uploader {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.upload-button {
  background-color: green;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 5px;
  cursor: pointer;
}

.upload-button:hover {
  background-color: darkgreen;
}

.error-message {
  color: red;
}

.success-message {
  color: green;
}
</style>