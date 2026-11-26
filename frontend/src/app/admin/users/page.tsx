import type { Metadata } from "next";
import { cookies } from "next/headers";
import Link from "next/link";
import { notFound } from "next/navigation";
import { cache, Suspense } from "react";
import ChangePasswordModal from "@/components/admin/ChangePasswordModal";
import UserRoleForm from "@/components/admin/UserRoleForm";
import UserRepository from "@/lib/user/repository/UserRepository";
import UserService from "@/lib/user/service/UserService";

const dateFormat = new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeZone: "Asia/Seoul" });

const hasAdminAccess = cache(async () => {
    const sid = (await cookies()).get("sid")?.value;
    return sid ? new UserService().isAdmin(sid) : false;
});

export async function generateMetadata(): Promise<Metadata> {
    return { title: await hasAdminAccess() ? "사용자 관리 | Blog" : "404 | Blog" };
}

async function AdminUsers() {
    if (!(await hasAdminAccess())) {
        notFound();
    }

    const users = await new UserRepository().findAll();

    return (
        <section className="py-5" aria-labelledby="admin-title">
            <header className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
                <h1 id="admin-title" className="h3 mb-0">사용자 관리</h1>
                <Link className="btn btn-sm btn-outline-secondary rounded-3" href="/admin">
                    관리자 메뉴
                </Link>
            </header>
            <div className="table-responsive">
                <table className="table align-middle mb-0">
                    <thead>
                        <tr>
                            <th scope="col">아이디</th>
                            <th scope="col">가입일</th>
                            <th scope="col">상태</th>
                            <th scope="col">역할</th>
                            <th scope="col">비밀번호</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => (
                            <tr key={user.id}>
                                <th scope="row">{user.username}</th>
                                <td>{dateFormat.format(user.createdAt)}</td>
                                <td>{user.deletedAt ? "탈퇴" : "활성"}</td>
                                <td>{user.deletedAt ? user.role : <UserRoleForm user={user} />}</td>
                                <td>{user.deletedAt ? "—" : <ChangePasswordModal user={user} />}</td>
                            </tr>
                        ))}
                        {!users.length && <tr><td colSpan={5}>등록된 사용자가 없어요.</td></tr>}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

export default function AdminUsersPage() {
    return (
        <Suspense fallback={null}>
            <AdminUsers />
        </Suspense>
    );
}
