import csv

#
# This is the simulator functionality.
#
def simulate(str_ruleweights_filename, str_rules_filename):
    # Store rulenames/weights in a hash
    # The ruleweights file is in the following format:
    #           RuleName,RuleWeight
    #           INVALID_account_address_zip,0
    #           UnusualProxyAttributes,-10
    #           SmartIDValid,0
    #           ExactIDInvalid,0
    #           MobileDevice,0
    #           DeviceOnLocalBlacklist,-100
    #           FuzzyDeviceOnLocalBlacklist,-100
    #           TrueIPOnLocalBlacklist,-100
    #           TrueIPGeoOnLocalBlacklist,-100
    #
    f_ruleweights = open(str_ruleweights_filename)
    next(f_ruleweights) # Skip the header row

    h_results_weights = {} # Lookup table for rules/weights
    for val in f_ruleweights:
        l_val = next(csv.reader([val.strip()]))
        str_rulename = l_val[0]
        str_ruleweight = l_val[1]
        h_results_weights[str_rulename] = int(str_ruleweight)

    # Read the rules file and calculate the policy score.
    # The rules file is in the following format:
    #       Request ID,Rules,ruleweight
    #       62fb6a5682e04c97a466a0e3c050bc63,DeviceLocalAgeLessThanOneHour,0
    #       62fb6a5682e04c97a466a0e3c050bc63,FuzzyDeviceLocalAgeLessThanOneHour,0
    #       71145c2a008945a58d3083d8a94959ad,DeviceLocalAgeLessThanOneHour,0
    #       71145c2a008945a58d3083d8a94959ad,DeviceGlobalAgeLessThanOneHour,0
    #       71145c2a008945a58d3083d8a94959ad,FuzzyDeviceLocalAgeLessThanOneHour,0
    #       8a03e707b5104628a206f2c2c60f955a,FlashDisabled,-5
    # Store results in a hash
    #
    f_rules = open(str_rules_filename)
    next(f_rules) # Skip the header row

    h_results = {}
    for val in f_rules:
        l_val = next(csv.reader([val.strip()]))
        str_requestid = l_val[0]
        str_rulename = l_val[1]

        if str_requestid in h_results:
            if str_rulename == 'no_rules':
                pass
            else:
                score = h_results[str_requestid] + h_results_weights[str_rulename]
                h_results[str_requestid] = score
        else:
            if str_rulename == 'no_rules':
                h_results[str_requestid] = 0
            else:
                weight = h_results_weights[str_rulename]
                h_results[str_requestid] = weight

    # Iterate throught the results and set all values -101
    # and less to "-100". Also set values 101 or greater to "100"
    for key,value in h_results.items():
        if h_results[key] < -100:
            h_results[key] = -100
        if h_results[key] > 100:
            h_results[key] = 100


    # Output the values from h_results. These are the calculated
    # policy scores
    print('Request ID,Calc Policy Score')
    for key,value in h_results.items():
        print("{0},{1}".format(key,value))


#
# This is the simulator functionality. The only difference is that
# we assume that there are rules in the rules file that are not in the
# lookup table.
#
def simulate2(str_ruleweights_filename, str_rules_filename):
    # Store rulenames/weights in a hash
    # The ruleweights file is in the following format:
    #           RuleName,RuleWeight
    #           ------------- HEADER -------------,0
    #           INVALID_account_address_zip,0
    #           UnusualProxyAttributes,-10
    #           SmartIDValid,0
    #           ExactIDInvalid,0
    #           MobileDevice,0
    #           ------------- LOCAL BLACKLISTS -------------,0
    #           DeviceOnLocalBlacklist,-100
    #           FuzzyDeviceOnLocalBlacklist,-100
    #           TrueIPOnLocalBlacklist,-100
    #           TrueIPGeoOnLocalBlacklist,-100
    #
    f_ruleweights = open(str_ruleweights_filename)
    next(f_ruleweights) # Skip the header row

    h_results_weights = {} # Lookup table for rules/weights
    for val in f_ruleweights:
        l_val = next(csv.reader([val.strip()]))
        str_rulename = l_val[0]
        str_ruleweight = l_val[1]
        h_results_weights[str_rulename] = int(str_ruleweight)

    # Read the rules file and calculate the policy score.
    # The rules file is in the following format:
    #       Request ID,Rules,ruleweight
    #       62fb6a5682e04c97a466a0e3c050bc63,DeviceLocalAgeLessThanOneHour,0
    #       62fb6a5682e04c97a466a0e3c050bc63,FuzzyDeviceLocalAgeLessThanOneHour,0
    #       71145c2a008945a58d3083d8a94959ad,DeviceLocalAgeLessThanOneHour,0
    #       71145c2a008945a58d3083d8a94959ad,DeviceGlobalAgeLessThanOneHour,0
    #       71145c2a008945a58d3083d8a94959ad,FuzzyDeviceLocalAgeLessThanOneHour,0
    #       8a03e707b5104628a206f2c2c60f955a,FlashDisabled,-5
    # Store results in a hash
    #
    f_rules = open(str_rules_filename)
    next(f_rules) # Skip the header row

    h_results = {}
    for val in f_rules:
        l_val = next(csv.reader([val.strip()]))
        str_requestid = l_val[0]
        str_rulename = l_val[1]

        if not (str_rulename in h_results_weights):
            pass # Assumption is that this rule is not part of simulation
        elif str_requestid in h_results:
            if str_rulename == 'no_rules':
                pass
            else:
                score = h_results[str_requestid] + h_results_weights[str_rulename]
                h_results[str_requestid] = score
        else:
            if str_rulename == 'no_rules':
                h_results[str_requestid] = 0
            else:
                weight = h_results_weights[str_rulename]
                h_results[str_requestid] = weight

    # Iterate throught the results and set all values -101
    # and less to "-100". Also set values 101 or greater to "100"
    for key,value in h_results.items():
        if h_results[key] < -100:
            h_results[key] = -100
        if h_results[key] > 100:
            h_results[key] = 100


    # Output the values from h_results. These are the calculated
    # policy scores
    print('Request ID,Calc Policy Score')
    for key,value in h_results.items():
        print("{0},{1}".format(key,value))


