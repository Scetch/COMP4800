import json

class DBJson(object):
    def __init__(self, location):
        self.location = location
        with open(location, "r") as dbFile:
            self.db = json.load(dbFile)
        self.tables = [list(tables.keys())[0] for tables in self.db]
    
    def getTable(self, table):
        return self.db[self.tables.index(table)]
    
    def getRow(self, tableName, key, value):
        table = self.getTable(tableName)[tableName]
        for obj in table:
            if key in obj:
                if obj[key] == value:
                    return obj
        
        return {}
    
    def getAllRows(self, tableName, key, value):
        table = self.getTable(tableName)[tableName]
        rows = []
        for obj in table:
            if key in obj:
                if obj[key] == value:
                    rows.append(obj)
        
        return rows
    
    def dumpdb(self):
        json.dump(self.db, open(self.location, "w+"))

# db1 = DBJson('db.json')
# user = db1.getRow('cookies', 'username', 'dhillon') 
# print(user)
# user['sessionCookie'] = "c1"
# user = db1.getRow('cookies', 'username', 'dhillon') 
# print(user)
# db1.dumpdb()
