FROM projectriff/java-function-invoker:0.0.1-SNAPSHOT
ARG FUNCTION_JAR=/functions/counter-1.0.0.jar
ARG FUNCTION_CLASS=functions.Counter
ADD target/counter-1.0.0.jar $FUNCTION_JAR
ENV FUNCTION_URI file://${FUNCTION_JAR}?handler=${FUNCTION_CLASS}
