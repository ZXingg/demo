#!/bin/bash

PID=`ps -ef | grep '/opt/wpwl/installed/filebeat-7.3.0-linux-x86_64/filebeat' | grep -v 'grep' | awk {'print $2'}`

#if [[ X${PID} != X ]]; then
#        echo "exec command kill -9  ${PID}"
#        kill -9 ${PID}
#        if [[ $? -ne 0 ]]; then
#                echo "kill -9  ${PID} failure, restart interrupt!"
#                exit
#        fi
#fi

for pid in ${PID} ; do
        echo "exec command kill -9  ${pid}"
        kill -9 ${pid}
        if [[ $? -ne 0 ]]; then
                echo "kill -9  ${pid} failure, restart interrupt!"
                exit
        fi
done


echo "starting filebeat..."
echo "output to /opt/wpwl/installed/filebeat-7.3.0-linux-x86_64/filebeat.log"

nohup /opt/wpwl/installed/filebeat-7.3.0-linux-x86_64/filebeat run --path.config=/opt/wpwl/installed/filebeat-7.3.0-linux-x86_64 >> /opt/wpwl/installed/filebeat-7.3.0-linux-x86_64/filebeat.log  2>&1 &


if [[ $? -eq 0 ]]; then
        echo $! > /opt/wpwl/installed/filebeat-7.3.0-linux-x86_64/filebeat.pid
        echo -e "filebeat started. \n pid-file: /opt/wpwl/installed/filebeat-7.3.0-linux-x86_64/filebeat.pid \n pid: $!"
else
        echo "restart failed!"
fi