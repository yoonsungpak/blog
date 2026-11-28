import type { Category } from "@/lib/board/domain/Categories";

export default function CategoryField({ categories, categoryId }: {
    categories: Category[];
    categoryId?: number | null;
}) {
    return (
        <div>
            <label className="form-label" htmlFor="board-category">카테고리</label>
            <select className="form-select rounded-3" id="board-category" name="categoryId" defaultValue={categoryId ?? ""}>
                <option value="">미분류</option>
                {categories.map(({ id, name }) => <option key={id} value={id}>{name}</option>)}
            </select>
        </div>
    );
}
