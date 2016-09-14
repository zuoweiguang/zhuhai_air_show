#!/usr/bin/python
# coding=utf-8

import os
import sys
import multiprocessing
import datetime
import collections
import logging
import logging.handlers
import cx_Oracle as cxOrcl
import extracter.cross_extracter as crossex
import extracter.link_extracter as linkex

extypecoll = collections.namedtuple('ExportType',('Link', 'Cross', 'All'))
ExportType = extypecoll(0, 1, 2)

class Extracter:
    """
    Extract the Geolive information to JSON
    """
    
    PIDSPAN = 100000

    def __init__(self, tnsname, user, pwd):
        self.tnsentry = tnsname
        self.username = user
        self.password = pwd

    def connect(self):
        self.__connection = cxOrcl.connect(self.username, self.password, self.tnsentry, threaded = True)

    def disconnect(self):
        if self.__connection is None:
            return

        self.__connection.close()
        
    def get_minmax_id(self, table='Cross'):
        if table == 'Cross':
            extracter = crossex.CrossExtracter(self.__connection)
        else:
            extracter = linkex.LinkExtracter(self.__connection)
        
        return extracter.getrange()
 
    def extract_cross(self, targetfolder, minid, maxid):
        logger = logging.getLogger('Export{0}'.format(minid))
        logger.info('Begin to export cross')
        
        cross_extracter = crossex.CrossExtracter(self.__connection)
        cross_extracter.extract(targetfolder, minid, maxid)
        
        logger.info('End to export cross')
            
    def extract_link(self, targetfolder, minid, maxid):
        logger = logging.getLogger('Export{0}'.format(minid))
        logger.info('Begin to export link')
        
        link_extracter = linkex.LinkExtracter(self.__connection)
        link_extracter.extract(targetfolder, minid, maxid)
        
        logger.info('End to export link')

    def extractinonethread(self, targetfolder):
        exter = linkex.LinkExtracter(self.__connection)
        
        exter.extract(targetfolder,13200039,13200040)
        

def extract_func(output, dbprop, minid, maxid, table):
    print "Excute export in a process"
    
    try:
        logfile = os.path.join(output, "export{0}.log".format(minid))
        handler = logging.handlers.RotatingFileHandler(logfile, maxBytes = 1024*1024, backupCount = 5)
        fmt = '%(asctime)s - %(filename)s:%(lineno)s - %(name)s - %(message)s'  
        formatter = logging.Formatter(fmt) 
        handler.setFormatter(formatter)
    
        logger = logging.getLogger('Export{0}'.format(minid))  
        logger.addHandler(handler)
        logger.setLevel(logging.DEBUG)
    except Exception, e:
        print e.message

    ins, user, pwd = dbprop
    logger.info("Connect to {0}, user:{1}, password:{2}".format(ins, user, pwd))
    ex = Extracter(ins, user, pwd)
    ex.connect()
    
    print "begin export".format(minid)
    try:
        if table == ExportType.Cross:
            ex.extract_cross(output, minid, maxid)
        else:
            ex.extract_link(output, minid, maxid)
    except Exception, e:
        logger.error(e.message)
        print e.message
    else:
        logger.info('Export Success!')
        
    ex.disconnect()
    
def open_proc_export(output, pool, dbprop, idrange, table):
    lowerbound = idrange[0]
    while lowerbound < idrange[1]:
        upperbound = lowerbound + Extracter.PIDSPAN

        step_maxid = idrange[1]+1 if upperbound >= idrange[1] else upperbound
        
        pool.apply_async(extract_func, (output, dbprop, lowerbound, step_maxid, table))
        
        lowerbound = upperbound

def do_extract(output, orains, orauser, orapwd, targettype):
    crossmin_max = (0, 0)
    linkmin_max = (0, 0)

    ex = Extracter(orains, orauser, orapwd)
    ex.connect()

    try:
        crossmin_max = ex.get_minmax_id()
        linkmin_max = ex.get_minmax_id('Link')
    except:
        pass
    finally:
        ex.disconnect()

    pool = multiprocessing.Pool(processes=8)

    oraprop = (orains, orauser, orapwd)
    if targettype == ExportType.All:
        open_proc_export(output, pool, oraprop, linkmin_max, ExportType.Link)
        open_proc_export(output, pool, oraprop, crossmin_max, ExportType.Cross)
    elif targettype == ExportType.Cross:
        open_proc_export(output, pool, oraprop, crossmin_max, ExportType.Cross)
    else:
        open_proc_export(output, pool, oraprop, linkmin_max, ExportType.Link)

    pool.close()
    pool.join()

    print 'All Process Done.'
    

if "__main__" == __name__:
    
    output_path = ""
    dt = datetime.date.today()
    todayfolder = "{:%Y%m%d}".format(dt)
    
    if len(sys.argv) > 1:
        output_path = os.path.join(sys.argv[1], todayfolder)
    else:
        currentpath = os.path.dirname(sys.argv[0])
        parpath = os.path.abspath(os.path.join(currentpath, os.pardir))
        output_path = os.path.join(parpath, "data\\{0}".format(todayfolder))
        
    if not os.path.exists(output_path):
        os.mkdir(output_path)
        
    orains = sys.argv[2]    #ORCL_151
    orauser = sys.argv[3]   #IDBG_15SUM_0705_BJ
    orapwd = sys.argv[4]    #2

    targettype = ExportType.All
    if len(sys.argv) > 5:
        tname = sys.argv[5].lower()
        if tname == 'cross':
            targettype = ExportType.Cross
        elif tname == 'link':
            targettype = ExportType.Link
    
    do_extract(output_path, orains, orauser, orapwd, targettype)

    #extract_func(output_path, (orains, orauser, orapwd), 7096747, 7318083, targettype)
    
    

