module.exports = {
    indexPath: "index.html",
    publicPath: "./",
    pluginOptions: {

    },
    filenameHashing: false,
    chainWebpack(config) {
        config.optimization.delete('splitChunks');
    }

};
