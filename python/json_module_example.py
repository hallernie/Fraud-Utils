import json


val = json.loads('["foo", {"bar":["baz", null, 1.0, 2]}]')
print(type(val))
print(json.dumps(val))
val = json.loads('{"foo": 3}')
print(type(val))
print(json.dumps(val))

