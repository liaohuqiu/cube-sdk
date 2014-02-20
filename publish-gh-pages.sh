#!/bin/bash
branch=$1
if [ -z "$branch" ] || [ -a "$branch" == " " ]; then
    branch='gh-pages'
fi

checkout "$branch"
cp -r _site/* . && rm -rf _site/ && touch .nojekyll
