"use client"

import styles from "@/components/auth/AuthForm.module.css";
import { useRouter } from "next/navigation";
import type {SubmitEvent} from "react";
import { getSafeRedirect } from "./getSafeRedirect";

export default function LoginForm() {
    const router = useRouter();

    async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault();

        const formData = new FormData(event.currentTarget);
        const username = String(formData.get("username") ?? "").trim().toLowerCase();
        const password = String(formData.get("password") ?? "").trim();


        if (!username || !password) {
            return alert("아이디 또는 비밀번호를 입력해주세요.");
        }

        try {
            const res = await fetch("/api/auth/login", {
                method: "POST",
                redirect: "error",
                headers: {
                    "Content-Type": "application/json",
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
            return alert("인터넷 연결 상태를 확인해주세요.");
        }

        router.replace(getSafeRedirect(window.location.search) ?? "/");
    }

    return (
        <form className="d-grid gap-3" aria-labelledby="login-title" method="post" onSubmit={handleSubmit}>
            <div>
                <label className="form-label" htmlFor="login-username">아이디</label>
                <input className="form-control" id="login-username" name="username" autoComplete="username"/>
            </div>
            <div>
                <label className="form-label" htmlFor="login-password">비밀번호</label>
                <input className="form-control" id="login-password" name="password" type="password" autoComplete="current-password"/>
            </div>
            <button className={`btn ${styles.submit}`} type="submit">로그인</button>
        </form>
    )
}
