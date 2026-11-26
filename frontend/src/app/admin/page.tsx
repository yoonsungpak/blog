import type { Metadata } from "next";
import { cookies } from "next/headers";
import Link from "next/link";
import { notFound } from "next/navigation";
import { cache, Suspense } from "react";
import UserService from "@/lib/user/service/UserService";
import styles from "./AdminMenu.module.css";

const hasAdminAccess = cache(async () => {
    const sid = (await cookies()).get("sid")?.value;
    return sid ? new UserService().isAdmin(sid) : false;
});

export async function generateMetadata(): Promise<Metadata> {
    return { title: await hasAdminAccess() ? "관리자 | Blog" : "404 | Blog" };
}

async function AdminMenu() {
    if (!(await hasAdminAccess())) {
        notFound();
    }

    return (
        <section className="py-5" aria-labelledby="admin-title">
            <h1 id="admin-title" className="h3 mb-4">관리자 메뉴</h1>
            <nav className="row g-3" aria-labelledby="admin-title">
                <div className="col-12 col-sm-6">
                    <Link className={styles.card} href="/admin/users">
                        <h2>사용자 관리 <span aria-hidden="true">→</span></h2>
                        <p>가입 상태를 확인하고 역할과 비밀번호를 변경합니다.</p>
                    </Link>
                </div>
                <div className="col-12 col-sm-6">
                    <Link className={styles.card} href="/admin/boards">
                        <h2>게시판 관리 <span aria-hidden="true">→</span></h2>
                        <p>게시판 이름과 주소를 확인하고 게시판으로 이동합니다.</p>
                    </Link>
                </div>
            </nav>
        </section>
    );
}

export default function AdminPage() {
    return (
        <Suspense fallback={null}>
            <AdminMenu />
        </Suspense>
    );
}
