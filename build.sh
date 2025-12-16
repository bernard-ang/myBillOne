set -e
# defaults to "uat". to target production, run "./build.sh prod"
export TARGET_ENV=${1:-uat}

if [ "$TARGET_ENV" == "uat" ]
then
  echo "Building for ${TARGET_ENV}"
  ./1-build-backend-images.sh
  ./2-build-ui-uat-images.sh
  ./3-push-uat-images.sh
fi

if [ "$TARGET_ENV" == "prod" ]
then
  echo "Building for ${TARGET_ENV}"
  ./1-build-backend-images.sh
  ./4-build-ui-prod-images.sh
  ./5-push-prod-images.sh
fi
