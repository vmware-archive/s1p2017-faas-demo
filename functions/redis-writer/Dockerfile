FROM sk8s/java-function-invoker:0.0.1-SNAPSHOT
ENV REDIS_HOST counters-redis.default.svc.cluster.local
ENV REDIS_PORT 6379
ADD target/redis-writer-1.0.0.jar /functions/function.jar
