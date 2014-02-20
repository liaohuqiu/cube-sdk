#!/bin/bash

function exe_cmd() {
    echo $1
    eval $1
}

branch=$1
if [ -z "$branch" ] || [ -a "$branch" == " " ]; then
    branch='gh-pages'
fi

jekyll build
if [ ! -d '_site' ];then
    echo "not content to be published"
    exit
fi

exe_cmd "git checkout $branch"
error_code=$?
if [ $error_code != 0 ];then
    echo 'Switch branch fail.'
    exit
else
    cmd="ls | grep -v _site|xargs rm -rf && cp -r _site/* . && rm -rf _site/ && touch .nojekyll"
    exe_cmd $cmd
fi
