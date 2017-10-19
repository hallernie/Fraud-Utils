import urllib.parse
import urllib.request

url = 'https://h-api.online-metrix.net/api/update'

values = {'org_id' : '4ji5n0wo',
          'api_key' : 'i6gqqs4i2bukjyty',
          'action' : 'update_event_tag',
          'event_tag' : 'fraud_confirmed'}

f_input = open('all_fraud_may.csv')
# Skip the header row
next(f_input)

# Process the truth data file
try:
    for val in f_input:
        str_request_id = val.strip().split(',')[0].strip('"')
        values['request_id'] = str_request_id

        data = urllib.parse.urlencode(values)
        data = data.encode('ascii') # data should be bytes
        req = urllib.request.Request(url, data)

        with urllib.request.urlopen(req) as response:
            the_page = response.read()

        print(the_page)
except IOError as e: 
    if hasattr(e, 'code'): # HTTPError 
        print('http error code: ', e.code)
    elif hasattr(e, 'reason'): # URLError 
        print("can't connect, reason: ", e.reason)
    else: 
        raise
