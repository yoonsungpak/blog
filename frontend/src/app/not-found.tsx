import ErrorPage from "@/components/errors/ErrorPage";

export default function NotFound() {
    return (
        <ErrorPage
            code={404}
            title="페이지를 찾을 수 없어요"
            description="주소를 확인하거나 홈으로 돌아가 주세요."
        />
    );
}
