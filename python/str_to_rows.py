import sys
import csv

#
# Input:
#   csv file with a header row
# Output:
#   Numbered column names from the header
#
if len(sys.argv) < 2:
    print('Usage:')
    print('   % str_to_rows csv_filename')
    exit()

for value in open(sys.argv[1]):
    if(value.strip() != ''): # Must be the first non-blank line
        val = csv.reader([value.strip()])
        n_cnt = 1
        for vals in next(val):
            print("     {0} {1}".format(n_cnt,vals))
            n_cnt += 1
        exit()
