# On-Premise Servers

DNS mapping
*.uat.e-stmt.com mapped to 175.144.213.21

## UAT
Installed Ubuntu server 22.04

### Installation Configuration

VM for uat (ubuntu login)
grabbill
P@ssw0rd

Subnet
192.168.10.0/24 (testing this)

IP Address
192.168.10.40

Gateway
192.168.10.254

Name Servers
192.168.10.1

Machine Name
DOCUPPUSVR10-GB2

### Post Installation Steps

sudo apt update
sudo apt upgrade
sudo apt-get htop mc screen

install docker via:
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh ./get-docker.sh --dry-run

add $USER (grabbill) to docker group so we don't have to sudo to use docker
https://docs.docker.com/engine/install/linux-postinstall/

reconfigure ssh server port to 2200

enable key based login via ssh (password login disabled)
ssh-copy-id -p 2200 grabbill@app.uat.e-stmt.com

### Verified

- able to connect remotely via port 2200, 80, 443, 3605

portainer
admin/kuvMD^p83M%2
