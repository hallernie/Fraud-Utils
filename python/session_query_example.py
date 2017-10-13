import urllib.parse
import urllib.request

url = 'https://h-api.online-metrix.net/api/session-query'

values = {'org_id' : 'put valid org id here',
          'api_key' : 'put valid api_key here',
          'service_type' : 'session-policy',
          'session_id' : 'rodan-5bdfb003-f1cf-4b7f-b72a-d77e92a2254b'}

data = urllib.parse.urlencode(values)
data = data.encode('ascii') # data should be bytes
req = urllib.request.Request(url, data)

with urllib.request.urlopen(req) as response:
   the_page = response.read()

print(the_page)
   
