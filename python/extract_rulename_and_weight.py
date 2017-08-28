import sys
import json

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
    for val in l_rules:
        if(val["ruleId"] not in [120,121,122]): # ruleId 121 is "Terminate" rule.
            print("{0},{1}".format(val['displayName'], val['riskWeight']))
        elif("rules" in val.keys()): # if not in, then this IF statement has no internal rules
            get_rulename_and_weight(val["rules"])


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
l_rules = policy["policyVersion"]["rulesSet"]["rules"]

# Print header row
print("RuleName,RuleWeight")
get_rulename_and_weight(l_rules)
