import sys
import csv
import re

if len(sys.argv) != 4:
    print('usage:')
    print(('    $ python program.py "event_file" "column_list"'
            ' "rule_weights"'))

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
