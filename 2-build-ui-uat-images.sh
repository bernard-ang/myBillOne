set -e
export DRY_RUN=
# to see execute steps/commands, run in dry run mode
#export DRY_RUN=" --dryRun"

export BUILDER=
if [[ $(uname -p) == 'arm' ]]; then
  # pororo is the name of my remote docker builder
  export BUILDER=" --builder pororo"
fi

# NOTE: you will need sidekick (sk). install via: npm install -g @evos-tech/sidekick@next --registry https://npm.evos.tech

sk dev build-image --name grabbill/grabbill-website-uat --dockerFile grabbill-ui/website-uat.Dockerfile $BUILDER $DRY_RUN
sk dev build-image --name grabbill/grabbill-client-uat --dockerFile grabbill-ui/client-uat.Dockerfile $BUILDER $DRY_RUN
sk dev build-image --name grabbill/grabbill-admin-uat --dockerFile grabbill-ui/admin-uat.Dockerfile $BUILDER $DRY_RUN
