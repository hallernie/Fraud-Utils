import re
import numpy as np

# Regex for line containing unit # and name
unit_re = re.compile(r"^Unit [0-9]")

# Regex for line containing a question
question_re = re.compile(r"^[0-9]+[.]")

# Regex for line containing a question
az_question_re = re.compile(r"^[0-9]+")

# Regex for line containing the correct answer
correct_answer_re = re.compile(r"^[A-D][)] X")

# Regex for line containing a choice letter (A-D)
choice_letter_re = re.compile(r"^[A-D][)]")

# Regex for blank line
blank_line_re = re.compile(r"^$")


##### method to store questions and answers
#
#     get_questions_and_answers(str_filename)
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

 ['3. The person who prepares an abstract of title for a parcel of real estate', 'A) insures the condition of the title.', 'B) inspects the property.', 'C) issues title insurance.', 'D) searches the public records and then summarizes the events and proceedings that affect title.', 'D', 'Unit # and title'],
 
 ['4. When A recorded the deed received from B, the legal consequence of the recording was to', 'A) give B assurance of holding a first lien.', 'B) protect B from existing adverse claims.', 'C) transfer title.', 'D) serve as constructive notice of A’s interest.', 'D', 'Unit # and title']

]
"""
##### 
def get_questions_and_answers(str_filename):
    f_input = open(str_filename)
    total_list = []
    tmp_list =[]

    for val in f_input:
        # find the first question
        if question_re.match(val):
            break;
        # store the unit number and description
        if unit_re.match(val):
            str_unit = val.strip()

    # store the first question
    tmp_list.append(val.strip())

    # Continue processing the file
    for val in f_input:
        #skip blank lines
        if blank_line_re.match(val):
            pass
        elif question_re.match(val):
            # add the correct answer to the end of the list
            tmp_list.append(str_correct_answer)
            # add the unit # and description to the end of the list
            tmp_list.append(str_unit)
            # add the question, choices, correct answer, and unit number/desc to the total
            total_list.append(tmp_list)

            # start a new question
            tmp_list = []
            # append the new question to the beginning of the list
            tmp_list.append(val.strip())
        elif choice_letter_re.match(val):
            str_choice = ""
            if correct_answer_re.match(val):
                str_choice += val.strip().split(" ")[0]
                str_correct_answer = val[0]
            else:
                str_choice += val.strip()
        else: # must be the choice text
            str_choice += f" {val.strip()}"
            tmp_list.append(str_choice)

    # Store the last question/answer set
    tmp_list.append(str_correct_answer)
    tmp_list.append(str_unit)
    total_list.append(tmp_list)

    return total_list
##### end: get_questions_and_answers(str_filename)



##### method to store questions and answers
#
#     get_questions_and_answers2(str_filename)
#
# Input is a file containing questions and answers:
#
"""

***
Unit 6
***

1 B
2 D
3 B
4 B
5 A
6 C
7 C
8 B
9 D

