#!/bin/sh
THIS=`pwd`/`dirname $0`
SPATH=`which d2r-server`
SDIR=`dirname $SPATH`
make
# asserted
(cd $SDIR && d2r-server $* $THIS/obd-d2rq.n3) &
# entailed
(cd $SDIR && d2r-server -p 2021 $* $THIS/obd-d2rq-entailed.n3) &
echo "now point yr browser at: http://127.0.0.1:2020/snorql/"
wait
