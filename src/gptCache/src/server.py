from fastapi import FastAPI
from pydantic import BaseModel
from starlette.responses import JSONResponse

from sentence_transformers import SentenceTransformer

import sqlite3
import sqlite_vss

from utils import get_logger

# model setup
model = SentenceTransformer("distiluse-base-multilingual-cased-v1")
distance_threshold = 0.1

# logger setup
logger = get_logger("server_log", "server.log")

def get_query_embedding(query: str):
    return model.encode([query])[0]

# fastapi setup
app = FastAPI()

# sqlite setup
def get_db_cursor():
    db = sqlite3.connect("gptcache.db")
    db.enable_load_extension(True)
    sqlite_vss.load(db)

    return db, db.cursor()


# request/response format
class Entry(BaseModel):
    query: str
    answer: str


@app.on_event("startup")
async def startup():
    logger.info("server start")

@app.on_event("shutdown")
async def shutdown():
    logger.info("server down")


@app.get("/")
def lookup_entries(query: str = ''):
    logger.info(f"query: {query}")
    _, cur = get_db_cursor()

    def get_nearnest_cache_row_id(query: str):
        query_embedding = get_query_embedding(query)

        nearest_cache = cur.execute(
        """
            select rowid
            from vss_caches
            where vss_search(
                query_embedding,
                vss_search_params (?, 5) 
            ) and distance < ?
        """, [query_embedding.tobytes(), distance_threshold]
        ).fetchone()

        return nearest_cache[0] if nearest_cache is not None else None


    nearnest_cache_row_id = get_nearnest_cache_row_id(query)

    if nearnest_cache_row_id is not None:
        query, answer = cur.execute(
        """
            select query, answer
            from caches
            where rowid = ?
        """, [nearnest_cache_row_id]
        ).fetchone()

        res = {
            "valid": True,
            "query": query,
            "answer": answer
        }
    else:
        res = {
            "valid": False
        }

    return JSONResponse(res)


@app.post("/")
def append_new_entry(entry: Entry):
    db, cur = get_db_cursor()
    logger.info(f"query: {entry.query}, answer: {entry.answer}")
    cur.execute("insert into caches values (?, ?, ?)", [entry.query, get_query_embedding(entry.query).tobytes(), entry.answer])
    cur.execute("drop table if exists vss_caches")
    cur.execute("create virtual table if not exists vss_caches using vss0(query_embedding(512))")
    cur.execute("insert into vss_caches(rowid, query_embedding) select rowid, query_embedding from caches")
    db.commit()

    return {"success": True}