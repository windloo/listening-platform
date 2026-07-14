# ===== 构建阶段：Maven 编译 =====
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 配置阿里云 Maven 镜像（国内构建加速）
RUN mkdir -p /root/.m2 && echo '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

# 先复制 pom 文件，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY model/pom.xml model/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY services/pom.xml services/pom.xml
COPY services/identity-service/pom.xml services/identity-service/pom.xml
COPY services/listening-service/pom.xml services/listening-service/pom.xml
COPY services/file-service/pom.xml services/file-service/pom.xml
COPY services/media-encoder-service/pom.xml services/media-encoder-service/pom.xml
COPY services/search-service/pom.xml services/search-service/pom.xml
RUN mvn dependency:go-offline -B || true
# 复制全部源码并构建
COPY . .
RUN mvn clean package -Dmaven.test.skip=true

# ===== 运行阶段：JRE =====
FROM eclipse-temurin:17-jre
WORKDIR /app
ARG SERVICE
COPY --from=builder /build/${SERVICE}/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx256m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]