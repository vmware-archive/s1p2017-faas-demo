FROM projectriff/node-function-invoker:latest
RUN npm install redis@2.8.0 --production
ENV FUNCTION_URI ./vote-counter.js
COPY vote-counter.js .
