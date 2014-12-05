# replace version for pom
# replace version for gradle
# update version for reade me
# run gradle task to publish

import re

def replace_file(fname, start, end, replace, line_from = 1, line_to = 0):

    with open(fname) as f:
            content = f.readlines()
    pre = content[0:line_from-1]
    if line_to == 0:
        mid = content
        post = []
    else:
        mid = content[line_from-1:line_to]
        post = content[line_to:]
    pre = ''.join(pre)
    mid = ''.join(mid)
    post = ''.join(post)
    mid = replace_between(mid, start, end, replace)
    content = pre + mid  + post
    with open(fname, "w") as f:
        f.write(content)

def replace_between(str, start, end, replace):
    pos1 = str.find(start);
    pos2 = str.find(end);
    return str[0:pos1 + len(start)] + replace + str[pos2:]

replace_file('README.md', '<version>', '</version>', 'xxxxxx', 22, 27)
replace_file('core/gradle.properties', 'VERSION_NAME=', ' // VERSION_NAME', 'xxxxxx', 1, 2)
