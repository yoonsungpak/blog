import { and, asc, eq, sql } from "drizzle-orm";
import { cookies } from "next/headers";
import { connection } from "next/server";
import { cache } from "react";
import UserService from "@/lib/user/service/UserService";
import { getDb } from "../../db/getDb";
import { type Board, boards } from "../domain/Boards";
import { categories } from "../domain/Categories";

const displayOrder = sql`nullif(${boards.displayOrder}, 0) asc nulls last`;
const findCategories = cache(async () => {
    await connection();
    return getDb().select().from(categories)
        .orderBy(sql`nullif(${categories.displayOrder}, 0) asc nulls last`, asc(categories.id))
        .all();
});

const findAll = cache(async () => {
    await connection();
    return getDb().select().from(boards).orderBy(displayOrder, asc(boards.id)).all();
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
        .orderBy(displayOrder, asc(boards.id))
        .all();
});

const findFirstVisible = cache(async () => {
    const includePrivate = await canViewPrivateBoards();
    const db = getDb();
    // Keep sample posts attached to the earliest-created board when the menu is reordered.
    const firstBoardId = db.select({ id: boards.id }).from(boards).orderBy(asc(boards.id)).limit(1);
    return db.select().from(boards)
        .where(and(eq(boards.id, firstBoardId), includePrivate ? undefined : eq(boards.isPublic, true)))
        .get();
});

export default class BoardRepository {
    public findCategories() {
        return findCategories();
    }

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
