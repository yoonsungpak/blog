import Link from "next/link";
import { notFound } from "next/navigation";
import { Suspense } from "react";
import { samplePosts } from "@/data/SamplePosts";
import styles from "./page.module.css";

export function generateStaticParams() {
    return samplePosts.map(({ slug }) => ({ slug }));
}

export default function PostPage({ params }: PageProps<"/posts/[slug]">) {
    return (
        <Suspense fallback={null}>
            <PostContent params={params} />
        </Suspense>
    );
}

async function PostContent({ params }: Pick<PageProps<"/posts/[slug]">, "params">) {
    const { slug } = await params;
    const post = samplePosts.find((entry) => entry.slug === slug);

    if (!post) {
        notFound();
    }

    return (
        <article className={styles.article}>
            <time dateTime={post.date}>{post.date.replace(/-/g, ".")}</time>
            <div className={`${styles.articleHeading} d-flex flex-wrap align-items-baseline justify-content-between`}>
                <h1>{post.title}</h1>
                <Link href={`/${post.board.slug}`}>글 목록</Link>
            </div>
            {post.paragraphs.map((paragraph) => <p key={paragraph}>{paragraph}</p>)}
        </article>
    );
}
