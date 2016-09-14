#!/usr/bin/python
# -*- coding: utf-8 -*-

import math


class nicefloat(float):

    @staticmethod
    def str(num):
        if not num or num+1 == num or num != num:
            return float.__repr__(num)
        f, e = math.frexp(num)
        if num < 0:
            f = -f
        f = int(f*2**53)
        e -= 53
        if e >= 0:
            be = 2**e
            if f != 2**52:
                r, s, mp, mm = f*be*2, 2, be, be
            else:
                be1 = be*2
                r, s, mp, mm = f*be1*2, 4, be1, be
        elif e == -1074 or f != 2**52:
            r, s, mp, mm = f*2, 2**(1-e), 1, 1
        else:
            r, s, mp, mm = f*4, 2**(2-e), 2, 1
        k = 0
        round = (f%2 == 0)
        while not round and r+mp*10 <= s or r+mp*10 < s:
            r *= 10; mp *= 10; mm *= 10; k -= 1
        while round and r+mp >= s or r+mp > s:
            s *= 10; k += 1
        l = []
        while True:
            d, r = divmod(r*10, s)
            d = int(d)
            mp *= 10
            mm *= 10
            tc1 = round and r == mm or r < mm
            tc2 = round and r+mp == s or r+mp > s
            if not tc1:
                if not tc2:
                    l.append(d)
                    continue
                l.append(d+1)
            elif not tc2 or r*2 < s:
                l.append(d)
            else:
                l.append(d+1)
            break
        if k <= 0:
            l.insert(0, "0" * abs(k))
            l.insert(0, "0.")
        elif k < len(l):
            l.insert(k, ".")
        else:
            l.append("0" * (k - len(l)))
            l.append(".0")
        if num < 0:
            l.insert(0, "-")
        return "".join([str(x) for x in l])

    def __repr__(self):
        return self.str(self)
    def __str__(self):
        return self.str(self)

    def __neg__(self):
        return nicefloat(float.__neg__(self))
    def __pos__(self):
        return nicefloat(float.__pos__(self))
    def __abs__(self):
        return nicefloat(float.__abs__(self))
    def __add__(self, other):
        return nicefloat(float.__add__(self, other))
    def __radd__(self, other):
        return nicefloat(float.__radd__(self, other))
    def __sub__(self, other):
        return nicefloat(float.__sub__(self, other))
    def __rsub__(self, other):
        return nicefloat(float.__rsub__(self, other))
    def __mul__(self, other):
        return nicefloat(float.__mul__(self, other))
    def __rmul__(self, other):
        return nicefloat(float.__rmul__(self, other))
    def __div__(self, other):
        return nicefloat(float.__div__(self, other))
    def __rdiv__(self, other):
        return nicefloat(float.__rdiv__(self, other))
    def __pow__(self, other):
        return nicefloat(float.__pow__(self, other))
    def __rpow__(self, other):
        return nicefloat(float.__rpow__(self, other))
    def __truediv__(self, other):
        return nicefloat(float.__truediv__(self, other))
    def __rtruediv__(self, other):
        return nicefloat(float.__rtruediv__(self, other))
    def __floordiv__(self, other):
        return nicefloat(float.__floordiv__(self, other))
    def __rfloordiv__(self, other):
        return nicefloat(float.__rfloordiv__(self, other))
    def __coerce__(self, other):
        return self, nicefloat(other)
    def __divmod__(self, other):
        div, mod = float.__divmod__(self, other)
        return nicefloat(div), nicefloat(mod)
    def __rdivmod__(self, other):
        div, mod = float.__divmod__(self, other)
        return nicefloat(div), nicefloat(mod)
