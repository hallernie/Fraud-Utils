import sys
import json
import re

#
#   Counts the number of rules in a ThreatMetrix json policy file.
#
def countRules(l_rules):
    cnt = 0
    for val in l_rules:
        if(val["ruleId"] != 120):
            cnt +=1
        elif("rules" in val.keys()): # if not in, then this IF statement has no internal rules
            cnt += countRules(val["rules"])
    return cnt

#
# Takes the policy json file from ThreatMetrix and extracts:
#       RuleName,RuleWeight
def get_rulename_and_weight(l_rules):
    re_if_statement = re.compile(r"IF#")
    for val in l_rules:
        if re_if_statement.search(val['id']):
            get_rulename_and_weight(val['rules'])
        else: 
            try:
                if val['displayName'] != 'Terminate':
                    print("{0},{1}".format(val['displayName'], val['riskWeight']))
            except KeyError:
                pass


### MAIN ####
if(len(sys.argv) != 2):
    print('Usage:')
    print(' % extract_rulename_and_weight json_policy_filename')
    exit()

str_json_policy_filename = sys.argv[1]
str_val = ""
for val in open(str_json_policy_filename):
    str_val += val

policy = json.loads(str_val)

# A "rule" consists of a list of maps
l_rules = policy["policyVersion"]["rules"]

# Print header row
print("RuleName,RuleWeight")
get_rulename_and_weight(l_rules)
