#
# Takes a sequence plus a string value. Checks each
# value in the sequence to see if it contains the giving string.
#
def findit(seq,s_tofind):
    for val in seq:
        if s_tofind in str(val).lower():
            print(val)
