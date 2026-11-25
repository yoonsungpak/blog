import { cookies } from "next/headers";
import Link from "next/link";
import LogoutButton from "@/components/auth/LogoutButton";
import { getSessionBySid } from "@/lib/auth/getCurrentSession";
import UserService from "@/lib/user/service/UserService";
import styles from "./SiteShell.module.css";

export default async function Navbar() {
    const sid = (await cookies()).get("sid")?.value;
    const [session, isAdmin] = await Promise.all([
        getSessionBySid(sid),
        sid ? new UserService().isAdmin(sid).catch(() => false) : false,
    ]);

    return (
        <nav className="d-flex flex-wrap align-items-center justify-content-between gap-3" aria-label="주 메뉴">
            <Link className={styles.wordmark} href="/">blog<span>.</span></Link>
            <div className={`${styles.actions} d-flex flex-wrap align-items-center gap-2`}>
                {session
                    ? <>
                        {session.username && <span className={styles.authAction}>{session.username}님</span>}
                        {isAdmin && <Link className={styles.authAction} href="/admin">관리자</Link>}
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
