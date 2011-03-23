#!/bin/env bash

while [[ $# -gt 0 ]]
do
 qid=`grep -oP "(?<=query-id=['\"]).+?(?=['\"])" $1`
 filename=$qid.sgm
 echo $filename
 ./capitalize_named_entities2.pl $1 > in/$filename
 echo $filename >> list
 shift
done
