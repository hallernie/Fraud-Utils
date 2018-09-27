def acc_address_state_trueip_regioncode_compare(row):
    """if row['true_ip_region_code'] != 'unknown':
        if row['Account Address State'] != row['true_ip_region_code']:
            return 'no match'
        else:
            return 'match'
    else:
        return 'unknown'

    # Method assumes that true_ip_region_code is the actual code, or the
    # string "unknown".
    """
    if row['true_ip_region_code'] != 'unknown':
        if row['Account Address State'] != row['true_ip_region_code']:
            return 'no match'
        else:
            return 'match'
    else:
        return 'unknown'
