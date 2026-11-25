import { redirect } from "next/navigation";
import { samplePosts } from "@/data/SamplePosts";

export default function InitialCommitPost() {
    redirect(`/posts/${samplePosts[0].slug}`);
}
