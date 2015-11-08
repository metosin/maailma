#!/bin/bash

rev=$(git rev-parse HEAD)
remoteurl=$(git ls-remote --get-url origin)

git fetch
if [[ -z $(git branch -r --list origin/gh-pages) ]]; then
    (
    mkdir doc
    cd doc
    git init
    git remote add origin ${remoteurl}
    git checkout -b gh-pages
    git commit --allow-empty -m "Init"
    git push -u origin gh-pages
    )
elif [[ ! -d doc ]]; then
    git clone --branch gh-pages ${remoteurl} doc
else
    (
    cd doc
    git pull
    )
fi

lein codox
rm -r doc/*
cp -r target/doc/* doc
cd doc
git add --all
git commit -m "Build docs from ${rev}."
git push origin gh-pages
