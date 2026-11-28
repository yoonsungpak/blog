"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type SubmitEvent } from "react";
import type { Category } from "@/lib/board/domain/Categories";
import styles from "./BoardEditor.module.css";

export default function CategoryModal({ category }: { category?: Category }) {
    const router = useRouter();
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const endpoint = category ? `/api/categories/${category.id}` : "/api/categories";

    async function submit(method: "POST" | "PUT" | "DELETE", body?: { name: string }) {
        if (saving) return;
        setSaving(true);
        setError("");
        try {
            const response = await fetch(endpoint, {
                method,
                ...(body && { headers: { "Content-Type": "application/json" }, body: JSON.stringify(body) }),
            });
            if (!response.ok) {
                const data = await response.json().catch(() => null);
                setError(data?.message ?? "카테고리 변경을 저장하지 못했습니다.");
                setSaving(false);
                return;
            }
            router.replace("/admin/boards", { scroll: false });
            router.refresh();
        } catch {
            setError("서버에 연결하지 못했습니다. 다시 시도해 주세요.");
            setSaving(false);
        }
    }

    function save(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const name = String(form.get("name") ?? "").trim();
        if (!name) {
            setError("카테고리 이름을 입력해 주세요.");
            return;
        }
        void submit(category ? "PUT" : "POST", { name });
    }

    return (
        <form className={styles.backdrop} onSubmit={save}>
            <section className={styles.editor} role="dialog" aria-modal="true" aria-labelledby="category-editor-title">
                <header className={styles.header}>
                    <h2 id="category-editor-title">{category ? "카테고리 수정" : "카테고리 만들기"}</h2>
                    <Link className="btn-close" href="/admin/boards" scroll={false} aria-label="닫기" />
                </header>
                <div className="d-grid gap-3">
                    <div>
                        <label className="form-label" htmlFor="category-name">이름</label>
                        <input className="form-control rounded-3" id="category-name" name="name"
                            defaultValue={category?.name} maxLength={255} required />
                    </div>
                </div>
                {error && <p className="small text-danger mt-3 mb-0" role="alert">{error}</p>}
                <footer className={`${styles.actions} mt-4`}>
                    {category && (
                        <button className="btn btn-outline-danger rounded-3 me-auto" type="button" disabled={saving}
                            onClick={() => {
                                if (confirm("카테고리를 삭제할까요? 소속 게시판은 미분류로 이동합니다.")) void submit("DELETE");
                            }}>
                            삭제
                        </button>
                    )}
                    <Link className="btn btn-outline-secondary rounded-3" href="/admin/boards" scroll={false}>취소</Link>
                    <button className={`btn rounded-3 ${styles.primary}`} type="submit" disabled={saving}>
                        {saving ? "처리 중…" : "저장"}
                    </button>
                </footer>
            </section>
        </form>
    );
}
