# -*- coding: utf-8 -*-
"""
Created on Thu Feb 25 09:29:02 2016

@author: SongHuiXing
"""

import os
import simplejson
import traceback

from cross import RDCross
import baseextracter as bse
from custom_encoder import CustomJsonEncoder as jsEncoder

class CrossExtracter(bse.BaseExtracter):
    def __init__(self, orcl_conn):
        bse.BaseExtracter.__init__(self, orcl_conn)
        
    def extract(self, folder, minid=0, maxid=0):
        fname = os.path.join(folder,'Cross{0}_{1}'.format(minid,maxid))
        fp = open(fname, 'w')
        
        try:
            cur = self.connection.cursor()

            cur.execute("select {fields} from {tblname} where {fields} >= :mini and {fields} < :maxi".format(
                            fields='PID',
                            tblname='RD_Cross'),
                        mini=minid,
                        maxi=maxid)

            innercur = self.connection.cursor()

            for pid, in cur:
                cross = RDCross(pid)

                cross.build(innercur)
                #cross.makebuffermatrix()

                simplejson.dump(cross, fp, cls=jsEncoder)
                fp.write("\n")
                
                del cross

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