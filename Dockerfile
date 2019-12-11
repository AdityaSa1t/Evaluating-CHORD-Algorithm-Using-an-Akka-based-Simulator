FROM java:8-jdk-alpine
COPY ./target/scala-2.12/abhijeet_mohanty_cs441_course_project-assembly-0.1.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "abhijeet_mohanty_cs441_course_project-assembly-0.1.jar"]