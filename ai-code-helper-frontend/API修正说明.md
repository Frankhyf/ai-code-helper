# 前端API对接修正说明

## 修正概述

已根据后端Controller代码完成前端API对接修正，主要修正了字段名映射、请求参数格式和响应数据处理。

---

## 一、修正内容详情

### 1. 用户认证模块

#### 1.1 登录接口 (`POST /user/login`)

**修正前：**
```typescript
{
  account: '用户账号',
  password: '密码'
}
```

**修正后：**
```typescript
{
  userAccount: '用户账号',
  userPassword: '密码'
}
```

**响应数据映射：**
- 后端返回：`LoginUserVO { userAccount, userName, userRole, ... }`
- 前端映射为：`User { account, nickname, role, ... }`

#### 1.2 注册接口 (`POST /user/register`)

**修正前：**
```typescript
{
  account: '用户账号',
  password: '密码',
  confirmPassword: '确认密码'
}
```

**修正后：**
```typescript
{
  userAccount: '用户账号',
  userPassword: '密码',
  checkPassword: '确认密码'
}
```

**注意：** 注册接口不支持昵称参数，昵称需要注册后通过更新接口设置。

#### 1.3 获取登录用户信息 (`GET /user/get/login`)

**响应字段映射：**
- `userAccount` → `account`
- `userName` → `nickname`
- `userAvatar` → `avatar`
- `userRole` → `role`（'admin' 或 'user'）

---

### 2. 应用管理模块

#### 2.1 创建应用 (`POST /app/add`)

**请求参数：**
```typescript
{
  initPrompt: '应用描述或初始提示词'
}
```

**返回：** 应用ID（Long类型）

#### 2.2 更新应用 (`POST /app/update`)

**请求参数：**
```typescript
{
  id: 123,
  appName: '新应用名称'
}
```

**注意：** 普通用户只能更新 `appName` 字段。

#### 2.3 应用列表查询

**分页参数映射：**
- 前端使用：`current`（当前页）、`size`（每页大小）
- 后端需要：`pageNum`、`pageSize`

**应用类型映射：**
- 前端：`'single'` | `'multiple'`
- 后端：`'html'` | `'multi_file'`

**响应数据字段：**
- `records`: 应用列表
- `current`: 当前页
- `pages`: 总页数
- `totalRow`: 总记录数
- `size`: 每页大小

**AppVO字段映射：**
- `appName` → `name`
- `initPrompt` → `description`
- `codeGenType` → `type`（转换为 'single' 或 'multiple'）
- `deployKey` → 用于确定应用状态
- `priority` → 99 表示精选应用

#### 2.4 部署应用 (`POST /app/deploy`)

**请求参数：**
```typescript
{
  appId: 123
}
```

**返回：** 完整的部署URL字符串（例如：`http://localhost:8123/api/static/html_123/`）

**deployKey提取：**
从返回的URL中提取deployKey（例如：`html_123`），用于后续的预览访问。

#### 2.5 AI对话生成 (`GET /app/chat/gen/code`)

**SSE流式接口：**
- URL参数：`appId`、`message`
- 响应格式：每个SSE事件包含 `{"d": "内容片段"}`
- 结束事件：`event: done`

---

### 3. 管理员模块

已创建独立的管理员API服务文件 `src/utils/admin-api.ts`。

#### 3.1 管理员应用管理

- `POST /app/admin/list/page/vo` - 获取应用列表
- `GET /app/admin/get/vo` - 获取应用详情
- `POST /app/admin/update` - 更新应用（支持更多字段）
- `POST /app/admin/delete` - 删除应用

#### 3.2 管理员用户管理

- `GET /user/list` - 获取所有用户
- `GET /user/getInfo/{id}` - 获取用户详情
- `POST /user/add` - 添加用户
- `PUT /user/update` - 更新用户
- `DELETE /user/remove/{id}` - 删除用户

---

## 二、配置文件修改

### 1. 新增文件

#### `.gitignore`
添加了标准的前端项目忽略规则，解决了ESLint报错问题。

#### `src/utils/admin-api.ts`
集中管理管理员相关的API调用，便于维护。

### 2. 修改文件

#### `src/utils/request.ts`
- 添加了请求拦截器（预留token认证）
- 修正了响应拦截器的成功判断逻辑（`code === 200`）
- 优化了错误处理逻辑

#### `src/stores/user.ts`
- 修正登录/注册的请求参数字段名
- 修正用户信息字段映射逻辑

#### `src/stores/app.ts`
- 修正应用创建、更新、删除的请求参数
- 修正应用列表的分页参数和响应处理
- 修正部署接口的URL解析逻辑
- 优化了字段映射和数据转换

---

## 三、测试指南

### 1. 环境准备

确保后端服务已启动并监听在 `http://localhost:8123`。

**检查后端服务：**
```bash
curl http://localhost:8123/api/user/list
```

如果返回用户列表，说明后端服务正常。

### 2. 前端启动

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问：`http://localhost:5173`（或终端显示的端口）

### 3. 功能测试清单

#### 3.1 用户认证测试

**测试注册：**
1. 访问 `/register` 页面
2. 输入账号（3-20位字符）
3. 输入密码（8位以上，需包含大小写字母和数字）
4. 确认密码
5. 点击注册

**预期结果：**
- 注册成功，跳转到登录页面
- 浏览器Network标签显示请求参数为 `{userAccount, userPassword, checkPassword}`

**测试登录：**
1. 访问 `/login` 页面
2. 输入注册的账号和密码
3. 点击登录

