
PROG=/home/esco/load_liste_boursiers.pl
REP=/home/esco/logs

FILE=load_liste_boursiers

LOG=$REP/${FILE}.log

echo " " > $LOG
echo " ------ $PROG ------ " >>  $LOG
$PROG >> $LOG 2>&1

cat $LOG

/bin/cat $LOG >> $REP/${FILE}_$(date +%m).log

find $REP -name $FILE'_*'.log -mtime +60 -delete

