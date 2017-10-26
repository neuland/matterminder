#!/usr/bin/env bash

rm -f /usr/local/matterminder/target/universal/stage/RUNNING_PID

# will not work if the last component of the path used to find the script is a symlink 
DIR_OF_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
$DIR_OF_SCRIPT/target/universal/stage/bin/matterminder