# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine
# copy jar into image
COPY target/scala-2.12/abhijeet_mohanty_cs441_course_project_2.12-0.1.jar /course_project.jar
# run application with this command line
CMD ["/usr/bin/java", "-jar", "/course_project.jar"]