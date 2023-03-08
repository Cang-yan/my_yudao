根据 ruoyi-vue-pro([ruoyi-vue-pro: 🔥 官方推荐 🔥 RuoYi-Vue 全新 Pro 版本，优化重构所有功能。基于 Spring Boot + MyBatis Plus + Vue & Element 实现的后台管理系统 + 微信小程序，支持 RBAC 动态权限、数据权限、SaaS 多租户、Flowable 工作流、三方登录、支付、短信、商城等功能。你的 ⭐️ Star ⭐️，是作者生发的动力！ (gitee.com)](https://gitee.com/zhijiantianya/ruoyi-vue-pro))修改而来的框架，符合自身的开发习惯

## 修改要点

* 主键均改成了string类型，uuid便于迁移到分布式中
* 更改了代码生成的目录结构，把前端生成的代码移入了`codegen`文件夹下，生成的代码不会把开发者写的代码覆盖掉





## 前端部署

```shell
# 进入项目目录
cd yudao-ui-admin

# 安装 Yarn，提升依赖的安装速度
npm install --global yarn
# 安装依赖
yarn install

# 启动服务
npm run dev
```





## 后端部署

### 软件环境要求



mysql 5.7

redis 5.0.14.1  最新版本就好



### 数据库文件初始化

创建数据库

运行    `根目录/sql/mysql/yudao.sql`  即可



### 项目打包



#### 倘若提示单元测试类中有些包未找到，则随意运行一个单元测试，重新运行主类即可



#### 修改配置文件

`yudao-server`模块下的  `resource/application.yml `打开profile.active注释



`yudao-server`模块下的  `resource/application-dev.yml`   修改mysql redis的相关配置



#### 打包

maven的打包就好  打包完的jar文件  在` yudao-server/test` 目录下





## 开发中需要注意

1. 每次代码生成某个表，所有的sql文件都要运行，涉及到后台管理系统的**目录**问题
2. 后台管理系统  超级管理员用户名密码

```
admin
admin123
```

3. 创建新数据表时，要把租户字段也加上



