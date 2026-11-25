import { notFound } from "next/navigation";

export default function TestErrorPage() {
    if (process.env.NODE_ENV !== "development") {
        notFound();
    }

    throw new Error("Intentional error for testing the 500 page.");
}
