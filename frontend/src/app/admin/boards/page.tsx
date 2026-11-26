import type { Metadata } from "next";
import { cookies } from "next/headers";
import { notFound } from "next/navigation";
import { cache, Suspense } from "react";
import Link from "next/link";
import BoardEditor from "@/components/admin/BoardEditor";
import styles from "@/components/admin/BoardEditor.module.css";
import { boards } from "@/data/boards";
import UserService from "@/lib/user/service/UserService";

const hasAdminAccess = cache(async () => {
    const sid = (await cookies()).get("sid")?.value;
    return sid ? new UserService().isAdmin(sid) : false;
});

export async function generateMetadata(): Promise<Metadata> {
    return { title: await hasAdminAccess() ? "게시판 관리 | Blog" : "404 | Blog" };
}

async function AdminBoards({ searchParams }: Pick<PageProps<"/admin/boards">, "searchParams">) {
    if (!(await hasAdminAccess())) {
        notFound();
    }

    const { edit, create } = await searchParams;
    const selectedBoard = typeof edit === "string" ? boards.find((board) => board.slug === edit) : undefined;
    const creating = create === "1";

    return (
        <section className="py-5" aria-labelledby="boards-title">
            <header className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
                <h1 id="boards-title" className="h3 mb-0">게시판 관리</h1>
                <div className="d-flex flex-wrap gap-2">
                    <Link className="btn btn-sm btn-outline-secondary rounded-3" href="/admin">
                        관리자 메뉴
                    </Link>
                    <Link className={`btn btn-sm rounded-3 ${styles.primary}`} href="/admin/boards?create=1" scroll={false}>
                        새로 만들기
                    </Link>
                </div>
            </header>
            <div className="table-responsive border rounded-4">
                <table className="table align-middle mb-0">
                    <thead className="table-light">
                        <tr>
                            <th className="px-3 py-3" scope="col">게시판 이름</th>
                            <th className="px-3 py-3" scope="col">주소</th>
                            <th className="px-3 py-3" scope="col">공개 여부</th>
                            <th className="px-3 py-3 text-end" scope="col">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        {boards.map(({ slug, name }) => (
                            <tr key={slug}>
                                <th className="px-3 py-3 border-bottom-0" scope="row">{name}</th>
                                <td className="px-3 py-3 border-bottom-0">
                                    <Link className="text-decoration-underline font-monospace" href={`/${slug}`}>
                                        /{slug}
                                    </Link>
                                </td>
                                <td className="px-3 py-3 border-bottom-0 text-nowrap">공개</td>
                                <td className="px-3 py-3 border-bottom-0 text-end">
                                    <Link className="btn btn-sm btn-outline-secondary rounded-3"
                                        href={{ pathname: "/admin/boards", query: { edit: slug } }}
                                        scroll={false} aria-label={`${name} 수정`}>
                                        수정
                                    </Link>
                                </td>
                            </tr>
                        ))}
                        {!boards.length && (
                            <tr><td className="p-3 border-bottom-0" colSpan={4}>등록된 게시판이 없어요.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
            {(creating || selectedBoard) && (
                <BoardEditor key={creating ? "new" : selectedBoard?.slug} board={creating ? undefined : selectedBoard} />
            )}
        </section>
    );
}

export default function AdminBoardsPage({ searchParams }: PageProps<"/admin/boards">) {
    return (
        <Suspense fallback={null}>
            <AdminBoards searchParams={searchParams} />
        </Suspense>
    );
}
