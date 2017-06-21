import sys
import time
import calendar
import csv

# Constants
m_minute = 60
m_hour = 3600
m_day = 86400
m_30_days = 2592000
m_year = 31536000

#
# Name: *** calc_window_counts ***
#
# Input:
#   d_values: dictionary with id/time_ints pairs
#   i_window_size: the size of the rolling window
#
# Output:
#   Two colomn id/count pairs, where the count is the number of
#   transactions that have occured for that id, in the window_size.
#   Output is to standard out.
#
# Example:
#   d_values = {"id1":100, "id2":105, "id3":115, "id4":120}
#   i_window_size = 5
#
#   Example output:
#       "id1",1
#       "id2":2
#       "id3":1
#       "id4":2
#
def calc_window_counts(d_values, i_window_size):
    for key, value in d_values.items():
        cnt = 0
        for key1, value1 in d_values.items():
            if( (value - value1 >= 0) and
                (value - value1 <= i_window_size) ):
                cnt += 1

        print("{0},{1}".format(key, cnt))
#
### end: calc_window_counts
#


#
# Name: *** calc_window_counts2 ***
#
# Input:
#   d_values: dictionary with id/time_ints pairs
#   i_window_size: the size of the rolling windowd_secondary_attributes
#   d_secondary_attributes: dictionary with saved secondary attributes
#
# Output:
#   Two colomn id/count pairs, where the count is the number of
#   distinct secondary attributes that have occured for that id, in the window_size.
#   Output is to standard out.
#
# Example:
#   d_values = {'id1':100, 'id2':105, 'id3':115, 'id4':120}
#   i_window_size = 5
#   d_secondary_attributes = {'id1':'ernie.hall@vantiv.com',
#                               'id2':'bob@vantiv.com',
#                               'id3':'carol@vantiv.com',
#                               'id4':'judy@vantiv.com'}
#
#   Example output:
#       "id1",1
#       "id2":2
#       "id3":1
#       "id4":2
#
def calc_window_counts2(d_values, i_window_size, d_secondary_attributes):
    for key, value in d_values.items():
        # Dictionary to hold the secondary attribute values.
        # In the form: {'secondary attrib': 'dont care'}
        d_cnt = {}

        for key1, value1 in d_values.items():
            if( (value - value1 >= 0) and
                (value - value1 <= i_window_size) ):
                d_cnt[d_secondary_attributes[key1]] = 'dont care' 

        print("{0},{1}".format(key, len(d_cnt)))
#
### end: calc_window_counts2
#


#
# Name: *** seconds_from_datetime ***
#
# Input:
#   datetime: date/time string in the form "2017/04/19 20:32:03.864"
#
# Output:
#   number of seconds since the epoch for the input date/time
#
def seconds_from_datetime(datetime):
    # Parse the input string for the various date/time parts. Milli is discarded.
    # time.strptime("2017 04 19 20 32 03", "%Y %m %d %H %M %S")
    str_datepart = datetime.split(" ")[0]
    str_timepart = datetime.split(" ")[1]

    str_year = str_datepart.split("/")[0]
    str_month = str_datepart.split("/")[1]
    str_day = str_datepart.split("/")[2]

    str_hour = str_timepart.split(":")[0]
    str_min = str_timepart.split(":")[1]
    str_sec = str_timepart.split(":")[2].split(".")[0]

    #datetime2 = time.strptime("2017 04 19 20 32 03", "%Y %m %d %H %M %S")
    datetime2 = time.strptime("{0} {1} {2} {3} {4} {5}".format(str_year,str_month,str_day,str_hour,str_min,str_sec),
                              "%Y %m %d %H %M %S")
    #print(str_year)
    #print(str_month)
    #print(str_day)
    #print(str_hour)
    #print(str_min)
    #print(str_sec)
    #print(datetime2)

    return calendar.timegm(datetime2)
#
### end: seconds_from_datetime
##


