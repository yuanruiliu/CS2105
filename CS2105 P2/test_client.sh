#!/bin/bash

#
# CS2105-P2: Test Script for testing the FTP client
# Any issues/bugs in the test script should be mail to girisha@comp.nus.edu.sg
#

#
# globals 
#
TRUE=1
FALSE=0
total_makrs=0
SERVER_PORT=-1
SERVER="127.0.0.1"
SERVER_LOG=$FALSE

if [ $# -ne 1 ]
then
	echo -e "\t usage: ./test_client.sh <server_control_port>"
	exit 1
else
	SERVER_PORT=$1
fi 

#
# check if server port is a number
#
re='^[0-9]+$'
if ! [[ $SERVER_PORT =~ $re ]]
then
   	echo "error: the port specified is not a number "  
	exit 1
fi

echo ''
echo '#------------------------------------------------------------------------------------'
echo '# stage 1: copy the server and compile the client code'
echo '#------------------------------------------------------------------------------------'

echo -e "\t + changing directory, to server/"

cd server 

if [ $? -ne 0 ] #check for directory 
then 
	echo -e "\t + there is something wrong with the directory structure. cannot find server/"
	exit 1 # issue with the directory structre 
fi

echo -e "\t + copying the server.jar, to server/"

cp ../server.jar .

echo -e "\n\t + changing directory, to client/"

cd ../client 

if [ $? -ne 0 ] #check for directory 
then 
	echo -e "\t + there is something wrong with the directory structure. cannot find client/"
	exit 1 # issue with the directory structre 
fi

echo -e "\t + compling the FTPClient.java"
rm -f *.class
javac FTPClient.java

if [ $? -ne 0 ] #check for compile!!
then 
	echo -e "\t + The FTPClient.java didn't COMPILE"
	exit # client didnt compile 
else
	echo -e "\t + The FTPClient.java COMPLIED without ERROR, ADD 10 marks"
	total_marks=$(($total_marks + 10))
fi

echo ''
echo '#-----------------------------------------------------------------------------------'
echo '# stage 2: run the server'
echo '#------------------------------------------------------------------------------------'

cd ../server # no need to check for directory structure again, if code comes here the directory structure is complete

rm -rf server-directory
cp ../server-directory.zip .
unzip server-directory.zip > /dev/null 2>&1
rm -f server-directory.zip

#
# kill any server if running
#

# assuming students have some server already running
PIDS=$( ps -fu $USER | grep -i "java FTPServer" | grep -v grep | awk '{print $2}' )

for pid in $PIDS
do
	kill $pid
	echo -e "\t + killed previous server running with PID "$pid 
done

PIDS=$( ps -fu $USER | grep -i "java -jar server" | grep -v grep | awk '{print $2}' )

for pid in $PIDS
do
	kill $pid
	echo -e "\t + killed previous server jar running with PID "$pid 
done

if [ $SERVER_LOG -eq $TRUE ]
then
	echo -e "\t + server output logged " 
	java -jar server.jar $SERVER_PORT > log 2>&1 & 
else
	echo -e "\t + server output not logged " 
	java -jar server.jar $SERVER_PORT > /dev/null 2>&1 & 
fi

echo ''
echo '#------------------------------------------------------------------------------------'
echo '# stage 3: client test cases'
echo '#------------------------------------------------------------------------------------'

cd ../client

rm -rf client-directory
cp ../client-directory.zip .
unzip client-directory.zip > /dev/null 2>&1 
rm -f client-directory.zip

#
# test case 1: get server directory dump
#

echo -e '\n + starting test case 1: list server contents'

java FTPClient $SERVER $SERVER_PORT DIR #> /dev/null 2>&1 

diff client-directory/directory_listing ../originals/file_dump_before #> /dev/null 2>&1

if [ $? -ne 0 ]
then
	echo -e "\t + test case 1 failed" 
else
	echo -e "\t + test case 1 pass, adding in 10 marks" 
	total_marks=$(($total_marks + 10))
fi

rm -f directory_listing log

#
# test case 2: get file from server  
#

echo -e '\n + starting test case 2: getting file from server'

java FTPClient $SERVER $SERVER_PORT GET directory/another-directory/castle.jpg #> /dev/null 2>&1

diff client-directory/castle.jpg ../originals/castle.jpg #> /dev/null 2>&1

if [ $? -ne 0 ]
then
	echo -e "\t + test case 2 failed" 
else
	echo -e "\t + test case 2 pass, adding in 10 marks" 
	total_marks=$(($total_marks + 10))
fi

rm -f log

#
# test case 3 a): put file to server 
#

echo -e '\n + starting test case 3 a): puting file to server'

java FTPClient $SERVER $SERVER_PORT PUT big.txt big #> /dev/null 2>&1

diff log ../originals/200_OK #> /dev/null 2>&1

if [ $? -ne 0 ]
then
	echo -e "\t + test case 3 a) failed" 
else
	echo -e "\t + test case 3 a) pass, adding in 5 marks" 
	total_marks=$(($total_marks + 5))
fi

rm -f log

#
# test case 3 b): get server directory dump 
#

echo -e '\n + starting test case 3 b): file dump after puting file to server'

java FTPClient $SERVER $SERVER_PORT DIR #> /dev/null 2>&1

diff client-directory/directory_listing ../originals/file_dump_after #> /dev/null 2>&1

if [ $? -ne 0 ]
then
	echo -e "\t + test case 3 b) failed" 
else
	echo -e "\t + test case 3 b) pass, adding in 5 marks" 
	total_marks=$(($total_marks + 5))
fi

rm -f client-directory/directory_listing log

#
# test case 4: try putting non-existent file 
#

echo -e '\n + starting test case 4 putting unknown file'

java FTPClient $SERVER $SERVER_PORT PUT abc #> /dev/null 2>&1

diff log ../originals/UN_FILE #> /dev/null 2>&1

if [ $? -ne 0 ]
then
	echo -e "\t + test case 4 failed" 
else
	echo -e "\t + test case 4 pass, adding in 5 marks" 
	total_marks=$(($total_marks + 5))
fi

rm -f log

echo ''
echo '#------------------------------------------------------------------------------------'
echo '# stage 4: kill any servers started by you'
echo '#------------------------------------------------------------------------------------'

# assuming students have some server already running
PIDS=$( ps -fu $USER | grep -i "java FTPServer" | grep -v grep | awk '{print $2}' ) 

for pid in $PIDS
do
	kill $pid
	echo -e "\t + killed server with PID "$pid 
done

PIDS=$( ps -fu $USER | grep -i "java -jar server" | grep -v grep | awk '{print $2}' )

for pid in $PIDS
do
	kill $pid
	echo -e "\t + killed server jar running with PID "$pid 
done

echo ''
echo '#------------------------------------------------------------------------------------'
echo '# stage 5: marks'
echo '#------------------------------------------------------------------------------------'
	
echo -e "\t + total marks "$total_marks" / 45"
