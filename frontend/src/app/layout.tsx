import type { Metadata } from "next";
import SiteShell from "@/components/layout/SiteShell";
import "bootstrap/dist/css/bootstrap.min.css";
import "./globals.css";

export const metadata: Metadata = {
    title: "개발 기록 | Blog",
    description: "블로그를 만들면서 결정한 것과 배운 것을 기록합니다.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
    return (
        <html lang="ko">
            <body>
                <SiteShell>{children}</SiteShell>
            </body>
        </html>
    );
}
