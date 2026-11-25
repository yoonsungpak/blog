import Link from "next/link";
import type { ReactNode } from "react";
import styles from "./SiteShell.module.css";

export default function SiteShell({ children }: { children: ReactNode }) {
    return (
        <div className={styles.shell}>
            <header className={`${styles.header} ${styles.container}`}>
                <nav className="d-flex flex-wrap align-items-center justify-content-between gap-3" aria-label="주 메뉴">
                    <Link className={styles.wordmark} href="/">blog<span>.</span></Link>
                    <div className={`${styles.actions} d-flex align-items-center gap-2`}>
                        <Link className={styles.login} href="/login">로그인</Link>
                        <div className={styles.search}>
                            <label className="visually-hidden" htmlFor="post-search">글 검색</label>
                            <input className="form-control" id="post-search" type="search" placeholder="글 검색" />
                        </div>
                    </div>
                </nav>
            </header>
            <main className={`${styles.main} ${styles.container}`}>{children}</main>
            <footer className={styles.footer}>
                <div className={styles.container}>
                    <p className="mb-0">© 2026 blog. All rights reserved.</p>
                </div>
            </footer>
        </div>
    );
}
