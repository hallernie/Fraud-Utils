import csv
import sys

#
# Removes duplicate entries from a CLEANED event file, assuming that "Session ID" is the key.
#
if len(sys.argv) != 2:
    print('Usage:')
    print('  % python remove_duplicates_from_event_file.py event_file_name')
    exit()

# Open the input file
f_file = open(sys.argv[1])

# Find the Session ID column
str_header = next(f_file).strip()
r_header = csv.reader([str_header])
n_session_id_col = 0
for value in next(r_header):
    if value == 'Session ID':
        break
    n_session_id_col += 1

# print header row
print(str_header)

d_tmp = {}
for value in f_file:
    r_reader = csv.reader([value])
    l_value = next(r_reader)
    d_tmp[l_value[n_session_id_col]] = value

for value in d_tmp.values():
    print(value.strip())
