"use client";

import { useRouter } from "next/navigation";
import { useEffect, useRef, useState, type PointerEvent } from "react";
import type { Board } from "@/lib/board/domain/Boards";
import styles from "./BoardEditor.module.css";

export default function BoardOrderModal({ boards }: { boards: Board[] }) {
    const router = useRouter();
    const dialogRef = useRef<HTMLDialogElement>(null);
    const [order, setOrder] = useState(() => boards.map(({ id }) => id));
    const [dropTarget, setDropTarget] = useState<number | null>(null);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const dirty = order.some((id, index) => id !== boards[index].id);
    const boardsById = new Map(boards.map((board) => [board.id, board]));

    useEffect(() => {
        const dialog = dialogRef.current;
        dialog?.showModal();
        return () => dialog?.close();
    }, []);

    function moveBoard(from: number, to: number) {
        if (saving || from === to || to < 0 || to >= order.length) return;
        const next = [...order];
        const [id] = next.splice(from, 1);
        next.splice(to, 0, id);
        setOrder(next);
        setError("");
    }

    function getDropIndex(event: PointerEvent<HTMLSpanElement>) {
        if (!event.currentTarget.hasPointerCapture(event.pointerId)) return -1;
        const row = document.elementFromPoint(event.clientX, event.clientY)?.closest("li[data-board-id]");
        return order.indexOf(Number(row?.getAttribute("data-board-id")));
    }

    async function saveOrder() {
        setSaving(true);
        setError("");
        try {
            const response = await fetch("/api/boards/order", {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ boardIds: order }),
            });
            if (!response.ok) {
                const data = await response.json().catch(() => null);
                setError(data?.message ?? "게시판 순서를 저장하지 못했습니다.");
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

    return (
        <dialog ref={dialogRef} className={`${styles.editor} ${styles.orderDialog}`} aria-labelledby="board-order-title"
            onCancel={(event) => {
                event.preventDefault();
                if (!saving) router.replace("/admin/boards", { scroll: false });
            }}>
            <header className={styles.header}>
                <h2 id="board-order-title">게시판 순서 수정</h2>
                <button className="btn-close" type="button" aria-label="닫기" disabled={saving}
                    onClick={() => router.replace("/admin/boards", { scroll: false })} />
            </header>
            <p className="small text-body-secondary">손잡이를 드래그하거나 화살표로 순서를 바꿀 수 있습니다.</p>
            <ol className={styles.orderList}>
                {order.map((id, index) => {
                    const board = boardsById.get(id)!;
                    const label = board.name ?? board.url ?? `게시판 ${id}`;
                    return (
                        <li key={id} data-board-id={id} data-drop-target={dropTarget === index}>
                            <span
                                className={styles.orderHandle}
                                aria-hidden="true"
                                onPointerDown={(event) => {
                                    if (saving || !event.isPrimary || event.button !== 0) return;
                                    event.currentTarget.setPointerCapture(event.pointerId);
                                    setDropTarget(index);
                                }}
                                onPointerMove={(event) => {
                                    if (!event.currentTarget.hasPointerCapture(event.pointerId)) return;
                                    const target = getDropIndex(event);
                                    setDropTarget(target < 0 ? null : target);
                                }}
                                onPointerUp={(event) => moveBoard(index, getDropIndex(event))}
                                onLostPointerCapture={() => setDropTarget(null)}
                            >⠿</span>
                            <span className={styles.orderName}>{label}</span>
                            <button className="btn btn-sm btn-outline-secondary" type="button"
                                aria-label={`${label} 위로 이동`} disabled={saving || index === 0}
                                onClick={() => moveBoard(index, index - 1)}>↑</button>
                            <button className="btn btn-sm btn-outline-secondary" type="button"
                                aria-label={`${label} 아래로 이동`} disabled={saving || index === order.length - 1}
                                onClick={() => moveBoard(index, index + 1)}>↓</button>
                        </li>
                    );
                })}
            </ol>
            {error && <p className="small text-danger mt-3 mb-0" role="alert">{error}</p>}
            <footer className={`${styles.actions} mt-4`}>
                <button className="btn btn-outline-secondary rounded-3" type="button" disabled={saving}
                    onClick={() => router.replace("/admin/boards", { scroll: false })}>취소</button>
                <button className={`btn rounded-3 ${styles.primary}`} type="button"
                    onClick={saveOrder} disabled={!dirty || saving}>
                    {saving ? "저장 중…" : "저장"}
                </button>
            </footer>
        </dialog>
    );
}
