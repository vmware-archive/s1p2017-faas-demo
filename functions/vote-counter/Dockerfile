FROM projectriff/node-function-invoker:0.0.8
ENV FUNCTION_URI /functions/
COPY . ${FUNCTION_URI}
RUN (cd ${FUNCTION_URI} && npm install --production)
