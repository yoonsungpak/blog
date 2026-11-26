"use client"

import Link from "next/link";
import type { Board } from "@/lib/board/domain/Boards";
import styles from "./BoardEditor.module.css";

export default function EditBoardModal({ board }: { board: Board }) {
    async function deleteBoard() {
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

    return (
        <form className={styles.backdrop} method="post">
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
                    <div className="form-check form-switch">
                        <input className="form-check-input" id="board-public" name="public"
                            type="checkbox" role="switch" defaultChecked={board.isPublic} />
                        <label className="form-check-label" htmlFor="board-public">공개</label>
                    </div>
                </div>
                <p className="small text-body-secondary mt-4 mb-3">게시판 변경을 저장하는 기능은 아직 연결되지 않았습니다.</p>
                <footer className={styles.actions}>
                    <button className="btn btn-outline-danger rounded-3 me-auto" type="button" onClick={deleteBoard}>
                        삭제
                        <span className={`${styles.icon} ${styles.trash}`} aria-hidden="true" />
                    </button>
                    <Link className="btn btn-outline-secondary rounded-3" href="/admin/boards" scroll={false}>취소</Link>
                    <button className={`btn rounded-3 ${styles.primary}`} type="submit" disabled>
                        저장
                        <span className={`${styles.icon} ${styles.floppy}`} aria-hidden="true" />
                    </button>
                </footer>
            </section>
        </form>
    );
}
