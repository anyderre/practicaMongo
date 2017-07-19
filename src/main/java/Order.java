import org.bson.Document;

import java.util.Date;

/**
 * Created by anyderre on 14/07/17.
 */
public class Order {
    private String fechaDeseada;
    private String producto;
    private int cantidadDeseada;
    private int cantidadAPedir;
    private Document suplidor;
    private int fechaOrden;

    public Order(String fechaDeseada, int fechaOrden, String producto, int cantidadDeseada, int cantidadAPedir, Document suplidor) {
        this.fechaDeseada = fechaDeseada;
        this.producto = producto;
        this.cantidadDeseada = cantidadDeseada;
        this.cantidadAPedir = cantidadAPedir;
        this.suplidor = suplidor;
        this.fechaOrden =fechaOrden;
    }

    public Order(){

    }

    public int getFechaOrden() {
        return fechaOrden;
    }

    public void setFechaOrden(int  fechaOrden) {
        this.fechaOrden = fechaOrden;
    }

    public int getCantidadAPedir() {
        return cantidadAPedir;
    }

    public void setCantidadAPedir(int cantidadAPedir) {
        this.cantidadAPedir = cantidadAPedir;
    }

    public Document getSuplidor() {
        return suplidor;
    }

    public void setSuplidor(Document suplidor) {
        this.suplidor = suplidor;
    }

    public String getFechaDeseada() {
        return fechaDeseada;
    }

    public void setFechaDeseada(String fechaDeseada) {
        this.fechaDeseada = fechaDeseada;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public int getCantidadDeseada() {
        return cantidadDeseada;
    }

    public void setCantidadDeseada(int cantidadDeseada) {
        this.cantidadDeseada = cantidadDeseada;
    }
}
