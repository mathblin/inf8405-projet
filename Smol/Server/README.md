# Server for Robot project

## What is it for
The server's purpose is to receive commands from the mobile application via sockets and to transmit them to the Arduino via serial communication. The Arduino will then execute the commands like moving forward, backward, in $360^\circ$ 

## Main entryfile
The main file to run is `server.py`. If you have want to know the possible flags, you can run `python server.py -h` on Windows or `python3 server.py -h` on linux. You can also use `--help` which does the same.

## Flags
The main purpose of the flags is to either change the server IP to match the computer (laptop, tower, Raspberry PI), debug with prints, test serial communication, etc. If you still have doubt on how the flags work, follow the `Main entryfile` section.

