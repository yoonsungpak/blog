import Link from "next/link";
import LogoutButton from "@/components/auth/LogoutButton";
import { getCurrentSession } from "@/lib/auth/getCurrentSession";
import styles from "./SiteShell.module.css";

export default async function Navbar() {
    const session = await getCurrentSession();

    return (
        <nav className="d-flex flex-wrap align-items-center justify-content-between gap-3" aria-label="주 메뉴">
            <Link className={styles.wordmark} href="/">blog<span>.</span></Link>
            <div className={`${styles.actions} d-flex align-items-center gap-2`}>
                {session
                    ? <>
                        {session.username && <span className={styles.authAction}>{session.username}님</span>}
                        <LogoutButton className={styles.authAction} />
                    </>
                    : <Link className={styles.authAction} href="/login">로그인</Link>}
                <div className={styles.search}>
                    <label className="visually-hidden" htmlFor="post-search">글 검색</label>
                    <input className="form-control" id="post-search" type="search" placeholder="글 검색" />
                </div>
            </div>
        </nav>
    );
}
