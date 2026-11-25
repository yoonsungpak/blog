import type { Metadata } from "next";
import Link from "next/link";
import styles from "@/components/auth/AuthForm.module.css";
import LoginForm from "@/components/auth/LoginForm";

export const metadata: Metadata = {
    title: "로그인 | Blog",
};

export default function LoginPage() {
    return (
        <section className={styles.panel} aria-labelledby="login-title">
            <h1 id="login-title">로그인</h1>
            <LoginForm />
            <p className={styles.alternate}><Link href="/register">회원가입</Link></p>
        </section>
    );
}
