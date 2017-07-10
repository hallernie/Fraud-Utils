import sys
import csv
import re

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
#re_newconsultant = re.compile(r"newconsultant")
#re_true = re.compile(r"true")
#re_false = re.compile(r"false")

def getConsultantId(ca5):
    ca5 = val[1]
    try:
        if(ca5.split('%')[int(sys.argv[1])] == ""):
            print("ca5= >{0}<".format(ca5))
        elif( (ca5.split('%')[int(sys.argv[1])] == "10" ) ):
            print("10 = >{0}<".format(ca5))
        else:
            print(">{0}<".format(ca5.split('%')[int(sys.argv[1])]))
    except IndexError:
        print("IndexError= >{0}<".format(ca5))

#*** MAIN ***

# Open output files for writing
#f_shipping_method = open("shipping_method.csv", "w")
#f_shipping_method.write('Request ID,shipping_method\n')

#f_order_type = open("order_type.csv", "w")
#f_order_type.write('Request ID,order_type\n')

#f_product_type = open("product_type.csv", "w")
#f_product_type.write('Request ID,product_type\n')

f_new_consultant = open("new_consultant.csv", "w")
f_new_consultant.write('Request ID,new_consultant\n')

re_newconsultant = re.compile(r"newconsultant")
re_true = re.compile(r"true")
re_false = re.compile(r"false")

csv_reader = csv.reader(open("ca5.csv", newline=''))
next(csv_reader) # skip the header row
for val in csv_reader:
    request_id = val[0]
    ca5 = val[1]
    #getShippingMethod(request_id, ca5, f_shipping_method)
    #getOrderType(request_id, ca5, f_order_type)
    #getProductType(request_id, ca5, f_product_type)
    getNewConsultant(request_id, ca5, f_new_consultant)
    #getConsultantId(ca5)
