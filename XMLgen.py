#! /usr/bin/python
from datetime import datetime 
import xml.etree.ElementTree as ET
import random 
import string 
import sys
import math
import time


ProjectKeys = ["JIR", "TEST1"]
Antennas = ["DGS-A", "DGS-B", "DGS-C", "DGS-D"]

def randomContactTimeRange():
    SECS_IN_WEEK = 604800
    CONTACT_LENGTH_SECS = 10800
    CURR_TIME_SECS = int(time.time())
    contact_start_time = CURR_TIME_SECS - random.randint(CONTACT_LENGTH_SECS, SECS_IN_WEEK)
    contact_end_time = random.randint(contact_start_time, contact_start_time + CONTACT_LENGTH_SECS)
    start_str =  datetime.fromtimestamp(contact_start_time).strftime('%Y-%m-%d %H:%M:%S')
    end_str = datetime.fromtimestamp(contact_end_time).strftime('%Y-%m-%d %H:%M:%S')
    return (start_str, end_str)

def genRandomDataPoints(AOS, LOS):
    dataPoints = []
    start_time = int(datetime.strptime(AOS, '%Y-%m-%d %H:%M:%S').timestamp())
    end_time = int(datetime.strptime(LOS, '%Y-%m-%d %H:%M:%S').timestamp())
    numData = random.choice(range(3, 8))
    for data in range(numData):
        rand_data_time = random.randint(start_time, end_time)
        rand_data_str = datetime.fromtimestamp(rand_data_time).strftime('%Y-%m-%d %H:%M:%S')
        dataPoints.append(
            {
            "TIME": rand_data_str,
            "DELTA_AZ" : "0.3",
            "DELTA_EL" : "10.4",
            "TLM_FR" : str(random.choice(range(5000))),
            "CMD" : str(random.choice(range(10)))
            },
        )
        start_time = rand_data_time
    return (dataPoints)
    

def generateContact():
    AOS, LOS = randomContactTimeRange()
    contactDict = {
        "proj_key": random.choice(ProjectKeys),
        "contact_id": ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(8)),
        "FIRST_CLASS" : [
            ("PN-H", str(random.choice(range(8)))),
            ("MP", "TACO"),
            ("ANTENNA", random.choice(Antennas)),
            ("AOS", AOS),
            ("LOS", LOS),
        ],
        "PARAMS" : [
            ("H-EQUIP", "KS252-6 KS252-7 WS2_6901"),
            ("H-CONFIG", "D"),
            ("L-CONFIG", "L")
        ],
        "DATA" : genRandomDataPoints(AOS, LOS),
        "EVENTS" : [
            ("Acquisition of Service", AOS),
            ("Loss of Service", LOS)
        ]
    }

    root = ET.Element('CONTACT')
    root.attrib = {"project_id" : contactDict["proj_key"], "contact_id" : contactDict["contact_id"]}
    tree = ET.ElementTree(root)

    for fc in contactDict["FIRST_CLASS"]:
        fcElem = ET.SubElement(root, fc[0])
        fcElem.text = fc[1]
    
    Params = ET.SubElement(root, 'PARAMS')
    for par in contactDict["PARAMS"]:
        param = ET.SubElement(Params, 'PARAM')
        param_name = ET.SubElement(param, 'NAME')
        param_val = ET.SubElement(param, 'VALUE')
        param_name.text = par[0]
        param_val.text = par[1]

    Data = ET.SubElement(root, 'DATA')
    for data in contactDict["DATA"]:
        dp = ET.SubElement(Data, 'DATAPOINT')
        for key, val in data.items():
            dataElem = ET.SubElement(dp, key)
            dataElem.text = val

    Events = ET.SubElement(root, 'EVENTS')
    for event in contactDict["EVENTS"]:
        eventElem = ET.SubElement(Events, 'EVENT')
        eventElem_name = ET.SubElement(eventElem, 'NAME')
        eventElem_time = ET.SubElement(eventElem, 'TIME')
        eventElem_name.text = event[0]
        eventElem_time.text = event[1]

        
    ET.indent(tree, space="  ", level=0)
    tree.write('contacts/' + 'Contact_' + contactDict["contact_id"] + '.xml')

if '-n' in sys.argv:
    for x in range(int(sys.argv[2])):
        generateContact()

else:
    generateContact() 