"""
#
#
#
# Output is a list of lists:
#
"""
[

["Unit 6", "1", "B"],
["Unit 6", "2", "D"],
["Unit 6", "3", "B"]

]
"""
##### 
def get_questions_and_answers2(str_filename):
    f_input = open(str_filename)
    total_list = []

    for val in f_input:
        # find the first question
        if az_question_re.match(val):
            tmp_list =[]
            tmp_list.append(str_unit)
            val = val.strip()
            value = val.split(" ")
            tmp_list.append(value[0])
            tmp_list.append(value[1])
            total_list.append(tmp_list)

        # store the unit number and description
        if unit_re.match(val):
            str_unit = val.strip()

    return total_list


##### end: get_questions_and_answers2(str_filename)


##### method to present one question, prompt for answer, and display if answer was correct
#
# Input is a list in the form:
#
# 	[question, choice1, choice2, choice3, choice4, correct_answer, Unit#]
#
"""
['3. The person who prepares an abstract of title for a parcel of real estate', 'A) insures the condition of the title.', 'B) inspects the property.', 'C) issues title insurance.', 'D) searches the public records and then summarizes the events and proceedings that affect title.', 'D']
"""
#
def present_question(l_question):
    print(f"### {l_question[6]} ###")
    print()
    print(l_question[0])
    print()
    print(l_question[1])
    print(l_question[2])
    print(l_question[3])
    print(l_question[4])
    print()

    answer = input('Your answer: ')

    if str(answer).lower() == str(l_question[5]).lower():
        print()
        print()
        print('                                             ***********CORRECT***********')
        print()
        print()
        print()
        return None
    else:
        print()
        print()
        print(f"***INCORRECT*** The correct answer is: {l_question[5]}")
        print()
        print()
        print()
        return(l_question)

##### end: present_question(l_question)



##### method to present one question, prompt for answer, and display if answer was correct
#
# Input is a list in the form:
#
"""
[['Unit 6', '1', 'B'], ['Unit 6', '2', 'D'], ['Unit 6', '3', 'B'], ['Unit 6', '4', 'B'], ['Unit 6', '5', 'A'], ['Unit 6', '6', 'C'], ['Unit 6', '7', 'C'], ['Unit 6', '8', 'B'], ['Unit 6', '9', 'D'], ['Unit 6', '10', 'A'], ['Unit 6', '11', 'C'], ['Unit 6', '12', 'C'], ['Unit 6', '13', 'A'], ['Unit 6', '14', 'D'], ['Unit 6', '15', 'B'], ['Unit 6', '16', 'A'], ['Unit 6', '17', 'A'], ['Unit 6', '18', 'B'], ['Unit 6', '19', 'D'], ['Unit 6', '20', 'C'], ['Unit 6', '21', 'D'], ['Unit 6', '22', 'B'], ['Unit 6', '23', 'B'], ['Unit 6', '24', 'C'], ['Unit 6', '25', 'B'], ['Unit 6', '26', 'A'], ['Unit 6', '27', 'A'], ['Unit 6', '28', 'B'], ['Unit 6', '29', 'D']]
"""
#
def present_question2(l_question):
    print(f"### {l_question[0]} ###")
    print()
    print(l_question[1])
    print()

    answer = input('Your answer: ')

    if str(answer).lower() == str(l_question[2]).lower():
        print()
        print()
        print('                                             ***********CORRECT***********')
        print()
        print()
        print()
        return None
    else:
        print()
        print()
        print(f"***INCORRECT*** The correct answer is: {l_question[2]}")
        print()
        print()
        print()
        return(l_question)

##### end: present_question2(l_question)



##### method to randomly deliver test questions
#
# units: "0" for all units, or an integer corresponding to the unit #
# int_num_questions: the number of questions to ask
# int_random_seed: an integer random seed, used to shuffle the questions
#
def process_questions(l_question_bank, units, int_num_questions, int_random_seed):
    l_incorrect = []

    if units == '0':
        np.random.seed(int_random_seed)
        idx = np.arange(len(l_question_bank))
        np.random.shuffle(idx)

        if int_num_questions > len(l_question_bank):
            int_num_questions = len(l_question_bank)

        for i in range(int_num_questions):
            tmp_val = present_question(l_question_bank[idx[i]])
            if tmp_val is not None:
                l_incorrect.append(tmp_val)

    # Replay the incorrect questions
    if len(l_incorrect) > 0:
        print(f"You missed {len(l_incorrect)} questions.")
        print()

        for val in l_incorrect:
            tmp_val1 = val[6].strip().split(":")[0]
            tmp_val2 = val[0].strip().split(".")[0]
            print(f"{tmp_val1}: {tmp_val2}")

        print()
        print()

        for i in range(len(l_incorrect)):
            tmp_val = present_question(l_incorrect[i])

##### end: process_questions()



##### method to randomly deliver test questions
#
# units: "0" for all units, or an integer corresponding to the unit #
# int_num_questions: the number of questions to ask
# int_random_seed: an integer random seed, used to shuffle the questions
#
def process_questions2(l_question_bank, units, int_num_questions, int_random_seed):
    l_incorrect = []

    if units == '0':
        np.random.seed(int_random_seed)
        idx = np.arange(len(l_question_bank))
        np.random.shuffle(idx)

        if int_num_questions > len(l_question_bank):
            int_num_questions = len(l_question_bank)

        for i in range(int_num_questions):
            tmp_val = present_question2(l_question_bank[idx[i]])
            if tmp_val is not None:
                l_incorrect.append(tmp_val)

    # Replay the incorrect questions
    if len(l_incorrect) > 0:
        print(f"You missed {len(l_incorrect)} questions.")
        print()
        
        for val in l_incorrect:
            print(f"{val[0]}: {val[1]}")

        print()
        print()

        for i in range(len(l_incorrect)):
            tmp_val = present_question2(l_incorrect[i])

##### end: process_questions2()


## MAIN ##

test_type = input("Enter 1 for national, 2 for Arizona: ")

filename = input("Enter the test file name: ")
num_questions = input("Enter length of test: ")
unit_number = input("Enter unit number, or 0 for all units: ")
random_seed = input("Enter a random integer: ")

if test_type == "1":
    questions = get_questions_and_answers(filename)
    process_questions(questions, unit_number, int(num_questions), int(random_seed))

elif test_type == "2":
    questions = get_questions_and_answers2(filename)
    process_questions2(questions, unit_number, int(num_questions), int(random_seed))
    

## TO DO ##
#
# Keep track of correct and incorrect
#----- give % correct
#----- list incorrect answers (unit and question #)
#----- summary by unit
#--------- % correct for each unit
#----- option to enter specific question #s, and present these questions in random order
