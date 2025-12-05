<template>
  <div class="container home-container">
    <div class="hero-section">
      <div class="logo-placeholder">AI</div>
      <h1 class="welcome-title">AI Java 面试官</h1>
      <p class="subtitle">智能分析简历，精准匹配职位，助你轻松拿下 Offer</p>
      
      <div class="button-group">
        <router-link to="/login" class="btn-link primary-btn">立即登录</router-link>
        <router-link to="/register" class="btn-link secondary-btn">注册账号</router-link>
      </div>
    </div>

    <div class="features-grid">
      <div class="feature-card">
        <div class="icon">🚀</div>
        <h3>快速分析</h3>
        <p>上传简历，秒级生成分析报告</p>
      </div>
      <div class="feature-card">
        <div class="icon">💡</div>
        <h3>面试模拟</h3>
        <p>针对性生成面试题，模拟真实场景</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// No script logic needed for this simple view
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 80vh;
  justify-content: center;
}

.hero-section {
  text-align: center;
  margin-bottom: 60px;
  animation: fadeIn 0.8s ease-out;
}

.logo-placeholder {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-hover));
  color: white;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  margin: 0 auto 20px;
  box-shadow: 0 10px 20px rgba(66, 185, 131, 0.3);
  transform: rotate(-5deg);
}

.welcome-title {
  color: var(--secondary-color);
  font-size: 3rem;
  margin-bottom: 15px;
  font-weight: 800;
  letter-spacing: -1px;
}

.subtitle {
  font-size: 1.2rem;
  color: #64748b;
  margin-bottom: 40px;
  max-width: 600px;
  line-height: 1.6;
}

.button-group {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.btn-link {
  display: inline-block;
  padding: 14px 36px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
}

.primary-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-hover));
  color: white;
  box-shadow: 0 4px 15px rgba(66, 185, 131, 0.4);
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(66, 185, 131, 0.5);
}

.secondary-btn {
  background-color: white;
  color: var(--primary-color);
  border: 2px solid var(--primary-color);
}

.secondary-btn:hover {
  background-color: #f0f9f4;
  transform: translateY(-2px);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  width: 100%;
  max-width: 900px;
  animation: slideUp 0.8s ease-out 0.2s backwards;
}

.feature-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
  text-align: center;
  transition: transform 0.3s, box-shadow 0.3s;
  border: 1px solid #f0f0f0;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.icon {
  font-size: 40px;
  margin-bottom: 15px;
}

.feature-card h3 {
  color: var(--secondary-color);
  margin-bottom: 10px;
}

.feature-card p {
  color: #64748b;
  font-size: 14px;
  line-height: 1.5;
}
</style>
