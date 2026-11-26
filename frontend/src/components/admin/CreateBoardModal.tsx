"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type SubmitEvent } from "react";
import styles from "./BoardEditor.module.css";

export default function CreateBoardModal() {
    const router = useRouter();
    const [pending, setPending] = useState(false);
    const [error, setError] = useState("");

    async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();
        if (pending) return;

        const formData = new FormData(event.currentTarget);
        const name = String(formData.get("name") ?? "").trim();
        const url = String(formData.get("url") ?? "").trim().toLowerCase();
        const isPublic = formData.has("public");
        if (!name || !url) {
            setError("게시판 이름과 주소를 입력해 주세요.");
            return;
        }
        if (!/^[a-z-]+$/.test(url)) {
            setError("게시판 주소에는 영문 소문자와 하이픈(-)만 사용할 수 있습니다.");
            return;
        }

        setPending(true);
        setError("");
        try {
            const res = await fetch("/api/boards", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ name, url, isPublic }),
            });

            if (!res.ok) {
                const data = await res.json().catch(() => null);
                setError(data?.message ?? "게시판을 만들지 못했습니다.");
                return;
            }
            alert("게시판이 생성되었습니다.");
            router.replace("/admin/boards");
        } catch {
            setError("서버에 연결하지 못했습니다. 다시 시도해 주세요.");
        } finally {
            setPending(false);
        }
    }

    return (
        <form className={styles.backdrop} onSubmit={handleSubmit}>
            <section className={styles.editor} role="dialog" aria-modal="true" aria-labelledby="board-editor-title">
                <header className={styles.header}>
                    <h2 id="board-editor-title">
                        게시판 만들기
                    </h2>
                    <Link className="btn-close" href="/admin/boards" scroll={false} aria-label="닫기" />
                </header>
                <div className="d-grid gap-3">
                    <div>
                        <label className="form-label" htmlFor="board-name">이름</label>
                        <input className="form-control rounded-3" id="board-name" name="name" required />
                    </div>
                    <div>
                        <label className="form-label" htmlFor="board-slug">주소</label>
                        <div className="input-group">
                            <span className="input-group-text">/</span>
                            <input className="form-control" id="board-slug" name="url" required />
                        </div>
                    </div>
                    <div className="form-check form-switch">
                        <input className="form-check-input" id="board-public" name="public"
                            type="checkbox" role="switch" defaultChecked />
                        <label className="form-check-label" htmlFor="board-public">공개</label>
                    </div>
                </div>
                {error && <p className="small text-danger mt-3 mb-0" role="alert">{error}</p>}
                <footer className={`${styles.actions} mt-4`}>
                    <Link className="btn btn-outline-secondary rounded-3" href="/admin/boards" scroll={false}>취소</Link>
                    <button className={`btn rounded-3 ${styles.primary}`} type="submit" disabled={pending}>
                        저장
                        <span className={`${styles.icon} ${styles.floppy}`} aria-hidden="true" />
                    </button>
                </footer>
            </section>
        </form>
    );
}
