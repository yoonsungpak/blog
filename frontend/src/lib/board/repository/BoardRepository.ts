import { asc } from "drizzle-orm";
import { connection } from "next/server";
import { cache } from "react";
import { getDb } from "../../db/getDb";
import { type Board, boards } from "../domain/Boards";

const findAll = cache(async () => {
    await connection();
    return getDb().select().from(boards).orderBy(asc(boards.id)).all();
});

export default class BoardRepository {
    public async findAll(): Promise<Board[]> {
        return findAll();
    }
}
