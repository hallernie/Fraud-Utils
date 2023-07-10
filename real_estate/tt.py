import re

# Open the question bank for reading
#f_input = open('unit_2.txt')

# Regex for line containing unit # and name
unit_re = re.compile(r"^Unit [0-9]")

# Regex for line containing a question
question_re = re.compile(r"^[0-9]+[.]")

# Regex for line containing the correct answer
correct_answer_re = re.compile(r"^[A-D][)] X")

# Regex for line containing a choice letter (A-D)
choice_letter_re = re.compile(r"^[A-D][)]")

# Regex for blank line
blank_line_re = re.compile(r"^$")


##### function to store questions and answers
#
# Input is a file containing questions and answers:
#
"""
*****
Note: Ignore this content
*****

*****
Unit 7: Title Records
*****

41. A title that has no defects that could carry over as a problem for the next property owners is called

A)
a certified title.
B)
an encumbered title.
C) X
a marketable title.
D)
a suit to quiet title.

42. Chain of title is MOST accurately defined as

A)
the examination of the record and hidden risks such as forgeries, undisclosed heirs, errors in the public records, and so on.
B)
an instrument or document that protects the insured parties (subject to specific exceptions) against defects in the record of a property's ownership.
C)
a report of the contents of the public record, including all legal proceedings, regarding a particular property.
D) X
the record of a property's ownership.
"""
#
#
#
# Output is a list of lists:
#
"""
[

 ['3. The person who prepares an abstract of title for a parcel of real estate', 'A) insures the condition of the title.', 'B) inspects the property.', 'C) issues title insurance.', 'D) searches the public records and then summarizes the events and proceedings that affect title.', 'D']
 
 ['4. When A recorded the deed received from B, the legal consequence of the recording was to', 'A) give B assurance of holding a first lien.', 'B) protect B from existing adverse claims.', 'C) transfer title.', 'D) serve as constructive notice of A’s interest.', 'D']

]
"""
##### 
def get_questions_and_answers(str_filename):
    f_input = open(str_filename)
    total_list = []
    tmp_list =[]

    # find the first question
    for val in f_input:
        if question_re.match(val):
            break;

    # store the first question
    tmp_list.append(val.strip())

    for val in f_input:
        #skip blank lines
        if blank_line_re.match(val):
            pass
        elif question_re.match(val):
            # add the correct answer to the end of the list
            tmp_list.append(str_correct_answer)
            # add the question, choices, and answer to the total
            total_list.append(tmp_list)

            # start a new set
            tmp_list = []
            tmp_list.append(val.strip())
        elif choice_letter_re.match(val):
            str_choice = ""
            if correct_answer_re.match(val):
                str_choice += val.strip().split(" ")[0]
                str_correct_answer = val[0]
            else:
                str_choice += val.strip()
        else: # must be a choice
            str_choice += f" {val.strip()}"
            tmp_list.append(str_choice)

    # Store the last question/answer set
    tmp_list.append(str_correct_answer)
    total_list.append(tmp_list)

    return total_list
##### end: get_questions_and_answers(str_filename)


def present_question(l_question):
    print(l_question[0])
    print()
    print(l_question[1])
    print(l_question[2])
    print(l_question[3])
    print(l_question[4])
    print()

    answer = input('Your answer: ')

    if str(answer).lower() == str(l_question[5]).lower():
        print('Correct!')
    else:
        print(f"The correct answer is: {l_question[5]}")

    print()
    print()

questions = get_questions_and_answers('unit_7.txt')
for x in questions:
    present_question(x)


# One pass through the file to build up test bank data structure
#for x in f_input:
    #if question_re.match(x):
        #print(x.strip())
