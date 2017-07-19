<#include "header.ftl">
<#include "nav.ftl">

<div class="container">

    <div class="panel panel-primary" style=" margin-left: 30px">
        <div class="panel-heading">Inventario</div>
        <div class="panel-body">
            <div style=" border-radius: 3px;border: 1px solid gray; padding: 8px">
                <form action="/" method="post">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="fecha">Fecha</label>
                                <input type="date" class="form-control" name="fecha" id="fecha">
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="articulo">Articulo</label>
                                <select class="form-control" name="articulo" id="articulo">
                                    <option selected="disabled">Select One</option>
                                    <#if articulos??>
                                        <#list articulos as articulo >
                                            <option>${articulo}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="form-group">
                                <label for="cantidadDeseada">Cantidad deseada</label>
                                <input class="form-control" type="number" name="cantidadDeseada" id="cantidadDeseada">
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-md-offset-6 col-md-6">
                            <div class="form-group">
                                <div class="col-md-6">
                                    <button type="reset" class="btn btn-primary form-control">vaciar campo</button>
                                </div>
                                <div class="col-md-6">
                                    <button type="submit" class="btn btn-primary form-control">agregar</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>
            </div>

            <div class="row" style="margin:5px;">
                <h4>Lista de elementos a ordenar</h4>
                <div class="form-group" style=" border-radius: 3px;border: 1px solid gray">
                    <ul class="list-group">
                        <#if orders??>
                            <#list orders as order>
                            <li class="list-group-item" id="articulo${order?counter}"><span style="margin-right: 10px" id="number">${order?counter}</span>.-<strong style="font-family: Arial; color: red;">Componente:</strong> ${order.getProducto()}   <strong style="font-family: Arial; color: red;margin-left: 150px">Cantidad:</strong>${order.getCantidadDeseada()} <span class="badge"><img class="delete" src="/images/delete.png" alt=""></span></li>
                            </#list>
                        </#if>

                    </ul>
                </div>
            </div>
            <div class="row">
                <form action="/generate/order" method="post">
                <div class="col-md-offset-6 col-md-6">
                    <div class="form-group">
                        <div class="col-md-6"></div>
                        <div class="col-md-6">
                            <button type="submit" class="btn btn-primary form-control">Generar Orden</button>
                        </div>
                    </div>
                </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(document).ready(function () {
        $(".delete").click(function(){
            $("#articulo"+$("#number").text()).hide()
            $.ajax({url:"/delete/"+$("#number").text(), success:function(data) {
                console.log(data);
            }
            })
        });
    })
</script>
<#include "footer.ftl">

