<#ftl encoding="utf-8">
<nav class="navbar navbar-inverse" style="margin-bottom: 0px;border-radius: 0px">
    <div class="container">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
                <li>
                    <div style="margin-top:8px;align-content: center">
                        <a href="${ctx}/"><img  src="${ctx}/res/img/logo.png">
                        <span style="vertical-align: middle;color:#f0ad4e;margin-left:5px;font-size:18px;font-weight:bold">Easy Task</span></div>
                    </a>
                </li>

                <li><a href="javascript:alert('easy task console')">关于</a></li>

            </ul>
        </div><!--/.nav-collapse -->
    </div>
</nav>
<p></p>
<script>
    if( document.getElementById("menu_${menu}")) {
        document.getElementById("menu_${menu}").style.color = "red";
    }


</script>