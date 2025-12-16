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

sk dev build-image --name grabbill/grabbill-engine --dockerFile grabbill-backend/engine.Dockerfile $BUILDER $DRY_RUN
sk dev build-image --name grabbill/grabbill-server --dockerFile grabbill-backend/server.Dockerfile $BUILDER $DRY_RUN
