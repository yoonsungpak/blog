import "server-only";
import { resolve } from "node:path";
import Database from "better-sqlite3";
import { drizzle, type BetterSQLite3Database } from "drizzle-orm/better-sqlite3";
import { users } from "../user/domain/Users";

const schema = { users };
let db: BetterSQLite3Database<typeof schema> | undefined;

export function getDb() {
    if (!db) {
        const dbPath = resolve(/* turbopackIgnore: true */ process.cwd(), process.env.SQLITE_DB_PATH ?? "../database.db");
        db = drizzle(new Database(dbPath, { readonly: true }), { schema });
    }

    return db;
}
