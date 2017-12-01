FROM projectriff/java-function-invoker:0.0.2-snapshot
ARG FUNCTION_JAR=/functions/vote-slider-counter-1.0.0.jar
ARG FUNCTION_CLASS=functions.VoteSliderCounter
ADD target/vote-slider-counter-1.0.0.jar $FUNCTION_JAR
ENV FUNCTION_URI file://${FUNCTION_JAR}?handler=${FUNCTION_CLASS}
