# ai-java-interviewer-front

这是一个基于 Vue 3 的前端项目，旨在提供用户登录、注册和简历上传的功能。

## 项目结构

- **src/**: 源代码目录
  - **App.vue**: 应用的主组件，负责渲染不同的视图。
  - **main.ts**: 应用的入口文件，创建 Vue 实例并挂载到 DOM。
  - **api/**: 包含与身份验证和简历上传相关的 API 调用。
    - **auth.ts**: 身份验证 API。
    - **resume.ts**: 简历上传 API。
  - **assets/**: 存放样式文件。
    - **base.css**: 基础样式文件，设置全局样式。
    - **main.css**: 主要样式文件，包含特定于应用的样式。
  - **components/**: 可复用的 Vue 组件。
    - **LoginForm.vue**: 登录表单组件。
    - **RegisterForm.vue**: 注册表单组件。
    - **ResumeUploader.vue**: 简历上传组件。
  - **router/**: 路由配置文件，定义应用的不同路由。
  - **stores/**: Vuex 状态管理文件。
    - **auth.ts**: 管理用户的身份验证状态。
    - **counter.ts**: 示例状态管理文件。
  - **views/**: 页面视图，渲染不同的组件。
    - **LoginView.vue**: 登录页面视图。
    - **RegisterView.vue**: 注册页面视图。
    - **UploadResumeView.vue**: 上传简历页面视图。
  - **__tests__/**: 测试文件，包含对主要组件的单元测试。

## 安装与运行

1. 克隆项目到本地：
   ```
   git clone <repository-url>
   ```

2. 进入项目目录：
   ```
   cd ai-java-interviewer-front
   ```

3. 安装依赖：
   ```
   npm install
   ```

4. 启动开发服务器：
   ```
   npm run dev
   ```

5. 打开浏览器访问 `http://localhost:3000`。

## 功能

- 用户可以通过登录界面进行身份验证。
- 提供注册功能，用户可以创建新账户。
- 用户可以上传简历文件。

## 贡献

欢迎任何形式的贡献！请提交问题或拉取请求。

## 许可证

本项目使用 MIT 许可证。