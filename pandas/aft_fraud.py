def aft_fraud(row):
    if( (row['CBK_OPEN_DATE'] != '--') or
            (row['VI_ALERT_CREATE_DATE'] != '--') or
            (row['MC_ALERT_CREATE_DATE'] != '--') ):
        return 1
    else:
        return 0
