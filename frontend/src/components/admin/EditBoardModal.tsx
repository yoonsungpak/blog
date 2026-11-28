"use client"

import Link from "next/link";
import type { Board } from "@/lib/board/domain/Boards";
import type { Category } from "@/lib/board/domain/Categories";
import CategoryField from "./CategoryField";
import styles from "./BoardEditor.module.css";
import { type SubmitEvent } from "react";
import { useRouter } from "next/navigation";

export default function EditBoardModal({ board, categories }: { board: Board; categories: Category[] }) {
    const router = useRouter();

    async function deleteBoard() {
        if (!confirm("정말로 게시판을 삭제하겠습니까? 게시글도 함께 삭제되며 복구가 불가능합니다."))
            return

        try {
            const res = await fetch(`/api/boards/${board.id}`, { method: "DELETE" });
            if (!res.ok) {
                const data = await res.json();
                return alert(data.message);
            }
        } catch {
            return alert("게시판을 삭제하지 못했습니다.");
        }

        alert("게시판을 삭제했습니다.");
        return location.reload();
    }

    async function editBoard(e: SubmitEvent<HTMLFormElement>) {
        e.preventDefault();

        const formData = new FormData(e.currentTarget);
        const name = String(formData.get("name") ?? "");
        const url = String(formData.get("slug") ?? "");
        const isPublic = formData.has("public");
        const categoryId = Number(formData.get("categoryId")) || null;

        if (!name)
            return alert("게시판 이름을 입력해주세요.");

        if (!url)
            return alert("게시판 주소를 입력해주세요.");

        const changes = {
            ...(name !== (board.name ?? "") && { name : name }),
            ...(url !== (board.url ?? "") && {url : url}),
            ...(isPublic !== board.isPublic && {isPublic : isPublic}),
            ...(categoryId !== board.categoryId && { category: { id: categoryId } }),
        };

        if (Object.keys(changes).length === 0) {
            return alert("변경 사항이 없습니다.");
        }

        try {
            const res = await fetch(`/api/boards/${board.id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(changes),
            });

            if (!res.ok) {
                const data = await res.json();
                return alert(data.message);
            }
        } catch {
            return alert("게시판 수정 실패");
        }

        alert("게시판이 수정되었습니다.");
        router.replace("/admin/boards");
        router.refresh();
    }

    return (
        <form className={styles.backdrop} onSubmit={editBoard}>
            <section className={styles.editor} role="dialog" aria-modal="true" aria-labelledby="board-editor-title">
                <header className={styles.header}>
                    <h2 id="board-editor-title">
                        게시판 수정
                    </h2>
                    <Link className="btn-close" href="/admin/boards" scroll={false} aria-label="닫기" />
                </header>
                <div className="d-grid gap-3">
                    <div>
                        <label className="form-label" htmlFor="board-name">이름</label>
                        <input className="form-control rounded-3" id="board-name" name="name"
                            defaultValue={board.name ?? ""} />
                    </div>
                    <div>
                        <label className="form-label" htmlFor="board-slug">주소</label>
                        <div className="input-group">
                            <span className="input-group-text">/</span>
                            <input className="form-control" id="board-slug" name="slug"
                                defaultValue={board.url ?? ""} />
                        </div>
                    </div>
                    <CategoryField categories={categories} categoryId={board.categoryId} />
                    <div className="form-check form-switch">
                        <input className="form-check-input" id="board-public" name="public"
                            type="checkbox" role="switch" defaultChecked={board.isPublic} />
                        <label className="form-check-label" htmlFor="board-public">공개</label>
                    </div>
                </div>
                <footer className={styles.actions}>
                    <button className="btn btn-outline-danger rounded-3 me-auto" type="button" onClick={deleteBoard}>
                        삭제
                        <span className={`${styles.icon} ${styles.trash}`} aria-hidden="true" />
                    </button>
                    <Link className="btn btn-outline-secondary rounded-3" href="/admin/boards" scroll={false}>취소</Link>
                    <button className={`btn rounded-3 ${styles.primary}`} type="submit">
                        저장
                        <span className={`${styles.icon} ${styles.floppy}`} aria-hidden="true" />
                    </button>
                </footer>
            </section>
        </form>
    );
}
