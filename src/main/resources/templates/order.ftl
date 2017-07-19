<#include "header.ftl">
<#include "nav.ftl">

<div class="container">
    <div class="row" style="margin: 5px;">
        <div class="col-md-offset-1 col-md-9">
            <h4>Salida Ordenes Generadas</h4>
            <div class="myTable">

            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(document).ready(function () {
        var numeroOrden;
        $.ajax({url:"/ordenCompra", success:function (data) {
            numeroOrden= parseInt(data)
        }});

        $.getJSON("/order", {get_param: 'value'}, function (data) {
            console.log(data)

            var $suplidor = $("#suplidor");
            var $fechaOrden = $("#fechaOrden");
            var $total = $("#total");
            $total = 0;
            var count = 0;
            $.each(data, function (index, element) {
                console.log(element);
               count = count + 1

                element.forEach(function (doc) {

                    $(".myTable").append(
                            $("<div>").addClass("myTable-white"+count).append(
                                    $("<table>").addClass("table").append(
                                            $("<thead>").append(
                                                    $("<tr>").append(
                                                            $("<th>").append("Componente"), $("<th>").append("Cantidad"),
                                                            $("<th>").append("Precio"), $("<th>").append("Importe")
                                                    )),
                                            $("<tbody>").append(
                                                    $("<tr>").append(
                                                            $("<td>").append(doc["producto"]),
                                                            $("<td>").append(doc["cantidadAPedir"]),
                                                            $("<td>").append(doc["suplidor"]["precioCompra"]),
                                                            $("<td>").append(doc["cantidadAPedir"] * doc["suplidor"]["precioCompra"])
                                                    )))
                            ));


                    $suplidor = doc["suplidor"]["codigoSuplidor"];
                    var mydate = addDays(new Date(), doc["fechaOrden"]);
                    $fechaOrden = addDays(mydate, -doc["suplidor"]["tiempoEntrega"]);
                    $total = $total + (doc["cantidadAPedir"] * doc["suplidor"]["precioCompra"]);
                });
                numeroOrden+=1
               $(".myTable-white"+count).prepend(
                        $("<div>").attr("id", "dashe").css({"border": "1px dashed cornflowerblue"}).append(
                                $("<div>").append(
                                        $("<div>").addClass("pull-right").css({
                                            "padding": "10px",
                                            "font-weight": "bold"
                                        }).append("No Orden: ").append(
                                                $("<span>").css("margin-left", "20px").append(numeroOrden)),
                                        $("<div>").css({
                                            "padding": "10px",
                                            "font-weight": "bold"
                                        }).append("Suplidor: ").append(
                                                $("<span>").css({
                                                    "margin-left": "20px",
                                                    "font-weight": "bold"
                                                }).append($suplidor))
                                ),
                                $("<div>").append(
                                        $("<div>").addClass("pull-right").css({
                                            "padding": "10px",
                                            "font-weight": "bold"
                                        }).append("Fecha Orden: ").append(
                                                $("<span>").css("margin-left", "20px").append($fechaOrden)),
                                        $("<div>").css({
                                            "padding": "10px",
                                            "font-weight": "bold"
                                        }).append("Monto Total: ").append(
                                                $("<span>").css({
                                                    "margin-left": "20px",
                                                    "font-weight": "bold"
                                                }).append($total))
                                )
                        ));

            });
        });
    });
    function addDays(date, days) {
        var result = new Date(date);
        result.setDate(result.getDate() + days);
        return result;
    }
</script>
<#include "footer.ftl">