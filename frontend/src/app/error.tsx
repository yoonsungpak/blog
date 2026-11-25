"use client";

import ErrorPage from "@/components/errors/ErrorPage";

export default function ErrorBoundary() {
    return (
        <ErrorPage
            code={500}
            title="문제가 발생했어요"
            description="잠시 후 다시 시도해 주세요."
        />
    );
}
