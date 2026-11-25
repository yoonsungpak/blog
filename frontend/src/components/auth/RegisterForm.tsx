"use client";

import { type SubmitEvent } from "react";
import styles from "./AuthForm.module.css";
import { useRouter } from "next/navigation";
import { getSafeRedirect } from "./getSafeRedirect";

export default function RegisterForm() {
    const router = useRouter();

    async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();

        const data = new FormData(event.currentTarget);
        const username = String(data.get("username") ?? "").trim().toLowerCase();
        const password = data.get("password");
        const passwordConfirmation = data.get("passwordConfirmation");

        if (!username) {
            return alert("아이디를 입력해주세요.");
        }

        if (!password) {
            return alert("비밀번호를 입력해주세요.");
        }

        if (password != passwordConfirmation) {
            return alert("비밀번호 재확인이 일치하지 않습니다.");
        }

        try {
            const res = await fetch("/api/auth/register", {
                method: "POST",
                redirect: "error",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    username: username,
                    password: password,
                })
            });

            if (!res.ok) {
                const data = await res.json();
                return alert(data.message);
            }
        } catch {
            return alert("회원가입 요청을 완료하지 못했습니다.");
        }

        alert("회원가입이 완료되었습니다.");
        const target = getSafeRedirect(window.location.search);
        const query = target ? new URLSearchParams({ redirect: target }) : null;
        router.replace(query ? `/login?${query}` : "/login");
    }

    return (
        <form className="d-grid gap-3" aria-labelledby="register-title" method="post" onSubmit={handleSubmit}>
            <div>
                <label className="form-label" htmlFor="register-username">아이디</label>
                <input className="form-control" id="register-username" name="username" type="text" autoComplete="username" />
            </div>
            <div>
                <label className="form-label" htmlFor="register-password">비밀번호</label>
                <input className="form-control" id="register-password" name="password" type="password" autoComplete="new-password" />
            </div>
            <div>
                <label className="form-label" htmlFor="register-password-confirmation">비밀번호 재확인</label>
                <input className="form-control" id="register-password-confirmation" name="passwordConfirmation" type="password" autoComplete="new-password" />
            </div>
            <button className={`btn ${styles.submit}`} type="submit">회원가입</button>
        </form>
    );
}
