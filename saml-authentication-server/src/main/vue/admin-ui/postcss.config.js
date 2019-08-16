module.exports = {
    plugins: {
        autoprefixer: {},
        /*"postcss-prefix-selector": {
            prefix: ".app",
            exclude: [":root"],
            transform: function(prefix, selector, prefixedSelector) {
                if (selector.toString().startsWith('.q-')) return selector;
                if (selector.toString().startsWith('body')) return selector.toString().replace("body", "div.app");
                return prefixedSelector;
            }
        }*/
    }
}
