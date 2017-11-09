import csv

#
# Removes duplicate entries from an event file, assuming that session id is the key.
#
f_file = open("rodan_EventsCleaned.csv")
# print header row
print(next(f_file).strip())

d_tmp = {}
for value in f_file:
    r_reader = csv.reader([value])
    l_value = next(r_reader)
    d_tmp[l_value[4]] = value # This was the Session ID column for the given input file.

for value in d_tmp.values():
    print(value.strip())
