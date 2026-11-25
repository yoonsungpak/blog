import { notFound } from "next/navigation";
import { Suspense } from "react";
import { boards } from "@/data/boards";
import PostFeed from "@/components/posts/PostFeed/PostFeed";

export function generateStaticParams() {
    return boards.map(({ slug }) => ({ board: slug }));
}

export default function BoardPage({ params }: PageProps<"/[board]">) {
    return (
        <Suspense fallback={null}>
            <BoardFeed params={params} />
        </Suspense>
    );
}

async function BoardFeed({ params }: Pick<PageProps<"/[board]">, "params">) {
    const { board: slug } = await params;
    const board = boards.find((entry) => entry.slug === slug);

    if (!board) {
        notFound();
    }

    return <PostFeed board={board} />;
}
