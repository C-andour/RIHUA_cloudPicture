# 使用官方的 OpenJDK 镜像作为基础镜像
FROM adoptopenjdk/openjdk11:latest
# 设置工作目录
WORKDIR /app
# 将本地的 JAR 文件复制到容器中
COPY rihua-caritas-backend-0.0.1-SNAPSHOT.jar /app/app.jar
# 暴露应用运行的端口（假设是 8080）
EXPOSE 8123
# 定义启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]

# 删除原有jar文件
# rm ../rihua-caritas-backend/rihua-caritas-backend-0.0.1-SNAPSHOT.jar
# 转移jar文件
# mv rihua-caritas-backend-0.0.1-SNAPSHOT.jar ../rihua-caritas-backend/
# 构建镜像命令：
# docker build -t rihua-caritas-backend .
# 运行镜像命令：
# docker run -d -p 8123:8123 --name rihua rihua-caritas-backend