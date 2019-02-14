import urllib.parse
import urllib.request

url = 'https://h-api.online-metrix.net/api/session-query'

values = {'org_id' : 'i6txanc0',
          'api_key' : '3jq8903xunkcmuaj',
          'service_type' : 'session-policy',
          'policy' : 'ErnieTest',
          'local_attrib_1' : 'ca1_value',
          'local_attrib_2' : 'ca2_value',
          'local_attrib_3' : 'ca3_value',
          'local_attrib_4' : 'ca4_value',
          'local_attrib_5' : 'ca5_value',
          'event_type' : 'payment'}

#f_input = open('all_fraud_may.csv')
# Skip the header row
#next(f_input)

# Process the truth data file
f_input = ['Test-0007','Test-0008','Test-0009','Test-0010','Test-0011']
try:
    for val in f_input:
        #str_request_id = val.strip().split(',')[0].strip('"')
        #values['request_id'] = str_request_id
        values['session_id'] = val

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
