#!/usr/bin/env bash
set -o errexit
set -o nounset
set -o noclobber

function create_from_local() {
  declare -r IS_PRIVATE="true"

  # Create repository to use api.
  curl \
    --silent \
    -X POST \
    -u "$USERNAME:$PASSWORD" \
    -H "Content-Type: application/json" \
    -d '{"scm": "git", "is_private": "'${IS_PRIVATE}'", "fork_policy": "no_public_forks" }' \
    "https://api.bitbucket.org/2.0/repositories/$USERNAME/$REPO_NAME" \
    | grep -oE 'https://[^"]*.git' | sed 's/^/REPOSITORY_URL : /'

  # Remote origin setting if exists.
  [[ -n "$(git remote -v)" ]] && git remote rm origin

  git remote add origin "https://${USERNAME}@bitbucket.org/${USERNAME}/${REPO_NAME}.git"

  sleep 3 && git fetch --all

  git push -u origin --all
  git push -u origin --tags
}

# ======================================================

# load .bitbucket ini file.
source <(
  cat "$HOME/.bitbucket" \
    | sed -n -E 's/^\s*(\S+)\s*=\s*(.+)$/\1=\2/p'
)

declare -gr USERNAME="$username"
declare -gr PASSWORD="$password"
declare -gr REPO_NAME="${PWD##*/}"

SUBCOMMAND="${1}"
shift

case "${SUBCOMMAND}" in
  # I want to add more features later.
  create_from_local | *)
    create_from_local
    ;;
esac
