import sys
import csv
import re

# Extracts the sku from CA2.
#
f_events = open(sys.argv[1])
str_header = next(f_events)
r_header = csv.reader([str_header])

# Print the header
print('Request ID,SKU')

# Find Custom Attribute 2, and Request ID columns
ca2_col = 0
request_id_col = 0
cnt = 0
found_ca2 = False
found_request_id = False
for val in next(r_header):
    if val == 'Custom Attribute 2':
        ca2_col = cnt
        found_ca2 = True
    if val == 'Request ID':
        request_id_col = cnt
        found_request_id = True
    cnt+=1

if not found_ca2:
    print("Custom Attribute 2 column not found!")
if not found_request_id:
    print("Request ID column not found!")
if not found_ca2 or not found_request_id:
    exit()

# Process the remainder of the file
for value in f_events:
    str_request_id = next(csv.reader([value]))[request_id_col]
    str_ca2 = next(csv.reader([value]))[ca2_col]
    l_skus = str_ca2.split(':')[1:]
    for val in l_skus:
        print("{0},{1}".format(str_request_id,val))

