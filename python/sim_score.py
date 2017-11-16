import csv

def simulate(str_ruleweights_filename, str_rules_filename):
    # Store rulenames/weights in a hash

    #f_ruleweights = open('ruleweights.csv')
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
    #f_rules = open('irc_Rules.csv')
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


#***MAIN***
simulate('ruleweights.csv', 'irc_Rules.csv')
