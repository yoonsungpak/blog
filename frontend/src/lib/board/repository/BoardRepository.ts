import { and, asc, eq } from "drizzle-orm";
import { cookies } from "next/headers";
import { connection } from "next/server";
import { cache } from "react";
import UserService from "@/lib/user/service/UserService";
import { getDb } from "../../db/getDb";
import { type Board, boards } from "../domain/Boards";

const findAll = cache(async () => {
    await connection();
    return getDb().select().from(boards).orderBy(asc(boards.id)).all();
});

const canViewPrivateBoards = cache(async () => {
    const sid = (await cookies()).get("sid")?.value;
    return sid ? new UserService().isAdmin(sid) : false;
});

const findVisible = cache(async () => {
    if (await canViewPrivateBoards()) {
        return findAll();
    }

    return getDb().select().from(boards)
        .where(eq(boards.isPublic, true))
        .orderBy(asc(boards.id))
        .all();
});

const findFirstVisible = cache(async () => {
    const includePrivate = await canViewPrivateBoards();
    const db = getDb();
    // Sample posts belong to the first board, even when it is private.
    const firstBoardId = db.select({ id: boards.id }).from(boards).orderBy(asc(boards.id)).limit(1);
    return db.select().from(boards)
        .where(and(eq(boards.id, firstBoardId), includePrivate ? undefined : eq(boards.isPublic, true)))
        .get();
});

export default class BoardRepository {
    public async findAll(): Promise<Board[]> {
        return findAll();
    }

    public async findVisible(): Promise<Board[]> {
        return findVisible();
    }

    public async findFirstVisible(): Promise<Board | undefined> {
        return findFirstVisible();
    }
}
