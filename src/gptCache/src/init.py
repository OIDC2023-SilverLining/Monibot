import sqlite3
import sqlite_vss

from sentence_transformers import SentenceTransformer

model = SentenceTransformer("distiluse-base-multilingual-cased-v1")

db = sqlite3.connect("gptcache.db")
db.enable_load_extension(True)
sqlite_vss.load(db)

cur = db.cursor()

cur.execute("create table if not exists caches (query text, query_embedding blob, answer)")
cur.execute("create virtual table if not exists vss_caches using vss0 (query_embedding(512))")
db.commit()