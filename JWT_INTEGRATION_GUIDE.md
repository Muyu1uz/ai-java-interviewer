# JWT 认证集成说明

## 概述

本项目已集成 JWT (JSON Web Token) 认证机制,用于替代传统的 Session 认证方式,提升系统的鉴权效率和可扩展性。

## 主要改进

### 1. 无状态认证
- 不再依赖 Session,降低服务器内存压力
- Token 包含所有必要的用户信息,无需查询会话状态
- 支持分布式部署和水平扩展

### 2. 性能提升
- 减少数据库/缓存查询次数
- JWT 在过滤器层面解析,业务层直接获取用户信息
- 降低响应延迟

### 3. 安全性
- 使用 HS256 算法签名,防止 Token 被篡改
- 设置合理的过期时间(默认 24 小时)
- Token 包含用户 ID 和用户名等必要声明

## 核心组件

### 1. JWT 工具类 (`JwtUtil`)
- **位置**: `com.muyulu.aijavainterviewer.util.JwtUtil`
- **功能**:
  - `generateToken(userId, username)`: 生成 JWT Token
  - `parseToken(token)`: 解析 Token 获取 Claims
  - `validateToken(token)`: 验证 Token 有效性
  - `getUserIdFromToken(token)`: 获取用户 ID
  - `getUsernameFromToken(token)`: 获取用户名

### 2. JWT 认证过滤器 (`JwtAuthenticationFilter`)
- **位置**: `com.muyulu.aijavainterviewer.filter.JwtAuthenticationFilter`
- **功能**:
  - 拦截所有请求,从 Authorization 头获取 Token
  - 验证 Token 有效性
  - 将用户信息存入请求属性 (`userId`, `username`)

### 3. 登录拦截器 (`LoginInterceptor`)
- **位置**: `com.muyulu.aijavainterviewer.interceptor.LoginInterceptor`
- **功能**:
  - 检查带有 `@RequireLogin` 注解的接口
  - 验证请求中是否包含有效的用户信息
  - 未登录时返回 401 状态码

### 4. 登录注解 (`@RequireLogin`)
- **位置**: `com.muyulu.aijavainterviewer.annotation.RequireLogin`
- **用法**: 在需要登录的接口方法上添加此注解

## 配置说明

### application.yml 配置
```yaml
jwt:
  secret: YourSuperSecretKeyForJWT2025MustBeAtLeast256BitsLongForHS256Algorithm
  expiration: 86400  # 24小时,单位:秒
```

**重要**: 生产环境请务必修改 `jwt.secret` 为自己的密钥,密钥长度至少 256 位。

## 使用方式

### 1. 用户登录

**接口**: `POST /api/user/login`

**请求体**:
```json
{
  "userAccount": "user123",
  "password": "password123"
}
```

**响应**:
```json
{
  "id": 1,
  "username": "张三",
  "userAccount": "user123",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoi5byg5LiJIiwic3ViIjoiMSIsImlhdCI6MTY3MzE4MDAwMCwiZXhwIjoxNjczMjY2NDAwfQ.xxxxx"
}
```

### 2. 后续请求携带 Token

前端在后续请求中需要在 `Authorization` 头中携带 Token:

**请求头**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoi5byg5LiJIiwic3ViIjoiMSIsImlhdCI6MTY3MzE4MDAwMCwiZXhwIjoxNjczMjY2NDAwfQ.xxxxx
```

### 3. 在业务代码中获取当前用户

```java
@RestController
@RequestMapping("/api/example")
public class ExampleController {
    
    @Resource
    private UserService userService;
    
    @PostMapping("/some-action")
    @RequireLogin  // 添加此注解表示需要登录
    public ResponseEntity<?> someAction(HttpServletRequest request) {
        // 通过 UserService 获取当前登录用户
        User currentUser = userService.getLoginUser(request);
        
        // 使用用户信息
        Long userId = currentUser.getId();
        String username = currentUser.getUsername();
        
        // 业务逻辑...
        return ResponseEntity.ok("success");
    }
}
```

### 4. 已更新的接口

以下接口已添加 `@RequireLogin` 注解,需要登录后才能访问:

- `POST /api/resume/create` - 上传简历
- `GET /api/resume/check` - 检查简历上传状态
- `POST /api/interview-chat/start` - 开始面试对话
- `POST /api/interview-chat/continue` - 继续面试对话

## 前端对接指南

### 1. 登录流程
```javascript
// 登录请求
const loginResponse = await fetch('/api/user/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    userAccount: 'user123',
    password: 'password123'
  })
});

const loginData = await loginResponse.json();

// 保存 Token 到 localStorage
localStorage.setItem('token', loginData.token);
localStorage.setItem('userId', loginData.id);
localStorage.setItem('username', loginData.username);
```

### 2. 发送认证请求
```javascript
// 从 localStorage 获取 Token
const token = localStorage.getItem('token');

// 在请求头中携带 Token
const response = await fetch('/api/resume/create', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: formData
});

// 处理响应
if (response.status === 401) {
  // Token 过期或未登录,跳转到登录页
  window.location.href = '/login';
} else {
  const data = await response.json();
  // 处理业务数据...
}
```

### 3. Axios 拦截器示例
```javascript
import axios from 'axios';

// 请求拦截器 - 自动添加 Token
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理 401 未授权
axios.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 清除本地 Token
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      
      // 跳转到登录页
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

## 优势总结

1. **性能提升**: 无需每次请求都查询 Session,减少数据库/缓存压力
2. **可扩展性**: 支持分布式部署,无需 Session 共享
3. **无状态**: 服务器不存储会话信息,降低内存占用
4. **跨域友好**: Token 可以轻松在不同域名间传递
5. **移动端友好**: 移动 App 可以方便地使用 Token 认证

## 安全建议

1. **密钥管理**: 
   - 生产环境使用强密钥(至少 256 位)
   - 定期更换密钥
   - 不要将密钥提交到版本控制系统

2. **Token 过期时间**: 
   - 根据业务需求设置合理的过期时间
   - 敏感操作可以设置更短的过期时间

3. **HTTPS**: 
   - 生产环境必须使用 HTTPS
   - 防止 Token 在传输过程中被窃取

4. **Token 刷新**: 
   - 可以实现 Refresh Token 机制
   - 在 Access Token 过期前自动刷新

5. **Token 撤销**: 
   - 如需实现 Token 撤销,可以配合 Redis 黑名单机制
   - 用户退出登录时将 Token 加入黑名单

## 故障排查

### 问题 1: 返回 401 未授权
- 检查请求头是否包含 `Authorization: Bearer <token>`
- 检查 Token 是否过期
- 检查 Token 格式是否正确

### 问题 2: Token 解析失败
- 检查 `jwt.secret` 配置是否正确
- 检查 Token 是否被篡改
- 检查 Token 是否完整(没有被截断)

### 问题 3: 获取用户信息失败
- 检查接口是否添加了 `@RequireLogin` 注解
- 检查 JWT 过滤器是否正常工作
- 检查用户 ID 是否存在于数据库中

## 依赖说明

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

## 扩展功能建议

1. **Refresh Token**: 实现 Token 刷新机制,提升用户体验
2. **Token 黑名单**: 使用 Redis 实现 Token 撤销功能
3. **多端登录管理**: 记录用户登录设备,支持踢出其他设备
4. **权限管理**: 在 Token 中添加角色/权限信息,实现细粒度权限控制
5. **登录日志**: 记录用户登录时间、IP 地址等信息

## 联系方式

如有问题,请联系开发团队或查看项目文档。
