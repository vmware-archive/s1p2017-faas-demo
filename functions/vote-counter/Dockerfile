FROM projectriff/node-function-invoker:0.0.2-s1p
RUN npm install redis@2.8.0 --production
ENV FUNCTION_URI /functions/function.js
ADD vote-counter.js ${FUNCTION_URI}
