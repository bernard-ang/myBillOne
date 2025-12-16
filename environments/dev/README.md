# `dev` environment

To get a local development environment up and running, follow these instructions.

- [Optional for local dev environment] Run `build-ui-images.sh` to build Docker images for UI apps.
  - NOTE: All `.sh` scripts must be run from the script's directory.
- [Optional for local dev environment] Run `build-backend-images.sh` to build Docker images for backend apps.
- Run `1-create-network.sh` to create `traefik-network` Docker network.
- Run `2-copy-config.sh` to copy configuration to user directory.
- Run `sudo vi /etc/hosts` and add the following entries to `/etc/hosts`:

```
127.0.0.1 traefik.grabbill.localhost

127.0.0.1 db.grabbill.localhost
127.0.0.1 queue.grabbill.localhost
127.0.0.1 queue-console.grabbill.localhost
127.0.0.1 storage.grabbill.localhost
127.0.0.1 storage-console.grabbill.localhost
127.0.0.1 mail.grabbill.localhost
127.0.0.1 mail-console.grabbill.localhost

127.0.0.1 www.grabbill.localhost
127.0.0.1 app.grabbill.localhost
127.0.0.1 admin.grabbill.localhost
127.0.0.1 api.grabbill.localhost
127.0.0.1 engine.grabbill.localhost
```

- Run `docker compose up -d` from `environments/dev/grabbill-infra` to start the following infra services:
  - `traefik`
    - Route traffic to other services.
    - Login via `https://traefik.grabbill.localhost`. 
    - Default login `admin:AN&58VnMuck^`. To add/change this, see `add-traefik-user.sh`.
  - `mariadb`
    - Accessible via `db.grabbill.localhost:3306`.
    - See `grabbill-infra/docker-compose.yml` for credentials.
  - `rabbitmq`
    - Accessible via `queue.grabbill.localhost:5672`.
    - See `grabbill-infra/docker-compose.yml` for credentials.
    - Login to console via `https://queue-console.grabbill.localhost`.
  - `minio`
      - Accessible via `https://storage.grabbill.localhost`.
      - See `grabbill-infra/docker-compose.yml` for credentials.
      - Login to console via `https://storage-console.grabbill.localhost`.
  - 'mailhog'
      - Accessible via 'mail.grabbill.localhost:1025'
      - Login to console via 'https://mail-console.grabbill.localhost/'

- With `grabbill-infra` composed services, all non-application specific services is now usable locally.

- To use application services, run `docker compose up -d` from `environments/dev/grabbill-app`. It provides the following services:
  - `grabbill-website`
    - Accessible via `https://www.grabbill.localhost`.
  - `grabbill-client`
      - Accessible via `https://app.grabbill.localhost`.
  - `grabbill-admin`
      - Accessible via `https://admin.grabbill.localhost`.
  - `grabbill-server`
      - Accessible via `https://api.grabbill.localhost`.
  - `grabbill-engine`
      - Accessible via `https://engine.grabbill.localhost`. Is this needed?

Note: Each time `traefik` service in `grabbill-infra` is recreated, the self-sign HTTP certs will change. Impact: Need to restart browsers (all instances) for new certs to be recognised.

## GrabBill Backend

### Prerequisite
- JDK 11
- maven 3

- Go to grabbill-backend
  - `mvn clean install`
- To start GrabBill server
  - `mvn spring-boot:run -pl grabbill-server`
- To start GrabBill engine
- `mvn spring-boot:run -pl grabbill-engine`

## GrabBill Frontend

### Prerequisite
- NPM 18

- Go to grabbill-ui
  - `npm i`
  - `npm start`