#
# Name: *** entity_velocity ***
# Description: Detect if specified entity seen multiple times in a specified time frame.
#
# Input:
#   str_event_filename: Cleaned event file. Note: file must contain column "Event Time" and "Request ID"
#   str_attrib_to_aggregate: Column name from event file containing the attribute that will be aggregated
#   i_window_size: The size of the rolling window
#   str_output_column_name: The name of the output variable (output column name)
#                           that will contain the aggregate value
#
# Output:
#   Two column csv data: "Request ID", "Aggregate Count"
#       assume:
#           str_attrib_to_aggregate = 'Credit Card Hash'
#           str_output_column_name = 'cc_per_day'
#
#       Request ID,cc_per_day
#   5e6db53ea0d2695dcc98a894caac123bec387cb8,5
#   aeaa2d2e19a59ba63144ccd77a74a41cf878412d,1
#                   ...
#                   ...
#                   ...
#
def entity_velocity(str_event_filename,
                                str_attrib_to_aggregate,
                                i_window_size,
                                str_output_column_name):

    # find "Event Time", "Request ID", and str_attrib column indexes
    event_file_csv_reader = csv.reader(open(str_event_filename, newline=''))
    l_header = next(event_file_csv_reader)
    event_time_col = findColumnGivenHeader(l_header, 'Event Time')
    request_id_col = findColumnGivenHeader(l_header, 'Request ID')
    attrib_col = findColumnGivenHeader(l_header, str_attrib_to_aggregate)

    if(event_time_col == -1):
        print('entity_velocity: "Event Time" column not found.')
        exit()
    if(attrib_col == -1):
        print('entity_velocity: "attribute" column not found.')
        exit()
    if(request_id_col == -1):
        print('entity_velocity: "Request ID" column not found.')
        exit()

    # Print the header row
    print("Request ID," + str_output_column_name)

    # Process the remainder of the input event file
    d_checked_attrib = {} # Keep track of the attributes that have already been seen

    # Dictionary in the form:
    # {str_attrib_to_aggregate: {Request ID1: time_in_seconds,
    #                            Request ID2: time_in_seconds}
    d_values_plus = {}

    for val in event_file_csv_reader:
        attrib_val = val[attrib_col]
        event_time_val = val[event_time_col]
        request_id_val = val[request_id_col]

        if(attrib_val not in d_checked_attrib):
            d_checked_attrib[attrib_val] = ""
            d_values_plus[attrib_val] = {request_id_val: seconds_from_datetime(event_time_val)}
        else: # must have seen this attrib before
            d_values_plus[attrib_val][request_id_val] = seconds_from_datetime(event_time_val)

    for d_values in d_values_plus.values():
        calc_window_counts(d_values, i_window_size)
#
### end: entity_velocity
##


#
# Name: *** attribute_anomaly ***
# Description: Detect when one attribute varies for events with another attribute in common.
#
# Input:
#   str_event_filename: Cleaned event file. Note: file must contain column "Event Time" and "Request ID"
#   str_primary_attrib: Fixed attribute
#   str_secondary_attrib: Varying attribute
#   i_window_size: The size of the rolling window
#   str_output_column_name: The name of the output variable (output column name)
#                           that will contain the aggregate value
#
# Output:
#   Two column csv data: "Request ID", "Aggregate Count"
#       assume:
#          str_primary_attrib = ['Credit Card Hash',
#          str_secondary_attrib = ['Account Email',
#          str_output_column_name = 'emails_per_cc_per_day'
#          i_window_size: 86400
#
#       Request ID,cc_per_day
#   5e6db53ea0d2695dcc98a894caac123bec387cb8,5
#   aeaa2d2e19a59ba63144ccd77a74a41cf878412d,1
#                   ...
#                   ...
#                   ...
#
def attribute_anomaly(str_event_filename,
                      str_primary_attrib,
                      str_secondary_attrib,
                      i_window_size,
                      str_output_column_name):

    # find "Event Time", "Request ID", str_primary_attrib, and str_secondary_attrib column indexes
    event_file_csv_reader = csv.reader(open(str_event_filename, newline=''))
    l_header = next(event_file_csv_reader)
    event_time_col = findColumnGivenHeader(l_header, 'Event Time')
    request_id_col = findColumnGivenHeader(l_header, 'Request ID')
    primary_attrib_col = findColumnGivenHeader(l_header, str_primary_attrib)
    secondary_attrib_col = findColumnGivenHeader(l_header, str_secondary_attrib)

    if(event_time_col == -1):
        print('attribute_anomaly: "Event Time" column not found.')
        exit()
    if(request_id_col == -1):
        print('attribute_anomaly: "Request ID" column not found.')
        exit()
    if(primary_attrib_col == -1):
        print('attribute_anomaly: "str_primary_attrib" column not found.')
        exit()
    if(secondary_attrib_col == -1):
        print('attribute_anomaly: "secondary_attrib_col" column not found.')
        exit()

    # Print the header row
    print("Request ID," + str_output_column_name)

    # Process the remainder of the input event file

    # Dictionary to keep track of the attributes that have already been seen
    d_checked_attrib = {} 

    # Use two dictionaries to keep track of primary/secondary attribute data
    # Dictionary in the form:
    # {str_primary_attrib: {Request ID1: time_in_seconds,
    #                       Request ID2: time_in_seconds}
    d_values_plus = {}

    # Dictionary in the form:
    # {'Request ID': 'secondary attribute'}
    d_secondary_attributes = {}

    for val in event_file_csv_reader:
        primary_attrib_val = val[primary_attrib_col]
        secondary_attrib_val = val[secondary_attrib_col]
        event_time_val = val[event_time_col]
        request_id_val = val[request_id_col]

        # Store secondary attribute
        d_secondary_attributes[request_id_val] = secondary_attrib_val

        if(primary_attrib_val not in d_checked_attrib):
            d_checked_attrib[primary_attrib_val] = ""
            d_values_plus[primary_attrib_val] = {request_id_val: seconds_from_datetime(event_time_val)}
        else: # must have seen this attrib before
            d_values_plus[primary_attrib_val][request_id_val] = seconds_from_datetime(event_time_val)

    for d_values in d_values_plus.values():
        calc_window_counts2(d_values, i_window_size, d_secondary_attributes)

