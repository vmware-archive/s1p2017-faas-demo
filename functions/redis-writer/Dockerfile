FROM projectriff/node-function-invoker:0.0.6-snapshot
ENV FUNCTION_URI /functions/
COPY . ${FUNCTION_URI}
RUN (cd ${FUNCTION_URI} && npm install --production)
