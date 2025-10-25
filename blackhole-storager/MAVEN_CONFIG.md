# Maven Configuration for Blackhole-Storager

## 配置说明

本项目已配置Maven使用以下设置：

### 1. Maven安装路径
- 安装路径：`D:\Trae area\apache-maven-3.9.9`
- 配置文件：`D:\Trae area\apache-maven-3.9.9\conf\settings.xml`

### 2. 本地仓库路径
- 本地仓库：`D:\Trae area\.m2\repository`

### 3. 运行脚本
- Windows脚本：`mvn-run.bat`
- Unix/Linux脚本：`mvn-run.sh`

## 使用方法

### Windows系统
```bash
# 编译项目
mvn-run.bat clean compile

# 打包项目
mvn-run.bat clean package

# 运行测试
mvn-run.bat test

# 安装到本地仓库
mvn-run.bat clean install
```

### Unix/Linux系统
```bash
# 给脚本添加执行权限
chmod +x mvn-run.sh

# 编译项目
./mvn-run.sh clean compile

# 打包项目
./mvn-run.sh clean package

# 运行测试
./mvn-run.sh test

# 安装到本地仓库
./mvn-run.sh clean install
```

## 依赖配置

项目已配置以下主要依赖：
- SQLite JDBC Driver
- MySQL JDBC Driver  
- Redis Client (Jedis)
- Hive JDBC Driver
- HBase Client
- HikariCP (连接池)
- Jackson (YAML配置)
- SLF4J (日志)

## 镜像配置

Maven配置中已添加阿里云镜像，加速依赖下载：
```xml
<mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```