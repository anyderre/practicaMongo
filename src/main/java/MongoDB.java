import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import org.bson.Document;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import freemarker.template.Configuration;
import org.bson.conversions.Bson;
import spark.ModelAndView;


import spark.template.freemarker.FreeMarkerEngine;

import static com.mongodb.client.model.Filters.*;
import static spark.Spark.*;
import static spark.debug.DebugScreen.enableDebugScreen;

/**
 * Created by anyderre on 04/07/17.
 */
public class MongoDB
{
    private static MongoClient  mongoClient;
    private static MongoDatabase mongoDatabase;
    public static List<Order>orders;

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static void setMongoClient(MongoClient mongoCliente) {
        mongoClient = mongoCliente;
    }

    public static MongoDatabase getMongodb() {
        return mongoDatabase;
    }

    public static void setMongodb(MongoDatabase mongodb) {
        mongoDatabase = mongodb;
    }

    public static void connectDatabase(){
        setMongoClient(new MongoClient("localhost", 27017));
        setMongodb(getMongoClient().getDatabase("Inventario"));
    }
    public static void main(String[] args) {
        orders=new ArrayList<>();
        //indicando los recursos publicos.
        port(getHerokuAssignedPort());
        enableDebugScreen();
        staticFileLocation("/publico");


        //Indicando la carpeta por defecto que estaremos usando.
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setClassForTemplateLoading(MongoDB.class, "/templates");
        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine(configuration);

        get("/",(request,response)->{
            Map<String, Object> model = new HashMap<>();
            model.put("titulo", "Inventarios");
            connectDatabase();
            MongoCollection<Document>collection = mongoDatabase.getCollection("articulo");
            List<String> articulos=new ArrayList<>();
            for(Document doc: (List<Document>)collection.find().into(new ArrayList<Document>())){
                articulos.add(doc.getString("descripcion"));
            }
            model.put("articulos", articulos);

            return new ModelAndView(model, "Inventario.ftl");
        },freeMarkerEngine);
        post("/", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("titulo", "Inventarios");

            boolean existe=false;
            int index=0;
           if(orders.isEmpty()){
               Order order = new Order();
               order.setCantidadDeseada(Integer.parseInt(request.queryParams("cantidadDeseada")));
               order.setFechaDeseada(request.queryParams("fecha"));
               order.setProducto(request.queryParams("articulo"));
               orders.add(order);
           }else{
               int count =0;
               for(Order ord : orders){
                   if(request.queryParams("articulo").equals(ord.getProducto()) && request.queryParams("fecha").equals(ord.getFechaDeseada())){
                       index=count;
                       existe=true;

                       break;
                    }
               count++;
               }
               if(existe){
                   int suma  =  orders.get(index).getCantidadDeseada()+ Integer.parseInt(request.queryParams("cantidadDeseada"));
                   orders.get(index).setCantidadDeseada(suma);
               }else {
                   Order order = new Order();
                   order.setCantidadDeseada(Integer.parseInt(request.queryParams("cantidadDeseada")));
                   order.setFechaDeseada(request.queryParams("fecha"));
                   order.setProducto(request.queryParams("articulo"));
                   orders.add(order);
               }
           }

            model.put("orders", orders);
            connectDatabase();
            MongoCollection<Document>collection = mongoDatabase.getCollection("articulo");
            List<String> articulos=new ArrayList<>();
            for(Document doc: (List<Document>)collection.find().into(new ArrayList<Document>())){
                articulos.add(doc.getString("descripcion"));
            }
            model.put("articulos", articulos);
            return new ModelAndView(model,"Inventario.ftl");
        }, freeMarkerEngine);

        get("/delete/:id", (request, response) -> {
            List<Order> temp = new ArrayList<>();
            for(int i=0; i<orders.size(); i++){
                if (i!=(Integer.parseInt(request.params("id")))-1){
                    temp.add(orders.get(i));
                }
                orders=temp;
            }
            return "deleted";
        });

