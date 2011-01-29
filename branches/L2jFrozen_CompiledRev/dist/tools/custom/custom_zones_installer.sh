#!/bin/bash
############################################
## WARNING!  WARNING!  WARNING!  WARNING! ##
##                                        ##
## DON'T USE NOTEPAD TO CHANGE THIS FILE  ##
## INSTEAD USE SOME DECENT TEXT EDITOR.   ##
## NEWLINE CHARACTERS DIFFER BETWEEN DOS/ ##
## WINDOWS AND UNIX.                      ##
##                                        ##
## USING NOTEPAD TO SAVE THIS FILE WILL   ##
## LEAVE IT IN A BROKEN STATE!!!          ##
############################################
## Writen by Shyla                     ##
## License: GNU GPL                       ##
## Based on Vincenzo Esposito's script    ##
## E-mail: navis83@hotmail.it      ##
## From "L2jFrozenInterlude"                    ##
## Bug reports:  navis83@hotmail.it     ##
############################################
trap finish 2

configure() {
echo "#############################################"
echo "# You entered script configuration area     #"
echo "# No change will be performed in your DB    #"
echo "# I will just ask you some questions about  #"
echo "# your hosts and DB.                        #"
echo "#############################################"
MYSQLDUMPPATH=`which mysqldump 2>/dev/null`
MYSQLPATH=`which mysql 2>/dev/null`
if [ $? -ne 0 ]; then
echo "We were unable to find MySQL binaries on your path"
while :
 do
  echo -ne "\nPlease enter MySQL binaries directory (no trailing slash): "
  read MYSQLBINPATH
    if [ -e "$MYSQLBINPATH" ] && [ -d "$MYSQLBINPATH" ] && [ -e "$MYSQLBINPATH/mysqldump" ] && [ -e "$MYSQLBINPATH/mysql" ]; then
       MYSQLDUMPPATH="$MYSQLBINPATH/mysqldump"
       MYSQLPATH="$MYSQLBINPATH/mysql"
       break
    else
       echo "The data you entered is invalid. Please verify and try again."
       exit 1
    fi
 done
fi

#GS
echo -ne "\nPlease enter MySQL Game Server hostname (default localhost): "
read GSDBHOST
if [ -z "$GSDBHOST" ]; then
  GSDBHOST="localhost"
fi
echo -ne "\nPlease enter MySQL Game Server database name (default gameserver_beta): "
read GSDB
if [ -z "$GSDB" ]; then
  GSDB="gameserver_beta"
fi
echo -ne "\nPlease enter MySQL Game Server user (default root): "
read GSUSER
if [ -z "$GSUSER" ]; then
  GSUSER="root"
fi
echo -ne "\nPlease enter MySQL Game Server $GSUSER's password (won't be displayed): "
stty -echo
read GSPASS
stty echo
echo ""
if [ -z "$GSPASS" ]; then
  echo "Hum.. I'll let it be but don't be stupid and avoid empty passwords"
elif [ "$GSUSER" == "$GSPASS" ]; then
  echo "You're not too brilliant choosing passwords huh?"
fi
}

gsbackup(){
while :
  do
   echo ""
   echo -ne "Do you want to make a backup copy of your GSDB? (y/n): "
   read GSB
   if [ "$GSB" == "Y" -o "$GSB" == "y" ]; then
     echo "Making a backup of the original gameserver database."
     $MYSQLDUMPPATH --add-drop-table -h $GSDBHOST -u $GSUSER --password=$GSPASS $GSDB > gameserver_backup.sql
     if [ $? -ne 0 ];then
     echo ""
     echo "There was a problem accesing your GS database, either it wasnt created or authentication data is incorrect."
     exit 1
     fi
     break
   elif [ "$GSB" == "n" -o "$GSB" == "N" ]; then 
     break
   fi
  done 
}

asktype(){
echo ""
echo ""
echo "---------------------------------------------------------------------"
echo ""
echo "  L2-Frozen Team - Custom Zones Installer" 
echo "_____________________________________________________________________"
echo ""
echo "  1 - Install Monastery Custom Zone (Powered Mobs (lvl85), Gold Tokens Spoil (10% just on all mobs), TOP76LS and HIGH76LS on few mobs spoil (15%), High EXP/Adena(4x) drop)"
echo "  2 - (Not yet) Install ACM Custom Zone (Powered Mobs (lvl80-85), Frozen Adena Drop (1/4*AdenaDrop just on Higher mobs), High EXP/Adena drop)"
echo "  3 - (Not yet) Install Imperial Tomb Custom Zone (Powered Mobs (lvl85-90) with Raids, Raid Tokens Drop, Frozen Adena Drop (2/4*AdenaDrop just on all mobs), High EXP/Adena drop)"
echo "  4 - (Not yet) Install Every Custom Zone (1,2,3)"
echo "  5 - Revert to retail
echo "  6 - Exit from installer
echo "---------------------------------------------------------------------

read INSTALLTYPE
case "$INSTALLTYPE" in
	"1") monastery;;
	"2") acm;;
	"3") imperial;;
	"4") every;;
	"5") revert;;
	"6") finish;;
	*) asktype;;
esac
}

monastery(){
echo "*** Installing Monastery Custom Zone ***"
$MYG < ../../sql/custom_zones/monastery_custom_zone.sql &> /dev/null
echo "*** Done ***"
asktype
}

acm(){
echo "echo *** Installing ACM Custom Zone ***"
$MYG < ../../sql/custom_zones/acm_custom_zone.sql &> /dev/null
echo "*** Done ***"
asktype
}

imperial(){
echo "*** Installing Imperial Custom Zone ***"
$MYG < ../../sql/custom_zones/imperial_custom_zone.sql &> /dev/null
echo "*** Done ***"
asktype
}

every(){
echo "*** Installing Monastery Custom Zone ***"
$MYG < ../../sql/custom_zones/monastery_custom_zone.sql &> /dev/null
echo "*** Done ***"
echo "echo *** Installing ACM Custom Zone ***"
$MYG < ../../sql/custom_zones/acm_custom_zone.sql &> /dev/null
echo "*** Done ***"
echo "*** Installing Imperial Custom Zone ***"
$MYG < ../../sql/custom_zones/imperial_custom_zone.sql &> /dev/null
echo "*** Done ***"
asktype
}

finish(){
echo ""
echo "Script execution finished."
exit 0
}

clear
configure
MYL="$MYSQLPATH -h $LSDBHOST -u $LSUSER --password=$LSPASS -D $LSDB"
MYG="$MYSQLPATH -h $GSDBHOST -u $GSUSER --password=$GSPASS -D $GSDB"
gsbackup
asktype