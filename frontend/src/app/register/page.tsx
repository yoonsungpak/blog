import type { Metadata } from "next";
import Link from "next/link";
import styles from "@/components/auth/AuthForm.module.css";
import RegisterForm from "@/components/auth/RegisterForm";

export const metadata: Metadata = {
    title: "회원가입 | Blog",
};

export default function RegisterPage() {
    return (
        <section className={styles.panel} aria-labelledby="register-title">
            <h1 id="register-title">회원가입</h1>
            <RegisterForm />
            <p className={styles.alternate}><Link href="/login">로그인</Link></p>
        </section>
    );
}