**预期结果：**
- 登录成功，跳转到首页
- 用户信息保存在localStorage
- 导航栏显示用户昵称/账号

#### 3.2 应用管理测试

**测试创建应用：**
1. 登录后访问首页
2. 在Hero区域输入应用描述（例如："创建一个简单的计算器"）
3. 点击生成

**预期结果：**
- 请求参数为 `{initPrompt: "创建一个简单的计算器"}`
- 返回应用ID
- 可以在"我的应用"列表中看到新应用

**测试应用列表：**
1. 访问 `/app/list` 页面

**预期结果：**
- 显示当前用户创建的应用列表
- 每个应用卡片显示名称、类型、状态
- 搜索和筛选功能正常

**测试AI对话：**
1. 在应用列表点击某个应用的"编辑对话"按钮
2. 在对话框输入优化需求
3. 点击发送

**预期结果：**
- 建立SSE连接
- 左侧显示对话历史
- AI回复逐字显示（流式输出）
- 进度条正常显示

**测试应用部署：**
1. 在应用对话页面点击"部署应用"按钮

**预期结果：**
- 显示"正在部署..."
- 部署成功后显示"部署成功"
- 应用状态变为"已部署"
- 右侧预览iframe显示部署的应用

#### 3.3 管理员功能测试

**前提条件：** 使用管理员账号登录（`userRole = 'admin'`）

**测试应用管理：**
1. 访问 `/admin/app` 页面

**预期结果：**
- 显示所有用户的应用列表
- 可以搜索、筛选、删除应用
- 可以设置/取消精选应用

---

## 四、常见问题排查

### 1. 登录失败

**问题：** 点击登录后提示"请求失败"

**排查步骤：**
1. 打开浏览器开发者工具 → Network标签
2. 查看 `/user/login` 请求的详情
3. 检查请求参数是否为 `{userAccount, userPassword}`
4. 检查响应状态码和响应内容

**常见原因：**
- 账号或密码错误
- 后端服务未启动
- 跨域问题（需要后端配置CORS）

### 2. 应用列表为空

**问题：** 访问"我的应用"页面显示空列表

**排查步骤：**
1. 确认已登录并创建过应用
2. 检查 `/app/my/list/page/vo` 请求
3. 查看请求参数中的 `userId` 是否正确

**常见原因：**
- 用户未创建应用
- userId参数错误
- 后端查询条件有误

### 3. AI对话无响应

**问题：** 发送消息后AI一直"生成中"

**排查步骤：**
1. 检查 `/app/chat/gen/code` 的SSE连接状态
2. 查看Console是否有错误日志
3. 确认后端AI服务配置正确

**常见原因：**
- 后端AI服务未配置或API密钥失效
- SSE连接被防火墙阻断
- 消息参数编码问题

### 4. 部署失败

**问题：** 点击部署后提示"部署失败"

**排查步骤：**
1. 检查 `/app/deploy` 请求
2. 查看请求参数是否为 `{appId: 数字}`
3. 查看后端日志

**常见原因：**
- 应用代码未生成
- 应用ID错误
- 文件系统权限不足

---

## 五、后续优化建议

### 1. 安全性增强

- 实现JWT token认证机制
- 添加请求签名验证
- 实现refresh token自动刷新

### 2. 用户体验优化

- 添加骨架屏加载效果
- 优化错误提示信息
- 实现离线缓存策略

### 3. 功能完善

- 完善用户管理界面
- 实现应用版本管理
- 添加应用分享功能
- 实现应用模板市场

### 4. 性能优化

- 实现虚拟滚动
- 图片懒加载
- 路由懒加载
- 代码分割优化

---

## 六、API端点总览

### 用户相关
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /user/register | 用户注册 | 否 |
| POST | /user/login | 用户登录 | 否 |
| POST | /user/logout | 用户登出 | 是 |
| GET | /user/get/login | 获取当前用户 | 是 |

### 应用相关（普通用户）
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /app/add | 创建应用 | 是 |
| POST | /app/update | 更新应用 | 是 |
| POST | /app/delete | 删除应用 | 是 |
| GET | /app/get/vo | 获取应用详情 | 否 |
| POST | /app/my/list/page/vo | 我的应用列表 | 是 |
| POST | /app/good/list/page/vo | 精选应用列表 | 否 |
| POST | /app/deploy | 部署应用 | 是 |
| GET | /app/chat/gen/code | AI对话生成（SSE） | 是 |

### 应用相关（管理员）
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /app/admin/list/page/vo | 所有应用列表 | 管理员 |
| GET | /app/admin/get/vo | 获取应用详情 | 管理员 |
| POST | /app/admin/update | 更新应用 | 管理员 |
| POST | /app/admin/delete | 删除应用 | 管理员 |

### 用户管理（管理员）
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /user/list | 所有用户列表 | 管理员 |
| GET | /user/getInfo/{id} | 获取用户详情 | 管理员 |
| POST | /user/add | 添加用户 | 管理员 |
| PUT | /user/update | 更新用户 | 管理员 |
| DELETE | /user/remove/{id} | 删除用户 | 管理员 |

### 静态资源
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | /static/{deployKey}/** | 访问已部署应用 | 否 |

---

## 七、联系与支持

如遇到问题或需要帮助，请：
1. 查看浏览器Console和Network标签的错误信息
2. 查看后端日志
3. 参考本文档的"常见问题排查"章节

祝测试顺利！ 🎉

