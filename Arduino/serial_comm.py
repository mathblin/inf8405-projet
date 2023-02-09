import serial.tools.list_ports

ports = serial.tools.list_ports.comports()
serialInst = serial.Serial()

portsList = []

for onePort in ports:
    portsList.append(str(onePort))
    print(str(onePort))

val = input("Select Port: COM")

portVar = "No COM found"

for x in range(0, len(portsList)):
    if portsList[x].startswith("COM" + str(val)):
        portVar = "COM" + str(val)
        print(portVar)

print(portVar)
serialInst.port = portVar
serialInst.open()
# serialInst = serial.Serial(portVar)

while True:
    command = input("Arduino command: ")
    serialInst.write(command.encode("UTF-8"))

    if command == "q":
        exit()
