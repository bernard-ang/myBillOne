set -e
export DRY_RUN=
# to see execute steps/commands, run in dry run mode
#export DRY_RUN=" --dryRun"

export BUILDER=
if [[ $(uname -p) == 'arm' ]]; then
  # pororo is the name of my remote docker builder
  export BUILDER=" --builder amd64-builder"
fi

# NOTE: you will need sidekick (sk). install via: npm install -g @evos-tech/sidekick@next --registry https://npm.evos.tech

#sk dev build-image --name grabbill/grabbill-website --dockerFile grabbill-ui/website.Dockerfile $BUILDER $DRY_RUN
sk dev build-image --name grabbill/grabbill-client --dockerFile grabbill-ui/client.Dockerfile $BUILDER $DRY_RUN
#sk dev build-image --name grabbill/grabbill-admin --dockerFile grabbill-ui/admin.Dockerfile $BUILDER $DRY_RUN
