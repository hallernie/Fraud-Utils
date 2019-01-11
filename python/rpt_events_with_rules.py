import sys
import csv
import re

#
#   Takes a TMX formatted transaction amount, and formats it into normal currency format.
#
#   Example event file values before:
#   10783.0
#   10789.0
#   10797.0
#
#   Example event file values after:
#   107.83
#   107.89
#   107.97
#
def convert_it(val):
    if len(val) == 3:
        tmp_val = val.split('.')[0]
        tmp_val = '0.0' + tmp_val
    elif len(val) == 4:
        tmp_val = val.split('.')[0]
        tmp_val = '0.' + tmp_val
    else:
        tmp_val = val.split('.')[0]
        tmp_val = (tmp_val[0:len(tmp_val)-2] + '.' +
                tmp_val[len(tmp_val)-2:])
        
    return tmp_val
## END: convert_it ##

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
        if col == 'Custom Attribute 9':
            str_event += ('"' + l_event[h_header[col]].split(':')[1] + '",')
        elif col == 'Transaction Amount':
            str_event += ('"' + convert_it(l_event[h_header[col]]) + '",')
        else:
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
## END: print_rule_and_weight ##

#
#
#
def format_report():
    if len(sys.argv) != 4:
        print('usage:')
        print(('    $ python rpt_events_with_rules.py "unclean_event_file.csv" "column_list_file.txt"'
                ' "rule_weights_lookup_file.csv"'))
        print()
        print("Note: The unclean event file must contain the 'Reasons' column.")
        print("      But do not include 'Reasons' column in the column_list file.")
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
## END: format_report ##

#
#   Takes the output from format_report() and splits it into chunked files.
#
def split_file(str_input_file, n_number_of_files):
    re_utc = re.compile(r"UTC")

    # Make first pass through file and count the number
    # of transactions
    f_infile = open(str_input_file)
    n_cnt = 0
    for val in f_infile:
        if re_utc.search(val.strip()):
            n_cnt += 1
    n_records_per_file = int(n_cnt/n_number_of_files)

    # Now create output
    f_infile = open(str_input_file)
    str_header = next(f_infile)

    n_outfile = 0
    f_out = open("Proactiv" + str(n_outfile) + ".csv", "w")

    n_current_record_cnt = 0
    for val in f_infile:
        if n_current_record_cnt == 0:
            f_out.write(str_header.strip() + "\n")
            #print(str_header.strip())
        if re_utc.search(val.strip()):
            n_current_record_cnt += 1
        if n_current_record_cnt < n_records_per_file:
            f_out.write(val.strip() + "\n")
            #print(val.strip())
        if n_current_record_cnt == n_records_per_file:
            f_out.close()
            n_outfile += 1
            f_out = open("Proactiv" + str(n_outfile) + ".csv", "w")
            f_out.write(str_header.strip() + "\n")
            #print(str_header.strip())
            f_out.write(val.strip() + "\n")
            #print(val.strip())
            n_current_record_cnt = 1
    f_out.close()
## END: split_file ##

## MAIN ##
split_file('final_report_full.csv', 40)
#format_report()
