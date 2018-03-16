FROM projectriff/node-function-invoker:0.0.5-snapshot
RUN npm install redis@2.8.0 --production
ENV FUNCTION_URI ./vote-counter.js
COPY vote-counter.js .
