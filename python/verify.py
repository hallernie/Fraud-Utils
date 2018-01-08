import sys
import csv

#f_tmx_rules = open('rodan_TmxRules.csv')
#next(f_tmx_rules)
#for value in f_tmx_rules:
    #value = value.replace('"','')
    #str_rulename = next(csv.reader([value.strip()]))[2]
    #print(str_rulename)
    #exit()

# Verify customer rules
# Open adjusted rules file and store in hash
d_adjusted_rules= {}
for value in open("adjusted_rules.txt"):
    d_adjusted_rules[value.strip()] = 0

# Verify merchant rules
# Loop through merchant rules file and increment count each time
# "adjusted" rule is found
f_merchant_rules = open('rodan_Rules_Jun.csv')
next(f_merchant_rules)
for value in f_merchant_rules:
    str_rulename = next(csv.reader([value.strip()]))[1]
    if(str_rulename in d_adjusted_rules):
        d_adjusted_rules[str_rulename] += 1

# Verify TMX summary rules
f_tmx_summary_rules = open('rodan_TmxSummaryRules_Jun.csv')
next(f_tmx_summary_rules)
for value in f_tmx_summary_rules:
    str_rulename = next(csv.reader([value.strip()]))[1]
    if(str_rulename in d_adjusted_rules):
        d_adjusted_rules[str_rulename] += 1

# Verify TMX rules
f_tmx_rules = open('rodan_TmxRules.csv')
next(f_tmx_rules)
for value in f_tmx_rules:
    value = value.replace('"','')
    str_rulename = next(csv.reader([value.strip()]))[2]
    if(str_rulename in d_adjusted_rules):
        d_adjusted_rules[str_rulename] += 1

for key,value in d_adjusted_rules.items():
    print("{0}:  {1}".format(key,value))
