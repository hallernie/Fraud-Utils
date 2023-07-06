import re

# Open the question bank for reading
f_input = open('unit_2.txt')

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

# function to store questions and answers
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

for x in get_questions_and_answers('unit_3.txt'):
    print(x)
    print()


# One pass through the file to build up test bank data structure
#for x in f_input:
    #if question_re.match(x):
        #print(x.strip())
