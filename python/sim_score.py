import csv

f_ruleweights = open('ruleweights.csv')
# Skip the header row
next(f_ruleweights)

# Store rulenames/weights in a hash
h_rules_weights = {} # Lookup table for rules/weights
for val in f_ruleweights:
    l_val = next(csv.reader([val.strip()]))
    str_rulename = l_val[0]
    str_ruleweight = l_val[1]
    h_rules_weights[str_rulename] = int(str_ruleweight)

# Read the rules file and calculate the policy score.
# Store results in a hash
f_rules = open('irc_Rules.csv')
# Skip the header row
next(f_rules)

h_rules = {}
for val in f_rules:
    l_val = next(csv.reader([val.strip()]))
    str_requestid = l_val[0]
    str_rulename = l_val[1]

    if str_requestid in h_rules:
        if str_rulename == 'no_rules':
            pass
        else:
            score = h_rules[str_requestid] + h_rules_weights[str_rulename]
            h_rules[str_requestid] = score
    else:
        if str_rulename == 'no_rules':
            h_rules[str_requestid] = 0
        else:
            weight = h_rules_weights[str_rulename]
            h_rules[str_requestid] = weight

# Output the values from h_rules. These are the calculated
# policy scores
print('Request ID,Calc Policy Score')
for key,value in h_rules.items():
    print("{0},{1}".format(key,value))
