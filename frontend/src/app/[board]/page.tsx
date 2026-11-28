import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import PostFeed from "@/components/posts/PostFeed/PostFeed";
import BoardRepository from "@/lib/board/repository/BoardRepository";

export async function generateMetadata({ params }: PageProps<"/[board]">): Promise<Metadata> {
    const [{ board }, boards] = await Promise.all([params, new BoardRepository().findVisible()]);
    return boards.some(({ url }) => url === board) ? {} : { title: "404 | Blog" };
}

export default function BoardPage({ params }: PageProps<"/[board]">) {
    return (
        <Suspense fallback={null}>
            <BoardFeed params={params} />
        </Suspense>
    );
}

async function BoardFeed({ params }: Pick<PageProps<"/[board]">, "params">) {
    const [{ board: slug }, boards] = await Promise.all([params, new BoardRepository().findVisible()]);
    const board = boards.find(({ url }) => url === slug);

    if (!board) {
        notFound();
    }

    return <PostFeed board={board} />;
}
