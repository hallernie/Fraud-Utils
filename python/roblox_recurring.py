import re

def recurring(x):
    """re_recur = re.compile(r"recurring")
    if re_recur.search(x):
        return 1
    else:
        return 0
    """

    re_recur = re.compile(r"recurring")
    if re_recur.search(x):
        return 1
    else:
        return 0

