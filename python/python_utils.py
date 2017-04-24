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
#   dictionary with id/count pairs, where the count is the number of
#   transactions that have occured for that id, in the window_size
#
# Example:
#   d_values = {"id1":100, "id2":105, "id3":115, "id4":120}
#   i_window_size = 5
#
#   d_output = {"id1":1, "id2":2, "id3":1, "id4":2}
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
# Name: *** straight_velocity_aggregate ***
#
# Input:
#   str_event_filename: Cleaned event file. Note: file must contain column "Event Time" and "Request ID"
#   str_attrib_to_aggregate: Column name from event file containing the attribute that will be aggregated
#   i_window_size: The size of the rolling window
#   str_output_column_name: The name of the output variable (output column name)
#                           that will contain the aggregate value
#
# Output:
#   Two column csv file: "Request ID", "Aggregate Count"
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
def straight_velocity_aggregate(str_event_filename,
                                str_attrib_to_aggregate,
                                i_window_size,
                                str_output_column_name):

    # find "Event Time" and str_attrib column indexes
    event_file_csv_reader = csv.reader(open(str_event_filename, newline=''))
    l_header = next(event_file_csv_reader)
    event_time_col = findColumnGivenHeader(l_header, 'Event Time')
    request_id_col = findColumnGivenHeader(l_header, 'Request ID')
    attrib_col = findColumnGivenHeader(l_header, str_attrib_to_aggregate)

    if(event_time_col == -1):
        print('straight_velocity_aggregate: "Event Time" column not found.')
        exit()
    if(attrib_col == -1):
        print('straight_velocity_aggregate: "attribute" column not found.')
        exit()
    if(request_id_col == -1):
        print('straight_velocity_aggregate: "Request ID" column not found.')
        exit()

    # Print the header row
    print("Request ID," + str_output_column_name)
    # Process the remainder of the input event file
    l_checked_attrib = []
    d_values_plus = {}

    for val in event_file_csv_reader:
        attrib_val = val[attrib_col]
        event_time_val = val[event_time_col]
        request_id_val = val[request_id_col]

        if(attrib_val not in l_checked_attrib):
            l_checked_attrib.append(attrib_val)
            d_values_plus[attrib_val] = {request_id_val: seconds_from_datetime(event_time_val)}
        else: # must have seen this attrib before
            d_values_plus[attrib_val][request_id_val] = seconds_from_datetime(event_time_val)

    for key, value in d_values_plus.items():
        calc_window_counts(value,i_window_size)
    #def calc_window_counts(d_values, i_window_size):
    #return d_values_plus

#
### end: straight_velocity_aggregate
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
straight_velocity_aggregate('ydesign_events_cleaned.csv',
                                'Credit Card Hash',
                                86400, # 1 Day 86400
                                'cc_hash_per_day')
