FROM node:8.9.1-alpine
WORKDIR /usr/app
COPY . .
RUN npm install --production

EXPOSE 3000
CMD [ "node", "server" ]