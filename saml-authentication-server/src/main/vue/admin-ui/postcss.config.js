module.exports = {
    plugins: {
        autoprefixer: {},
        "postcss-prefix-selector": {
            prefix: ".app",
            exclude: [":root"]
        }
    }
}
