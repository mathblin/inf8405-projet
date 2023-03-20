import argparse     # For flag arguments

DEFAULT_HOST = '10.0.0.158'
DEFAULT_PORT = 5050

# ArgumentParser object creation
parser = argparse.ArgumentParser(description='Smol arguments for server.')

# Arguments corresponding to flags
parser.add_argument('-ip', nargs=1, dest='ip', type=str, default=[DEFAULT_HOST], help="The ip adress of the current server, default={0}".format(DEFAULT_HOST))
parser.add_argument('-p', nargs=1, dest='port', type=str, default=[DEFAULT_PORT], help="The port number of the current server, default={0}".format(DEFAULT_PORT))
parser.add_argument('-debug', required=False, dest='debug', action='store_true', help="Debugger mode. debug prints like flags and tests.")

# Parsing the arguments
args = parser.parse_args()

# debug function to show args
def show_flags_from_argparser():
    if args.debug:
        print('# Debugger mode #')
    if args.debug:
        print("\nHere's your flags:")
    if args.ip:
        print('Server IP address:', args.ip[0])
    if args.port:
        print('port: ', args.port[0])
    print('')
