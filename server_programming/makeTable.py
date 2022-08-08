import psycopg2

con = psycopg2.connect(database="postgres", user="postgres", password="sselab0812!", host="localhost", port = "5432")

cur = con.cursor()
cur.execute('''CREATE TABLE account (
        num INT PRIMARY KEY NOT NULL,
        user_id VARCHAR(50) NOT NULL,
        user_name VARCHAR(30) NOT NULL,
        birth VARCHAR(40),
        sex VARCHAR(10),
        device_id VARCHAR(60),
        token_key VARCHAR(60),
        timestamp VARCHAR(50),
        underlying_disease VARCHAR(50),
        drinking VARCHAR(20),
        smoking VARCHAR(20)
        );''')

con.commit()
con.close()