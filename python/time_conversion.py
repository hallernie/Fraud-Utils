import calendar
import datetime
import time

# 2017/07/08 01:56:36
# Create datetime from Event Time
datetime_a = datetime.datetime(2017,7,8,1,56,36)
print(datetime_a.isoformat())  # 2017-07-08T01:56:36

# Convert to integer and subtract 4 hours (14400 seconds)
time_a = calendar.timegm(datetime_a.utctimetuple())
time_a -= 14400

# Create a datetime object so that I can print the isoformat
datetime_b = datetime.datetime.utcfromtimestamp(time_a)
print(str(datetime_b.isoformat()).split('T')[0])  # 2017-07-07
