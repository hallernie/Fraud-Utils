import sys
import csv
import re
import os


#
# Reads Sessions Authorization report (by payment type) and extracts the Online "Invalid Transaction" percentages.
#
re_infile = re.compile(r"^file.*$")
re_visa = re.compile(r"^VISA")
re_mastercard = re.compile(r"^MasterCard")
re_amex = re.compile(r"^American Express")
re_discover = re.compile(r"^Discover")
re_invalid = re.compile(r"^Invalid Transaction")

def do_it(str_file_name):
    d_starting = {'Visa':'0.00','MasterCard':'0.00','Amex':'0.00','Discover':'0.00'}
    l_order = ['Visa','MasterCard','Amex','Discover']
    str_current_card_brand = ''

    # Get the month
    f_file = open(str_file_name)
    str_out = next(f_file).strip()
    str_out = str_out.split('From:')[1].split(' ')[1]
    str_out += ','
    for val in open(str_file_name):
        val=val.strip()
        if re_visa.match(val):
            str_current_card_brand = 'Visa'
        if re_mastercard.match(val):
            str_current_card_brand = 'MasterCard'
        if re_amex.match(val):
            str_current_card_brand = 'Amex'
        if re_discover.match(val):
            str_current_card_brand = 'Discover'
        if re_invalid.match(val):
            str_tmp = next(csv.reader([val]))[4]
            str_tmp = str_tmp[:len(str_tmp)-1]
            #d_starting[str_current_card_brand] = str_tmp
            d_starting[str_current_card_brand] = float(str_tmp) / 100

    #str_out = ''
    for val in l_order:
        str_out += (str(d_starting[val]) + ',')
    str_out = str_out[:len(str_out)-1]
    print(str_out)

str_header = 'Date,Visa,MasterCard,Amex,Discover'
print(str_header)

l_tmp = []
for val in os.listdir():
    if re_infile.match(val):
        l_tmp.append(val)
    
for val in sorted(l_tmp):
    do_it(val)
