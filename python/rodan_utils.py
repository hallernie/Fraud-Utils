import sys
import csv
import re

# Some regex(s)
re_newconsultant = re.compile(r"newconsultant")
re_true = re.compile(r"true")
re_false = re.compile(r"false")
re_consultantid = re.compile(r"consultantid:")
re_request_id = re.compile(r"Request ID")
re_ca5 = re.compile(r"Custom Attribute 5")

#ordertype:6%shippingmethod:%producttype:9%newconsultant:false%consultantid:2086088
#ordertype:3%shippingmethod:fedex 2 day %producttype:10%newconsultant:true

#value = csv.reader(["bob,alice"])
#print(next(value))

def getOrderType(request_id, ca5, f_order_type):
    try:
        val = ca5.split('%')[0].split(':')[1].strip()
        f_order_type.write("{0},{1}\n".format(request_id,val))
    except IndexError:
        f_order_type.write("{0},no_ca5\n".format(request_id))

def getShippingMethod(request_id, ca5, f_shipping_method):
    try:
        val = ca5.split('%')[1].split(':')[1].strip()
        if(val == ""):
            #ordertype:6%shippingmethod:%producttype:9%newconsultant:false%consultantid:2086088
            f_shipping_method.write("{0},no_shipping_method\n".format(request_id))
        else:
            #ordertype:3%shippingmethod:fedex 2 day %producttype:10%newconsultant:true
            f_shipping_method.write("{0},{1}\n".format(request_id,val))
    except IndexError:
        f_shipping_method.write("{0},no_ca5\n".format(request_id))

def getProductType(request_id, ca5, f_product_type):
    try:
        f_product_type.write("{0},{1}\n".format(request_id, ca5.split('%')[2].split(':')[1].strip()))
    except IndexError:
        f_product_type.write("{0},no_ca5\n".format(request_id))
        #print("IndexError= >{0}<".format(ca5))

def getNewConsultant(request_id, ca5, f_product_type):
    if(re_newconsultant.search(ca5)):
        if(re_true.search(ca5)):
            f_product_type.write("{0},true\n".format(request_id))
        elif(re_false.search(ca5)):
            f_product_type.write("{0},false\n".format(request_id))
    else:
        f_product_type.write("{0},not_found\n".format(request_id))

# Assumes that consultant id is always the last value in ca5.
def getConsultantId(request_id, ca5, f_consultant_id):
    if(re_consultantid.search(ca5)):
        f_consultant_id.write("{0},{1}\n".format(request_id, re_consultantid.split(ca5)[1]))
    else:
        f_consultant_id.write("{0},not_found\n".format(request_id))

def processCA5(f_cleaned_event_file):
    
    # f_cleaned_event_file should be cleaned event file containing a minimum of "Request ID, Custom Attribute 5"
    csv_reader = csv.reader(open(f_cleaned_event_file, newline=''))

    l_header_row = next(csv_reader) # get the header row

    # find 'Request ID' and 'Custom Attribute 5' columns
    n_request_id_column = -1
    n_ca5_column = -1
    cnt = 0
    for val in l_header_row:
        if(re_request_id.search(val)):
            n_request_id_column = cnt
        if(re_ca5.search(val)):
            n_ca5_column = cnt
        cnt+=1

    # exit if both column are not found
    #print("req_id: {0},  ca5: {1}".format(n_request_id_column, n_ca5_column))
    if( (n_request_id_column == -1) or (n_ca5_column == -1)):
        print("Error: 'Request ID' and 'Custom Attribute 5' column are required")
        exit()
        
    # Open output files for writing
    f_shipping_method = open("shipping_method.csv", "w")
    f_shipping_method.write('Request ID,shipping_method\n')

    f_order_type = open("order_type.csv", "w")
    f_order_type.write('Request ID,order_type\n')

    f_product_type = open("product_type.csv", "w")
    f_product_type.write('Request ID,product_type\n')

    f_new_consultant = open("new_consultant.csv", "w")
    f_new_consultant.write('Request ID,new_consultant\n')

    f_consultant_id = open("consultant_id.csv", "w")
    f_consultant_id.write('Request ID,consultant_id\n')

    for val in csv_reader:
        request_id = val[n_request_id_column]
        ca5 = val[n_ca5_column]
        getShippingMethod(request_id, ca5, f_shipping_method)
        getOrderType(request_id, ca5, f_order_type)
        getProductType(request_id, ca5, f_product_type)
        getNewConsultant(request_id, ca5, f_new_consultant)
        getConsultantId(request_id, ca5, f_consultant_id)

#*** MAIN ***

processCA5('ca5.csv')
