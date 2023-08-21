#! /usr/bin/python
import os;
import sys;

if "-a" in sys.argv:
    for file in os.listdir("./contacts"):
        filePath = "contacts/" + file
        os.system("touch -m " + filePath)
        os.system("mv " + filePath + " contact-gen/" + file)

if "-rm" in sys.argv:
    for file in os.listdir("./contact-gen"):
        filePath = "contact-gen/" + file
        os.system("mv " + filePath + " contacts/" + file)

if"-tgen" in sys.argv:
    for file in os.listdir("./contact-gen"):
        os.system("touch -m contact-gen/" + file)

if "-t" in sys.argv:
    if sys.argv[2] == "all":
        for file in os.listdir("./contacts"):
            filePath = "contacts/" + file
            os.system("touch -m " + filePath)
    else:
        os.system("touch -m contacts/" + sys.argv[2])

if "-m" in sys.argv:
    os.system("mv contacts/" + sys.argv[2] + " contact-gen/" + sys.argv[2])

if "-clear" in sys.argv:
    for file in os.listdir("./contacts"):
        filePath = "contacts/" + file
        os.system("rm " + filePath)

