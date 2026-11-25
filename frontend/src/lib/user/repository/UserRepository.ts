import { getDb } from "../../db/getDb";
import { type User, users } from "../domain/Users";
import { and, asc, eq, isNull } from "drizzle-orm";

export default class UserRepository {
    public async findAll(): Promise<User[]> {
        return getDb().select().from(users).orderBy(asc(users.username), asc(users.id)).all();
    }

    public async findById(id: string): Promise<User | undefined> {
        return getDb().select().from(users)
            .where(and(eq(users.id, id), isNull(users.deletedAt)))
            .get();
    }

    public async findByUsername(username: string): Promise<User | undefined> {
        return getDb().select().from(users)
            .where(and(eq(users.username, username), isNull(users.deletedAt)))
            .get();
    }
}
