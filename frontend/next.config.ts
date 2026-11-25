import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    /* config options here */
    cacheComponents: true,
    partialPrefetching: true,
    reactCompiler: true,
    async rewrites() {
        return [
            {
                source: "/api/:path*",
                destination: "http://localhost:3001/api/:path*",
            },
        ];
    },
};

export default nextConfig;
