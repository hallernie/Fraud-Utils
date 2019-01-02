#
# Takes a sequence plus a string value. Checks each
# value in the sequence to see if it contains the giving string.
#
def findit(seq,s_tofind):
    """def findit(seq,s_tofind):
    for val in seq:
        if s_tofind.lower() in str(val).lower():
            print(val)
    """
    for val in seq:
        if s_tofind.lower() in str(val).lower():
            print(val)


#
# Takes a sequence and prints its values one per line
#
def printit(seq):
    """def printit(seq):
    for x,val in enumerate(seq):
        print("    {0}:   {1}".format(x,str(val)))
    """
    for x,val in enumerate(seq):
        print("    {0}:   {1}".format(x,str(val)))
