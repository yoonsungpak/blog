"use client";

import { useId, useRef, useState, type SubmitEvent } from "react";
import styles from "./ChangePasswordModal.module.css";
import {User} from "@/lib/user/domain/Users";

export default function ChangePasswordModal({ user }: { user: User }) {
    const id = useId();
    const dialog = useRef<HTMLDialogElement>(null);
    const form = useRef<HTMLFormElement>(null);
    const confirmation = useRef<HTMLInputElement>(null);
    const [error, setError] = useState("");

    async function submit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();

        const formData = new FormData(event.currentTarget);
        if (formData.get("password") !== formData.get("confirmation")) {
            setError("새 비밀번호와 확인 입력이 일치하지 않습니다.");
            confirmation.current?.focus();
            return;
        }

        try {
            const res = await fetch(`/api/users/${user.id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    password: formData.get("password")
                })
            });

            if (!res.ok) {
                const data = await res.json();
                return setError(data.message);
            }
        } catch (e) {
            return alert("인터넷 연결 상태를 확인해주세요.");
        }

        alert("비밀번호가 변경되었습니다.");
        dialog.current?.close();
    }

    return (
        <>
            <button className={`btn btn-sm ${styles.cancel} text-nowrap`} type="button"
                aria-haspopup="dialog" aria-label={`${user.username} 비밀번호 변경`}
                onClick={() => dialog.current?.showModal()}>
                비밀번호 변경
            </button>
            <dialog
                ref={dialog}
                className={styles.dialog}
                aria-labelledby={`${id}-title`}
                aria-describedby={`${id}-user`}
                onClose={() => {
                    form.current?.reset();
                    setError("");
                }}
            >
                <header className={styles.header}>
                    <div>
                        <h2 id={`${id}-title`}>비밀번호 변경</h2>
                        <p id={`${id}-user`} className="small text-body-secondary mt-2 mb-0">{user.username}</p>
                    </div>
                    <button className="btn-close" type="button" aria-label="닫기"
                        onClick={() => dialog.current?.close()} />
                </header>
                <form ref={form} method="post" onSubmit={submit}>
                    <div className="d-grid gap-3">
                        <div>
                            <label className="form-label" htmlFor={`${id}-password`}>새 비밀번호</label>
                            <input className="form-control" id={`${id}-password`} name="password"
                                type="password" autoComplete="new-password" required autoFocus
                                pattern="[a-zA-Z0-9_]{10,50}" aria-describedby={`${id}-rule`} />
                            <div id={`${id}-rule`} className="form-text">영문, 숫자, 밑줄(_)을 사용해 10~50자로 입력해 주세요.</div>
                        </div>
                        <div>
                            <label className="form-label" htmlFor={`${id}-confirmation`}>새 비밀번호 확인</label>
                            <input ref={confirmation} className="form-control" id={`${id}-confirmation`}
                                name="confirmation" type="password" autoComplete="new-password" required />
                        </div>
                    </div>
                    {error && <p className={styles.error} role="alert">{error}</p>}
                    <div className={styles.actions}>
                        <button className={`btn ${styles.cancel}`} type="button"
                            onClick={() => dialog.current?.close()}>취소</button>
                        <button className={`btn ${styles.primary}`} type="submit">비밀번호 변경</button>
                    </div>
                </form>
            </dialog>
        </>
    );
}
