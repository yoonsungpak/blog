import { Suspense } from "react";
import PostFeed from "@/components/posts/PostFeed/PostFeed";

export default function Home() {
    return (
        <Suspense fallback={null}>
            <PostFeed />
        </Suspense>
    );
}
