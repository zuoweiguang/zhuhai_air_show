#!/usr/bin/python
#coding=utf-8

__author__ = 'SongHuiXing'

from spatialutil import SpatialUtil as spatial


class LinkChain:
    STOP_FIND_ANGLE = 60
    STOP_FIND_COUNT = 4
    ONLY_GET_SEQUENCE = True    #只查找双线连接的link，不计算岔路后的link

    def __init__(self, pid, geo, direct, from_start, cursor):
        self.__begin_id = pid
        self.__begin_geo = geo
        self.__begin_dir = direct   #1--双， 2--进入， 3--退出
        self.__begin_fromS = from_start

        self.__dbCursor = cursor

        self.__final_angle = 0


    def get_chain(self):
        lastid = self.__begin_id
        lastgeo = self.__begin_geo
        laststart = self.__begin_fromS
        lastdir = self.__begin_dir

        chain = []

        while -1 != lastid and len(chain) < LinkChain.STOP_FIND_COUNT:
            if lastid in chain:
                break

            chain.append(lastid)

            lastid, lastdir, lastgeo, laststart = self.__get_next_link(lastid, lastgeo, lastdir, laststart)

        return chain

    def __get_next_link(self, lid, lgeo, dir, isstart):
        next_node = self.__get_next_nodeid(lid, isstart)

        sql = '''select LINK_PID, Direct, Geometry from RD_LINK
                 where {nodepidfield} = {pidvalue} and LINK_PID <> {lid}'''

        self.__dbCursor.execute(sql.format(nodepidfield='S_NODE_PID', pidvalue=next_node, lid=lid))
        slinks = {i:(d, g) for i, d, g in self.__dbCursor}

        self.__dbCursor.execute(sql.format(nodepidfield='E_NODE_PID', pidvalue=next_node, lid=lid))
        elinks = {i:(d, g) for i, d, g in self.__dbCursor}

        if (len(slinks) + len(elinks)) == 1:
            if len(slinks) == 1:
                k = slinks.keys()[0]
                info = slinks[k]
                return k, dir, info[1], True
            else:
                k = elinks.keys()[0]
                info = elinks[k]
                return k, dir, info[1], False

        if LinkChain.ONLY_GET_SEQUENCE:
            return -1, 1, None, isstart

        #按照通行方向过滤不符合条件的
        if dir == 1:                #双方向
            alllinks = [(k, dir, v[1], True) for k,v in slinks.iteritems() if v[0] == dir]
            alllinks.extend([(k, dir, v[1], False) for k,v in elinks.iteritems() if v[0] == dir])
        elif dir == 2:              #向路口内
            alllinks = [(k, dir, v[1], True) for k,v in slinks.iteritems() if v[0] == 3]
            alllinks.extend([(k, dir, v[1], False) for k,v in elinks.iteritems() if v[0] == 2])
        else:                       #向路口外
            alllinks = [(k, dir, v[1], True) for k,v in slinks.iteritems() if v[0] == 2]
            alllinks.extend([(k, dir, v[1], False) for k,v in elinks.iteritems() if v[0] == 3])

        if len(alllinks) == 0:
            return -1, 1, None, isstart

        linkangles = map(lambda p:self.__getlink_angle(p, lgeo, isstart), alllinks)

        linkangles.sort(lambda x,y: cmp(x[1], y[1]))

        pid, angle = linkangles[0]
        self.__final_angle += angle

        if self.__final_angle >= LinkChain.STOP_FIND_ANGLE:
            return -1, 1, None, isstart

        if pid in slinks:
            info = slinks[pid]
            return pid, dir, info[1], True
        else:
            info = elinks[pid]
            return pid, dir, info[1], False

    def __get_next_nodeid(self, linkpid, isstart):
        sql = "select {pidfield} from RD_LINK where LINK_PID = {pid}"

        if isstart:
            self.__dbCursor.execute(sql.format(pidfield='E_NODE_PID', pid=linkpid))
        else:
            self.__dbCursor.execute(sql.format(pidfield='S_NODE_PID', pid=linkpid))

        anotherPid, = self.__dbCursor.fetchone()

        return anotherPid

    def __getlink_angle(self, link_tuple, srclink, src_is_start):
        lid = link_tuple[0]
        lgeo = link_tuple[2]
        link_is_start = link_tuple[3]

        return lid, spatial.calc_angle(srclink, src_is_start, lgeo, link_is_start)