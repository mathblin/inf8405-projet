#!/bin/sh -e
#
# rc.local
#
# This script is executed at the end of each multiuser runlevel.
# Make sure that the script will "exit 0" on success or any other
# value on error.
#
# In order to enable or disable this script just change the execution
# bits.
#
# By default this script does nothing.

# Print the IP address
_IP=$(hostname -I) || true
if [ "$_IP" ]; then
  printf "My IP address is %s\n" "$_IP"
fi

until $(curl --output /dev/null --silent --head --fail http://google.com ); do
    sleep 4
done

#start camera.py and server.py
python /home/pi/server.py &
python3 /home/pi/camera.py &

exit 0