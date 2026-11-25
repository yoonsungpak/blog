if (process.env.NODE_ENV === "development") {
    const measure = performance.measure.bind(performance);

    performance.measure = (name, startOrOptions, endMark) => {
        // React's RSC profiler can report a negative end time for an aborted prerender.
        if (
            name.startsWith("\u200b") &&
            typeof startOrOptions === "object" &&
            typeof startOrOptions.end === "number" &&
            startOrOptions.end < 0
        ) {
            const start = typeof startOrOptions.start === "number" ? Math.max(0, startOrOptions.start) : 0;
            return measure(name, { ...startOrOptions, start, end: start });
        }

        return measure(name, startOrOptions, endMark);
    };
}
