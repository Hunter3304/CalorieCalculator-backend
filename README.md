# 🥗 CalorieCalculator - Backend

基于 Spring Boot 构建的卡路里计算器 RESTful API 服务。提供底层食物字典数据支撑与核心的 4-4-9 宏量营养素（Macros）计算逻辑。

## 🛠️ 技术栈 (Tech Stack)
- **核心框架:** Java 17 + Spring Boot
- **数据库:** PostgreSQL
- **ORM 数据持久层:** MyBatis
- **工具:** Lombok

## 🚀 快速启动 (Quick Start)

### 1. 环境准备
- JDK 17 或更高版本
- PostgreSQL 数据库
- Maven

### 2. 数据库初始化
请在 PostgreSQL 中新建数据库，并执行 `src/main/resources/sql/schema.sql` 脚本以初始化 `food_items` 表和测试数据。

### 3. 配置数据库连接
请检查并修改 `src/main/resources/application.yml`（或 properties）文件中的数据库用户名和密码：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/calorie_calculator
    username: postgres
    password: 123456
