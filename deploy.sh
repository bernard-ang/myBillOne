# deployment involves pulling latest images, down and up the relevant
set -e
# defaults to "uat". to target production, run "./deploy.sh prod"
export TARGET_ENV=${1:-uat}

echo "Deploying to ${TARGET_ENV}"

#sk ops pull -e ${TARGET_ENV} -g core
sk ops pull -e ${TARGET_ENV} -g grabbill-backend
sk ops pull -e ${TARGET_ENV} -g grabbill-frontend

#sk ops down -e ${TARGET_ENV} -g core
sk ops down -e ${TARGET_ENV} -g grabbill-backend
sk ops down -e ${TARGET_ENV} -g grabbill-frontend

#sk ops up -e ${TARGET_ENV} -g core
sk ops up -e ${TARGET_ENV} -g grabbill-backend
sk ops up -e ${TARGET_ENV} -g grabbill-frontend