        post("/generate/order",(request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("titulo", "Orden generada");

            return new ModelAndView(model,"order.ftl");
        },freeMarkerEngine);
        get("/ordenCompra", (request, response) -> {
            connectDatabase();
            MongoCollection<Document>collection = mongoDatabase.getCollection("ordenCompra");
            return collection.count();
        });
        get("/order", (request, response) -> {

            List<Order> ordersTomake = new ArrayList<>();
            Random random = new Random();
            for(Order order : orders){
                Order order1 =calculateOrderDetails(order.getProducto(), consumoDiario(order.getProducto()),order.getCantidadDeseada(),order.getFechaDeseada());
                ordersTomake.add(order1);
            }
            HashMap<Double, List<Order>> ordersFinal = new HashMap<>();
            for(Order o: ordersTomake){
                if(o.getSuplidor().getDouble("codigoSuplidor")!=0){
                    if(!ordersFinal.containsKey(o.getSuplidor().getDouble("codigoSuplidor"))){
                        List<Order> newOrder = new ArrayList<>();
                        newOrder.add(o);
                        ordersFinal.put(o.getSuplidor().getDouble("codigoSuplidor"), newOrder);
                    }else{
                        if(o.getFechaOrden()==ordersFinal.get(o.getSuplidor().getDouble("codigoSuplidor")).get(0).getFechaOrden()){
                            ordersFinal.get(o.getSuplidor().getDouble("codigoSuplidor")).add(o);
                        }else{
                            List<Order>newOrder = new ArrayList<>();
                            newOrder.add(o);
                            ordersFinal.put((o.getSuplidor().getDouble("codigoSuplidor")*random.nextInt(10)+1)+random.nextInt(100)+50, newOrder);
                        }
                    }
                }

            }
            connectDatabase();
            MongoCollection<Document>collection = mongoDatabase.getCollection("ordenCompra");
            for (Map.Entry<Double, List<Order>> entry : ordersFinal.entrySet())
            {
                double NumeroOrden;
                double suplidor=0.0;
                double montoTotal =0.0;
                Date fechaOrden=new Date();
                Date  tempDate=new Date ();
                List<Document> orderDetail = new ArrayList<>();
                Document document;
                for (Order order: entry.getValue()){
                    suplidor=order.getSuplidor().getDouble("codigoSuplidor");
                    tempDate = addDays(tempDate,order.getFechaOrden());
                    fechaOrden = addDays(tempDate, (int)-order.getSuplidor().getDouble("tiempoEntrega"));
                    montoTotal = montoTotal+ order.getCantidadAPedir()*order.getSuplidor().getDouble("precioCompra");

                    document= new Document();
                    document.put("articulo", order.getProducto());
                    document.put("cantidad", order.getCantidadAPedir());
                    document.put("precio", order.getSuplidor().getDouble("precioCompra"));
                    document.put("importe", order.getCantidadAPedir()*order.getSuplidor().getDouble("precioCompra"));

                    orderDetail.add(document);

                }
                NumeroOrden = collection.count()+1;
                Document documentFinal = new Document();
                documentFinal.put("NumeroOrden", NumeroOrden);
                documentFinal.put("Suplidor", suplidor);
                documentFinal.put("FechaOrden", fechaOrden);
                documentFinal.put("MontoTotal", montoTotal);
                documentFinal.put("Elementos",orderDetail);

                collection.insertOne(documentFinal);
            }
            orders = new ArrayList<>();
            return ordersFinal;
        }, JsonUtilidades.json());
    }

    private static int consumoDiario(String articulo){
        connectDatabase();
        MongoCollection<Document>collectionArticulo = mongoDatabase.getCollection("articulo");

        FindIterable<Document> consulta = collectionArticulo.find(new Document("descripcion", articulo));

        double codigoArticulo =0;
        for (Document articuloEncuestion: consulta) {
            codigoArticulo = articuloEncuestion.getDouble("codigoArticulo");

        }
        MongoCollection<Document>collection = mongoDatabase.getCollection("movimientoInventario");
        List<Document> consumoPromediomensual = new ArrayList<>();

        consumoPromediomensual.add(new Document("$match", new Document("tipoMovimiento", "SALIDA").
                append("codigoArticulo",codigoArticulo)
        ));
        consumoPromediomensual.add(new Document("$group", new Document("_id",
                new Document("month", new Document("$month", "$fechaMovimiento"))).append("total", new Document("$sum", "$cantidad"))));

        consumoPromediomensual.add(new Document("$group", new Document("_id", null).
                append("cantidad", new Document("$avg", "$total"))));

        AggregateIterable<Document> consumo = collection.aggregate(consumoPromediomensual);

        int consumoResultante=0;
        for (Document  document: consumo){
            consumoResultante=(int)((double)document.getDouble("cantidad")/30);
        }
        System.out.println(consumoResultante);
        return consumoResultante;
    }
    public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }
    private static Order calculateOrderDetails(String articulo, int consumoDiario, int cantidadDeseada, String fecha){
        MongoCollection<Document>collection = mongoDatabase.getCollection("articulo");

        FindIterable<Document> consulta = collection.find(new Document("descripcion", articulo));

        double cantidadDisponible= 0;
        double codigoArticulo =0;
        for (Document articuloEncuestion: consulta){
            codigoArticulo=articuloEncuestion.getDouble("codigoArticulo");
            List<Document> almacenes= (List<Document>)articuloEncuestion.get("almacenes");

            for (Document almacen : almacenes){
                cantidadDisponible=cantidadDisponible+almacen.getDouble("balanceActual");
            }
            System.out.println(cantidadDisponible);
        }
        int resta= (int)cantidadDisponible%consumoDiario;
        int diasRestantes= (int)cantidadDisponible/consumoDiario;

        DateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");
        Date fechaDeseada=new Date();
        try {
            fechaDeseada=dateFormat.parse(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, diasRestantes);
        System.out.println("Va a terminar"+ dateFormat.format(c.getTime()));
        Date fechaPeticio=new Date();
        try {
            fechaPeticio=dateFormat.parse(dateFormat.format(c.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int cantidadApedir=0;
        int diasPorConsumir=0;
        int cantidadDeDiasPorPedir;
        if(fechaPeticio.before(fechaDeseada)){
            cantidadDeDiasPorPedir = diasRestantes;
            diasPorConsumir = (int)getDifferenceDays(fechaPeticio, fechaDeseada);
            cantidadApedir = (diasPorConsumir*consumoDiario)+cantidadDeseada-resta;
        }else if (fechaPeticio.after(fechaDeseada)){
            diasPorConsumir = (int)getDifferenceDays(fechaPeticio, fechaDeseada); //da un valor negativo
            cantidadDeDiasPorPedir = diasRestantes+diasPorConsumir; // es una resta ya que dias por consumir da negativo
            cantidadApedir = (diasPorConsumir*consumoDiario)+cantidadDeseada-resta;
        }else {
            cantidadDeDiasPorPedir=diasRestantes;
            cantidadApedir = cantidadDeseada-resta;
        }


        MongoCollection<Document>collectionSuplidores = mongoDatabase.getCollection("articuloSuplidor");
        FindIterable<Document> suplidores = collectionSuplidores.find(new Document("codigoArticulo", codigoArticulo));



        int count = 0;
        double min =1000000000;
        Document documento = new Document();
        for(Document suplidor:suplidores){
            System.out.println(suplidor.toJson());
            if(suplidor.getDouble("tiempoEntrega") < (double)cantidadDeDiasPorPedir){
                if (suplidor.getDouble("precioCompra")<= min){
                    min = suplidor.getDouble("precioCompra");
                    documento=suplidor;
                }
            }
            count++;
        }
        System.out.println("----------------------------------------------------------------------------------");
        System.out.println(documento.toJson());

        Order order = new Order();
        order.setProducto(articulo);
        order.setCantidadDeseada(cantidadDeseada);
        order.setFechaDeseada(fecha);
        order.setCantidadAPedir(cantidadApedir);
        order.setSuplidor(documento);

        order.setFechaOrden(cantidadDeDiasPorPedir);
        return order;
    }

    public static long getDifferenceDays(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    private static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}
