import type { Metadata } from "next";
import { cookies } from "next/headers";
import { notFound } from "next/navigation";
import { cache, Suspense } from "react";
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
            <h1 id="admin-title" className="h3 mb-4">사용자 관리</h1>
            <p className="small text-body-secondary">역할 변경 기능은 준비 중입니다.</p>
            <div className="table-responsive">
                <table className="table align-middle mb-0">
                    <thead>
                        <tr>
                            <th scope="col">아이디</th>
                            <th scope="col">가입일</th>
                            <th scope="col">상태</th>
                            <th scope="col">역할</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => (
                            <tr key={user.id}>
                                <th scope="row">{user.username}</th>
                                <td>{dateFormat.format(user.createdAt)}</td>
                                <td>{user.deletedAt ? "탈퇴" : "활성"}</td>
                                <td>{user.deletedAt ? user.role : <UserRoleForm user={user} />}</td>
                            </tr>
                        ))}
                        {!users.length && <tr><td colSpan={4}>등록된 사용자가 없어요.</td></tr>}
                    </tbody>
                </table>
            </div>
        </section>
    );
}

export default function AdminPage() {
    return (
        <Suspense fallback={null}>
            <AdminUsers />
        </Suspense>
    );
}
