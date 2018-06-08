<#ftl encoding="utf-8">

<div style="margin: 4px">
<#include "page2.ftl">
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <button type="button" class="btn btn-primary btn-xs" onclick="edit(0)"><span
            class="glyphicon glyphicon-plus" aria-hidden="true"></span> <span lang-key="add">add</span>
    </button>
    <#--<#if   (RequestParameters.taskType!"0")=="0">-->
    <button type="button" class="btn btn-danger btn-xs" onclick="doBatch('delete.htm?')"><span
            class="glyphicon glyphicon-minus" aria-hidden="true"></span> <span lang-key="delete">delete</span>
    </button>
    <#--</#if>-->

    &nbsp;&nbsp;&nbsp;
    <div class="btn-group">
        <button class="btn btn-primary btn-xs dropdown-toggle" data-toggle="dropdown">
            <span lang-key="importOrExport">importOrExport</span><span class="caret"></span>
        </button>
        <ul class="dropdown-menu" role="menu" style="min-width:50px;">

            <li><a href="#"   onclick="doBatch('exportTasks.htm?',null,true)" lang-key="exportToFile">exportToFile</a></li>
            <li>
                <a href="#">
                    <span class="fileinput-button">
                    <span lang-key="importFromFile">importFromFile</span>
                    <input type="file" id="file1" name="file1" onchange="ajaxFileUpload()">
                    </span>
                </a>
            </li>
        </ul>
    </div>


    &nbsp;&nbsp;&nbsp;
    <button type="button" class="btn btn-primary btn-xs" onclick="doStatus(this,0,null)"><span
            class="glyphicon glyphicon-play" aria-hidden="true"></span> <span lang-key="start">start</span>
    </button>
    <button type="button" class="btn btn-primary btn-xs" onclick="doStatus(this,1,null)"><span
            class="glyphicon glyphicon-stop" aria-hidden="true"></span> <span lang-key="stop">stop</span>
    </button>
    &nbsp;&nbsp;&nbsp;

    <div class="btn-group">
        <input type="text" class="form-control" style="height:25px;width:120px" id="switchTargetIp" name="targetIp"
               autocomplete="off"
               data-toggle="dropdown" aria-expanded="false" value="" data-rule-required="true"/>
        <ul class="dropdown-menu" style="overflow: auto; max-height: 300px" role="menu">
            <#list  switchMachines as item>
                <li><a href="#" onclick="$('#switchTargetIp',$(this).parents('div')[0]).val(this.innerText)">${item}</a></li>
            </#list>
        </ul>
    </div>
    <button type="button" class="btn btn-primary btn-xs" onclick="doSwitchIp($('#switchTargetIp',this.parentNode))"><span
            class="glyphicon glyphicon-share-alt" aria-hidden="true"></span> <span lang-key="switchIP">switchIP</span>
    </button>

</div>