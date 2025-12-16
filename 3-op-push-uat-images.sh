set -e
export TAG_SUFFIX=${1:-${USER}}
export DRY_RUN=
# to see execute steps/commands, run in dry run mode
#export DRY_RUN=" --dryRun --force"

# NOTE: you will need sidekick (sk). install via: npm install -g @evos-tech/sidekick@next --registry https://npm.evos.tech

sk dev push-image --name grabbill/grabbill-engine --registry docker.evos.tech --tag ${TAG_SUFFIX} $DRY_RUN
sk dev push-image --name grabbill/grabbill-server --registry docker.evos.tech --tag ${TAG_SUFFIX} $DRY_RUN

sk dev push-image --name grabbill/grabbill-client-op-uat --registry docker.evos.tech --tag ${TAG_SUFFIX} $DRY_RUN
