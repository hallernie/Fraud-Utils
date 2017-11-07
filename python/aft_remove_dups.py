import sys
import csv
import re

###
# Removes duplicate rows, keyed on AUTH_PAYMENT_ID, from an aft file.
# Priority is given to CBK_OPEN_DATE, and then to VI_ALERT_CREATE_DATE
# and MC_ALERT_CREATE_DATE
#
# Input:
#       aft filename
#
# Output:
#       aft data with duplicate rows removed. Output to standard out.
###

f_aft = open(sys.argv[1])
str_header = next(f_aft)
print(str_header.strip())
r_header = csv.reader([str_header])

# Find auth, cbk, vi_alert, and mc_alert columns
auth_col = 0
cbk_col = 0
vi_alert_col = 0
mc_alert_col = 0
cnt = 0
for val in next(r_header):
    if val == 'AUTH_PAYMENT_ID':
        auth_col = cnt
    if val == 'CBK_OPEN_DATE':
        cbk_col = cnt
    if val == 'VI_ALERT_CREATE_DATE':
        vi_alert_col = cnt
    if val == 'MC_ALERT_CREATE_DATE':
        mc_alert_col = cnt
    cnt+=1

# Process the remainder of the file
d_auth_ids = {}

for value in f_aft:
    str_status = ''

    l_value = next(csv.reader([value]))
    str_auth_id = l_value[auth_col]
    str_cbk = l_value[cbk_col]
    str_vi_alert = l_value[vi_alert_col]
    str_mc_alert = l_value[mc_alert_col]

    if str_cbk != '--':
        str_status = 'cbk'
    elif (str_vi_alert != '--') or (str_mc_alert != '--'):
        str_status = 'alert'
    else:
        str_status = 'none'


    if not (str_auth_id in d_auth_ids): # not found so just store values
        d_auth_ids[str_auth_id] = [str_status,value]
    elif str_status == 'cbk': # current line is cbk, so replace previous value
        d_auth_ids[str_auth_id] = [str_status,value]
    elif d_auth_ids[str_auth_id][0] == 'cbk': # previus was cbk, so skip
        pass
    elif ((d_auth_ids[str_auth_id][0] == 'alert') and 
            (str_status == 'alert')): # both previous and current are alerts, so keep the current
        d_auth_ids[str_auth_id] = [str_status,value]
    elif ((d_auth_ids[str_auth_id][0] == 'alert') and 
            (str_status == 'none')): # previous is alert and current none, so pass
        pass
    else: # previous must have been none, and current is none, so keep the current
        d_auth_ids[str_auth_id] = [str_status,value]

for value in d_auth_ids.values():
    print(value[1].strip())
