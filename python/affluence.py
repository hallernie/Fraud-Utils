import sys
import csv
import re

# Extracts the afflence data from CA8. Each affluence attribute is given
#
# its own column.
#
# Custom Attribute 8 and Request ID columns must exist in the input file
#
f_events = open(sys.argv[1])
str_header = next(f_events)
r_header = csv.reader([str_header])

# Print the header
print(("Request ID,FUNDING_SOURCE_TYPE,PREPAID_CARD_TYPE," + 
       "RELOADABLE,ISSUER_COUNTRY,CARD_PRODUCT_TYPE," +
       "AFFLUENCE,VIRTUAL_ACCOUNT_NUMBER"))

# Find Custom Attribute 8, and Request ID columns
ca8_col = 0
request_id_col = 0
cnt = 0
found_ca8 = False
found_request_id = False
for val in next(r_header):
    if val == 'Custom Attribute 8':
        ca8_col = cnt
        found_ca8 = True
    if val == 'Request ID':
        request_id_col = cnt
        found_request_id = True
    cnt+=1

if not found_ca8:
    print("Custom Attribute 8 column not found!")
if not found_request_id:
    print("Request ID column not found!")
if not found_ca8 or not found_request_id:
    exit()

# Process the remainder of the file
for value in f_events:
    str_request_id = next(csv.reader([value]))[request_id_col]
    str_ca8 = next(csv.reader([value]))[ca8_col]
    print("{0},{1}".format(str_request_id,str_ca8))
