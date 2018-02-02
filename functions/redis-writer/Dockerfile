FROM projectriff/node-function-invoker:0.0.4-snapshot
RUN npm install redis@2.8.0 --production
ENV FUNCTION_URI ./redis-writer.js
COPY redis-writer.js .
