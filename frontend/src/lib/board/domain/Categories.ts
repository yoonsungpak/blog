import "server-only";
import { integer, sqliteTable, text } from "drizzle-orm/sqlite-core";

export const categories = sqliteTable("categories", {
    id: integer("id").primaryKey(),
    name: text("name").notNull(),
    displayOrder: integer("display_order").notNull().default(0),
});

export type Category = typeof categories.$inferSelect;
