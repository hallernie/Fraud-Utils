def true_ip_reason_code(row):
    """if row['trueip_flag']:
        return_val = row['True IP Region Code']
    else:
        return_val = 'unknown'
    return return_val

    # Method returns a column with the value of True IP Region Code
    # or the string "unknown".
    #
    # Assumes that trueip_flag column was set using
    # notnull()
    """
    if row['trueip_flag']:
        return_val = row['True IP Region Code']
    else:
        return_val = 'unknown'
    return return_val
