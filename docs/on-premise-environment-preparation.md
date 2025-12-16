# 2024-02-29 On Premise Environment Preparation

- Production URL:
    - https://app.e-stmt.com/
- UAT URL:
  - https://uat.app.e-stmt.com/

- For each environment, the following services will be running within Docker:
  - Core Services
    - Traefik
    - RabbitMQ
    - MariaDB
    - MinIO
    - Log Viewer
    - DB Viewer
    - Portainer
  - Application Services
    - GrabBill On-Premise (OP) UI
    - GrabBill OP API
    - GrabBill OP Engine

- For each environment, we will need the following subdomains (e.g. app.e-stmt.com, etc):
    - app.xxx
    - api.xxx
    - traefik.xxx
    - logs.xxx
    - db.xxx
    - minio.xxx
    - queue.xxx
    - portainer.xxx

- IP addresses for Windows server for production & UAT.
  - Admin user access required.
  - Make sure the Windows server is properly patched and updated.

- Windows Server ports required to be open to external access:
  - RDP (3389)
  - SSH (22)
  - Docker (2375, 2376)
  - MariaDB (3306)
  - HTTP/HTTPS (80, 443)

- Do not recommend sharing OS for both production & UAT as it complicates environment setup & maintenance.

- Note: Windows server configuration, maintenance, patching and server/data backups are not within our scope. So, ensure that proper process on these are performed.
