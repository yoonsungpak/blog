import type { Category } from "./domain/Categories";
import type { Board } from "./domain/Boards";

export function groupBoards(boards: Board[], categories: Category[]) {
    const byCategory = new Map<number | null, Board[]>();
    for (const board of boards) {
        const group = byCategory.get(board.categoryId) ?? [];
        group.push(board);
        byCategory.set(board.categoryId, group);
    }

    return [
        { category: null, boards: byCategory.get(null) ?? [] },
        ...categories.map((category) => ({ category, boards: byCategory.get(category.id) ?? [] })),
    ];
}
