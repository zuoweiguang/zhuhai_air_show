# -*- coding: utf-8 -*-
"""
Created on Thu Feb 25 09:39:19 2016

@author: SongHuiXing
"""

import os
import simplejson
import traceback

import baseextracter as bse
from rdlink import RDLink
from custom_encoder import CustomJsonEncoder as jsEncoder

class LinkExtracter(bse.BaseExtracter):
    def __init__(self, orcl_conn):
        bse.BaseExtracter.__init__(self, orcl_conn)
        
    def getrange(self):
        cursor = self.connection.cursor()

        cursor.execute("select MIN(LINK_PID), MAX(LINK_PID) from RD_LINK")

        res = cursor.fetchone()

        cursor.close()

        del cursor

        return res
        
    def extract(self, folder, minid=0, maxid=0):
        fp = open(os.path.join(folder,'Link_{0}_{1}'.format(minid,maxid)), 'w')
        
        try:
            cur = self.connection.cursor()

            sql = "select {fields} from {tblname} where {fields} >= :mini and {fields} < :maxi".format(
                            fields='LINK_PID',
                            tblname='RD_LINK')
                            
            cur.execute(sql, mini=minid, maxi=maxid)

            innercur = self.connection.cursor()

            for pid, in cur:
                link = RDLink(pid)
                link.build(innercur)

                simplejson.dump(link, fp, cls=jsEncoder)
                fp.write("\n")
                
                del link

            innercur.close()
            del innercur

            cur.close()
            del cur
        except:
            traceback.print_exc()
        else:
            print "Dump completed."
        
        fp.flush()
        fp.close()