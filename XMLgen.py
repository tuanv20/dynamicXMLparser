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
Events = ["Misalignment", "Peak TLM", "Outage", "Overlap"]

#Function that generates a random Contact Time Range within the last week
#and a maximum duration of 3 hours 
def randomContactTimeRange():
    SECS_IN_WEEK = 604800 
    CONTACT_LENGTH_SECS = 10800 #3 hour maximum contact duration in seconds 
    CURR_TIME_SECS = int(time.time())
    contact_start_time = CURR_TIME_SECS - random.randint(CONTACT_LENGTH_SECS, SECS_IN_WEEK)
    contact_end_time = random.randint(contact_start_time, contact_start_time + CONTACT_LENGTH_SECS)
    start_str =  datetime.fromtimestamp(contact_start_time).strftime('%Y-%m-%d %H:%M:%S')
    end_str = datetime.fromtimestamp(contact_end_time).strftime('%Y-%m-%d %H:%M:%S')
    return (start_str, end_str)


#Function that generates random datapoints with ascending time values 
#within the bounds of the contact 
def genRandomDataPoints(AOS, LOS):
    dataPoints = []
    events = []
    #Converts AOS and LOS string to epochs
    start_time = int(datetime.strptime(AOS, '%Y-%m-%d %H:%M:%S').timestamp())
    event_time = start_time
    end_time = int(datetime.strptime(LOS, '%Y-%m-%d %H:%M:%S').timestamp())
    tlm_fr = 0
    #3 to 6 events generated randomly
    #Generate a random time between start and end and then increment start to that value 
    numData = random.choice(range(3, 6))
    for data in range(numData):
        rand_event_time = random.randint(event_time, end_time)
        rand_data_time = random.randint(start_time, end_time)
        rand_data_str = datetime.fromtimestamp(rand_data_time).strftime('%Y-%m-%d %H:%M:%S')
        rand_event_str = datetime.fromtimestamp(rand_event_time).strftime('%Y-%m-%d %H:%M:%S')
        dataPoints.append(
            {
            "TIME": rand_data_str,
            "DELTA_AZ" : "0.3",
            "DELTA_EL" : "10.4",
            "TLM_FR" : str(tlm_fr),
            "CMD" : str(random.choice(range(10)))
            },
        )

        events.append(
            (random.choice(Events), rand_event_str)
        )
        
        start_time = rand_data_time
        event_time = rand_event_time
        tlm_fr = random.randint(tlm_fr, 5000)
    return (dataPoints, events)
    
#Generates an XML file for a contact using randomly generated data
#into the contacts directory uses XML ElementTree library 
def generateContact():
    AOS, LOS = randomContactTimeRange()
    DATA, EVENTS = genRandomDataPoints(AOS, LOS)
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
        "DATA" : DATA,
        "EVENTS" : EVENTS
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

    #Formats XML so that it isn't a single line 
    ET.indent(tree, space="  ", level=0)
    tree.write('contacts/' + 'Contact_' + contactDict["contact_id"] + '.xml')

#Option to generate multiple contacts
if '-n' in sys.argv:
    for x in range(int(sys.argv[2])):
        generateContact()

else:
    generateContact() 

