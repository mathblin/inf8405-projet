# Server for Robot project

## What is it for?
The server's purpose is to receive commands from the mobile application via sockets and to transmit them to the Arduino via serial communication. The Arduino will then execute the commands like moving forward, backward, in $360^\circ$, etc.

## Main entryfile
The main file to run is `server.py`. If you have want to know the possible flags, you can run `python server.py -h` on Windows or `python3 server.py -h` on linux. You can also use `--help` which does the same.

## Flags
The mains purpose of the flags are to either change the server IP to match the computer (laptop, tower, Raspberry PI), debug with prints, test serial communication, etc. If you still have doubt on how the flags work, follow the `Main entryfile` section.

## Many modes
It is possible to test the serial communication by adding `-serial` flag when calling the server. This way, you can test new commands to the Arduino. Of course, if you do that, you'll have to adjust the Arduino code accodingly. Without the `-serial` flag, the mode is with socket. In this mode, you'll need the mobile app to be on a phone or tablet to receive commands in the server.

## IP address
The IP address must be set in the application. It must match the one in the server. For the server part, you'll have to either change the default IP address in the code or use the `-ip` flag followed by the address. For example, like this `python server.py -ip 127.0.0.1`. Search the following variable in the code:
```python 
DEFAULT_HOST
```
It should be, if not moved since, in the ``server_flags.py`` file. The default one we used is the one that `Smol` had at Polytechnique of Montreal university. It is likely it will have changed when you read this. You'll have to communicate with the tech department if you are a student.
