import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import { samplePosts } from "@/data/SamplePosts";
import BoardRepository from "@/lib/board/repository/BoardRepository";
import styles from "./page.module.css";

export function generateStaticParams() {
    return samplePosts.map(({ slug }) => ({ slug }));
}

export async function generateMetadata({ params }: PageProps<"/posts/[slug]">): Promise<Metadata> {
    const [{ slug }, [board]] = await Promise.all([params, new BoardRepository().findAll()]);
    return board?.isPublic && board.url && samplePosts.some((post) => post.slug === slug)
        ? {}
        : { title: "404 | Blog" };
}

export default function PostPage({ params }: PageProps<"/posts/[slug]">) {
    return (
        <Suspense fallback={null}>
            <PostContent params={params} />
        </Suspense>
    );
}

async function PostContent({ params }: Pick<PageProps<"/posts/[slug]">, "params">) {
    const [{ slug }, [board]] = await Promise.all([params, new BoardRepository().findAll()]);
    const post = samplePosts.find((entry) => entry.slug === slug);

    if (!post || !board?.isPublic || !board.url) {
        notFound();
    }

    return (
        <article className={styles.article}>
            <time dateTime={post.date}>{post.date.replace(/-/g, ".")}</time>
            <div className={`${styles.articleHeading} d-flex flex-wrap align-items-baseline justify-content-between`}>
                <h1>{post.title}</h1>
                <Link href={`/${board.url}`}>글 목록</Link>
            </div>
            {post.paragraphs.map((paragraph) => <p key={paragraph}>{paragraph}</p>)}
        </article>
    );
}
