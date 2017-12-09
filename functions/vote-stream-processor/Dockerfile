FROM projectriff/java-function-invoker:0.0.2-snapshot-streams
ARG FUNCTION_JAR=/functions/vote-stream-processor-1.0.0.jar
ARG FUNCTION_CLASS=functions.VoteStreamProcessor
ADD target/vote-stream-processor-1.0.0.jar $FUNCTION_JAR
ENV FUNCTION_URI file://${FUNCTION_JAR}?handler=${FUNCTION_CLASS}
