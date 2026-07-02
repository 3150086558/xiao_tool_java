#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import urllib.request
import json

res = urllib.request.urlopen('http://127.0.0.1:8000/api/records')
data = json.loads(res.read().decode())
print(f'共 {len(data["records"])} 条记录:')
for r in data['records']:
    print(f'  {r["record_date"]} | {r["type"]:8} | {r["category"][:20]:20} | ¥{r["amount"]:>8.2f} | {r.get("sub_category","")}')

print('\n汇总信息:')
res2 = urllib.request.urlopen('http://127.0.0.1:8000/api/summary')
summary = json.loads(res2.read().decode())
print(f'  收入: ¥{summary["income"]:.2f}')
print(f'  支出: ¥{summary["expense"]:.2f}')
print(f'  结余: ¥{summary["balance"]:.2f}')
