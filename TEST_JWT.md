# JWT 认证测试脚本

## 测试步骤

### 1. 启动应用
确保应用已经启动并运行在 http://localhost:8123

### 2. 用户登录获取 Token

使用以下命令测试登录接口:

```powershell
# 登录请求
$loginBody = @{
    userAccount = "user123"
    password = "password123"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8123/api/user/login" `
    -Method POST `
    -Body $loginBody `
    -ContentType "application/json"

# 显示响应
$response

# 保存 Token
$token = $response.token
Write-Host "Token: $token"
```

**预期响应:**
```json
{
  "id": 1,
  "username": "张三",
  "userAccount": "user123",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoi5byg5LiJIiwic3ViIjoiMSIsImlhdCI6MTY3MzE4MDAwMCwiZXhwIjoxNjczMjY2NDAwfQ.xxxxx"
}
```

### 3. 使用 Token 访问受保护的接口

```powershell
# 使用上面获取的 Token
$headers = @{
    "Authorization" = "Bearer $token"
}

# 测试检查简历接口
$response = Invoke-RestMethod -Uri "http://localhost:8123/api/resume/check" `
    -Method GET `
    -Headers $headers

Write-Host "简历检查结果: $response"
```

**预期响应:**
```json
true  # 或 false
```

### 4. 测试无 Token 访问(应该返回 401)

```powershell
# 不带 Token 访问受保护接口
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8123/api/resume/check" `
        -Method GET
    Write-Host "响应: $response"
} catch {
    Write-Host "错误: $_"
}
```

**预期响应:**
```json
{
  "code": 401,
  "message": "未登录或登录已过期"
}
```

### 5. 测试 Token 格式错误(应该返回 401)

```powershell
# 使用错误格式的 Token
$headers = @{
    "Authorization" = "Bearer invalid_token_here"
}

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8123/api/resume/check" `
        -Method GET `
        -Headers $headers
    Write-Host "响应: $response"
} catch {
    Write-Host "错误: $_"
}
```

## 使用 curl 测试(如果安装了 curl)

### 登录
```bash
curl -X POST http://localhost:8123/api/user/login \
  -H "Content-Type: application/json" \
  -d "{\"userAccount\":\"user123\",\"password\":\"password123\"}"
```

### 使用 Token 访问
```bash
# 替换 YOUR_TOKEN_HERE 为实际的 Token
curl -X GET http://localhost:8123/api/resume/check \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 查看日志

启动应用后,检查控制台日志,应该能看到类似以下的输出:

```
JWT过滤器处理请求: /api/resume/check
检测到 Token: eyJhbGciOiJIUzI1Ni...
Token验证成功,用户ID: 1, 用户名: 张三
登录拦截器处理请求: /api/resume/check
方法需要登录认证
从请求属性获取 userId: 1
用户认证通过,userId: 1
```

## 常见问题排查

### 问题 1: 401 未登录或登录已过期

**可能原因:**
1. Token 没有放在 `Authorization` 头中
2. Token 前缀不是 `Bearer ` (注意有空格)
3. Token 格式错误或已过期
4. JWT 过滤器没有正确解析 Token

**排查步骤:**
1. 检查请求头: `Authorization: Bearer <token>`
2. 查看应用日志,确认过滤器是否检测到 Token
3. 确认 Token 是从登录接口返回的完整 Token

### 问题 2: Token 验证失败

**可能原因:**
1. `jwt.secret` 配置不一致
2. Token 已过期(默认 24 小时)
3. Token 被篡改

**排查步骤:**
1. 检查 `application.yml` 中的 `jwt.secret` 配置
2. 重新登录获取新的 Token
3. 查看应用日志中的错误信息

### 问题 3: 过滤器没有执行

**可能原因:**
1. 过滤器配置问题
2. 请求路径不匹配

**排查步骤:**
1. 检查 `SecurityConfig.java` 中的过滤器配置
2. 确认请求路径以 `/api/` 开头
3. 查看应用启动日志,确认过滤器已注册

## 完整测试脚本 (PowerShell)

```powershell
# JWT 认证完整测试脚本
Write-Host "========== JWT 认证测试开始 ==========" -ForegroundColor Green

# 1. 登录获取 Token
Write-Host "`n1. 测试登录接口..." -ForegroundColor Yellow
$loginBody = @{
    userAccount = "user123"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8123/api/user/login" `
        -Method POST `
        -Body $loginBody `
        -ContentType "application/json"
    
    Write-Host "登录成功!" -ForegroundColor Green
    Write-Host "用户ID: $($loginResponse.id)"
    Write-Host "用户名: $($loginResponse.username)"
    Write-Host "Token: $($loginResponse.token.Substring(0, 30))..." 
    
    $token = $loginResponse.token
} catch {
    Write-Host "登录失败: $_" -ForegroundColor Red
    exit
}

# 2. 使用 Token 访问受保护接口
Write-Host "`n2. 测试使用 Token 访问受保护接口..." -ForegroundColor Yellow
$headers = @{
    "Authorization" = "Bearer $token"
}

try {
    $checkResponse = Invoke-RestMethod -Uri "http://localhost:8123/api/resume/check" `
        -Method GET `
        -Headers $headers
    
    Write-Host "访问成功! 简历检查结果: $checkResponse" -ForegroundColor Green
} catch {
    Write-Host "访问失败: $_" -ForegroundColor Red
}

# 3. 测试无 Token 访问
Write-Host "`n3. 测试无 Token 访问(应返回 401)..." -ForegroundColor Yellow
try {
    $noTokenResponse = Invoke-RestMethod -Uri "http://localhost:8123/api/resume/check" `
        -Method GET
    Write-Host "意外成功: $noTokenResponse" -ForegroundColor Red
} catch {
    Write-Host "正确返回 401 错误" -ForegroundColor Green
}

# 4. 测试错误 Token
Write-Host "`n4. 测试错误 Token(应返回 401)..." -ForegroundColor Yellow
$badHeaders = @{
    "Authorization" = "Bearer invalid_token"
}

try {
    $badTokenResponse = Invoke-RestMethod -Uri "http://localhost:8123/api/resume/check" `
        -Method GET `
        -Headers $badHeaders
    Write-Host "意外成功: $badTokenResponse" -ForegroundColor Red
} catch {
    Write-Host "正确返回 401 错误" -ForegroundColor Green
}

Write-Host "`n========== JWT 认证测试完成 ==========" -ForegroundColor Green
```

保存为 `test-jwt.ps1` 并运行:
```powershell
.\test-jwt.ps1
```
