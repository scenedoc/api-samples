import argparse, csv, base64, json
from urllib import request, error
import sys, time

# update_progress() : Displays or updates a console progress bar
## Accepts a float between 0 and 1. Any int will be converted to a float.
## A value under 0 represents a 'halt'.
## A value at 1 or bigger represents 100%
def update_progress(progress):
    barLength = 40 # Modify this to change the length of the progress bar
    status = ""
    if isinstance(progress, int):
        progress = float(progress)
    if not isinstance(progress, float):
        progress = 0
        status = "error: progress var must be float\r\n"
    if progress < 0:
        progress = 0
        status = "Halt...\r\n"
    if progress >= 1:
        progress = 1
        status = "Done...\r\n"
    block = int(round(barLength*progress))
    text = "\rPercent: [{0}] {1}% {2}".format( "#"*block + "-"*(barLength-block), progress*100, status)
    sys.stdout.write(text)
    sys.stdout.flush()

parser = argparse.ArgumentParser(description='Upload provided codetype file to specified environment')
# parser.add_argument('integers', metavar='N', type=int, nargs='+',
#                     help='an integer for the accumulator')
# parser.add_argument('--sum', dest='accumulate', action='store_const',
#                     const=sum, default=max,
#                     help='sum the integers (default: find the max)')
parser.add_argument('--username',"-u", required=True,
                    help='Username used to authenticate')
parser.add_argument('--password',"-p", required=True,
                    help='Password used to authenticate')
parser.add_argument('--baseUrl','-url', required=True,
                    help='Base URL for the environment')
parser.add_argument('--codeTypeId',"-c", required=True,
                    help='Id of the CodeType being uploaded to')
parser.add_argument('--filepath', "-f", required=True,type=argparse.FileType('r',encoding='utf-8-sig'),
                    help='Path to the file to be uploaded')
args = parser.parse_args()

url = args.baseUrl  + "/rest/codetypes/upload-item/" + args.codeTypeId

with args.filepath as filepath:
    testreader = csv.DictReader(filepath)
    data = list(testreader)

    
length = len(data)
for idx, row in enumerate(data):
        params = json.dumps(row).encode('utf8')
        message = '%s:%s' % (args.username, args.password)
        message_bytes = message.encode('ascii')
        base64string = base64.b64encode(message_bytes)
        try:
            req = request.Request(url, data=params,
                                    headers={'content-type': 'application/json', "Authorization": "Basic "+base64string.decode("ascii")})
            response = request.urlopen(req)
        except error.HTTPError as e:
            # Return code error (e.g. 404, 501, ...)
            # ...
            print('HTTPError: {}'.format(e.code))
        except error.URLError as e:
            # Not an HTTP-specific error (e.g. connection refused)
            # ...
            print('URLError: {}'.format(e.reason))
        update_progress(idx/length)
        time.sleep(0.5)



            

args.filepath.close()
