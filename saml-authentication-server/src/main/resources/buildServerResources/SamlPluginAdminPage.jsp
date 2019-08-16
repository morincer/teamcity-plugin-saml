<jsp:useBean id="pluginResources" scope="request" type="java.lang.String"/>


<style>
    iframe {
        width: 1px;
        min-width: 100%;
    }
</style>
<iframe id="widget" src="${pluginResources}admin-ui/index.jsp" frameborder="0" ></iframe>

<script src="${pluginResources}admin-ui/js/iframeResizer.min.js"></script>
<script>
    iFrameResize({log: false}, '#widget')
</script>
