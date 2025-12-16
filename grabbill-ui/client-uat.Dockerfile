# reference:
# https://dev.to/avatsaev/create-efficient-angular-docker-images-with-multi-stage-builds-1f3n

# --- stage 1: build
# allow caching of this stage unless package.json has changed
FROM node:20-alpine as builder

WORKDIR /build
COPY package.json package-lock.json buildinfo.json ./
RUN npm ci && mkdir ../app && mv ./node_modules ../app

WORKDIR /app
COPY . .
RUN npm run build-uat:client

# --- stage 2: setup
FROM nginx:1.28-alpine

RUN rm -fr /usr/share/nginx/html/*

COPY --from=builder /app/dist/client /usr/share/nginx/html
COPY config/nginx/conf.d /etc/nginx/conf.d/

CMD ["nginx", "-g", "daemon off;"]

EXPOSE 80
