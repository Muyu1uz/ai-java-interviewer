<template>
  <div class="register-view">
    <h1>注册</h1>
    <RegisterForm @register-success="handleRegisterSuccess" />
    <p class="redirect-text">
      没有注册？<router-link to="/login">先注册</router-link>
    </p>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import RegisterForm from '../components/RegisterForm.vue';

export default defineComponent({
  components: {
    RegisterForm,
  },
  methods: {
    handleRegisterSuccess() {
      this.$router.push('/login');
    },
  },
});
</script>

<style scoped>
.register-view {
  background-color: white;
  padding: 20px;
  text-align: center;
}

.redirect-text {
  margin-top: 10px;
  color: green;
}

h1 {
  color: green;
}
</style>