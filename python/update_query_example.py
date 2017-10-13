import urllib.parse
import urllib.request

url = 'https://h-api.online-metrix.net/api/update'

values = {'org_id' : 'real org id here',
          'api_key' : 'real api key here',
          'action' : 'update_event_tag',
          'event_tag' : 'fraud_confirmed',
          'request_id' : '8b7be1487253494aa57922de726e3c77'}

data = urllib.parse.urlencode(values)
data = data.encode('ascii') # data should be bytes
req = urllib.request.Request(url, data)

with urllib.request.urlopen(req) as response:
   the_page = response.read()

print(the_page)
   
#try: 
    #response = urllib.request.urlopen('https://login.salesforce.com/')  
    #print(response.geturl())
    #print('response headers: "%s"' % response.info())
#except IOError as e: 
    #if hasattr(e, 'code'): # HTTPError 
        #print('http error code: ', e.code)
    #elif hasattr(e, 'reason'): # URLError 
        #print("can't connect, reason: ", e.reason)
    #else: 
        #raise
