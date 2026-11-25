import type { Metadata } from "next";
import styles from "./page.module.css";

export const metadata: Metadata = {
    title: "로그인 | Blog",
};

export default function LoginPage() {
    return (
        <section className={styles.login} aria-labelledby="login-title">
            <h1 id="login-title">로그인</h1>
            <fieldset className="d-grid gap-3" aria-labelledby="login-title">
                <div>
                    <label className="form-label" htmlFor="login-email">이메일</label>
                    <input className="form-control" id="login-email" type="email" autoComplete="username" />
                </div>
                <div>
                    <label className="form-label" htmlFor="login-password">비밀번호</label>
                    <input className="form-control" id="login-password" type="password" autoComplete="current-password" />
                </div>
                <button className={`btn ${styles.submit}`} type="button" disabled>로그인</button>
                <p className={styles.status}>로그인 기능은 준비 중입니다.</p>
            </fieldset>
        </section>
    );
}