#
# Checks that the rules from the events file are contained in the lookup
# file containing the rule/weights. You should add any rules output
# from the function to the rule/weights lookup file.
#
# Note: Use this function if all rules in the rules file should be in the
#       lookup table. Otherwise you don't have to check, and can use the
#       function "simulate2".
#
def check_rules(str_ruleweights_filename, str_rules_filename):
    # Read rule lookup into a hash
    f_ruleweights = open(str_ruleweights_filename)
    next(f_ruleweights)

    h_rulenames = {} # Lookup table for rule names
    for val in f_ruleweights:
        l_val = next(csv.reader([val.strip()]))
        str_rulename = l_val[0]
        h_rulenames[str_rulename] = ''
    # Add "no_rules"
    h_rulenames['no_rules'] = ''

    # Read rule lookup into a hash
    f_rules = open(str_rules_filename)
    next(f_rules) # Skip the header row

    h_results = {}
    for val in f_rules:
        l_val = next(csv.reader([val.strip()]))
        str_rulename = l_val[1]

        if not (str_rulename in h_rulenames):
            h_results[str_rulename] = ''

    for key in h_results.keys():
        print(key)


#
# Updates a rules file with new and/or updated rule/ruleweight value
#
# Output is the update RuleName,RuleWeight csv values
#
def update_rules_lookup_table(str_ruleweights_filename, str_update_rules_filename,
                                str_lookup_column_name):
    # ruleweights file is in the following format:
    #
    #           RuleName,RuleWeight
    #           INVALID_account_address_zip,0
    #           UnusualProxyAttributes,-10
    #           SmartIDValid,0
    #           ExactIDInvalid,0
    #           MobileDevice,0
    #           DeviceOnLocalBlacklist,-100
    #           FuzzyDeviceOnLocalBlacklist,-100
    #           TrueIPOnLocalBlacklist,-100
    #           TrueIPGeoOnLocalBlacklist,-100
    #
    # str_update_rules_file is a csv file. Lookup column name indicates the column
    # that has the updated rule weights.

    # Load the initial rules/ruleweights
    f_ruleweights = open(str_ruleweights_filename)
    print(next(f_ruleweights).strip()) # Print the header row

    h_results_weights = {} # Lookup table for rules/weights
    for val in f_ruleweights:
        l_val = next(csv.reader([val.strip()]))
        str_rulename = l_val[0]
        str_ruleweight = l_val[1]
        h_results_weights[str_rulename] = int(str_ruleweight)

    # Now update the rules/ruleweights
    f_ruleweights = open(str_update_rules_filename)
    str_header = next(f_ruleweights).strip()
    n_weight_column = -1
    b_found = False

    for value in str_header.split(','):
        n_weight_column += 1
        if value == str_lookup_column_name:
            b_found = True
            break

    if not b_found:
        print('Did not find simulation column.')
        exit()

    for val in f_ruleweights:
        l_val = next(csv.reader([val.strip()]))
        str_rulename = l_val[0] # Assuming that rulename is always first column
        str_ruleweight = l_val[n_weight_column]
        h_results_weights[str_rulename] = int(str_ruleweight)

    # Output the new lookup table
    for key, value in h_results_weights.items():
        print("{0},{1}".format(key,value))

#***MAIN***
simulate2('ruleweights_sim1.csv', 'total_RulesJun.csv')
#check_rules('ruleweights.csv', 'total_RulesJun.csv')
#update_rules_lookup_table('ruleweights.csv', 'simulations.csv', 'Simulation1')
