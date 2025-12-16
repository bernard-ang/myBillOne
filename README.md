# GrabBill

## Setup development environment:
- Refer to development environment [README.md](environments/dev/README.md)


### Pre-requisites:
- Set the DigitalOcean token via `export DO_TOKEN=xxx`.
- Install sidekick (sk). install via `npm install -g @evos-tech/sidekick@next --registry https://npm.evos.tech`
    - Make sure the version is at least `0.0.1-next.12`. To check, `sk --version`.

### Environments

#### UAT

Branch: deploy/uat

Domain: *.uat.grabbill.com

#### Production

Branch: deploy/prod

Domain: *.mybillone.com

#### E-Statement UAT (e-stmt-uat)

Branch: deploy/op-uat

Domain: *.uat.e-stmt.com

#### E-Statement Production (e-stmt-prod)

Branch: deploy/op-prod

Domain: sunlifemalaysia.e-stmt.com, *.sunlifemalaysia.e-stmt.com


### Useful DevOps Commands

#### Tail Server Logs

```
# tail & follow uat logs for frontend/backend, since 72h ago, excluding noise (scheduler query hibernate queries)
sk ops logs -e uat -g grabbill-frontend -t -f --since 72h
sk ops logs -e uat -g grabbill-backend -t -f --since 72h | grep -v "job0_.status in (?)"
sk ops logs -e uat -g grabbill-backend -t -f --since 72h | grep -v "job0_.status in (?)" | grep -E -i "error|exception|warn"

# tail all production logs from core (traefik, mariadb, rabbitmq, portainer) group since 72 hours ago
sk ops logs -e prod -g core -t --since 72h

# tail all uat logs since 72 hours ago, excluding noise (scheduler query hibernate queries)
sk ops logs -e uat -t -f --since 72h | grep -v "job0_.status in (?)"

# tail all production logs since 72 hours ago, excluding noise (scheduler query hibernate queries)
sk ops logs -e prod -t -f --since 72h | grep -v "job0_.status in (?)"

# tail commands
sk ops logs --tail 100 -f -e uat --group grabbill-frontend
sk ops logs --tail 100 -f -e uat --group grabbill-backend
sk ops logs --tail 100 -f -e uat --service api
sk ops logs -e uat -g backend --since 10m

```

#### Monitor Services 
```
# list/monitor running services in UAT
sk ops stats -e uat -m grabbill-uat-m-01
```

#### SSH Commands

```
sk ops ssh -e uat
sk ops ssh-container -e uat
```

#### Update and restart for entire environment
```
sk ops pull -e uat
sk ops down -e uat
sk ops up -e uat

# update, down and up by group (for backend)
sk ops pull -e uat --group grabbill-backend
sk ops down -e uat --group grabbill-backend
sk ops up -e uat --group grabbill-backend

# update, down and up by group (for frontend)
sk ops pull -e uat --group grabbill-frontend
sk ops down -e uat --group grabbill-frontend
sk ops up -e uat --group grabbill-frontend

# update and restart by service
sk ops pull -e uat --service website
sk ops down -e uat --service website
sk ops up -e uat --service website
```

### Build & Deploy

Build and push Docker images. Credentials to push to docker.evos.tech registry needed.

```
cd grabbill-backend
./1-build-backend-images.sh
./2-push-backend-images.sh

cd grabbill-ui
./1-build-ui-images.sh
./2-push-ui-images.sh
```

Deployment is done via `docker-compose` commands with `DOCKER_HOST` set to the remote server.

Example:
```
> cd ./environments/uat/environments/grabbill-backend/api
> DOCKER_HOST=ssh://root@178.128.56.102 docker-compose ps

       Name                  Command            State    Ports  
----------------------------------------------------------------
api_grabbill-api_1   java -jar app/server.jar   Up      8080/tcp
```

For this to work, user must be able to log in to the server via SSH keys.

https://docs.digitalocean.com/products/droplets/how-to/add-ssh-keys/to-existing-droplet/
```
# from local machine
cat ~/.ssh/id_rsa.pub
# copy the output

# via digital ocean's web management, access the droplet's console as root
vi ~/.ssh/authorized_keys
# paste contents of id_rsa.pub and save
```

## Creating New Environment for OP

- Map the required domains (DNS config), managed by Alex:
```
  144.126.240.17
  generali.e-stmt.com
  *.generali.e-stmt.com
```

- Set DO_TOKEN environment.

- List digital ocean images

```shell
sk infra ldoi | grep ubuntu
```

- List digital ocean droplet sizes
```shell
sk infra ldods | grep 4gb
```

- Add a new environment to `sidekick.json` (based on existing OP environments like `op-generali`)

- Confirm this environment has not been provisioned
```shell
sk infra status
```

- Create infrastructure resources for environment
```shell
# xxx is environment name
sk infra provision -e xxx
```

- For production environment, should assign reserved IP (USD5/month)

- Copy docker compose services from an appropriate environment (e.g. `op-generali`)

- Update docker compose services for "core" group in the new environment. Items to change include:
  - domain name references
  - traefik basic auth admin password
  - mariadb passwords

- To update basic auth passwords
```
cd environments/op-generali/core/traefik/config/htpasswd
./add-user.sh server admin
```

- Update docker compose services for "backend" group in the new environment. Items to change include:
  - review diff between 2 op prod environments (e.g. `op-prod` `op-generali`)
  - digital ocean spaces must be created from digital ocean's website

- Once changes are made, update Trello card https://trello.com/c/Pn9Oinxp/14-documation-on-premise-server-details

TODO: CI/jenkins automation
sk dev build-image --builder amd64-builder --name grabbill/grabbill-client-generali-op --dockerFile grabbill-ui/client-op-prod-generali.Dockerfile
sk dev push-image --name grabbill/grabbill-client-generali-op --registry docker.evos.tech --tag jenkins
