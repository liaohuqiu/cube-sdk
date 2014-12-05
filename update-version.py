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


version_name = '1.0.42'

# replace version for pom
replace_file('core/pom.xml', '<version>', '</version>', version_name, 12, 17)

# replace version for gradle
replace_file('core/gradle.properties', 'VERSION_NAME=', '\n', version_name, 1, 2)

# update version for reade me
replace_file('README.md', '<version>', '</version>', version_name, 21, 28)
replace_file('README.md', '<version>', '</version>', version_name, 31, 38)
replace_file('README.md', 'cube-sdk', '@aar', version_name, 41, 45)

replace_file('README-cn.md', '<version>', '</version>', version_name, 18, 25)
replace_file('README-cn.md', '<version>', '</version>', version_name, 29, 35)
replace_file('README-cn.md', 'cube-sdk', '@aar', version_name, 41, 41)
