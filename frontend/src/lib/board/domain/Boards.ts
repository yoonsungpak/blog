import "server-only";
import { integer, sqliteTable, text } from "drizzle-orm/sqlite-core";

export const boards = sqliteTable("boards", {
    id: integer("id").primaryKey(),
    name: text("name"),
    url: text("url"),
    isPublic: integer("is_public", { mode: "boolean" }).notNull(),
    displayOrder: integer("display_order").notNull().default(0),
});

export type Board = typeof boards.$inferSelect;
