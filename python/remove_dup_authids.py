import csv

d_authid = {}
b_start = True
for val in open('aft_detail_with_truth_180200_20170401_20170430.csv'):
    if(b_start):
        print(val.strip())
        b_start = False
        continue
    # [4] in this case is column with the auth payment_id
    d_authid[next(csv.reader([val]))[4]] = val

for value in d_authid.values():
    print(value.strip())
