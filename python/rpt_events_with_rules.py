import sys
import csv
import re

#
# Takes comma delimited "reasons":
#
# "{""DeviceLocalAgeLessThanOneHour"",""DeviceGlobalAgeLessThanOneHour"",""FuzzyDeviceLocalAgeLessThanOneHour"",""FlashCookiesDisabled""}"
#
# and returns:
#
#       str_filler + rule1 + ',' + rule1_weight
#       str_filler + rule2 + ',' + rule2_weight
#       str_filler + rule3 + ',' + rule3_weight
#
def print_rule_and_weight(str_reasons,
                          h_rules,
                          str_filler,
                          l_columns,
                          h_header,
                          l_event):
    str_event = ''
    for col in l_columns:
        str_event += ('"' + l_event[h_header[col]] + '",')
    l_reasons = str_reasons.strip('{').strip('}').split(',')
    cnt = 0
    for val in l_reasons:
        val = val.strip('"')
        if cnt == 0:
            if val == '':
                val = 'no_rules'
            #print(l_event[h_header['Request ID']])
            #print(('>' + val + '<'))
            print("{0}{1},{2}".format(str_event,val,h_rules[val]))
            cnt += 1
        else:
            print("{0},{1},{2}".format(str_filler,val,h_rules[val]))


if len(sys.argv) != 4:
    print('usage:')
    print(('    $ python program.py "event_file" "column_list"'
            ' "rule_weights"'))
    exit()

try:
    f_events = open(sys.argv[1])
except FileNotFoundError:
    print("Event file '{0}' not found.".format(sys.argv[1]))

try:
    f_columns = open(sys.argv[2])
except FileNotFoundError:
    print("Columns file '{0}' not found.".format(sys.argv[2]))

try:
    f_weights = open(sys.argv[3])
except FileNotFoundError:
    print("Weights file '{0}' not found.".format(sys.argv[3]))

# Read all columns from events file, and store in a lookup table
# keyed by zero base column position
for i,val in enumerate(csv.reader([next(f_events)])):
    l_header = val

h_header = {}
for i, val in enumerate(l_header):
    h_header[val] = i

# Read report columns from file and store in a list.
# Error if column is not found in the event file
b_error = False
l_columns = []
for val in f_columns:
    l_columns.append(val.strip())
    if val.strip() not in h_header:
        print("Column '{0}' not found.".format(val.strip()))
        b_error = True
if b_error:
    print('Exiting: Missing column')
    exit()

# Create rule weight lookup
h_rules = {}
next(f_weights)
for val in f_weights:
    h_rules[val.strip().split(',')[0]] = int(val.strip().split(',')[1])

# Create the filler string
str_filler = ''
for val in range(len(l_columns)-1):
    str_filler += ","

# Print the report file header
str_header = ''
for val in l_columns:
    str_header += (val + ",")
str_header += 'Rule Name,Weight'

print(str_header)
#for i,val in enumerate(csv.reader([next(f_events)])):
for value in f_events:
    for i,val in enumerate(csv.reader([value])):
        print_rule_and_weight(val[h_header['Reasons']],
                            h_rules,
                            str_filler,
                            l_columns,
                            h_header,
                            val)
