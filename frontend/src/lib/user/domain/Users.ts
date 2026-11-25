import "server-only";
import { integer, sqliteTable, text } from "drizzle-orm/sqlite-core";

export const users = sqliteTable("users", {
    id: text("id").primaryKey().notNull(),
    username: text("username").notNull(),
    role: text("role", { enum: ["VISITOR", "WRITER", "ADMIN"] }).notNull(),
    createdAt: integer("created_at", { mode: "timestamp_ms" }).notNull(),
    deletedAt: integer("deleted_at", { mode: "timestamp_ms" }),
});

export type User = typeof users.$inferSelect;