#
### end: attribute_anomaly
##


#
# Name: *** get_attribute_and_datetime_from_file ***
#
# Input:
#   str_event_filename: Cleaned event file. Note: file must contain column "Event Time"
#   str_attrib: Column name from event file containing the attribute to extract
#
# Output:
#   d_values: dictionary with id/time_ints pairs
#       example:
#           str_attrib = "Credit Card Hash"
#           d_values =  {"5e6db53ea0d2695dcc98a894caac123bec387cb8":100,
#                        "aeaa2d2e19a59ba63144ccd77a74a41cf878412d":105,
#                        "2b31d638ea9ae3ddbcc78bd649e813925689b1ae":115,
#                        "8ab1a7479591c581cabb87c811e6b585683fbf67":120}
#
def get_attribute_and_datetime_from_file (str_event_filename,
                                          str_attrib):


    # build d_values
    d_values = {}
    for val in event_file_csv_reader:
        d_values[val[attrib_col]] = seconds_from_datetime(val[event_time_col])

    for key, value in d_values.items():
        print(key, value)

#
### end: get_attribute_and_datetime_from_file
##


#
# Name: *** findColumnGivenHeader ***
#
# Input:
#   l_header: Header from csv file as a list of strings
#   str_column_name: Column name from csv file we are trying to find
#
# Output:
#   The zero based column index of str_column_name, or -1 if not found
#
def findColumnGivenHeader(l_header, str_column_name):
    position = 0

    for val in l_header:
        if(val == str_column_name):
            return position
        position += 1

    return -1
#
### end: findColumnGivenHeader
##



#####
##### MAIN
#####
d_values = {"id1":100, "id2":105, "id3":115, "id4":120}
i_window_size = 30
datetime = "2017/04/19 20:32:03.864"

# calc_window_counts(d_values, i_window_size)
#print (seconds_from_datetime(datetime))

# Reading a csv file
#x = open("testing.csv", newline='')
#for z in csv.reader(x):
    #print(z)

#get_attribute_and_datetime_from_file ('ydesign_events_cleaned.csv', 'Credit Card Hash')
#entity_velocity('ydesign_events_cleaned.csv',
                            #'Custom Attribute 5',
                            #2, # 1 Day 86400
                            #'exact_id_per_day')

#attribute_anomaly('ydesign_events_cleaned.csv',
                    #'Credit Card Hash',
                    #'Account Email',
                    #86400,
                    #'emails_per_cc_per_day')



if(len(sys.argv) == 1):
    # Print usage:
    print('entity_velocity input_filename attribute_name window_in_seconds output_column_name')
    print('attribute_anomaly input_filename primary_attribute_name secondary_attribute_name window_in_seconds output_column_name')

elif(sys.argv[1] == 'entity_velocity'):
    if(len(sys.argv) != 6):
        print('entity_velocity input_filename attribute_name window_in_seconds output_column_name')
        exit()

    input_filename = sys.argv[2]
    attribute_name = sys.argv[3]
    window_in_seconds = int(sys.argv[4])
    output_column_name = sys.argv[5]

    entity_velocity(input_filename,
                    attribute_name,
                    window_in_seconds, # 1 Day 86400
                    output_column_name)

elif(sys.argv[1] == 'attribute_anomaly'):
    if(len(sys.argv) != 7):
        print('attribute_anomaly input_filename primary_attribute secondary_attribute window_in_seconds output_column_name')
        exit()

    input_filename = sys.argv[2]
    primary_attribute = sys.argv[3]
    secondary_attribute = sys.argv[4]
    window_in_seconds = int(sys.argv[5])
    output_column_name = sys.argv[6]

    attribute_anomaly(input_filename,
                        primary_attribute,
                        secondary_attribute,
                        window_in_seconds,
                        output_column_name)
